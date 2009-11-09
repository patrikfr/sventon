/*
 * ====================================================================
 * Copyright (c) 2005-2009 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sventon.RepositoryConnectionFactory;
import org.sventon.appl.Application;
import org.sventon.appl.ObjectCacheManager;
import org.sventon.appl.RepositoryConfiguration;
import org.sventon.cache.objectcache.ObjectCache;
import org.sventon.model.RepositoryName;
import org.sventon.repository.observer.RevisionObserver;
import org.sventon.service.RepositoryService;
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
  private RepositoryConnectionFactory repositoryConnectionFactory;

  /**
   * Constructor.
   *
   * @param observers List of observers to add
   */
  public RevisionObservableImpl(final List<RevisionObserver> observers) {
    logger.info("Starting revision observable");

    for (final RevisionObserver revisionObserver : observers) {
      logger.debug("Adding observer: " + revisionObserver.getClass().getName());
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
  @Autowired
  public void setApplication(final Application application) {
    this.application = application;
  }

  /**
   * Sets the object cache manager instance.
   *
   * @param objectCacheManager The cache manager instance.
   */
  @Autowired
  public void setObjectCacheManager(final ObjectCacheManager objectCacheManager) {
    this.objectCacheManager = objectCacheManager;
  }

  /**
   * Update.
   *
   * @param name             The Repository name
   * @param repository       Repository to use for update.
   * @param objectCache      ObjectCache instance to read/write information about last observed revisions.
   * @param flushAfterUpdate If <tt>true</tt>, caches will be flushed after update.
   */
  protected void update(final RepositoryName name, final SVNRepository repository, final ObjectCache objectCache,
                        final boolean flushAfterUpdate) {

    try {
      final long headRevision = repositoryService.getLatestRevision(repository);
      Long lastUpdatedRevision = (Long) objectCache.get(LAST_UPDATED_LOG_REVISION_CACHE_KEY + name);

      boolean clearCacheBeforeUpdate = false;

      if (lastUpdatedRevision == null) {
        logger.info("No record about previously fetched revisions exists - fetching all revisions for repository: " + name);
        clearCacheBeforeUpdate = true;
        lastUpdatedRevision = 0L;
      }

      handleSuspectedUrlChange(lastUpdatedRevision, headRevision);

      if (headRevision > lastUpdatedRevision) {
        long revisionsLeftToFetchCount = headRevision - lastUpdatedRevision;
        logger.debug("About to fetch [" + revisionsLeftToFetchCount + "] revisions from repository: " + name);

        do {
          final long fromRevision = lastUpdatedRevision + 1;
          final long toRevision = revisionsLeftToFetchCount > maxRevisionCountPerUpdate
              ? lastUpdatedRevision + maxRevisionCountPerUpdate : headRevision;

          final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
          logEntries.addAll(repositoryService.getRevisionsFromRepository(repository, fromRevision, toRevision));
          logger.debug("Read [" + logEntries.size() + "] revision(s) from repository: " + name);
          setChanged();
          logger.info(createNotificationLogMessage(fromRevision, toRevision, logEntries.size()));
          notifyObservers(new RevisionUpdate(name, logEntries, flushAfterUpdate, clearCacheBeforeUpdate));
          lastUpdatedRevision = toRevision;
          logger.debug("Updating 'lastUpdatedRevision' to: " + lastUpdatedRevision);
          objectCache.put(LAST_UPDATED_LOG_REVISION_CACHE_KEY + name, lastUpdatedRevision);
          objectCache.flush();
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
   * Sanity check of revision numbers.
   *
   * @param lastUpdatedRevision Last updated revision number
   * @param headRevision        Current head revision number.
   * @throws IllegalStateException if last updated revision is greater than head revision.
   */
  private void handleSuspectedUrlChange(final long lastUpdatedRevision, long headRevision) {
    if (headRevision < lastUpdatedRevision) {
      final String errorMessage = "Repository HEAD revision (" + headRevision + ") is lower than last cached"
          + " revision. The repository URL has probably been changed. Delete all cache files from the temp directory"
          + " and restart sventon.";
      logger.error(errorMessage);
      throw new IllegalStateException(errorMessage);
    }
  }

  /**
   * Creates an informative string for logging observer notifications.
   *
   * @param fromRevision    From revision
   * @param toRevision      To revision
   * @param logEntriesCount Number of log entries.
   * @return String intended for logging.
   */
  private String createNotificationLogMessage(long fromRevision, long toRevision, int logEntriesCount) {
    final StringBuffer notification = new StringBuffer();
    notification.append("Notifying observers about [");
    notification.append(logEntriesCount);
    notification.append("] revisions [");
    notification.append(fromRevision);
    notification.append("-");
    notification.append(toRevision);
    notification.append("]");
    return notification.toString();
  }

  /**
   * {@inheritDoc}
   */
  public void update(final RepositoryName repositoryName, final boolean flushAfterUpdate) {
    if (application.isConfigured()) {
      final RepositoryConfiguration configuration = application.getRepositoryConfiguration(repositoryName);

      if (configuration.isCacheUsed() && !application.isUpdating(repositoryName)) {
        application.setUpdatingCache(repositoryName, true);
        SVNRepository repository = null;
        try {
          repository = repositoryConnectionFactory.createConnection(configuration.getName(),
              configuration.getSVNURL(), configuration.getCacheCredentials());
          final ObjectCache objectCache = objectCacheManager.getCache(repositoryName);
          update(repositoryName, repository, objectCache, flushAfterUpdate);
        } catch (final Exception ex) {
          logger.warn("Unable to establish repository connection", ex);
        } finally {
          if (repository != null) {
            repository.closeSession();
          }
          application.setUpdatingCache(repositoryName, false);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void updateAll() {
    if (application.isConfigured()) {
      for (final RepositoryName repositoryName : application.getRepositoryNames()) {
        update(repositoryName, true);
      }
    }
  }

  /**
   * Sets the repository service instance.
   *
   * @param repositoryService The service instance.
   */
  @Autowired
  public void setRepositoryService(final RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  /**
   * Sets the repository connection factory instance.
   *
   * @param repositoryConnectionFactory Factory instance.
   */
  @Autowired
  public void setRepositoryConnectionFactory(final RepositoryConnectionFactory repositoryConnectionFactory) {
    this.repositoryConnectionFactory = repositoryConnectionFactory;
  }
}
