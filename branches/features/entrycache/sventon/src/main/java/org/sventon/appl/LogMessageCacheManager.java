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
package org.sventon.appl;

import org.apache.lucene.store.FSDirectory;
import org.sventon.cache.CacheException;
import org.sventon.cache.logmessagecache.LogMessageCache;
import org.sventon.cache.logmessagecache.LogMessageCacheImpl;
import org.sventon.model.RepositoryName;

import java.io.File;
import java.io.IOException;

/**
 * Handles LogMessageCache instances.
 *
 * @author jesper@sventon.org
 */
public final class LogMessageCacheManager extends CacheManager<LogMessageCache> {

  /**
   * Directory where to store cache files.
   */
  private final File repositoriesDirectory;

  /**
   * Lucene Analyzer to use.
   *
   * @see org.apache.lucene.analysis.Analyzer
   */
  private final String analyzerClassName;

  /**
   * Constructor.
   *
   * @param configDirectory   Root directory to use.
   * @param analyzerClassName Analyzer to use.
   */
  public LogMessageCacheManager(final ConfigDirectory configDirectory, final String analyzerClassName) {
    logger.debug("Starting cache manager. Using [" + configDirectory.getRepositoriesDirectory() + "] as root directory");
    this.repositoriesDirectory = configDirectory.getRepositoriesDirectory();
    this.analyzerClassName = analyzerClassName;
    System.setProperty("org.apache.lucene.lockDir", configDirectory.getRepositoriesDirectory().getAbsolutePath());
  }

  /**
   * Creates a new cache instance using given name and default settings.
   *
   * @param repositoryName Name of cache instance.
   * @return The created cache instance.
   * @throws CacheException if unable to create cache or unable to load analyzer.
   */
  protected LogMessageCache createCache(final RepositoryName repositoryName) throws CacheException {
    logger.debug("Creating cache: " + repositoryName);
    final FSDirectory fsDirectory;
    try {
      final File cachePath = new File(new File(repositoriesDirectory, repositoryName.toString()), "cache");
      cachePath.mkdirs();
      fsDirectory = FSDirectory.getDirectory(cachePath);
      logger.debug("Log cache dir: " + fsDirectory.getFile().getAbsolutePath());
    } catch (IOException ioex) {
      throw new CacheException("Unable to create LogMessageCache instance", ioex);
    }

    final Class<?> analyzer;
    try {
      logger.debug("Loading analyzer [" + analyzerClassName + "]");
      analyzer = Class.forName(analyzerClassName);
    } catch (final ClassNotFoundException cnfe) {
      throw new CacheException("Unable to load analyzer [" + analyzerClassName + "]", cnfe);
    }
    //noinspection unchecked
    final LogMessageCacheImpl cache = new LogMessageCacheImpl(fsDirectory, (Class) analyzer);
    cache.init();
    return cache;
  }

}
