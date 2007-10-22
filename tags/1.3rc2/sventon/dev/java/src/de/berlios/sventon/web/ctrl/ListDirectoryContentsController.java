package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.repository.RepositoryEntry;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.model.UserContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

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
 * @author jesper@users.berlios.de
 */
public class ListDirectoryContentsController extends AbstractSVNTemplateController implements Controller {

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand,
                                   final SVNRevision revision, final UserContext userContext,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws Exception {

    // Update trailing / for path
    if (!svnCommand.getPath().endsWith("/")) {
      svnCommand.setPath(svnCommand.getPath() + "/");
    }

    final String completePath = svnCommand.getPath();

    logger.debug("Getting directory contents for: " + completePath);
    final HashMap properties = new HashMap();
    final List<RepositoryEntry> entries = getRepositoryService().list(
        repository, completePath, revision.getNumber(), properties);

    logger.debug("Create model");
    final Map<String, Object> model = new HashMap<String, Object>();
    model.put("svndir", entries);
    logger.debug(properties);
    model.put("properties", properties);
    final ModelAndView modelAndView = new ModelAndView();
    modelAndView.addAllObjects(model);
    return modelAndView;
  }
}
