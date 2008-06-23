/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.repository.cache.revisioncache;

import de.berlios.sventon.repository.cache.Cache;
import de.berlios.sventon.repository.cache.CacheException;
import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * Contains cached revisions.
 *
 * @author jesper@users.berlios.de
 */
public interface RevisionCache extends Cache {

  /**
   * Gets a revision by number.
   *
   * @param revision Revision number of revision to get.
   * @return The revision info
   * @throws CacheException if error.
   */
  SVNLogEntry get(final long revision) throws CacheException;

  /**
   * Add one revision to the cache.
   *
   * @param logEntry The revision info to cache.
   * @throws CacheException if error.
   */
  void add(final SVNLogEntry logEntry) throws CacheException;

  /**
   * Flush cache to disk.
   */
  void flush();
}
