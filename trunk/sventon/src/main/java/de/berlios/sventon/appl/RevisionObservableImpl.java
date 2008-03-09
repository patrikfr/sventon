/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.appl;

import de.berlios.sventon.repository.RepositoryFactory;
import de.berlios.sventon.repository.cache.objectcache.ObjectCache;
import de.berlios.sventon.repository.cache.objectcache.ObjectCacheManager;
import de.berlios.sventon.service.RepositoryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Class to monitor repository changes.
 * During check, the latest revision number is fetched from the
 * repository and compared to the observable's latest revision.
 * If it differs, the revision delta will be fetched and published
 * to registered observers.
 *
 * @author jesper@user.berlios.de
 */
public final class RevisionObservableImpl extends Observable implements RevisionObservable {

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The application.
   */
  private Application application;

  /**
   * Object cache manager instance.
   * Used to get/put info regarding repository URL and last observed revision.
   */
  private ObjectCacheManager objectCacheManager;

  /**
   * Object cache key, <code>lastCachedLogRevision</code>.
   */
  private static final String LAST_UPDATED_LOG_REVISION_CACHE_KEY = "lastCachedLogRevision";

  /**
   * Maximum number of revisions to get each update.
   */
  private int maxRevisionCountPerUpdate;

  /**
   * Service.
   */
  private RepositoryService repositoryService;

  /**
   * Repository factory.
   */
  private RepositoryFactory repositoryFactory;

  /**
   * Constructor.
   *
   * @param observers List of observers to add
   */
  public RevisionObservableImpl(final List<RevisionObserver> observers) {
    logger.info("Starting revision observable");
    logger.debug("Adding [" + observers.size() + "] observers");
    for (final RevisionObserver revisionObserver : observers) {
      addObserver(revisionObserver);
    }
  }

  /**
   * Sets the maximum number of revisions to get (and update) each time.
   * If will hopefully decrease the memory consumption during cache loading
   * on big repositories.
   *
   * @param count Max revisions per update.
   */
  public void setMaxRevisionCountPerUpdate(final int count) {
    this.maxRevisionCountPerUpdate = count;
  }

  /**
   * Sets the application.
   *
   * @param application Application
   */
  public void setApplication(final Application application) {
    this.application = application;
  }

  /**
   * Sets the object cache manager instance.
   *
   * @param objectCacheManager The cache manager instance.
   */
  public void setObjectCacheManager(final ObjectCacheManager objectCacheManager) {
    this.objectCacheManager = objectCacheManager;
  }

  /**
   * Update.
   *
   * @param instanceName     The instance name.
   * @param repository       Repository to use for update.
   * @param objectCache      ObjectCache instance to read/write information about last observed revisions.
   * @param flushAfterUpdate If <tt>true</tt>, caches will be flushed after update.
   */
  protected void update(final String instanceName, final SVNRepository repository, final ObjectCache objectCache,
                        final boolean flushAfterUpdate) {

    try {
      Long lastUpdatedRevision = (Long) objectCache.get(LAST_UPDATED_LOG_REVISION_CACHE_KEY + instanceName);
      boolean clearCacheBeforeUpdate = false;

      if (lastUpdatedRevision == null) {
        logger.info("No record about previously fetched revisions exists - fetching all revisions for instance: "
            + instanceName);
        clearCacheBeforeUpdate = true;
        lastUpdatedRevision = 0L;
      }

      final long headRevision = repositoryService.getLatestRevision(repository);

      // Sanity check
      if (headRevision < lastUpdatedRevision) {
        final String errorMessage = "Repository HEAD revision (" + headRevision + ") is lower than last cached"
            + " revision. The repository URL has probably been changed. Delete all cache files from the temp directory"
            + " and restart sventon.";
        logger.error(errorMessage);
        throw new RuntimeException(errorMessage);
      }

      if (headRevision > lastUpdatedRevision) {
        long revisionsLeftToFetchCount = headRevision - lastUpdatedRevision;
        logger.debug("About to fetch [" + revisionsLeftToFetchCount + "] revisions");

        do {
          long fromRevision = lastUpdatedRevision + 1;
          long toRevision = revisionsLeftToFetchCount > maxRevisionCountPerUpdate
              ? lastUpdatedRevision + maxRevisionCountPerUpdate : headRevision;

          final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
          logEntries.addAll(repositoryService.getRevisionsFromRepository(
              repository, fromRevision, toRevision));
          logger.debug("Read [" + logEntries.size() + "] revision(s) from instance: " + instanceName);
          setChanged();
          final StringBuffer notification = new StringBuffer();
          notification.append("Notifying observers about [");
          notification.append(logEntries.size());
          notification.append("] revisions [");
          notification.append(fromRevision);
          notification.append("-");
          notification.append(toRevision);
          notification.append("]");
          logger.info(notification.toString());
          notifyObservers(new RevisionUpdate(instanceName, logEntries, flushAfterUpdate, clearCacheBeforeUpdate));

          lastUpdatedRevision = toRevision;
          logger.debug("Updating 'lastUpdatedRevision' to: " + lastUpdatedRevision);
          objectCache.put(LAST_UPDATED_LOG_REVISION_CACHE_KEY + instanceName, lastUpdatedRevision);
          clearCacheBeforeUpdate = false;
          revisionsLeftToFetchCount -= logEntries.size();
        } while (revisionsLeftToFetchCount > 0);
      }
    } catch (SVNException svnex) {
      logger.warn("Exception: " + svnex.getMessage());
      logger.debug("Exception [" + svnex.getErrorMessage().getErrorCode().toString() + "]", svnex);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void update(final String instanceName, final boolean flushAfterUpdate) {
    if (application.isConfigured()) {
      final Instance instance = application.getInstance(instanceName);
      final InstanceConfiguration configuration = instance.getConfiguration();

      if (configuration.isCacheUsed() && !instance.isUpdatingCache()) {
        synchronized (instanceName) {
          instance.setUpdatingCache(true);
          SVNRepository repository = null;
          try {
            repository = repositoryFactory.getRepository(configuration.getInstanceName(),
                configuration.getSVNURL(), configuration.getUid(), configuration.getPwd());
            final ObjectCache objectCache = objectCacheManager.getCache(instanceName);
            update(instanceName, repository, objectCache, flushAfterUpdate);
          } catch (final Exception ex) {
            logger.warn("Unable to establish repository connection", ex);
          } finally {
            if (repository != null) {
              repository.closeSession();
            }
            instance.setUpdatingCache(false);
          }
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void updateAll() {
    if (application.isConfigured()) {
      for (final Instance instance : application.getInstances()) {
        update(instance.getConfiguration().getInstanceName(), true);
      }
    }
  }

  /**
   * Sets the repository service instance.
   *
   * @param repositoryService The service instance.
   */
  public void setRepositoryService(final RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  /**
   * Sets the repository factory instance.
   *
   * @param repositoryFactory Factory.
   */
  public void setRepositoryFactory(final RepositoryFactory repositoryFactory) {
    this.repositoryFactory = repositoryFactory;
  }
}
