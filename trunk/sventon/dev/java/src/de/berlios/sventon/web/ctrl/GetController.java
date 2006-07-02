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

import de.berlios.sventon.cache.ObjectCache;
import de.berlios.sventon.util.EncodingUtils;
import de.berlios.sventon.util.ImageUtil;
import de.berlios.sventon.util.PathUtil;
import de.berlios.sventon.web.command.SVNBaseCommand;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * Controller used when downloading single files.
 * Image files can be gotten in three different ways.
 * <ul>
 * <li><b>thumb</b> - Gets a thumbnail version of the image.</li>
 * <li><b>inline</b> - Gets the image with correct content type.
 * Image will be displayed inline in browser.</li>
 * <li><b>attachment</b> - Gets the image with content type
 * application/octetstream. A download dialog will appear in browser.</li>
 * </ul>
 *
 * @author jesper@users.berlios.de
 */
public class GetController extends AbstractSVNTemplateController implements Controller {

  private ImageUtil imageUtil;
  private ObjectCache objectCache;

  public static final String THUMBNAIL_FORMAT = "png";
  public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
  public static final String DISPLAY_REQUEST_PARAMETER = "disp";
  public static final String DISPLAY_TYPE_THUMBNAIL = "thumb";
  public static final String DISPLAY_TYPE_INLINE = "inline";


  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final HttpServletRequest request,
                                   final HttpServletResponse response, final BindException exception) throws Exception {

    logger.debug("Getting file: " + svnCommand.getPath());

    final String displayType = RequestUtils.getStringParameter(request, DISPLAY_REQUEST_PARAMETER, null);
    final ServletOutputStream output;
    logger.debug("displayType: " + displayType);

    try {
      output = response.getOutputStream();

      if (DISPLAY_TYPE_THUMBNAIL.equals(displayType)) {
        logger.debug("Getting file as 'thumbnail'");
        if (!imageUtil.isImageFileExtension(PathUtil.getFileExtension(svnCommand.getPath()))) {
          logger.error("File '" + svnCommand.getTarget() + "' is not a image file");
          return null;
        }
        getAsThumbnail(response, svnCommand, repository, revision, output, request);
      } else {
        if (DISPLAY_TYPE_INLINE.equals(displayType)
            && imageUtil.isImageFileExtension(PathUtil.getFileExtension(svnCommand.getPath()))) {
          logger.debug("Getting file as 'inline'");
          getAsInlineImage(response, svnCommand, request, repository, revision, output);
        } else {
          logger.debug("Getting file as 'attachment'");
          getAsAttachment(svnCommand, response, request, repository, revision, output);
        }
      }
      output.flush();
      output.close();
    } catch (final IOException ioex) {
      logger.error(ioex);
    }
    return null;
  }

  private void getAsAttachment(SVNBaseCommand svnCommand, HttpServletResponse response, HttpServletRequest request, SVNRepository repository, SVNRevision revision, ServletOutputStream output) throws SVNException {
    String mimeType = null;
    try {
      mimeType = getServletContext().getMimeType(svnCommand.getTarget().toLowerCase());
    } catch (IllegalStateException ise) {
      logger.debug("Could not get mimeType for file as an ApplicationContext does not exist. Using default");
    }
    if (mimeType == null) {
      response.setContentType(DEFAULT_CONTENT_TYPE);
    } else {
      response.setContentType(mimeType);
    }
    response.setHeader("Content-disposition", "attachment; filename=\"" + EncodingUtils.encodeFilename(svnCommand.getTarget(), request) + "\"");
    // Get the image data and write it to the outputStream.
    repository.getFile(svnCommand.getPath(), revision.getNumber(), null, output);
  }

  private void getAsInlineImage(HttpServletResponse response, SVNBaseCommand svnCommand, HttpServletRequest request, SVNRepository repository, SVNRevision revision, ServletOutputStream output) throws SVNException {
    response.setContentType(imageUtil.getContentType(PathUtil.getFileExtension(svnCommand.getPath())));
    response.setHeader("Content-disposition", "inline; filename=\"" + EncodingUtils.encodeFilename(svnCommand.getTarget(), request) + "\"");
    // Get the image data and write it to the outputStream.
    repository.getFile(svnCommand.getPath(), revision.getNumber(), null, output);
  }

  private void getAsThumbnail(HttpServletResponse response, SVNBaseCommand svnCommand, SVNRepository repository, SVNRevision revision, ServletOutputStream output, HttpServletRequest request) throws SVNException, IOException {
    final ByteArrayOutputStream baos;
    response.setHeader("Content-disposition", "inline; filename=\"" + svnCommand.getTarget() + "\"");

    // Check if the thumbnail exists on the cache
    final HashMap properties = new HashMap();
    repository.getFile(svnCommand.getPath(), revision.getNumber(), properties, null);
    logger.debug(properties);
    String cacheKey = (String) properties.get(SVNProperty.CHECKSUM) + svnCommand.getPath();
    logger.debug("Using cachekey: " + cacheKey);
    byte[] thumbnailData = (byte[]) objectCache.get(cacheKey);
    if (thumbnailData != null) {
      // Writing cached thumbnail image to ServletOutputStream
      output.write(thumbnailData);
    } else {
      // Thumbnail was not in the cache.
      // Create the thumbnail.
      final StringBuilder urlString = new StringBuilder(request.getRequestURL());
      urlString.append("?");
      urlString.append(request.getQueryString().replaceAll(DISPLAY_REQUEST_PARAMETER + "=" + DISPLAY_TYPE_THUMBNAIL, DISPLAY_REQUEST_PARAMETER + "=" + DISPLAY_TYPE_INLINE));
      final URL url = new URL(urlString.toString());
      logger.debug("Getting full size image from url: " + url);
      BufferedImage image = ImageIO.read(url);
      int orgWidth = image.getWidth();
      int orgHeight = image.getHeight();

      // Get preferred thumbnail dimension.
      final Dimension thumbnailSize = imageUtil.getThumbnailSize(orgWidth, orgHeight);
      logger.debug("Thumbnail size: " + thumbnailSize.toString());
      // Resize image.
      final Image rescaled = image.getScaledInstance((int) thumbnailSize.getWidth(), (int) thumbnailSize.getHeight(), Image.SCALE_AREA_AVERAGING);
      final BufferedImage biRescaled = imageUtil.toBufferedImage(rescaled, BufferedImage.TYPE_INT_ARGB);
      response.setContentType(imageUtil.getContentType(PathUtil.getFileExtension(svnCommand.getPath())));

      // Write thumbnail to output stream.
      baos = new ByteArrayOutputStream();
      ImageIO.write(biRescaled, THUMBNAIL_FORMAT, baos);

      // Putting created thumbnail image into the cache.
      logger.debug("Caching thumbnail. Using cachekey: " + cacheKey);
      objectCache.put(cacheKey, baos.toByteArray());
      // Write thumbnail to ServletOutputStream.
      output.write(baos.toByteArray());
    }
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

  /**
   * Sets the object cache instance.
   *
   * @param objectCache The cache instance.
   */
  public void setObjectCache(final ObjectCache objectCache) {
    this.objectCache = objectCache;
  }

}
