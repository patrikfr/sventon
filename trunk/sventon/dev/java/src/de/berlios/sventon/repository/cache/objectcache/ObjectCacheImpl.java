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
package de.berlios.sventon.repository.cache.objectcache;

import de.berlios.sventon.repository.cache.CacheException;
import de.berlios.sventon.repository.cache.objectcache.ObjectCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Wrapper class for cache handling. Uses
 * <a href="http://ehcache.sourceforge.net/">ehcache</a> behind the scenes.
 *
 * @author jesper@users.berlios.de
 */
public final class ObjectCacheImpl implements ObjectCache {

  /**
   * The cache manager instance.
   */
  private CacheManager cacheManager;

  /**
   * The cache instance.
   */
  private Cache cache;

  /**
   * Logger for this class and subclasses
   */
  protected final Log logger = LogFactory.getLog(getClass());

  /**
   * Name of the cache.
   */
  private String cacheName;

  /**
   * Constructor.
   *
   * @param cacheName           Name of the cache.
   * @param maxElementsInMemory Max elements in memory
   * @param overflowToDisk      Overflow to disk
   * @param eternal             If true, objects never expire
   * @param timeToLiveSeconds   Object time to live in seconds
   * @param timeToIdleSeconds   Object time to idle in seconds
   * @param diskPersistent      If true, cache will be stored on disk
   * @param diskExpiryThreadIntervalSeconds
   *                            Expiry thread interval
   * @throws CacheException if unable to create cache.
   */
  public ObjectCacheImpl(final String cacheName,
                         final int maxElementsInMemory,
                         final boolean overflowToDisk,
                         final boolean eternal,
                         final int timeToLiveSeconds,
                         final int timeToIdleSeconds,
                         final boolean diskPersistent,
                         final int diskExpiryThreadIntervalSeconds) throws CacheException {
    try {
      // Initialize cache using failsafe configuration
      cacheManager = new CacheManager(getClass().getResource("/ehcache-failsafe.xml"));
      cache = new Cache(cacheName, maxElementsInMemory, overflowToDisk, eternal, timeToLiveSeconds,
          timeToIdleSeconds, diskPersistent, diskExpiryThreadIntervalSeconds);
      cacheManager.addCache(cache);
    } catch (net.sf.ehcache.CacheException ce) {
      throw new CacheException("Unable to create cache instance", ce);
    }
  }

  /**
   * {@inheritDoc]
   */
  public void put(final Object cacheKey, final Object value) {
    if (cacheKey == null) {
      throw new IllegalArgumentException("Cache key cannot be null");
    }
    Element element = new Element(cacheKey.toString(), (Serializable) value);
    cache.put(element);
  }

  /**
   * {@inheritDoc]
   */
  public Object get(final Object cacheKey) {
    if (cacheKey == null) {
      throw new IllegalArgumentException("Cache key cannot be null");
    }
    String key = cacheKey.toString();
    Element element = null;
    try {
      element = cache.get(key);
    } catch (net.sf.ehcache.CacheException ce) {
      // Nothing to do
    }
    return element != null ? element.getValue() : null;
  }

  /**
   * {@inheritDoc]
   */
  public long getHitCount() {
    return cache.getHitCount();
  }

  /**
   * {@inheritDoc]
   */
  public long getMissCount() {
    return cache.getMissCountNotFound();
  }

  /**
   * {@inheritDoc]
   */
  public void shutdown() throws CacheException {
    try {
      cacheManager.shutdown();
    } catch (net.sf.ehcache.CacheException ce) {
      throw new CacheException("Unable to shutdown cache instance", ce);
    }
  }

}
