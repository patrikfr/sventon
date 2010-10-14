package org.sventon.web.ctrl.template;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;
import org.sventon.TestUtils;
import org.sventon.appl.Application;
import org.sventon.appl.ConfigDirectory;
import org.sventon.appl.RepositoryConfiguration;
import org.sventon.model.DirEntry;
import org.sventon.model.RepositoryName;
import org.sventon.model.Revision;
import org.sventon.model.UserRepositoryContext;
import org.sventon.service.RepositoryService;
import org.sventon.web.command.BaseCommand;

import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class DirEntryTrayControllerTest {

  private final BaseCommand command = new BaseCommand();
  private RepositoryService mockService;
  private MockHttpServletRequest request;
  private DirEntryTrayController ctrl;
  private DirEntry entry;
  private UserRepositoryContext context;

  @Before
  public void setUp() throws Exception {
    final ConfigDirectory configDirectory = TestUtils.getTestConfigDirectory();
    configDirectory.setCreateDirectories(false);
    final MockServletContext servletContext = new MockServletContext();
    servletContext.setContextPath("sventon-test");
    configDirectory.setServletContext(servletContext);
    final Application application = new Application(configDirectory);

    final RepositoryConfiguration configuration = new RepositoryConfiguration("test");
    configuration.setEnableEntryTray(true);
    application.addConfiguration(configuration);

    entry = new DirEntry("/", "/trunk/test", "", null, DirEntry.Kind.FILE, 0, 0);

    command.setPath("/trunk/test");
    command.setName(new RepositoryName("test"));
    command.setRevision(Revision.create(10));
    command.setPegRevision(5);

    mockService = EasyMock.createMock(RepositoryService.class);
    request = new MockHttpServletRequest();
    ctrl = new DirEntryTrayController();
    ctrl.setRepositoryService(mockService);
    ctrl.setApplication(application);
    context = new UserRepositoryContext();
  }

  @After
  public void tearDown() throws Exception {
    EasyMock.reset(mockService);
  }

  private Map executeTest() throws Exception {
    expect(mockService.getEntryInfo(null, command.getPath(), command.getPegRevision().getNumber())).andStubReturn(entry);
    replay(mockService);
    final ModelAndView modelAndView = ctrl.svnHandle(null, command, 100, context, request, null, null);
    verify(mockService);
    return modelAndView.getModel();
  }

  @Test
  public void testAddAndRemove() throws Exception {
    request.setParameter("action", DirEntryTrayController.PARAMETER_ADD);
    assertEquals(0, context.getDirEntryTray().getSize());

    Map model = executeTest();

    assertEquals(0, model.size());
    assertEquals(1, context.getDirEntryTray().getSize());

    EasyMock.reset(mockService);

    request.setParameter("action", DirEntryTrayController.PARAMETER_REMOVE);

    model = executeTest();

    assertEquals(0, model.size());
    assertEquals(0, context.getDirEntryTray().getSize());
  }

}