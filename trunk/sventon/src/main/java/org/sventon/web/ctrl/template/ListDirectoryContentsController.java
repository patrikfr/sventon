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
package org.sventon.web.ctrl.template;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.SVNConnection;
import org.sventon.model.DirEntry;
import org.sventon.model.DirList;
import org.sventon.model.UserRepositoryContext;
import org.sventon.web.command.BaseCommand;
import org.tmatesoft.svn.core.SVNProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ListDirectoryContentsController.
 * Controller that lists the contents of given repository path at given revision.
 * The resulting model will include:
 * <ul>
 * <li><code>svndir</code> - The list of <code>SVNDirEntry</code> instances</li>
 * <li><code>properties</code> - The path's SVN properties</li>
 * </ul>
 * Note: Sub classes must specify <code>viewName</code> property.
 *
 * @author jesper@sventon.org
 */
public class ListDirectoryContentsController extends AbstractTemplateController {

  @Override
  protected ModelAndView svnHandle(final SVNConnection connection, final BaseCommand command,
                                   final long headRevision, final UserRepositoryContext userRepositoryContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    logger.debug("Getting directory contents for: " + command.getPath());
    final DirList dirList = getRepositoryService().list(
        connection, command.getPathWithTrailingSlash(), command.getRevisionNumber());
    final List<DirEntry> entries = dirList.getEntries();

    final Map<String, Object> model = new HashMap<String, Object>();
    logger.debug("Directory entries: " + entries.size());
    model.put("svndir", entries);
    model.put("properties", dirList.getProperties());
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addAllObjects(model);
    return modelAndView;
  }
}
