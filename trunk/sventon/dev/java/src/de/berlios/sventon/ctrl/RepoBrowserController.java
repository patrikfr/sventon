/*
 * ====================================================================
 * Copyright (c) 2005-2006 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.ctrl;

import de.berlios.sventon.command.SVNBaseCommand;
import de.berlios.sventon.repository.RepositoryEntryComparator;
import de.berlios.sventon.repository.RepositoryEntry;
import static de.berlios.sventon.repository.RepositoryEntryComparator.NAME;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * RepoBrowserController.
 * 
 * @author patrikfr@users.berlios.de
 */
public class RepoBrowserController extends AbstractSVNTemplateController implements Controller {

  @SuppressWarnings("unchecked")
  protected ModelAndView svnHandle(final SVNRepository repository, final SVNBaseCommand svnCommand, final SVNRevision revision,
                                   final HttpServletRequest request, final HttpServletResponse response,
                                   final BindException exception) throws SventonException, SVNException {

    final List<RepositoryEntry> dir = Collections.checkedList(new ArrayList<RepositoryEntry>(), RepositoryEntry.class);

    // Update trailing / for path
    if (!svnCommand.getPath().endsWith("/")) {
      svnCommand.setPath(svnCommand.getPath() + "/");
    }

    final String completePath = svnCommand.getPath();
    final Map<String, SVNLock> locks = getLocks(repository, completePath);

    logger.debug("Getting directory contents for: " + completePath);
    final HashMap properties = new HashMap();
    final Collection entries = repository.getDir(completePath, revision.getNumber(), properties, (Collection) null);
    for (Object ent : entries) {
      SVNDirEntry entry = (SVNDirEntry) ent;
      dir.add(new RepositoryEntry(entry,
          completePath,
          locks.get(completePath + entry.getName())));
    }

    Collections.sort(dir, new RepositoryEntryComparator(NAME, true));

    logger.debug("Create model");
    final Map<String, Object> model = new HashMap<String, Object>();
    model.put("svndir", dir);
    logger.debug(properties);
    model.put("properties", properties);

    return new ModelAndView("repobrowser", model);
  }
}
