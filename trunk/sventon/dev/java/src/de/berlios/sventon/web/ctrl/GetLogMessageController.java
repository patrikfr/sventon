package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.UserContext;
import de.berlios.sventon.repository.LogMessage;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.SVNLogEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Gets the log message for given revision.
 *
 * @author jesper@users.berlios.de
 */
public class GetLogMessageController extends AbstractSVNTemplateController implements Controller {

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final UserContext userContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    logger.debug("Getting log message from revision [" + revision.getNumber() + "]");

    final SVNLogEntry logEntry = getRepositoryService().getRevision(svnCommand.getName(), repository,
        revision.getNumber());
    final LogMessage logMessage = new LogMessage(revision.getNumber(), logEntry.getMessage());

    logger.debug("Create model");
    final Map<String, Object> model = new HashMap<String, Object>();
    model.put("logMessage", logMessage);

    return new ModelAndView("ajax/logMessage", model);
  }
}
