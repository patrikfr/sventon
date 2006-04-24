package de.berlios.sventon.repository.cache;

import de.berlios.sventon.repository.RepositoryConfiguration;
import de.berlios.sventon.repository.RepositoryEntry;
import de.berlios.sventon.repository.RepositoryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service class used to access the caches.
 * <p/>
 * Responsibility: Start/stop the transaction, trigger cache update and perform search.
 *
 * @author jesper@users.berlios.de
 */
public class CacheServiceImpl implements CacheService {

  private SVNRepository repository;
  private RepositoryConfiguration configuration;
  private EntryCache entryCache;
  private CommitMessageCache commitMessageCache;

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  private boolean updating = false;

  public CacheServiceImpl() {
    logger.info("Starting cache service");
  }

  public void setRepositoryConfiguration(final RepositoryConfiguration configuration) {
    this.configuration = configuration;
  }

  public void setRepositoryEntryCache(final EntryCache entryCache) {
    this.entryCache = entryCache;
  }

  public void setCommitMessageCache(final CommitMessageCache commitMessageCache) {
    this.commitMessageCache = commitMessageCache;
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void updateCaches() throws Exception {
    //TODO: Are we up-to-date?
    //TODO: Make the repo connection & get latest log entries & update both caches
/*
* Kolla om indexet �r p�slaget
* Kolla om det finns en repos connection
* Kolla att indexet �r initierat
* Toggla isIndexing-flagga
* Avg�ra om indexet �r uptodate
* Antingen populera eller trigga update
* Toggla isIndexing-flaggan igen

*/

    if (!establishConnection() || !configuration.isCacheUsed()) {
      return;
    }

    updating = true;

    updating = false;

  }

  public boolean isUpdating() {
    return updating;
  }


  /**
   * Checks if the repository connection is properly initialized.
   * If not, a connection will be created.
   *
   * @throws SVNException If an error occur during the repository connection creation.
   */
  private boolean establishConnection() throws SVNException {
    if (repository == null) {
      logger.debug("Establishing repository connection");
      repository = RepositoryFactory.INSTANCE.getRepository(configuration);
      if (repository == null) {
        logger.info("Repository not configured yet.");
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the cache is properly initialized.
   * If not, the cache will be loaded.
   *
   * @throws Exception if unable to load cache.
   */
  private void assertIndexIsInitialized() throws Exception {
  }

  /**
   * Populates the index by getting all entries in given path
   * and adding them to the index. This method will be recursively
   * called by <code>index()</code>.
   *
   * @param path The path to add to index.
   * @throws SVNException if a Subversion error occurs.
   */
  @SuppressWarnings("unchecked")
  private void populateEmptyEntryCache(final String path, final long revision) throws SVNException {
    final List<SVNDirEntry> entriesList = Collections.checkedList(new ArrayList<SVNDirEntry>(), SVNDirEntry.class);

    entriesList.addAll(repository.getDir(path, revision, null, (Collection) null));
    for (SVNDirEntry entry : entriesList) {
      final RepositoryEntry newEntry = new RepositoryEntry(entry, path, null);
      if (!entryCache.add(newEntry)) {
        logger.warn("Unable to add already existing entry to index: " + newEntry.toString());
      }
      if (entry.getKind() == SVNNodeKind.DIR) {
        populateEmptyEntryCache(path + entry.getName() + "/", revision);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  public List<RepositoryEntry> findEntry(final String searchString) throws Exception {
    updateCaches();
    return entryCache.findByPattern(searchString, RepositoryEntry.Kind.any, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Object> find(final String searchString) throws Exception {
    updateCaches();
    return commitMessageCache.find(searchString);
  }


}
