package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.repository.SVNRepositoryStub;
import de.berlios.sventon.web.command.SVNBaseCommand;
import junit.framework.TestCase;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.List;
import java.util.Map;

public class ShowThumbnailsControllerTest extends TestCase {

  public void testSvnHandle() throws Exception {
    final SVNBaseCommand command = new SVNBaseCommand();
    final ShowThumbnailsController ctrl = new ShowThumbnailsController();

    final ConfigurableMimeFileTypeMap mftm = new ConfigurableMimeFileTypeMap();
    mftm.afterPropertiesSet();
    ctrl.setMimeFileTypeMap(mftm);

    final MockHttpServletRequest req = new MockHttpServletRequest();
    final String[] pathEntries = new String[]{"file1.gif", "file2.jpg", "file.abc"};
    req.addParameter("entry", pathEntries);

    req.addParameter(GetController.DISPLAY_REQUEST_PARAMETER, GetController.DISPLAY_TYPE_INLINE);

    final ModelAndView modelAndView = ctrl.svnHandle(new TestRepository(),
        command, SVNRevision.HEAD, null, req, null, null);

    final Map model = modelAndView.getModel();
    final List entries = (List) model.get("thumbnailentries");

    assertEquals(2, entries.size());
  }

  static class TestRepository extends SVNRepositoryStub {
    public TestRepository() throws SVNException {
      super(SVNURL.parseURIDecoded("http://localhost/"), null);
    }
  }

}