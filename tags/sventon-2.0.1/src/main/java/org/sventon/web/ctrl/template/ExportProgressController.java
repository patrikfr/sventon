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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.export.ExportExecutor;
import org.sventon.model.UserRepositoryContext;
import org.sventon.web.command.SVNBaseCommand;
import org.tmatesoft.svn.core.io.SVNRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The export progress controller.
 *
 * @author jesper@sventon.org
 */
public final class ExportProgressController extends AbstractSVNTemplateController {

  /**
   * The export executor instance.
   */
  private ExportExecutor exportExecutor;

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand command,
                                   final long headRevision, final UserRepositoryContext userRepositoryContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    final Map<String, Object> model = new HashMap<String, Object>();
    final UUID exportUuid = UUID.fromString(ServletRequestUtils.getRequiredStringParameter(request, "uuid"));
    final boolean download = ServletRequestUtils.getBooleanParameter(request, "download", false);
    final boolean delete = ServletRequestUtils.getBooleanParameter(request, "delete", false);

    if (delete) {
      exportExecutor.delete(exportUuid);
      userRepositoryContext.setIsWaitingForExport(false);
    } else if (download) {
      logger.info("Downloading export file, uuid: " + exportUuid);
      exportExecutor.downloadByUUID(exportUuid, request, response);
      return null;
    } else {
      final boolean finished = exportExecutor.isExported(exportUuid);
      logger.debug("Export finished: " + finished);
      model.put("exportFinished", finished);
    }
    return new ModelAndView(getViewName(), model);
  }

  /**
   * Sets the export executor instance.
   *
   * @param exportExecutor Export executor instance.
   */
  public void setExportExecutor(final ExportExecutor exportExecutor) {
    this.exportExecutor = exportExecutor;
  }

}