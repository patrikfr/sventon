package org.sventon.cache.entrycache;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.*;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.sventon.cache.CacheException;
import org.sventon.model.RepositoryEntry;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * EntryCacheImpl.
 */
public class EntryCacheImpl implements EntryCache {

  /**
   * The logging instance.
   */
  final Log logger = LogFactory.getLog(getClass());

  /**
   * Default filename for storing the latest cached revision number.
   */
  private static final String ENTRY_CACHE_FILENAME = "entrycache.ser";

  /**
   * The cache file.
   */
  private File cacheFile;

  /**
   * Cached revision.
   */
  private final AtomicLong cachedRevision = new AtomicLong(0);

  private final CompassConfiguration compassConfiguration = new CompassConfiguration();
  private final File cacheDirectory;
  private Compass compass;

  private boolean useDiskStore;

  /**
   * Constructor.
   *
   * @param cacheRootDirectory Cache root directory
   */
  public EntryCacheImpl(final File cacheRootDirectory) {
    this.cacheDirectory = new File(cacheRootDirectory, "entrycache");
  }

  /**
   * Constructor.
   *
   * @param cacheRootDirectory Cache root directory
   * @param useDiskStore       If true index will be stored to disk. Otherwise it will be kept in memory.
   */
  public EntryCacheImpl(File cacheRootDirectory, final boolean useDiskStore) {
    this.cacheDirectory = new File(cacheRootDirectory, "entrycache");
    this.useDiskStore = useDiskStore;
  }

  /**
   * {@inheritDoc}
   */
  public void init() throws CacheException {
    final String connectionString;

    if (useDiskStore) {
      connectionString = cacheDirectory.getAbsolutePath();
    } else {
      connectionString = "ram://" + cacheDirectory.getAbsolutePath();
    }

    compassConfiguration.setSetting(CompassEnvironment.CONNECTION, connectionString)
        .setSetting(CompassEnvironment.DEBUG, String.valueOf(true))
        .addClass(RepositoryEntry.class);
    compass = compassConfiguration.buildCompass();
    cacheFile = new File(cacheDirectory, ENTRY_CACHE_FILENAME);

    if (useDiskStore) {
      loadLatestCachedRevisionNumber();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void shutdown() throws CacheException {
    compass.close();
  }

  /**
   * {@inheritDoc}
   */
  public final void setLatestCachedRevisionNumber(final long revision) {
    this.cachedRevision.set(revision);
  }

  /**
   * {@inheritDoc}
   */
  public final long getLatestCachedRevisionNumber() {
    return cachedRevision.get();
  }

  /**
   * {@inheritDoc}
   */
  public final int getSize() {
    final CompassTemplate template = new CompassTemplate(compass);
    return template.execute(new CompassCallback<Integer>() {
      public Integer doInCompass(CompassSession session) throws CompassException {
        final CompassHits compassHits = session.queryBuilder().matchAll().hits();
        return compassHits.length();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public final void add(final RepositoryEntry entry) {
    final CompassTemplate template = new CompassTemplate(compass);
    template.execute(new CompassCallbackWithoutResult() {
      protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
        session.save(entry);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public final void clear() {
    final CompassTemplate template = new CompassTemplate(compass);
    template.execute(new CompassCallbackWithoutResult() {
      protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
        session.delete(session.queryBuilder().matchAll());
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public final void removeEntry(final String pathAndName, final boolean recursive) {
    final CompassTemplate template = new CompassTemplate(compass);
    template.execute(new CompassCallbackWithoutResult() {
      protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
        if (recursive) {
          session.delete(session.queryBuilder().queryString("path:" + pathAndName + "*").toQuery());
          session.delete(RepositoryEntry.class, pathAndName);
        } else {
          session.delete(RepositoryEntry.class, pathAndName);
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public final List<RepositoryEntry> findEntries(final String searchString, final String startDir,
                                                 final boolean includeAuthors) {
    if (logger.isDebugEnabled()) {
      logger.debug("Finding [" + searchString + "] starting in [" + startDir + "]");
    }

    final CompassTemplate template = new CompassTemplate(compass);
    final List<RepositoryEntry> result = template.execute(new CompassCallback<List<RepositoryEntry>>() {
      public List<RepositoryEntry> doInCompass(CompassSession session) throws CompassException {
        return toEntriesList(session.find("path:" + startDir + "* name:*" + searchString + "*"));
      }
    });

    logResult(result);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public final List<RepositoryEntry> findDirectories(final String startPath) {
    if (logger.isDebugEnabled()) {
      logger.debug("Finding directories recursively from [" + startPath + "]");
    }

    final CompassTemplate template = new CompassTemplate(compass);
    final List<RepositoryEntry> result = template.execute(new CompassCallback<List<RepositoryEntry>>() {
      public List<RepositoryEntry> doInCompass(CompassSession session) throws CompassException {
        return toEntriesList(session.find("path:" + startPath + "* kind:DIR"));
      }
    });

    logResult(result);
    return result;
  }

  protected List<RepositoryEntry> toEntriesList(final CompassHits compassHits) {
    final List<RepositoryEntry> hits = new ArrayList<RepositoryEntry>(compassHits.length());
    for (CompassHit compassHit : compassHits) {
      hits.add((RepositoryEntry) compassHit.getData());
    }
    return hits;
  }

  protected void logResult(List<RepositoryEntry> result) {
    if (logger.isDebugEnabled()) {
      logger.debug("Result count: " + result.size());
      logger.debug("Result: " + result);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void flush() throws CacheException {
    if (useDiskStore) {
      saveLatestRevisionNumber();
    }
  }

  /**
   * @throws CacheException if unable to save revision number.
   */
  private void saveLatestRevisionNumber() throws CacheException {
    logger.info("Saving file to disk, " + cacheFile);
    ObjectOutputStream out = null;
    try {
      // Write to a temp file first, to keep the old file just in case.
      out = new ObjectOutputStream(new FileOutputStream(cacheFile));
      out.writeLong(getLatestCachedRevisionNumber());
      out.flush();
      out.close();
    } catch (IOException ioex) {
      throw new CacheException("Unable to store file to disk", ioex);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }


  /**
   * Loads the latest cached revision number from disk.
   *
   * @throws CacheException if unable to read file.
   */
  private void loadLatestCachedRevisionNumber() throws CacheException {
    logger.info("Loading file from disk, " + cacheFile);
    ObjectInputStream inputStream = null;
    if (cacheFile.exists()) {
      try {
        inputStream = new ObjectInputStream(new FileInputStream(cacheFile));
        setLatestCachedRevisionNumber(inputStream.readLong());
        logger.debug("Revision: " + getLatestCachedRevisionNumber());
      } catch (Exception ex) {
        throw new CacheException("Unable to read file from disk", ex);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    }
  }
}
