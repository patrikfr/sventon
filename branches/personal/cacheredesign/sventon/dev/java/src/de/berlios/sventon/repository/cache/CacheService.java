package de.berlios.sventon.repository.cache;

import de.berlios.sventon.repository.RepositoryEntry;

import java.util.List;

/**
 * Service class used to access the caches.
 * <p/>
 * Responsibility: Start/stop the transaction, trigger cache update and perform search.
 *
 * @author jesper@users.berlios.de
 */
public interface CacheService {

  void updateCaches() throws CacheException;

  boolean isUpdating();

  /**
   * Searches the cached entries for given string (name fragment).
   *
   * @param searchString String to search for
   * @return List of entries
   * @throws CacheException if error
   */
  List<RepositoryEntry> findEntry(final String searchString) throws CacheException;

  /**
   * Searches the cached entries for given string (name fragment) starting from given directory.
   *
   * @param searchString String to search for
   * @param startDir Start path
   * @return List of entries
   * @throws CacheException if error
   */
  List<RepositoryEntry> findEntry(final String searchString, final String startDir) throws CacheException;

  /**
   * Searches the cached entries for given string (name fragment) starting from given directory.
   *
   * @param searchString String to search for
   * @param startDir Start path
   * @param limit Limit search result entries.
   * @return List of entries
   * @throws CacheException if error
   */
  List<RepositoryEntry> findEntry(final String searchString, final String startDir, final Integer limit) throws CacheException;

  /**
   * Searches the cached entries for all directories below given start dir.
   *
   * @param fromPath Start path
   * @return List of entries
   * @throws CacheException if error
   */
  List<RepositoryEntry> findDirectories(final String fromPath) throws CacheException;

  /**
   * Searches the cached commit messages for given string.
   *
   * @param searchString String to search for
   * @return List of something. Depends on Lucene.
   * @throws CacheException if error
   */
  List<Object> find(final String searchString) throws CacheException;

}
