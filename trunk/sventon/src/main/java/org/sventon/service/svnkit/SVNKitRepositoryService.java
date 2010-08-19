/*
 * ====================================================================
 * Copyright (c) 2005-2010 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.service.svnkit;

import de.regnis.q.sequence.line.diff.QDiffGeneratorFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sventon.*;
import org.sventon.colorer.Colorer;
import org.sventon.diff.*;
import org.sventon.export.ExportDirectory;
import org.sventon.model.*;
import org.sventon.model.Properties;
import org.sventon.model.SVNURL;
import org.sventon.service.RepositoryService;
import org.sventon.web.command.DiffCommand;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.*;

import java.io.*;
import java.util.*;

/**
 * Service class for accessing the subversion repository.
 *
 * @author jesper@sventon.org
 */
public class SVNKitRepositoryService implements RepositoryService {

  /**
   * Logger for this class and subclasses.
   */
  final Log logger = LogFactory.getLog(getClass());

  @Override
  public LogEntry getLogEntry(final RepositoryName repositoryName, final SVNConnection connection, final long revision)
      throws SventonException {

    final SVNRepository repository = connection.getDelegate();
    try {
      return Converter.toLogEntry((SVNLogEntry) repository.log(new String[]{"/"}, null, revision, revision,
          true, false).iterator().next());
    } catch (SVNException ex) {
      return translateSVNException("Error getting log entry: ", ex);
    }
  }

  @Override
  public final List<LogEntry> getLogEntriesFromRepositoryRoot(final SVNConnection connection, final long fromRevision,
                                                              final long toRevision) throws SventonException {

    final SVNRepository repository = connection.getDelegate();
    final List<LogEntry> revisions = new ArrayList<LogEntry>();
    try {
      repository.log(new String[]{"/"}, fromRevision, toRevision, true, false, new ISVNLogEntryHandler() {
        public void handleLogEntry(final SVNLogEntry logEntry) {
          revisions.add(Converter.toLogEntry(logEntry));
        }
      });
    } catch (SVNException ex) {
      return translateSVNException("Unable to get logs", ex);
    }
    return revisions;
  }

  @Override
  public List<LogEntry> getLogEntries(final RepositoryName repositoryName, final SVNConnection connection,
                                      final long fromRevision, final long toRevision, final String path,
                                      final long limit, final boolean stopOnCopy, boolean includeChangedPaths)
      throws SventonException {

    logger.debug("Fetching [" + limit + "] revisions in the interval [" + toRevision + "-" + fromRevision + "]");
    final SVNRepository repository = connection.getDelegate();
    final List<LogEntry> logEntries = new ArrayList<LogEntry>();
    try {
      repository.log(new String[]{path}, fromRevision, toRevision, includeChangedPaths, stopOnCopy, limit, new ISVNLogEntryHandler() {
        public void handleLogEntry(final SVNLogEntry logEntry) {
          logEntries.add(Converter.toLogEntry(logEntry));
        }
      });
    } catch (SVNException ex) {
      return translateSVNException("Unable to get logs", ex);
    }
    return logEntries;
  }

  @Override
  public final void export(final SVNConnection connection, final List<PathRevision> targets, final long pegRevision,
                           final ExportDirectory exportDirectory) throws SventonException {

    final SVNRepository repository = connection.getDelegate();
    for (final PathRevision fileRevision : targets) {
      final String path = fileRevision.getPath();
      final long revision = fileRevision.getRevision().getNumber();
      final File revisionRootDir = new File(exportDirectory.getDirectory(), String.valueOf(revision));

      logger.debug("Exporting file [" + path + "] revision [" + revision + "]");
      if (!revisionRootDir.exists() && !revisionRootDir.mkdirs()) {
        throw new RuntimeException("Unable to create directory: " + revisionRootDir.getAbsolutePath());
      }

      try {
        final File entryToExport = new File(revisionRootDir, path);
        final SVNClientManager clientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
        final SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doExport(org.tmatesoft.svn.core.SVNURL.parseURIDecoded(
            repository.getLocation().toDecodedString() + path), entryToExport, SVNRevision.create(pegRevision),
            SVNRevision.create(revision), null, true, SVNDepth.INFINITY);
      } catch (SVNException ex) {
        translateSVNException("Error exporting [" + path + "@" + revision + "]", ex);
      }
    }
  }

