/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.appl.Application;
import de.berlios.sventon.appl.RepositoryConfiguration;
import de.berlios.sventon.model.AvailableCharsets;
import de.berlios.sventon.repository.RepositoryEntryComparator;
import de.berlios.sventon.repository.RepositoryEntrySorter;
import de.berlios.sventon.repository.RepositoryFactory;
import de.berlios.sventon.repository.cache.CacheGateway;
import de.berlios.sventon.service.RepositoryService;
import de.berlios.sventon.util.WebUtils;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.model.UserContext;
import de.berlios.sventon.web.model.UserRepositoryContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for use by controllers whishing to make use of basic
 * plumbing functionality such as authorization and basic application configuration.
 * <p/>
 * This abstract controller is based on the GoF Template pattern, the method to
 * implement for extending controllers is
 * <code>{@link #svnHandle(SVNRepository,SVNBaseCommand,SVNRevision,UserRepositoryContext,
 * HttpServletRequest,HttpServletResponse,BindException)}</code>.
 * <p/>
 * Workflow for this controller:
 * <ol>
 * <li>The controller inspects the application configuration object to see if it's
 * user id and pwd have been provided during setup. If credentials are
 * configured they will be used for authorized repository access, if they do not
 * exist the controller will try to set up the repository with anonymous access.
 * If this fails the user will be forwarded to an error page.
 * <li>The controller configures the <code>SVNRepository</code> object and
 * calls the extending class'
 * {@link #svnHandle(SVNRepository,SVNBaseCommand,SVNRevision,UserRepositoryContext,
 * HttpServletRequest,HttpServletResponse,BindException)}
 * method with the given {@link de.berlios.sventon.web.command.SVNBaseCommand}
 * containing request parameters.
 * <li>After the call returns, the controller adds additional information to
 * the the model (see below) and forwards the request to the view returned
 * together with the model by the
 * {@link #svnHandle(SVNRepository,SVNBaseCommand,SVNRevision,UserRepositoryContext,
 * HttpServletRequest,HttpServletResponse,BindException)}
 * method.
 * </ol>
 * <b>Model</b><br>
 * The following information will be added by this controller to the model
 * returned by the controller called (see flow above): <table>
 * <tr>
 * <th>key</th>
 * <th>content</th>
 * </tr>
 * <tr>
 * <td>url</td>
 * <td>SVN URL as configured for this web application</td>
 * </tr>
 * <tr>
 * <td>numrevision</td>
 * <td>SVN revision this request concerns, actual revision number</td>
 * </tr>
 * <tr>
 * <td>command</td>
 * <td>{@link SVNBaseCommand}-object</td>
 * </tr>
 * </table> <p/> <b>Input arguments</b><br>
 * Input to this argument is wrapped in a
 * <code>{@link SVNBaseCommand}</code> object by the
 * Spring framework. If the extending controller is configured in the Spring
 * config file with a validator for the <code>SVNBaseCommand</code> it will be
 * checked for binding errors. If binding errors were detected an exception
 * model will be created an control forwarded to an error view. respectively.
 * <b>Exception handling</b>
 * <dl>
 * <dt>Authentication exception
 * <dd>If a SVN authentication exception occurs during the call the request
 * will be forwarded to the authenticationfailuare.jsp page.
 * <dt>Other SVN exceptions
 * <dd>Other SVN exceptons are currently forwarded to a generic error handlng
 * page.
 * </dl>
 *
 * @author patrikfr@users.berlios.de
 * @author jesper@users.berlios.de
 */
public abstract class AbstractSVNTemplateController extends AbstractCommandController {

  /**
   * The application.
   */
  private Application application;

  /**
   * Gateway class for accessing the caches.
   */
  private CacheGateway cacheGateway;

  /**
   * Maximum number of revisions.
   */
  private int maxRevisionsCount;

  /**
   * Maximum number of entries per page.
   */
  private int maxEntriesCount;

  /**
   * Cached available charsets.
   */
  private AvailableCharsets availableCharsets;

  /**
   * Mode for searching entries.
   */
  public static final String ENTRIES_SEARCH_MODE = "entries";

