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
package org.sventon.cache.entrycache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sventon.cache.Cache;
import org.sventon.cache.CacheException;
import org.sventon.model.RepositoryEntry;
import static org.sventon.model.RepositoryEntry.Kind.ANY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains a cached set of the repository entries for a specific revision and URL.
 *
 * @author jesper@sventon.org
 */
public abstract class EntryCache implements Cache {

  /**
   * The logging instance.
   */
  final Log logger = LogFactory.getLog(getClass());

  /**
   * The cached entries.
   */
  private Set<RepositoryEntry> cachedEntries;

  /**
   * Cached revision.
   */
  private long cachedRevision = 0;

  /**
   * Initializes the cache.
   *
   * @throws CacheException if unable to load cache.
   */
  public abstract void init() throws CacheException;

  /**
   * Shuts down the cache.
   *
   * @throws CacheException if unable to shut down cache instance.
   */
  public abstract void shutdown() throws CacheException;

  /**
   * Flushes the cache. Will only have effect on disk persistent caches.
   *
   * @throws CacheException if unable to flush cache.
   */
  public abstract void flush() throws CacheException;

  /**
   * Sets the entries.
   *
   * @param entries Entries
   */
  final synchronized void setEntries(final Set<RepositoryEntry> entries) {
    this.cachedEntries = entries;
  }

  /**
   * Gets the cached revision number.
   * Used to determine if cache needs to be updated.
   *
   * @return Cached revision number.
   */
  protected final synchronized long getCachedRevision() {
    return cachedRevision;
  }

  /**
   * Gets the set of the cached entries.
   *
   * @return Set of cached entries
   */
  final synchronized Set<RepositoryEntry> getCachedEntries() {
    return cachedEntries;
  }

  /**
   * Sets the cached revision number.
   * Used if cache has been updated.
   *
   * @param revision Revision number.
   */
  final synchronized void setCachedRevision(final long revision) {
    this.cachedRevision = revision;
  }

  /**
   * Gets the number of entries in cache.
   *
   * @return Count
   */
  public final synchronized int getSize() {
    return cachedEntries.size();
  }

  /**
   * Adds one entry to the cache.
   *
   * @param entry The entry to parse and add
   * @return True if entry was added, false if not.
   */
  public final synchronized boolean add(final RepositoryEntry entry) {
    return cachedEntries.add(entry);
  }

  /**
   * Adds one or more entries to the cache.
   *
   * @param entries The entries to parse and add
   * @return True if entries were added, false if not.
   */
  public final synchronized boolean add(final List<RepositoryEntry> entries) {
    return cachedEntries.addAll(entries);
  }

  /**
   * Removes entries from the cache, based by path and file name.
   *
   * @param pathAndName Entry to remove from cache.
   * @param recursive   True if remove should be performed recursively
   */
  public final synchronized void removeByName(final String pathAndName, final boolean recursive) {
    final List<RepositoryEntry> toBeRemoved = new ArrayList<RepositoryEntry>();

    for (RepositoryEntry entry : cachedEntries) {
      if (recursive) {
        if (entry.getFullEntryName().startsWith(pathAndName)) {
          toBeRemoved.add(entry);
        }
      } else {
        if (entry.getFullEntryName().equals(pathAndName)) {
          toBeRemoved.add(entry);
          break;
        }
      }
    }
    cachedEntries.removeAll(toBeRemoved);
  }

  /**
   * Clears the entire cache.
   */
  public final synchronized void clear() {
    cachedEntries.clear();
  }

  /**
   * Finds entry names based on given regex pattern.
   *
   * @param pattern Entry name pattern to search for
   * @param kind    Entry kind
   * @return List of entries matching given pattern.
   */
  public final synchronized List<RepositoryEntry> findByPattern(final Pattern pattern, final RepositoryEntry.Kind kind) {
    if (logger.isDebugEnabled()) {
      logger.debug("Finding [" + pattern + "] of kind [" + kind + "]");
    }
    final List<RepositoryEntry> result = Collections.checkedList(new ArrayList<RepositoryEntry>(), RepositoryEntry.class);

    for (final RepositoryEntry entry : cachedEntries) {
      final Matcher matcher = pattern.matcher(entry.getFullEntryName());
      if (matcher.matches() && (entry.getKind() == kind || kind == ANY)) {
        result.add(entry);
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Result count: " + result.size());
      logger.debug("Result: " + result);
    }
    return result;
  }

  /**
   * Finds entry names containing given search string.
   *
   * @param searchString Entry name search string.
   * @param startDir     Directory/path to start in.
   * @return List of entries with names matching given search string.
   */
  public final synchronized List<RepositoryEntry> findEntry(final String searchString, final String startDir) {
    if (logger.isDebugEnabled()) {
      logger.debug("Finding [" + searchString + "] starting in [" + startDir + "]");
    }
    final List<RepositoryEntry> result = Collections.checkedList(new ArrayList<RepositoryEntry>(), RepositoryEntry.class);

    boolean hit = false;
    for (final RepositoryEntry entry : cachedEntries) {
      if (entry.getPath().startsWith(startDir)) {
        hit = true;
        if (entry.getName().contains(searchString)) {
          result.add(entry);
        }
      } else {
        if (hit) {
          break;
        } else {
          hit = false;
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Result count: " + result.size());
      logger.debug("Result: " + result);
    }
    return result;

  }
}