  protected final TextFile getTextFile(final SVNConnection connection, final String path, final long revision,
                                       final String charset) throws SventonException, IOException {
    logger.debug("Fetching file " + path + "@" + revision);
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    getFileContents(connection, path, revision, outStream);
    return new TextFile(outStream.toString(charset));
  }

  @Override
  public void getFileContents(final SVNConnection connection, final String path, final long revision,
                              final OutputStream output) throws SventonException {
    final SVNRepository repository = connection.getDelegate();
    try {
      final SVNClientManager clientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
      final SVNWCClient wcClient = clientManager.getWCClient();
      final org.tmatesoft.svn.core.SVNURL fileURL = org.tmatesoft.svn.core.SVNURL.parseURIDecoded(
          repository.getLocation().toDecodedString() + path);
      wcClient.doGetFileContents(fileURL, SVNRevision.create(revision), SVNRevision.create(revision), true, output);
    } catch (SVNException ex) {
      translateSVNException("Cannot get contents of file [" + path + "@" + revision + "]", ex);
    }
  }

  @Override
  public final Properties getFileProperties(final SVNConnection connection, final String path, final long revision)
      throws SventonException {
    final SVNProperties props = new SVNProperties();
    final SVNRepository repository = connection.getDelegate();
    try {
      repository.getFile(path, revision, props, null);
    } catch (SVNException e) {
      return translateSVNException("Could not get file properties for " + path + " at revision " + revision, e);
    }

    final Properties properties = new Properties();
    for (Object o : props.nameSet()) {
      final String key = (String) o;
      final String value = SVNPropertyValue.getPropertyAsString(props.getSVNPropertyValue(key));
      properties.put(new Property(key), new PropertyValue(value));
    }

    return properties;
  }

  protected final boolean isTextFile(final SVNConnection connection, final String path, final long revision) throws SventonException {
    final String mimeType = getFileProperties(connection, path, revision).getStringValue(Property.MIME_TYPE);
    return Property.isTextMimeType(mimeType);
  }

  @Override
  public final String getFileChecksum(final SVNConnection connection, final String path, final long revision) throws SventonException {
    return getFileProperties(connection, path, revision).getStringValue(Property.CHECKSUM);
  }

  @Override
  public final Long getLatestRevision(final SVNConnection connection) throws SventonException {
    final SVNRepository repository = connection.getDelegate();
    try {
      return repository.getLatestRevision();
    } catch (SVNException ex) {
      return translateSVNException("Cannot get latest revision", ex);
    }
  }

  @Override
  public final DirEntry.Kind getNodeKind(final SVNConnection connection, final String path, final long revision)
      throws SventonException {
    try {
      final SVNRepository repository = connection.getDelegate();
      final SVNNodeKind nodeKind = repository.checkPath(path, revision);
      return DirEntry.Kind.valueOf(nodeKind.toString().toUpperCase());
    } catch (SVNException svnex) {
      return translateSVNException("Unable to get node kind for: " + path + "@" + revision, svnex);
    }
  }

