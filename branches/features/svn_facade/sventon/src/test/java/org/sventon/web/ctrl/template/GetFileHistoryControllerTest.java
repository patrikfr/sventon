package org.sventon.web.ctrl.template;

import junit.framework.TestCase;

import static org.easymock.EasyMock.expect;

import org.easymock.classextension.EasyMock;

import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.model.RepositoryName;
import org.sventon.model.Revision;
import org.sventon.service.RepositoryService;
import org.sventon.web.command.BaseCommand;
import org.tmatesoft.svn.core.io.SVNFileRevision;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetFileHistoryControllerTest extends TestCase {

  private final List<SVNFileRevision> fileRevisions = new ArrayList<SVNFileRevision>();
  private final BaseCommand command = new BaseCommand();
  private RepositoryService mockService;
  private MockHttpServletRequest request;
  private GetFileHistoryController ctrl;

  protected void setUp() throws Exception {
    fileRevisions.add(new SVNFileRevision("/trunk/test", 1, null, null));
    fileRevisions.add(new SVNFileRevision("/trunk/test", 2, null, null));
    fileRevisions.add(new SVNFileRevision("/trunk/test", 3, null, null));

    command.setPath("/trunk/test");
    command.setName(new RepositoryName("test"));
    command.setRevision(Revision.create(12));

    mockService = EasyMock.createMock(RepositoryService.class);
    request = new MockHttpServletRequest();
    ctrl = new GetFileHistoryController();
    ctrl.setRepositoryService(mockService);
  }

  protected void tearDown() throws Exception {
    EasyMock.reset(mockService);
  }

  private Map executeTest() throws Exception {
    expect(mockService.getFileRevisions(null, command.getPath(), command.getRevisionNumber())).andStubReturn(fileRevisions);
    replay(mockService);
    final ModelAndView modelAndView = ctrl.svnHandle(null, command, 100, null, request, null, null);
    verify(mockService);
    return modelAndView.getModel();
  }

  public void testGetFileHistoryController() throws Exception {
    final Map model = executeTest();
    assertEquals(2, model.size());
    assertTrue(command.getRevision().getNumber() == (Long) model.get("currentRevision"));
    final List<SVNFileRevision> fileRevisionsInModel = (List<SVNFileRevision>) model.get("fileRevisions");
    assertEquals(fileRevisionsInModel.size(), fileRevisions.size());
    assertEquals(3, fileRevisionsInModel.get(0).getRevision());
  }

  public void testGetFileHistoryControllerArchivedEntry() throws Exception {
    request.setParameter(GetFileHistoryController.ARCHIVED_ENTRY, "zippedTestFile");
    final Map model = executeTest();
    assertEquals(3, model.size());
    assertTrue(command.getRevision().getNumber() == (Long) model.get("currentRevision"));
    final List<SVNFileRevision> fileRevisionsInModel = (List<SVNFileRevision>) model.get("fileRevisions");
    assertEquals(fileRevisionsInModel.size(), fileRevisions.size());
    assertEquals(3, fileRevisionsInModel.get(0).getRevision());
    assertEquals("zippedTestFile", model.get(GetFileHistoryController.ARCHIVED_ENTRY));
  }

  public void testDateFormat() throws Exception {
    final String source = "2008-08-19T11:12:38.765624Z";
    new SimpleDateFormat(GetFileHistoryController.ISO8601_FORMAT_PATTERN).parse(source);
  }
}