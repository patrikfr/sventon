package de.berlios.sventon.ctrl;

import de.berlios.sventon.svnsupport.SVNDirEntryComparator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static de.berlios.sventon.svnsupport.SVNDirEntryComparator.NAME;

/**
 * RepoBrowserController.
 * @author patrikfr@users.berlios.de
 */
public class RepoBrowserController extends AbstractSVNTemplateController implements Controller {

  protected ModelAndView svnHandle(SVNRepository repository, SVNBaseCommand svnCommand, SVNRevision revision,
      HttpServletRequest request, HttpServletResponse response) throws SVNException {
    
    List<SVNDirEntry> dir = Collections.checkedList(new ArrayList<SVNDirEntry>(), SVNDirEntry.class);
    
    String path = svnCommand.getPath();
    logger.debug("Getting directory contents for: " + path);
    HashMap properties = new HashMap();
    dir.addAll(repository.getDir(path, revision.getNumber(), properties, (Collection) null));
    logger.debug(properties);
    Collections.sort(dir, new SVNDirEntryComparator(NAME, true));

    logger.debug("Create model");
    Map<String, Object> model = new HashMap<String, Object>();
    model.put("svndir", dir);
    
    //Update trailing / for path
    if (!path.endsWith("/")) {
      svnCommand.setPath(path + "/");
    }

    return new ModelAndView("repobrowser", model);
  }
}
