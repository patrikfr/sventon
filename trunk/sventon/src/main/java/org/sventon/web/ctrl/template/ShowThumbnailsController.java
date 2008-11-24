/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
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
import org.sventon.model.UserRepositoryContext;
import org.sventon.web.command.MultipleEntriesCommand;
import org.sventon.web.command.SVNBaseCommand;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;

import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for showing selected repository entries as thumbnails.
 *
 * @author jesper@sventon.org
 */
public final class ShowThumbnailsController extends AbstractSVNTemplateController {

  /**
   * The mime/file type map.
   */
  private FileTypeMap mimeFileTypeMap;

  /**
   * {@inheritDoc}
   */
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand cmd,
                                   final long headRevision, final UserRepositoryContext userRepositoryContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    final MultipleEntriesCommand command = (MultipleEntriesCommand) cmd;

    final Map<String, Object> model = new HashMap<String, Object>();
    final List<SVNFileRevision> imageEntries = new ArrayList<SVNFileRevision>();

    logger.debug("Showing thumbnail images");
    // Check what entries are image files - and add them to the list of thumbnails.
    for (final SVNFileRevision entry : command.getEntries()) {
      logger.debug("entry: " + entry.getPath() + "@" + entry.getRevision());
      if (mimeFileTypeMap.getContentType(entry.getPath()).startsWith("image")) {
        imageEntries.add(entry);
      }
    }
    logger.debug(imageEntries.size() + " entries out of " + command.getEntries().length + " are image files");
    model.put("thumbnailentries", imageEntries);
    return new ModelAndView(getViewName(), model);
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