  @Override
  public Map<String, DirEntryLock> getLocks(final SVNConnection connection, final String startPath) {
    final String path = startPath == null ? "/" : startPath;
    logger.debug("Getting lock info for path [" + path + "] and below");

    final Map<String, DirEntryLock> locks = new HashMap<String, DirEntryLock>();
    final SVNRepository repository = connection.getDelegate();

    try {
      for (final SVNLock lock : repository.getLocks(path)) {
        logger.debug("Lock found: " + lock);
        final DirEntryLock dirEntryLock = new DirEntryLock(lock.getID(), lock.getPath(), lock.getOwner(),
            lock.getComment(), lock.getCreationDate(), lock.getExpirationDate());
        locks.put(lock.getPath(), dirEntryLock);
      }
    } catch (SVNException svne) {
      logger.debug("Unable to get locks for path [" + path + "]. Directory may not exist in HEAD", svne);
    }
    return locks;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public final DirList list(final SVNConnection connection, final String path, final long revision
  ) throws SventonException {
    final SVNRepository repository = connection.getDelegate();
    final SVNProperties properties = new SVNProperties();
    final Collection<SVNDirEntry> entries;
    try {
      entries = repository.getDir(path, revision, properties, (Collection) null);
    } catch (SVNException ex) {
      return translateSVNException("Could not get directory listing from [" + path + "@" + revision + "]", ex);
    }

    return DirEntry.createDirectoryList(Converter.convertDirEntries(entries, path), Converter.convertProperties(properties));
  }

  @Override
  public final DirEntry getEntryInfo(final SVNConnection connection, final String path, final long revision)
      throws SventonException {

    final SVNDirEntry dirEntry;
    try {
      final SVNRepository repository = connection.getDelegate();
      dirEntry = repository.info(path, revision);
    } catch (SVNException ex) {
      return translateSVNException("Cannot get info for [" + path + "@" + revision + "]", ex);
    }

    if (dirEntry != null) {
      return Converter.createDirEntry(dirEntry, FilenameUtils.getFullPath(path));
    } else {
      logger.warn("Entry [" + path + "] does not exist in revision [" + revision + "]");
      throw new DirEntryNotFoundException(path + "@" + revision);
    }
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public final List<FileRevision> getFileRevisions(final SVNConnection connection, final String path, final long revision)
      throws SventonException {

    //noinspection unchecked
    final List<SVNFileRevision> svnFileRevisions;
    try {
      final SVNRepository repository = connection.getDelegate();
      svnFileRevisions = (List<SVNFileRevision>) repository.getFileRevisions(
          path, null, 0, revision);
    } catch (SVNException ex) {
      return translateSVNException("Cannot get file revisions for [" + path + "@" + "]", ex);
    }

    if (logger.isDebugEnabled()) {
      final List<Long> fileRevisionNumbers = new ArrayList<Long>();
      for (final SVNFileRevision fileRevision : svnFileRevisions) {
        fileRevisionNumbers.add(fileRevision.getRevision());
      }
      logger.debug("Found revisions: " + fileRevisionNumbers);
    }
    return Converter.convertFileRevisions(svnFileRevisions);
  }

  @Override
  public final List<SideBySideDiffRow> diffSideBySide(final SVNConnection connection, final DiffCommand command,
                                                      final Revision pegRevision, final String charset)
      throws SventonException, DiffException {

    assertNotBinary(connection, command, pegRevision);

    try {
      final TextFile leftFile;
      final TextFile rightFile;

      if (Revision.UNDEFINED.equals(pegRevision)) {
        leftFile = getTextFile(connection, command.getFromPath(), command.getFromRevision().getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), command.getToRevision().getNumber(), charset);
      } else {
        leftFile = getTextFile(connection, command.getFromPath(), pegRevision.getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), pegRevision.getNumber(), charset);
      }
      return createSideBySideDiff(command, charset, leftFile, rightFile);
    } catch (IOException ioex) {
      throw new DiffException("Unable to produce unified diff", ioex);
    }
  }

