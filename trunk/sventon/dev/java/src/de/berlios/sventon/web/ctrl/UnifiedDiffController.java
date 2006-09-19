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
package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.diff.DiffCreator;
import de.berlios.sventon.diff.DiffException;
import de.berlios.sventon.diff.DiffProducer;
import de.berlios.sventon.web.command.DiffCommand;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.model.RawTextFile;
import de.berlios.sventon.web.model.UserContext;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a unified diff between two repository entries.
 *
 * @author jesper@users.berlios.de
 */
public class UnifiedDiffController extends AbstractSVNTemplateController implements Controller {

  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final UserContext userContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    final String[] entries = RequestUtils.getStringParameters(request, "entry");
    logger.debug("Unified diffing file content for: " + svnCommand);
    final Map<String, Object> model = new HashMap<String, Object>();

    try {
      final DiffCommand diffCommand = new DiffCommand(entries);
      model.put("diffCommand", diffCommand);
      logger.debug("Using: " + diffCommand);
      model.putAll(diffInternal(repository, diffCommand));
    } catch (DiffException dex) {
      model.put("diffException", dex.getMessage());
    }

    return new ModelAndView("unifieddiff", model);
  }

  /**
   * Internal method for creating the diff between two entries.
   *
   * @param repository  The repository instance.
   * @param diffCommand The <code>DiffCommand</code> including to/from diff instructions.
   * @return A populated map containing the following info:
   *         <ul>
   *         <li><i>isBinary</i>, indicates whether any of the entries were a binary file.</li>
   *         <li><i>leftFileContent</i>, <code>List</code> of <code>SourceLine</code>s for the left (from) file.</li>
   *         <li><i>rightFileContent</i>, <code>List</code> of <code>SourceLine</code>s for the right (to) file.</li>
   *         </ul>
   * @throws de.berlios.sventon.diff.DiffException
   *          if unable to produce diff.
   * @throws org.tmatesoft.svn.core.SVNException
   *          if a subversion error occurs.
   */
  protected Map<String, Object> diffInternal(final SVNRepository repository, final DiffCommand diffCommand)
      throws DiffException, SVNException {

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final Map<String, Object> model = new HashMap<String, Object>();
    final RawTextFile leftFile;
    final RawTextFile rightFile;

    final boolean isLeftFileTextType = getRepositoryService().isTextFile(repository, diffCommand.getFromPath(),
        diffCommand.getFromRevision().getNumber());
    final boolean isRightFileTextType = getRepositoryService().isTextFile(repository, diffCommand.getToPath(),
        diffCommand.getToRevision().getNumber());

    if (isLeftFileTextType || isRightFileTextType) {
      model.put("isBinary", false);

      // Get content of oldest file (left).
      logger.debug("Getting file content for (from) revision "
          + diffCommand.getFromRevision() + ", path: " + diffCommand.getFromPath());
      getRepositoryService().getFile(repository, diffCommand.getFromPath(), diffCommand.getFromRevision().getNumber(),
          outStream);
      leftFile = new RawTextFile(outStream.toString(), true);

      // Re-initialize stream
      outStream = new ByteArrayOutputStream();

      // Get content of newest file (right).
      logger.debug("Getting file content for (to) revision "
          + diffCommand.getToRevision() + ", path: " + diffCommand.getToPath());

      getRepositoryService().getFile(repository, diffCommand.getToPath(), diffCommand.getToRevision().getNumber(),
          outStream);
      rightFile = new RawTextFile(outStream.toString(), true);

      final ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
      final DiffProducer diffProducer = new DiffProducer(new ByteArrayInputStream(leftFile.getContent().getBytes()),
          new ByteArrayInputStream(rightFile.getContent().getBytes()), DiffCreator.ENCODING);
      try {
        diffProducer.doUniDiff(diffResult);
      } catch (final IOException ioex) {
        throw new DiffException("Unable to procude unified diff");
      }

      final String diffResultString = diffResult.toString();
      if ("".equals(diffResultString)) {
        throw new DiffException("Files are identical.");
      }

      model.put("diffResult", diffResultString);
    } else {
      model.put("isBinary", true);  // Indicates that the file is in binary format.
      logger.info("One or both files selected for diff is in binary format. "
          + "Diff will not be performed");
    }
    return model;
  }

}
