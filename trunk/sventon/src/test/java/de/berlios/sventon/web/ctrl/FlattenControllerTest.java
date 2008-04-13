package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.TestUtils;
import de.berlios.sventon.appl.RepositoryName;
import de.berlios.sventon.repository.RepositoryEntry;
import de.berlios.sventon.repository.RepositoryEntryComparator;
import de.berlios.sventon.repository.RepositoryEntrySorter;
import de.berlios.sventon.repository.cache.CacheGateway;
import de.berlios.sventon.web.command.SVNBaseCommand;
import de.berlios.sventon.web.model.UserRepositoryContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import org.springframework.web.servlet.ModelAndView;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.List;
import java.util.Map;

public class FlattenControllerTest extends TestCase {

  public void testSvnHandle() throws Exception {
    final CacheGateway mockCache = EasyMock.createMock(CacheGateway.class);

    final List<RepositoryEntry> entries = TestUtils.getFlattenedDirectoriesList();

    final SVNBaseCommand command = new SVNBaseCommand();
    command.setName(new RepositoryName("test"));
    command.setRevision(SVNRevision.create(12));

    final UserRepositoryContext context = new UserRepositoryContext();
    context.setSortMode(RepositoryEntrySorter.SortMode.DESC);
    context.setSortType(RepositoryEntryComparator.SortType.REVISION);

    final FlattenController ctrl = new FlattenController();
    ctrl.setCacheGateway(mockCache);

    expect(mockCache.findDirectories(command.getName(), command.getPath())).andStubReturn(entries);
    replay(mockCache);

    final ModelAndView modelAndView = ctrl.svnHandle(null, command, 100, context, null, null, null);
    final Map model = modelAndView.getModel();
    verify(mockCache);

    assertEquals(2, model.size());
    assertEquals(entries.get(2), ((List<RepositoryEntry>) model.get("svndir")).get(0));
    assertEquals(Boolean.TRUE, model.get("isFlatten"));
    assertEquals("flattenDirResult", modelAndView.getViewName());
  }

}