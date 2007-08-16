/*
 * ====================================================================
 * Copyright (c) 2005-2007 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.colorer.Colorer;
import de.berlios.sventon.util.ImageUtil;
import de.berlios.sventon.util.PathUtil;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.model.ArchiveFile;
import de.berlios.sventon.web.model.UserContext;
import de.berlios.sventon.model.TextFile;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ShowFileController.
 *
 * @author patrikfr@users.berlios.de
 * @author jesper@users.berlios.de
 */
public class ShowFileController extends AbstractSVNTemplateController implements Controller {

  /**
   * The colorer instance.
   */
  private Colorer colorer;

  /**
   * Image utility.
   */
  private ImageUtil imageUtil;

  /**
   * Regex pattern that identifies archive file extensions.
   */
  protected String archiveFileExtensionPattern;

  /**
   * FORMAT_REQUEST_PARAMETER = format.
   */
  private static final String FORMAT_REQUEST_PARAMETER = "format";


  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final UserContext userContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    logger.debug("Assembling file contents for: " + svnCommand);

    final String formatParameter = ServletRequestUtils.getStringParameter(request, FORMAT_REQUEST_PARAMETER, null);
    final Map<String, Object> model = new HashMap<String, Object>();
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final Map properties = getRepositoryService().getFileProperties(repository, svnCommand.getPath(), revision.getNumber());

    logger.debug(properties);

    final String charset = userContext.getCharset();
    logger.debug("Using charset encoding: " + charset);

    model.put("properties", properties);
    model.put("committedRevision", properties.get(SVNProperty.COMMITTED_REVISION));

    if (SVNProperty.isTextMimeType((String) properties.get(SVNProperty.MIME_TYPE))) {
      getRepositoryService().getFile(repository, svnCommand.getPath(), revision.getNumber(), outStream);
      if ("raw".equals(formatParameter)) {
        response.setContentType("text/plain; charset=\"UTF-8\"");
        response.getOutputStream().write(outStream.toByteArray());
        return null;
      } else {
        final TextFile textFile = new TextFile(outStream.toString(charset), svnCommand.getPath(), charset,
            colorer, properties, repository.getLocation().toDecodedString());
        model.put("file", textFile);
      }
      return new ModelAndView("showtextfile", model);
    } else {
      // It's a binary file
      logger.debug("Binary file detected");

      if (PathUtil.getFileExtension(svnCommand.getPath()).toLowerCase().
          matches(archiveFileExtensionPattern)) {
        logger.debug("Binary file as an archive file");
        getRepositoryService().getFile(repository, svnCommand.getPath(), revision.getNumber(), outStream);
        model.put("entries", new ArchiveFile(outStream.toByteArray()).getEntries());
        return new ModelAndView("showarchivefile", model);
      } else {
        if (imageUtil.isImageFileExtension(PathUtil.getFileExtension(svnCommand.getPath()))) {
          return new ModelAndView("showimagefile", model);
        }
        return new ModelAndView("showbinaryfile", model);
      }
    }
  }

  /**
   * Sets the <tt>Colorer</tt> instance.
   *
   * @param colorer The instance.
   */
  public void setColorer(final Colorer colorer) {
    this.colorer = colorer;
  }

  /**
   * Sets the archive file extension pattern.
   *
   * @param fileExtensionPattern The pattern
   */
  public void setArchiveFileExtensionPattern(final String fileExtensionPattern) {
    archiveFileExtensionPattern = fileExtensionPattern;
  }

  /**
   * Sets the <code>ImageUtil</code> helper instance.
   *
   * @param imageUtil The instance
   * @see de.berlios.sventon.util.ImageUtil
   */
  public void setImageUtil(final ImageUtil imageUtil) {
    this.imageUtil = imageUtil;
  }

}
