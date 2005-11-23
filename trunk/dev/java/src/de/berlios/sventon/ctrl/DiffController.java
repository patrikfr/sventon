/*
 * ====================================================================
 * Copyright (c) 2005 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.ctrl;

import de.berlios.sventon.diff.Diff;
import de.berlios.sventon.diff.DiffException;
import de.berlios.sventon.command.SVNBaseCommand;
import de.berlios.sventon.command.DiffCommand;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * DiffController.
 *
 * @author jesper@users.berlios.de
 */
public class DiffController extends AbstractSVNTemplateController implements Controller {

  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(SVNRepository repository, SVNBaseCommand svnCommand, SVNRevision revision,
                                   HttpServletRequest request, HttpServletResponse response, BindException exception) throws SVNException {

    logger.debug("Diffing file contents for: " + svnCommand);
    Map<String, Object> model = new HashMap<String, Object>();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    String leftLines = null;
    String rightLines = null;
    try {
      DiffCommand diffCommand = new DiffCommand(request.getParameterValues("rev"));

      model.put("fromRevision", diffCommand.getFromRevision());
      model.put("fromPath", diffCommand.getFromPath());
      model.put("toRevision", diffCommand.getToRevision());
      model.put("toPath", diffCommand.getToPath());

      HashMap properties = new HashMap();

      // Get the file's properties without requesting the content.
      // Make sure files are not in binary format.
      repository.getFile(svnCommand.getCompletePath(), revision.getNumber(), properties, null);
      boolean isTextType = SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE));
      repository.getFile(svnCommand.getCompletePath(), revision.getNumber(), properties, null);

      if (isTextType && SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE))) {
        model.put("isBinary", false);

        // Get content of oldest file (left).
        logger.debug("Getting file contents for (from) revision "
            + diffCommand.getFromRevision()
            + ", path: "
            + diffCommand.getFromPath());
        repository.getFile(diffCommand.getFromPath(), diffCommand.getFromRevision(), null, outStream);
        leftLines = StringEscapeUtils.escapeHtml(outStream.toString());

        // Re-initialize stream
        outStream = new ByteArrayOutputStream();

         // Get content of newest file (right).
        logger.debug("Getting file contents for (to) revision "
            + diffCommand.getToRevision()
            + ", path: "
            + diffCommand.getToPath());
        repository.getFile(diffCommand.getToPath(), diffCommand.getToRevision(), null, outStream);
        rightLines = StringEscapeUtils.escapeHtml(outStream.toString());

        Diff differ = new Diff(leftLines, rightLines);
        model.put("leftFileContents", differ.getLeft());
        model.put("rightFileContents", differ.getRight());
      } else {
        model.put("isBinary", true);  // Indicates that the file is in binary format.
        logger.info("One or both files selected for diff is in binary format. "
            + "Diff will not be performed.");
      }
    } catch (DiffException dex) {
      model.put("diffException", dex.getMessage());
    }

    return new ModelAndView("diff", model);
  }

}
