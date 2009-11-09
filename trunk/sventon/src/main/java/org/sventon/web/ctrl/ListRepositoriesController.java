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
package org.sventon.web.ctrl;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.sventon.appl.Application;
import org.sventon.model.RepositoryName;
import org.sventon.model.UserContext;
import org.sventon.model.UserRepositoryContext;
import static org.sventon.util.EncodingUtils.encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller that creates a list of all configured repositories.
 *
 * @author jesper@sventon.org
 */
public final class ListRepositoriesController extends AbstractController {

  /**
   * The application.
   */
  private Application application;

  /**
   * {@inheritDoc}
   */
  protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
      throws Exception {

    // If application config is not ok - redirect to config.jsp
    if (!application.isConfigured()) {
      logger.debug("sventon not configured, redirecting to '/repos/listconfigs'");
      return new ModelAndView(new RedirectView("/repos/listconfigs", true));
    }

    // Clear credentials if logout param is supplied
    final boolean logout = ServletRequestUtils.getBooleanParameter(request, "logout", false);

    if (logout) {
      final RepositoryName repositoryName = new RepositoryName(ServletRequestUtils.getRequiredStringParameter(request, "repositoryName"));
      final HttpSession session = request.getSession(false);
      if (session != null) {
        final UserContext userContext = (UserContext) session.getAttribute("userContext");
        final UserRepositoryContext userRepositoryContext = userContext.getUserRepositoryContext(repositoryName);
        if (userRepositoryContext != null) {
          userRepositoryContext.clearCredentials();
        }
      }
    }

    final ModelAndView modelAndView;

    if (application.getRepositoryCount() > 1) {
      final Map<String, Object> model = new HashMap<String, Object>();
      model.put("repositoryNames", application.getRepositoryNames());
      model.put("isEditableConfig", application.isEditableConfig());
      modelAndView = new ModelAndView("listRepositories", model);
    } else if (application.getRepositoryCount() == 1) {
      final RepositoryName repositoryName = application.getRepositoryNames().iterator().next();
      modelAndView = new ModelAndView(new RedirectView(createListUrl(repositoryName), true));
    } else {
      throw new IllegalStateException("No repository has been configured!");
    }
    return modelAndView;
  }

  protected String createListUrl(final RepositoryName repositoryName) {
    return "/repos/" + encode(repositoryName.toString()) + "/list/";
  }

  /**
   * Sets the application.
   *
   * @param application Application
   */
  public void setApplication(final Application application) {
    this.application = application;
  }
}
