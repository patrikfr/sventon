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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.model.FileExtensionList;
import org.sventon.model.RepositoryEntry;
import org.sventon.model.UserRepositoryContext;
import org.sventon.util.FileExtensionFilter;
import org.sventon.util.RepositoryEntrySorter;
import org.sventon.web.command.SVNBaseCommand;
import org.tmatesoft.svn.core.io.SVNRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * RepoBrowserController.
 *
 * @author patrikfr@users.berlios.de
 * @author jesper@users.berlios.de
 */
public final class RepoBrowserController extends ListDirectoryContentsController {

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final long headRevision, final UserRepositoryContext userRepositoryContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    final ModelAndView modelAndView = super.svnHandle(repository, svnCommand, headRevision, userRepositoryContext, request,
        response, exception);

    final Map<String, Object> model = modelAndView.getModel();

    final boolean bypassEmpty = ServletRequestUtils.getBooleanParameter(request, "bypassEmpty", false);
    final String filterExtension = ServletRequestUtils.getStringParameter(request, "filterExtension", "all");
    logger.debug("filterExtension: " + filterExtension);

    List<RepositoryEntry> entries = (List<RepositoryEntry>) model.get("svndir");

    if (bypassEmpty && entries.size() == 1) {
      final RepositoryEntry entry = entries.get(0);
      if (RepositoryEntry.Kind.dir == entry.getKind()) {
        logger.debug("Bypassing empty directory: " + svnCommand.getPath());
        svnCommand.setPath(svnCommand.getPath() + entry.getName() + "/");
        final ModelAndView bypassedModelAndView =
            svnHandle(repository, svnCommand, headRevision, userRepositoryContext, request, response, exception);
        bypassedModelAndView.getModel().put("bypassed", true);
        return bypassedModelAndView;
      }
    }

    if (!"all".equals(filterExtension)) {
      final FileExtensionFilter fileExtensionFilter = new FileExtensionFilter(filterExtension);
      entries = fileExtensionFilter.filter(entries);
    }

    logger.debug("Sort params: " + userRepositoryContext.getSortType().name() + ", " + userRepositoryContext.getSortMode());
    new RepositoryEntrySorter(userRepositoryContext.getSortType(), userRepositoryContext.getSortMode()).sort(entries);

    logger.debug("Adding data to model");
    model.put("svndir", entries);
    model.put("locks", getRepositoryService().getLocks(repository, svnCommand.getPath()));
    model.put("existingExtensions", new FileExtensionList(entries).getExtensions());
    model.put("filterExtension", filterExtension);
    modelAndView.setViewName("repoBrowser");
    return modelAndView;
  }
}