  /**
   * Mode for searching log messages.
   */
  public static final String LOGMESSAGES_SEARCH_MODE = "logMessages";

  /**
   * Request parameter controlling charset.
   */
  private static final String CHARSET_REQUEST_PARAMETER = "charset";

  /**
   * Request parameter controlling search mode.
   */
  private static final String SEARCH_MODE_REQUEST_PARAMETER = "searchMode";

  /**
   * Request parameter controlling revision count.
   */
  private static final String REVISION_COUNT_REQUEST_PARAMETER = "revcount";

  /**
   * Request parameter controlling entries sort type.
   */
  private static final String SORT_TYPE_REQUEST_PARAMETER = "sortType";

  /**
   * Request parameter controlling entries sort mode.
   */
  private static final String SORT_MODE_REQUEST_PARAMETER = "sortMode";

  /**
   * Service.
   */
  private RepositoryService repositoryService;

  /**
   * The repository factory.
   */
  private RepositoryFactory repositoryFactory;


  /**
   * Constructor.
   */
  protected AbstractSVNTemplateController() {
    setCommandClass(SVNBaseCommand.class);
    setCacheSeconds(0); // Tell Spring to generate no-cache headers.
  }

  /**
   * {@inheritDoc}
   */
  public final ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                                   final Object command, final BindException errors) {

    final SVNBaseCommand svnCommand = (SVNBaseCommand) command;

    // If application config is not ok - redirect to config.jsp
    if (!application.isConfigured()) {
      logger.debug("sventon not configured, redirecting to 'config.svn'");
      return new ModelAndView(new RedirectView("config.svn"));
    }

    final Set<String> instanceNames = application.getInstanceNames();
    if (svnCommand.getName() == null || !instanceNames.contains(svnCommand.getName())) {
      logger.debug("InstanceName [" + svnCommand.getName() + "] does not exist, redirecting to 'listinstances.svn'");
      return new ModelAndView(new RedirectView("listinstances.svn"));
    }

    if (errors.hasErrors()) {
      return prepareExceptionModelAndView(errors, svnCommand);
    }

    SVNRepository repository = null;
    try {
      final RepositoryConfiguration configuration = application.getInstance(svnCommand.getName()).getConfiguration();
      final UserRepositoryContext repositoryContext = getUserContext(request, svnCommand.getName());

      if (configuration.isAccessControlEnabled()) {
        repository = repositoryFactory.getRepository(configuration.getInstanceName(), configuration.getSVNURL(),
            repositoryContext.getUid(), repositoryContext.getPwd());
      } else {
        repository = repositoryFactory.getRepository(configuration.getInstanceName(), configuration.getSVNURL(),
            configuration.getUid(), configuration.getPwd());
      }

      final boolean showLatestRevInfo = ServletRequestUtils.getBooleanParameter(request, "showlatestrevinfo", false);
      final SVNRevision requestedRevision = convertAndUpdateRevision(svnCommand, repository);
      final long headRevision = getRepositoryService().getLatestRevision(repository);

      parseAndUpdateSortParameters(request, repositoryContext);
      parseAndUpdateLatestRevisionsDisplayCount(request, repositoryContext);
      parseAndUpdateCharsetParameter(request, repositoryContext);
      parseAndUpdateSearchModeParameter(request, repositoryContext);
      final ModelAndView modelAndView =
          svnHandle(repository, svnCommand, requestedRevision, repositoryContext, request, response, errors);

      // It's ok for svnHandle to return null in cases like GetController.
      // If the view is a RedirectView it's model has already been populated
      if (modelAndView != null && !(modelAndView.getView() instanceof RedirectView)) {
        final Map<String, Object> model = new HashMap<String, Object>();
        logger.debug("'command' set to: " + svnCommand);
        model.put("command", svnCommand); // This is for the form to work
        model.put("repositoryURL", configuration.getRepositoryDisplayUrl());
        model.put("numrevision", (requestedRevision == HEAD ? Long.toString(headRevision) : null));
        model.put("isHead", requestedRevision == HEAD);
        model.put("isUpdating", application.getInstance(svnCommand.getName()).isUpdatingCache());
        model.put("useCache", configuration.isCacheUsed());
        model.put("isZipDownloadsAllowed", configuration.isZippedDownloadsAllowed());
        model.put("instanceNames", application.getInstanceNames());
        model.put("maxRevisionsCount", getMaxRevisionsCount());
        model.put("headRevision", headRevision);
        model.put("charsets", availableCharsets.getCharsets());
        model.put("userRepositoryContext", repositoryContext);

        if (showLatestRevInfo) {
          logger.debug("Fetching [" + repositoryContext.getLatestRevisionsDisplayCount() + "] latest revisions for display");
          model.put("revisions", getRepositoryService().getRevisions(svnCommand.getName(), repository, headRevision, 1, "/",
              repositoryContext.getLatestRevisionsDisplayCount()));
        }

        modelAndView.addAllObjects(model);
      }
      return modelAndView;
    } catch (SVNAuthenticationException svnae) {
      logger.debug(svnae.getMessage());
      return forwardToAuthenticationFailureView(request);
    } catch (Exception ex) {
      logger.error("Exception", ex);
      final Throwable cause = ex.getCause();
      if (cause instanceof NoRouteToHostException || cause instanceof ConnectException) {
        errors.reject("error.message.no-route-to-host");
      } else {
        errors.reject(null, ex.getMessage());
      }
      return prepareExceptionModelAndView(errors, svnCommand);
    } finally {
      if (repository != null) {
        repository.closeSession();
      }
    }

  }

  /**
   * Parses the parameter controlling what charset to use.
   *
   * @param request     The request.
   * @param userContext The UserContext instance to update.
   */
  private void parseAndUpdateCharsetParameter(final HttpServletRequest request,
                                              final UserRepositoryContext userContext) {
    final String charset = ServletRequestUtils.getStringParameter(request, CHARSET_REQUEST_PARAMETER, null);
    if (charset != null) {
      userContext.setCharset(charset);
    } else if (userContext.getCharset() == null) {
      userContext.setCharset(availableCharsets.getDefaultCharset());
    }
  }

  /**
   * Parses the parameter controlling what search mode to use.
   *
   * @param request     The request.
   * @param userContext The UserContext instance to update.
   */
  private void parseAndUpdateSearchModeParameter(final HttpServletRequest request,
                                                 final UserRepositoryContext userContext) {
    final String searchMode = ServletRequestUtils.getStringParameter(request, SEARCH_MODE_REQUEST_PARAMETER, null);
    if (searchMode != null) {
      userContext.setSearchMode(searchMode);
    } else if (userContext.getSearchMode() == null) {
      userContext.setSearchMode(ENTRIES_SEARCH_MODE);
    }
  }

  /**
   * Parses the parameter controlling how many revisions should be displayed in the
   * <i>latest commit info</i> DIV.
   *
   * @param request     The request.
   * @param userContext The UserContext instance to update.
   */
  private void parseAndUpdateLatestRevisionsDisplayCount(final HttpServletRequest request,
                                                         final UserRepositoryContext userContext) {
    final int latestRevisionsDisplayCount =
        ServletRequestUtils.getIntParameter(request, REVISION_COUNT_REQUEST_PARAMETER, 0);
    if (latestRevisionsDisplayCount <= getMaxRevisionsCount() && latestRevisionsDisplayCount >= 0) {
      if (latestRevisionsDisplayCount > 0) {
        userContext.setLatestRevisionsDisplayCount(latestRevisionsDisplayCount);
      }
    } else {
      throw new IllegalArgumentException("Illegal revision count: " + latestRevisionsDisplayCount);
    }
  }

  /**
   * Parses sort mode and type parameters from the request instance and
   * updates the <code>UserContext</code> instance.
   *
   * @param request     The request.
   * @param userContext The UserContext instance to update.
   */
  protected final void parseAndUpdateSortParameters(final HttpServletRequest request,
                                                    final UserRepositoryContext userContext) {
    final String sortType = ServletRequestUtils.getStringParameter(request, SORT_TYPE_REQUEST_PARAMETER, null);
    final String sortMode = ServletRequestUtils.getStringParameter(request, SORT_MODE_REQUEST_PARAMETER, null);

    if (sortType != null) {
      userContext.setSortType(RepositoryEntryComparator.SortType.valueOf(sortType));
    } else if (userContext.getSortType() == null) {
      userContext.setSortType(RepositoryEntryComparator.SortType.FULL_NAME);
    }

    if (sortMode != null) {
      userContext.setSortMode(RepositoryEntrySorter.SortMode.valueOf(sortMode));
    } else if (userContext.getSortMode() == null) {
      userContext.setSortMode(RepositoryEntrySorter.SortMode.ASC);
    }
  }

  /**
   * Gets the UserContext instance from the user's HTTPSession.
   * If session does not exist, it will be created. If the attribute
   * <code>userContext</code> does not exists, a new instance will be
   * created and added to the session.
   *
   * @param request      The HTTP request.
   * @param instanceName Instance name.
   * @return The UserContext instance.
   * @see UserContext
   */
  protected final UserRepositoryContext getUserContext(final HttpServletRequest request, final String instanceName) {
    final HttpSession session = request.getSession(true);
    final String uid = ServletRequestUtils.getStringParameter(request, "uid", "");
    final String pwd = ServletRequestUtils.getStringParameter(request, "pwd", "");

    UserContext userContext = (UserContext) session.getAttribute("userContext");
    if (userContext == null) {
      userContext = new UserContext();
      session.setAttribute("userContext", userContext);
    }

    UserRepositoryContext repositoryContext = userContext.getRepositoryContext(instanceName);
    if (repositoryContext == null) {
      repositoryContext = new UserRepositoryContext();
      userContext.add(instanceName, repositoryContext);
    }

    if (!StringUtils.isEmpty(uid) && !StringUtils.isEmpty(pwd)) {
      repositoryContext.setUid(uid);
      repositoryContext.setPwd(pwd);
    }

    return repositoryContext;
  }

  /**
   * Prepare authentication model. This setus up a model and redirect view with
   * all stuff needed to redirect control to the login page.
   *
   * @param request Request.
   * @return Redirect view for logging in, with original request info stored in
   *         session to enable the authentication control to proceed with
   *         original request once the user is authenticated.
   */
  private ModelAndView forwardToAuthenticationFailureView(final HttpServletRequest request) {
    final Map<String, Object> model = new HashMap<String, Object>();
    model.put("parameters", request.getParameterMap());
    model.put("action", WebUtils.extractServletNameFromRequest(request));
    logger.debug("Forwarding to 'authenticationfailure' view");
    return new ModelAndView("authenticationFailure", model);
  }

  /**
   * Prepares the exception model and view with basic data
   * needed to for displaying a useful error message.
   *
   * @param exception  Bind exception from Spring MVC validation.
   * @param svnCommand Command object.
   * @return The packaged model and view.
   */
  @SuppressWarnings("unchecked")
  protected final ModelAndView prepareExceptionModelAndView(final BindException exception,
                                                            final SVNBaseCommand svnCommand) {
    final RepositoryConfiguration repositoryConfiguration = application.getInstance(svnCommand.getName()).getConfiguration();
    final Map<String, Object> model = exception.getModel();
    logger.debug("'command' set to: " + svnCommand);
    model.put("command", svnCommand);
    model.put("repositoryURL", repositoryConfiguration != null ? repositoryConfiguration.getRepositoryDisplayUrl() : "");
    model.put("numrevision", null);
    return new ModelAndView("goto", model);
  }

  /**
   * Converts the revision <code>String</code> to a format suitable for SVN,
   * also handles special logical revision HEAD. <code>null</code> and empty
   * string revision are converted to the HEAD revision.
   * <p/>
   * The given <code>SVNBaseCommand</code> instance will be updated with key
   * word HEAD, if revision was <code>null</code> or empty <code>String</code>.
   *
   * @param svnCommand Command object.
   * @param repository Repository instance.
   * @return The converted SVN revision.
   * @throws SVNException if unable to get dated revision.
   */
  protected final SVNRevision convertAndUpdateRevision(final SVNBaseCommand svnCommand, final SVNRepository repository)
      throws SVNException {

    if (svnCommand.getRevision() != null && !"".equals(svnCommand.getRevision())
        && !"HEAD".equals(svnCommand.getRevision())) {
      SVNRevision revision = SVNRevision.parse(svnCommand.getRevision());
      if (revision.getNumber() == -1 && revision.getDate() != null) {
        revision = SVNRevision.create(repository.getDatedRevision(revision.getDate()));
        svnCommand.setRevision(String.valueOf(revision.getNumber()));
      }
      return revision;
    } else {
      svnCommand.setRevision("HEAD");
      return HEAD;
    }
  }

  /**
   * Abstract method to be implemented by the controller subclassing this
   * controller. This is where the actual work takes place. See class
   * documentation for info on workflow and on how all this works together.
   *
   * @param repository            Reference to the repository, prepared with authentication
   *                              if applicable.
   * @param svnCommand            Command (basically request parameters submitted in user
   *                              request)
   * @param revision              SVN type revision.
   * @param userRepositoryContext The user's context instance for this repository.
   * @param request               Servlet request.
   * @param response              Servlet response.
   * @param exception             BindException, could be used by the subclass to add error
   *                              messages to the exception.
   * @return Model and view to render.
   * @throws Exception Thrown if exception occurs during SVN operations.
   */
  protected abstract ModelAndView svnHandle(final SVNRepository repository,
                                            final SVNBaseCommand svnCommand,
                                            final SVNRevision revision,
                                            final UserRepositoryContext userRepositoryContext,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final BindException exception) throws Exception;


  /**
   * Sets the cache gateway instance.
   *
   * @param cacheGateway The cache gateway instance.
   */
  public final void setCacheGateway(final CacheGateway cacheGateway) {
    this.cacheGateway = cacheGateway;
  }

  /**
   * Gets the cache instance.
   *
   * @return The instance.
   */
  public final CacheGateway getCache() {
    return cacheGateway;
  }

  /**
   * Sets the application.
   *
   * @param application Application
   */
  public final void setApplication(final Application application) {
    this.application = application;
  }

  /**
   * Get current application configuration.
   *
   * @param instanceName Instance name
   * @return ApplicationConfiguration
   */
  public final RepositoryConfiguration getInstanceConfiguration(final String instanceName) {
    return application.getInstance(instanceName).getConfiguration();
  }

  /**
   * Gets the repository service instance.
   *
   * @return Repository service
   */
  public final RepositoryService getRepositoryService() {
    return repositoryService;
  }

  /**
   * Sets the repository service instance.
   *
   * @param repositoryService The service instance.
   */
  public final void setRepositoryService(final RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  /**
   * Sets the maximum number of revisions.
   *
   * @param maxRevisionsCount Max count.
   */
  public final void setMaxRevisionsCount(final int maxRevisionsCount) {
    this.maxRevisionsCount = maxRevisionsCount;
  }

  /**
   * Gets the maximum number of revisions a user can choose to display
   * in the <i>latest commit info</i> DIV.
   *
   * @return Count
   */
  protected final int getMaxRevisionsCount() {
    return maxRevisionsCount;
  }

  /**
   * Sets the maximum number of entries per page.
   *
   * @param maxEntriesCount Max count.
   */
  public final void setMaxEntriesCount(final int maxEntriesCount) {
    this.maxEntriesCount = maxEntriesCount;
  }

  /**
   * Gets the maximum number of entries per page.
   *
   * @return Count
   */
  protected final int getMaxEntriesCount() {
    return maxEntriesCount;
  }

  /**
   * Sets the available charsets.
   *
   * @param availableCharsets Charsets
   */
  public final void setAvailableCharsets(final AvailableCharsets availableCharsets) {
    this.availableCharsets = availableCharsets;
  }

  /**
   * Sets the repository factory instance.
   *
   * @param repositoryFactory Factory.
   */
  public final void setRepositoryFactory(final RepositoryFactory repositoryFactory) {
    this.repositoryFactory = repositoryFactory;
  }

}