  @Override
  public final String diffUnified(final SVNConnection connection, final DiffCommand command, final Revision pegRevision,
                                  final String charset) throws SventonException, DiffException {

    assertNotBinary(connection, command, pegRevision);

    try {
      final TextFile leftFile;
      final TextFile rightFile;

      if (Revision.UNDEFINED.equals(pegRevision)) {
        leftFile = getTextFile(connection, command.getFromPath(), command.getFromRevision().getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), command.getToRevision().getNumber(), charset);
      } else {
        leftFile = getTextFile(connection, command.getFromPath(), pegRevision.getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), pegRevision.getNumber(), charset);
      }
      return createUnifiedDiff(command, charset, leftFile, rightFile);
    } catch (final IOException ioex) {
      throw new DiffException("Unable to produce unified diff", ioex);
    }
  }

  @Override
  public final List<InlineDiffRow> diffInline(final SVNConnection connection, final DiffCommand command,
                                              final Revision pegRevision, final String charset)
      throws SventonException, DiffException {

    assertNotBinary(connection, command, pegRevision);

    try {
      final TextFile leftFile;
      final TextFile rightFile;

      if (Revision.UNDEFINED.equals(pegRevision)) {
        leftFile = getTextFile(connection, command.getFromPath(), command.getFromRevision().getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), command.getToRevision().getNumber(), charset);
      } else {
        leftFile = getTextFile(connection, command.getFromPath(), pegRevision.getNumber(), charset);
        rightFile = getTextFile(connection, command.getToPath(), pegRevision.getNumber(), charset);
      }

      return createInlineDiff(command, charset, leftFile, rightFile);

    } catch (final IOException ioex) {
      throw new DiffException("Unable to produce inline diff", ioex);
    }
  }

  @Override
  public final List<DiffStatus> diffPaths(final SVNConnection connection, final DiffCommand command)
      throws SventonException {

    final SVNRepository repository = connection.getDelegate();
    final SVNClientManager clientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
    final SVNDiffClient diffClient = clientManager.getDiffClient();
    final List<DiffStatus> result = new ArrayList<DiffStatus>();
    final String repoRoot = repository.getLocation().toDecodedString();

    try {
      diffClient.doDiffStatus(
          org.tmatesoft.svn.core.SVNURL.parseURIDecoded(repoRoot + command.getFromPath()), SVNRevision.parse(command.getFromRevision().toString()),
          org.tmatesoft.svn.core.SVNURL.parseURIDecoded(repoRoot + command.getToPath()), SVNRevision.parse(command.getToRevision().toString()),
          SVNDepth.INFINITY, false, new ISVNDiffStatusHandler() {
            public void handleDiffStatus(final org.tmatesoft.svn.core.wc.SVNDiffStatus diffStatus) throws SVNException {
              if (diffStatus.getModificationType() != org.tmatesoft.svn.core.wc.SVNStatusType.STATUS_NONE || diffStatus.isPropertiesModified()) {
                result.add(new DiffStatus(StatusType.fromId(diffStatus.getModificationType().getID()),
                    new SVNURL(diffStatus.getURL().getURIEncodedPath()), diffStatus.getPath(), diffStatus.isPropertiesModified()));
              }
            }
          });
    } catch (SVNException e) {
      return translateSVNException("Could not calculate diff for " + command.toString(), e);
    }
    return result;
  }

  @Override
  public final AnnotatedTextFile blame(final SVNConnection connection, final String path, final long revision,
                                       final String charset, final Colorer colorer) throws SventonException {

    try {
      final SVNRepository repository = connection.getDelegate();
      final long blameRevision;
      if (Revision.UNDEFINED_NUMBER == revision) {
        blameRevision = repository.getLatestRevision();
      } else {
        blameRevision = revision;
      }

      logger.debug("Blaming file [" + path + "] revision [" + revision + "]");
      final AnnotatedTextFile annotatedTextFile = new AnnotatedTextFile(path, charset, colorer);
      final SVNClientManager clientManager = SVNClientManager.newInstance(null, repository.getAuthenticationManager());
      final SVNLogClient logClient = clientManager.getLogClient();
      final AnnotationHandler handler = new AnnotationHandler(annotatedTextFile);
      final SVNRevision startRev = SVNRevision.create(0);
      final SVNRevision endRev = SVNRevision.create(blameRevision);

      logClient.doAnnotate(org.tmatesoft.svn.core.SVNURL.parseURIDecoded(repository.getLocation().toDecodedString() + path), endRev, startRev,
          endRev, false, handler, charset);
      try {
        annotatedTextFile.colorize();
      } catch (IOException ioex) {
        logger.warn("Unable to colorize [" + path + "]", ioex);
      }
      return annotatedTextFile;
    } catch (SVNException ex) {
      return translateSVNException("Error blaming [" + path + "@" + revision + "]", ex);
    }
  }

  @Override
  public DirEntry.Kind getNodeKindForDiff(final SVNConnection connection, final DiffCommand command)
      throws SventonException, DiffException {

    final long fromRevision;
    final long toRevision;

    if (command.hasPegRevision()) {
      fromRevision = command.getPegRevision();
      toRevision = command.getPegRevision();
    } else {
      fromRevision = command.getFromRevision().getNumber();
      toRevision = command.getToRevision().getNumber();
    }

    final DirEntry.Kind nodeKind1;
    final DirEntry.Kind nodeKind2;
    nodeKind1 = getNodeKind(connection, command.getFromPath(), fromRevision);
    nodeKind2 = getNodeKind(connection, command.getToPath(), toRevision);

    assertFileOrDir(nodeKind1, command.getFromPath(), fromRevision);
    assertFileOrDir(nodeKind2, command.getToPath(), toRevision);
    assertSameKind(nodeKind1, nodeKind2);
    return nodeKind1;
  }

  @Override
  public Long translateRevision(Revision revision, long headRevision, final SVNConnection connection) throws SventonException {
    final long revisionNumber = revision.getNumber();

    try {
      if (revisionNumber < 0) {
        if (Revision.HEAD.equals(revision)) {
          return headRevision;
        } else if (revisionNumber == -1 && revision.getDate() != null) {
          return connection.getDelegate().getDatedRevision(revision.getDate());
        } else {
          logger.warn("Unexpected revision: " + revision);
          return headRevision;
        }
      }
      return revisionNumber;
    } catch (SVNException ex) {
      return translateSVNException("Unable to translate revision: " + revision, ex);
    }
  }

  @Override
  public List<LogEntry> getLatestRevisions(RepositoryName repositoryName, SVNConnection connection, int revisionCount) throws SventonException {
    return getLogEntries(repositoryName, connection, -1, Revision.FIRST, "/", revisionCount, false, true);
  }

  protected List<SideBySideDiffRow> createSideBySideDiff(DiffCommand command, String charset, TextFile leftFile, TextFile rightFile) throws IOException {
    final ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
    final InputStream leftStream = new ByteArrayInputStream(leftFile.getContent().getBytes());
    final InputStream rightStream = new ByteArrayInputStream(rightFile.getContent().getBytes());
    final DiffProducer diffProducer = new DiffProducer(leftStream, rightStream, charset);

    diffProducer.doNormalDiff(diffResult);
    final String diffResultString = diffResult.toString(charset);

    if ("".equals(diffResultString)) {
      throw new IdenticalFilesException(command.getFromPath() + ", " + command.getToPath());
    }
    return new SideBySideDiffCreator(leftFile, rightFile).createFromDiffResult(diffResultString);
  }

  protected String createUnifiedDiff(DiffCommand command, String charset, TextFile leftFile, TextFile rightFile) throws IOException {
    final ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
    final DiffProducer diffProducer = new DiffProducer(new ByteArrayInputStream(leftFile.getContent().getBytes()),
        new ByteArrayInputStream(rightFile.getContent().getBytes()), charset);

    diffProducer.doUnifiedDiff(diffResult);

    final String diffResultString = diffResult.toString(charset);
    if ("".equals(diffResultString)) {
      throw new IdenticalFilesException(command.getFromPath() + ", " + command.getToPath());
    }
    return diffResultString;
  }

  protected List<InlineDiffRow> createInlineDiff(DiffCommand command, String charset, TextFile leftFile, TextFile rightFile) throws IOException {
    final List<InlineDiffRow> resultRows = new ArrayList<InlineDiffRow>();
    final ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
    final Map generatorProperties = new HashMap();
    final int maxLines = Math.max(leftFile.getRows().size(), rightFile.getRows().size());
    //noinspection unchecked
    generatorProperties.put(QDiffGeneratorFactory.GUTTER_PROPERTY, maxLines);
    final DiffProducer diffProducer = new DiffProducer(new ByteArrayInputStream(leftFile.getContent().getBytes()),
        new ByteArrayInputStream(rightFile.getContent().getBytes()), charset, generatorProperties);

    diffProducer.doUnifiedDiff(diffResult);

    final String diffResultString = diffResult.toString(charset);
    if ("".equals(diffResultString)) {
      throw new IdenticalFilesException(command.getFromPath() + ", " + command.getToPath());
    }

    int rowNumberLeft = 1;
    int rowNumberRight = 1;
    //noinspection unchecked
    for (final String row : (List<String>) IOUtils.readLines(new StringReader(diffResultString))) {
      if (!row.startsWith("@@")) {
        final char action = row.charAt(0);
        switch (action) {
          case ' ':
            resultRows.add(new InlineDiffRow(rowNumberLeft, rowNumberRight, DiffAction.UNCHANGED, row.substring(1).trim()));
            rowNumberLeft++;
            rowNumberRight++;
            break;
          case '+':
            resultRows.add(new InlineDiffRow(null, rowNumberRight, DiffAction.ADDED, row.substring(1).trim()));
            rowNumberRight++;
            break;
          case '-':
            resultRows.add(new InlineDiffRow(rowNumberLeft, null, DiffAction.DELETED, row.substring(1).trim()));
            rowNumberLeft++;
            break;
          default:
            throw new IllegalArgumentException("Unknown action: " + action);
        }
      }
    }
    return resultRows;
  }

  private void assertSameKind(final DirEntry.Kind nodeKind1, final DirEntry.Kind nodeKind2) throws DiffException {
    if (nodeKind1 != nodeKind2) {
      throw new DiffException("Entries are different kinds! " + nodeKind1 + "!=" + nodeKind2);
    }
  }

  private void assertFileOrDir(final DirEntry.Kind nodeKind, final String path, final long revision) throws DiffException {
    if (DirEntry.Kind.DIR != nodeKind && DirEntry.Kind.FILE != nodeKind) {
      throw new DiffException("Path [" + path + "] does not exist as revision [" + revision + "]");
    }
  }

  private void assertNotBinary(final SVNConnection connection, final DiffCommand command, final Revision pegRevision)
      throws SventonException, IllegalFileFormatException {

    final boolean isLeftFileTextType;
    final boolean isRightFileTextType;
    if (Revision.UNDEFINED.equals(pegRevision)) {
      isLeftFileTextType = isTextFile(connection, command.getFromPath(), command.getFromRevision().getNumber());
      isRightFileTextType = isTextFile(connection, command.getToPath(), command.getToRevision().getNumber());
    } else {
      isLeftFileTextType = isTextFile(connection, command.getFromPath(), pegRevision.getNumber());
      isRightFileTextType = isTextFile(connection, command.getToPath(), pegRevision.getNumber());
    }

    if (!isLeftFileTextType && !isRightFileTextType) {
      throw new IllegalFileFormatException("Cannot diff binary files: " + command.getFromPath() + ", " + command.getToPath());
    } else if (!isLeftFileTextType) {
      throw new IllegalFileFormatException("Cannot diff binary file: " + command.getFromPath());
    } else if (!isRightFileTextType) {
      throw new IllegalFileFormatException("Cannot diff binary file: " + command.getToPath());
    }
  }

  private <T extends Object> T translateSVNException(String errorMessage, SVNException exception) throws SventonException {
    if (exception instanceof SVNAuthenticationException) {
      throw new AuthenticationException(exception.getMessage(), exception);
    }

    if (SVNErrorCode.FS_NO_SUCH_REVISION == exception.getErrorMessage().getErrorCode()) {
      throw new NoSuchRevisionException("Unable to get node kind: " + exception.getMessage());
    }

    throw new SventonException(errorMessage, exception);
  }

  private static class AnnotationHandler implements ISVNAnnotateHandler {

    private final AnnotatedTextFile annotatedTextFile;

    /**
     * Constructor.
     *
     * @param annotatedTextFile File
     */
    public AnnotationHandler(final AnnotatedTextFile annotatedTextFile) {
      this.annotatedTextFile = annotatedTextFile;
    }

    @Deprecated
    public void handleLine(final Date date, final long revision, final String author, final String line)
        throws SVNException {
      handleLine(date, revision, author, line, null, -1, null, null, 0);
    }

    public void handleLine(final Date date, final long revision, final String author, final String line,
                           final Date mergedDate, final long mergedRevision, final String mergedAuthor,
                           final String mergedPath, final int lineNumber) throws SVNException {
      annotatedTextFile.addRow(date, revision, author, line);
    }

    public boolean handleRevision(Date date, long revision, String author, File contents) throws SVNException {
      // We do not want our file to be annotated for each revision of the range, but only for the last
      // revision of it, so we return false
      return false;
    }

    public void handleEOF() {
      // Nothing to do.
    }

  }
}
