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

    final long fromRevision;
    final long toRevision;
    final String fromPath;
    final String toPath;

    logger.debug("Diffing file contents for: " + svnCommand);
    final String[] revisionParameters = request.getParameterValues("rev");
    String[] pathAndRevision;
    Map<String, Object> model = new HashMap<String, Object>();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    try {
      pathAndRevision = revisionParameters[0].split(";;");
      toPath = pathAndRevision[0];
      toRevision = Long.parseLong(pathAndRevision[1]);
      pathAndRevision = revisionParameters[1].split(";;");
      fromPath = pathAndRevision[0];
      fromRevision = Long.parseLong(pathAndRevision[1]);
      model.put("toRevision", toRevision);
      model.put("toPath", toPath);
      model.put("fromRevision", fromRevision);
      model.put("fromPath", fromPath);
    } catch (Exception ex) {
      throw new SVNException("Unable to diff. Unable to parse revision and path", ex);
    }

    String leftLines = null;
    String rightLines = null;
    try {
      HashMap properties = new HashMap();
      // Get the file's properties without requesting the content.
      // Make sure files are not in binary format.
      repository.getFile(svnCommand.getCompletePath(), revision.getNumber(), properties, null);
      boolean isTextType = SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE));
      repository.getFile(svnCommand.getCompletePath(), revision.getNumber(), properties, null);
      if (isTextType && SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE))) {
        model.put("isBinary", false);
        // Get content of oldest file (left).
        logger.debug("Getting file contents for (from) revision " + fromRevision + ", path: " + fromPath);
        repository.getFile(fromPath, fromRevision, null, outStream);
        leftLines = StringEscapeUtils.escapeHtml(outStream.toString());
        // Re-initialize stream
        outStream = new ByteArrayOutputStream();
         // Get content of newest file (right).
        logger.debug("Getting file contents for (to) revision " + toRevision + ", path: " + toPath);
        repository.getFile(toPath, toRevision, null, outStream);
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
