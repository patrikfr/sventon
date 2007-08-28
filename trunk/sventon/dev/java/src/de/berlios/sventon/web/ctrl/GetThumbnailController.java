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

import de.berlios.sventon.repository.cache.objectcache.ObjectCache;
import de.berlios.sventon.repository.cache.objectcache.ObjectCacheManager;
import de.berlios.sventon.util.ImageUtil;
import de.berlios.sventon.util.PathUtil;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.UserContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

/**
 * Controller used when downloading single image files as thumbnails.
 *
 * @author jesper@users.berlios.de
 */
public class GetThumbnailController extends AbstractSVNTemplateController implements Controller {

  /**
   * Object cache manager instance.
   */
  private ObjectCacheManager objectCacheManager;

  /**
   * Image utility.
   */
  protected ImageUtil imageUtil;

  /**
   * Image format name to use when generating thumbnails.
   */
  private String imageFormatName;

  /**
   * Specifies the maximum horizontal/vertical size (in pixels) for a generated thumbnail image.
   */
  private int maxThumbnailSize;

  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final UserContext userContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    logger.debug("Getting file as 'thumbnail'");

    final ServletOutputStream output = response.getOutputStream();

    if (!imageUtil.isImageFileExtension(PathUtil.getFileExtension(svnCommand.getPath()))) {
      logger.error("File '" + svnCommand.getTarget() + "' is not a image file");
      return null;
    }

    response.setHeader("Content-disposition", "inline; filename=\"" + svnCommand.getTarget() + "\"");
    response.setContentType(imageUtil.getContentType(PathUtil.getFileExtension(svnCommand.getPath())));
    final URL fullSizeImageUrl = new URL(getFullSizeImageURL(request));
    final ObjectCache objectCache = objectCacheManager.getCache(svnCommand.getName());
    getRepositoryService().getThumbnailImage(repository, objectCache, svnCommand.getPath(), revision.getNumber(),
        fullSizeImageUrl, imageFormatName, maxThumbnailSize, output);
    output.flush();
    output.close();
    return null;
  }

  private String getFullSizeImageURL(final HttpServletRequest request) {
    final StringBuilder urlString = new StringBuilder(
        request.getRequestURL().toString().replaceAll("getthumb.svn", "get.svn"));  //TODO: remove ugly hard-coding!
    urlString.append("?");
    urlString.append(request.getQueryString());
    return urlString.toString();
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
   * Sets the <code>ImageUtil</code> helper instance.
   *
   * @param imageUtil The instance
   * @see de.berlios.sventon.util.ImageUtil
   */
  public void setImageUtil(final ImageUtil imageUtil) {
    this.imageUtil = imageUtil;
  }

  /**
   * Sets the maximum vertical/horizontal size in pixels for the generated thumbnail images.
   *
   * @param maxSize Size in pixels.
   */
  public void setMaxThumbnailSize(final int maxSize) {
    this.maxThumbnailSize = maxSize;
  }
}
