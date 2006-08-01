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
package de.berlios.sventon.service;

import de.berlios.sventon.repository.RepositoryConfiguration;
import de.berlios.sventon.repository.cache.Cache;
import de.berlios.sventon.repository.cache.CacheException;
import de.berlios.sventon.repository.export.ExportEditor;
import de.berlios.sventon.repository.export.ExportReporterBaton;
import de.berlios.sventon.util.PathUtil;
import de.berlios.sventon.web.model.TextFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Service class for accessing the subversion repository.
 *
 * @author jesper@users.berlios.de
 */
public class RepositoryServiceImpl implements RepositoryService {

  /**
   * Logger for this class and subclasses.
   */
  protected final Log logger = LogFactory.getLog(getClass());

  /**
   * The repository configuration. Used to check whether caching is enabled or not.
   */
  private RepositoryConfiguration configuration;

  /**
   * The cache instance.
   */
  private Cache cache;

  /**
   * {@inheritDoc}
   */
  public SVNLogEntry getRevision(final SVNRepository repository, final long revision)
      throws SVNException {

    SVNLogEntry entry = null;
    if (configuration.isCacheUsed()) {
      try {
        logger.debug("Getting cached revision: " + revision);
        entry = getCachedRevision(revision);
      } catch (CacheException ce) {
        logger.warn("Unable to get cache revision: " + revision, ce);
        logger.info("Fallback - make a deep log fetch instead");
      }
    }
    return entry != null ? entry : getRevision(repository, revision, "/");
  }

  /**
   * {@inheritDoc}
   */
  public SVNLogEntry getRevision(final SVNRepository repository, final long revision, final String path)
      throws SVNException {

    return (SVNLogEntry) repository.log(
        new String[]{path}, null, revision, revision, true, false).iterator().next();
  }

  /**
   * {@inheritDoc}
   */
  public List<SVNLogEntry> getRevisions(final SVNRepository repository, final long fromRevision, final long toRevision)
      throws SVNException {

    return getRevisions(repository, fromRevision, toRevision, "/", -1);
  }

  /**
   * {@inheritDoc}
   */
  public List<SVNLogEntry> getRevisions(final SVNRepository repository, final long fromRevision, final long toRevision,
                                        final long limit) throws SVNException {

    return getRevisions(repository, fromRevision, toRevision, "/", limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<SVNLogEntry> getRevisions(final SVNRepository repository, final long fromRevision, final long toRevision,
                                        final String path, final long limit) throws SVNException {

    final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
    repository.log(new String[]{path}, fromRevision, toRevision, true, false, limit, new ISVNLogEntryHandler() {
      public void handleLogEntry(final SVNLogEntry logEntry) {
        logEntries.add(logEntry);
      }
    });
    return logEntries;
  }

  private SVNLogEntry getCachedRevision(final long revision) throws CacheException {
    return cache.getRevision(revision);
  }

  private List<SVNLogEntry> getCachedRevisions(final long fromRevision, final long toRevision) throws CacheException {
    //TODO: revisit!
    final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
    if (fromRevision < toRevision) {
      for (long revision = fromRevision; revision <= toRevision; revision++) {
        logEntries.add(cache.getRevision(revision));
      }
    } else {
      for (long revision = fromRevision; revision >= toRevision; revision--) {
        logEntries.add(cache.getRevision(revision));
      }
    }
    return logEntries;
  }

  /**
   * {@inheritDoc}
   */
  public void export(final SVNRepository repository, final List<String> targets, final long revision,
                     final File exportDir) throws SVNException {

    long exportRevision = revision;
    if (exportRevision == -1) {
      exportRevision = repository.getLatestRevision();
    }
    final ISVNReporterBaton reporterBaton = new ExportReporterBaton(exportRevision);

    try {
      for (final String target : targets) {
        final SVNNodeKind nodeKind = repository.checkPath(target, exportRevision);
        final File entryToExport = new File(exportDir, target);
        if (nodeKind == SVNNodeKind.FILE) {
          entryToExport.getParentFile().mkdirs();
          final OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(entryToExport));
          final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
          logger.debug("Exporting file [" + target + "] revision [" + exportRevision + "]");
          final Map properties = new HashMap();
          getFile(repository, target, exportRevision, outStream, properties);
          final TextFile textFile = new TextFile(outStream.toString(), properties,
              repository.getLocation().toDecodedString(), target);
          fileOutputStream.write(textFile.getContent().getBytes());
          fileOutputStream.flush();
          fileOutputStream.close();
        } else if (nodeKind == SVNNodeKind.DIR) {
          logger.debug("Exporting directory [" + target + "] revision [" + exportRevision + "]");
          entryToExport.mkdirs();
          // The update method does not accept a path, only a single file or dir
          // Temporarily changing repository location.
          final SVNURL originalLocation = repository.getLocation();
          repository.setLocation(SVNURL.parseURIDecoded(originalLocation.toDecodedString()
              + PathUtil.getPathNoLeaf(target)), false);
          // Do the export
          repository.update(exportRevision, target, true, reporterBaton, new ExportEditor(entryToExport.getParentFile()));
          // Reset the repository's original location
          repository.setLocation(originalLocation, false);
        } else {
          throw new IllegalArgumentException("Target [" + target + "] does not exist in revision [" + exportRevision + "]");
        }
      }
    } catch (final IOException ioex) {
      logger.warn(ioex);
      throw new RuntimeException(ioex);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void getFile(final SVNRepository repository, final String path, final long revision,
                      final OutputStream output) throws SVNException {
    repository.getFile(path, revision, null, output);
  }

  /**
   * {@inheritDoc}
   */
  public void getFile(final SVNRepository repository, final String path, final long revision,
                      final OutputStream output, final Map properties) throws SVNException {
    repository.getFile(path, revision, properties, output);
  }

  /**
   * {@inheritDoc}
   */
  public void getFileProperties(final SVNRepository repository, final String path, final long revision,
                                final Map properties) throws SVNException {
    repository.getFile(path, revision, properties, null);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isTextFile(final SVNRepository repository, final String path, final long revision) throws SVNException {
    final Map properties = new HashMap();
    repository.getFile(path, revision, properties, null);
    return SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE));
  }

  /**
   * {@inheritDoc}
   */
  public String getFileChecksum(final SVNRepository repository, final String path, final long revision) throws SVNException {
    final Map properties = new HashMap();
    repository.getFile(path, revision, properties, null);
    return (String) properties.get(SVNProperty.CHECKSUM);
  }

  /**
   * Set repository configuration.
   *
   * @param configuration Configuration
   */
  public void setRepositoryConfiguration(final RepositoryConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Sets the cache instance.
   *
   * @param cache Cache instance
   */
  public void setCache(final Cache cache) {
    this.cache = cache;
  }

}

