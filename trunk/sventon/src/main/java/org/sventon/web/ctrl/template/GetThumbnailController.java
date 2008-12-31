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
package org.sventon.web.ctrl.template;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.cache.objectcache.ObjectCache;
import org.sventon.cache.objectcache.ObjectCacheKey;
import org.sventon.cache.objectcache.ObjectCacheManager;
import org.sventon.model.UserRepositoryContext;
import org.sventon.util.EncodingUtils;
import org.sventon.util.ImageScaler;
import static org.sventon.util.WebUtils.CONTENT_DISPOSITION_HEADER;
import org.sventon.web.command.SVNBaseCommand;
import org.tmatesoft.svn.core.io.SVNRepository;

import javax.activation.FileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Controller used when downloading single image files as thumbnails.
 *
 * @author jesper@sventon.org
 */
public final class GetThumbnailController extends AbstractSVNTemplateController {

  /**
   * The mime/file type map.
   */
  private FileTypeMap mimeFileTypeMap;

  /**
   * Object cache manager instance.
   */
  private ObjectCacheManager objectCacheManager;

  /**
   * Image format name to use when generating thumbnails.
   */
  private String imageFormatName;

  /**
   * Specifies the maximum horizontal/vertical size (in pixels) for a generated thumbnail image.
   */
  private int maxThumbnailSize;

  /**
   * Image scaler.
   */
  private ImageScaler imageScaler;

  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand command,
                                   final long headRevision, final UserRepositoryContext userRepositoryContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    logger.debug("Getting file as 'thumbnail'");

    final OutputStream output = response.getOutputStream();

    if (!mimeFileTypeMap.getContentType(command.getPath()).startsWith("image")) {
      logger.error("File '" + command.getTarget() + "' is not a image file");
      return null;
    }

    prepareResponse(request, response, command);

    final boolean cacheUsed = getRepositoryConfiguration(command.getName()).isCacheUsed();

    ObjectCache objectCache;
    ObjectCacheKey cacheKey;
    byte[] thumbnailData;

    if (cacheUsed) {
      final String checksum = getRepositoryService().getFileChecksum(repository, command.getPath(), command.getRevisionNumber());
      objectCache = objectCacheManager.getCache(command.getName());
      cacheKey = new ObjectCacheKey(command.getPath(), checksum);
      logger.debug("Using cachekey: " + cacheKey);
      thumbnailData = (byte[]) objectCache.get(cacheKey);

      if (thumbnailData == null) {
        // Thumbnail did not exist - create it and cache it
        thumbnailData = createThumbnail(repository, command);
        logger.debug("Caching thumbnail. Using cachekey: " + cacheKey);
        objectCache.put(cacheKey, thumbnailData);
      }
    } else {
      // Cache is not used - always recreate the thumbnail
      thumbnailData = createThumbnail(repository, command);
    }

    output.write(thumbnailData);
    output.flush();
    output.close();
    return null;
  }

  /**
   * Creates a thumbnail version of a full size image.
   *
   * @param repository Repository
   * @param command    Command
   * @return array of image bytes
   */
  private byte[] createThumbnail(final SVNRepository repository, final SVNBaseCommand command) {
    logger.debug("Creating thumbnail for: " + command.getPath());
    final ByteArrayOutputStream fullSizeImageData = new ByteArrayOutputStream();
    final ByteArrayOutputStream thumbnailImageData = new ByteArrayOutputStream();
    try {
      getRepositoryService().getFile(repository, command.getPath(), command.getRevisionNumber(), fullSizeImageData);
      final BufferedImage image = ImageIO.read(new ByteArrayInputStream(fullSizeImageData.toByteArray()));
      ImageIO.write(imageScaler.getThumbnail(image, maxThumbnailSize), imageFormatName, thumbnailImageData);
    } catch (final Exception ex) {
      logger.warn("Unable to create thumbnail", ex);
    }
    return thumbnailImageData.toByteArray();
  }

  /**
   * Prepares the response by setting headers and content type.
   *
   * @param request  Request.
   * @param response Response.
   * @param command  Command.
   */
  protected void prepareResponse(final HttpServletRequest request, final HttpServletResponse response,
                                 final SVNBaseCommand command) {
    response.setHeader(CONTENT_DISPOSITION_HEADER, "inline; filename=\"" + EncodingUtils.encodeFilename(command.getTarget(), request) + "\"");
    response.setContentType(mimeFileTypeMap.getContentType(command.getPath()));
  }

  /**
   * Sets the image scaler.
   *
   * @param imageScaler Image scaler
   */
  public void setImageScaler(final ImageScaler imageScaler) {
    this.imageScaler = imageScaler;
  }

  /**
   * Sets the object cache manager instance.
   *
   * @param objectCacheManager The cache manager instance.
   */
  public void setObjectCacheManager(final ObjectCacheManager objectCacheManager) {
    this.objectCacheManager = objectCacheManager;
  }

  /**
   * Sets the image format name.
   *
   * @param imageFormatName The format name, e.g. <tt>png</tt>.
   */
  public void setImageFormatName(final String imageFormatName) {
    this.imageFormatName = imageFormatName;
  }

  /**
   * Sets the maximum vertical/horizontal size in pixels for the generated thumbnail images.
   *
   * @param maxSize Size in pixels.
   */
  public void setMaxThumbnailSize(final int maxSize) {
    this.maxThumbnailSize = maxSize;
  }

  /**
   * Sets the mime/file type map.
   *
   * @param mimeFileTypeMap Map.
   */
  public void setMimeFileTypeMap(final FileTypeMap mimeFileTypeMap) {
    this.mimeFileTypeMap = mimeFileTypeMap;
  }

}
