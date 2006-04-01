/*
 * ====================================================================
 * Copyright (c) 2005-2006 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.cache;

/**
 * Interface to be implemented by sventon object cache class.
 */
public interface SventonCache {

  /**
   * Puts an object into the cache.
   *
   * @param cacheKey The cache cacheKey.
   * @param value    The object to cache.
   * @throws IllegalArgumentException if cacheKey is null.
   */
  public void put(final Object cacheKey, final Object value);

  /**
   * Gets an object from the cache.
   *
   * @param cacheKey The key to the object to get.
   * @return The cached object. <code>null</code> if cache miss.
   * @throws IllegalArgumentException if cacheKey is null.
   */
  public Object get(final Object cacheKey);

  /**
   * Gets the cache hit count.
   *
   * @return The cache hit count.
   */
  public long getHitCount();

  /**
   * Gets the cache miss count.
   *
   * @return The cache miss count.
   */
  public long getMissCount();

}
