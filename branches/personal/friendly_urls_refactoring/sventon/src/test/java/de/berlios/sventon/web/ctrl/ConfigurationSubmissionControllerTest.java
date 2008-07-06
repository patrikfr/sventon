package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.TestUtils;
import static de.berlios.sventon.TestUtils.TEMPDIR;
import de.berlios.sventon.appl.Application;
import de.berlios.sventon.appl.RepositoryConfiguration;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.quartz.impl.StdScheduler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;

public class ConfigurationSubmissionControllerTest extends TestCase {

  public void testHandleRequestInternalConfigured() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationSubmissionController controller = new ConfigurationSubmissionController();
    final Application application = TestUtils.getApplicationStub();
    application.setConfigured(true);
    controller.setApplication(application);
    try {
      controller.handleRequestInternal(request, response);
      fail("Should throw IllegalStateException");
    } catch (IllegalStateException ise) {
      // expected
    }
  }

  public void testHandleRequestInternalNoAddedInstance() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationSubmissionController controller = new ConfigurationSubmissionController();
    final Application application = TestUtils.getApplicationStub();
    application.setConfigured(false);
    controller.setApplication(application);

    final ModelAndView modelAndView = controller.handleRequestInternal(request, response);
    assertEquals("configurationError", modelAndView.getViewName());
  }

  public void testHandleRequestInternal() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationSubmissionController controller = new ConfigurationSubmissionController();

    controller.setScheduler(new StdScheduler(null, null) {
      public void triggerJob(final String string, final String string1) {
      }
    });

    final Application application = new Application(new File(TEMPDIR), "tmpconfigfilename");

    final RepositoryConfiguration repositoryConfiguration1 = new RepositoryConfiguration("testrepos1");
    repositoryConfiguration1.setRepositoryUrl("http://localhost/1");
    repositoryConfiguration1.setUid("user1");
    repositoryConfiguration1.setPwd("abc123");
    repositoryConfiguration1.setCacheUsed(false);
    repositoryConfiguration1.setZippedDownloadsAllowed(false);

    final RepositoryConfiguration repositoryConfiguration2 = new RepositoryConfiguration("testrepos2");
    repositoryConfiguration2.setRepositoryUrl("http://localhost/2");
    repositoryConfiguration2.setUid("user2");
    repositoryConfiguration2.setPwd("123abc");
    repositoryConfiguration2.setCacheUsed(false);
    repositoryConfiguration2.setZippedDownloadsAllowed(false);

    application.addRepository(repositoryConfiguration1);
    application.addRepository(repositoryConfiguration2);
    application.setConfigured(false);
    controller.setApplication(application);

    final File configFile1 = new File(TEMPDIR, "testrepos1");
    final File configFile2 = new File(TEMPDIR, "testrepos2");

    assertFalse(configFile1.exists());
    assertFalse(configFile2.exists());

    final ModelAndView modelAndView = controller.handleRequestInternal(request, response);
    assertNotNull(modelAndView);
    assertNull(modelAndView.getViewName()); // Will be null as it is a redirect view.

    //File should now be written
    assertTrue(configFile1.exists());
    assertTrue(configFile2.exists());
    FileUtils.deleteDirectory(configFile1);
    FileUtils.deleteDirectory(configFile2);
    assertFalse(configFile1.exists());
    assertFalse(configFile2.exists());
    assertTrue(application.isConfigured());
  }
}