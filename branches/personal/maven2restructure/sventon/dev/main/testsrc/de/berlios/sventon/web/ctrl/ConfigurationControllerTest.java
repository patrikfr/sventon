package de.berlios.sventon.web.ctrl;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import de.berlios.sventon.appl.InstanceConfiguration;
import de.berlios.sventon.appl.Application;
import de.berlios.sventon.appl.Instance;
import de.berlios.sventon.web.command.ConfigCommand;

import java.util.Map;
import java.util.Set;
import java.io.File;

public class ConfigurationControllerTest extends TestCase {

  private static final String TEMPDIR = System.getProperty("java.io.tmpdir");

  public void testShowFormNonConfigured() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    ctrl.setApplication(new Application(new File(TEMPDIR), "filename"));
    final ModelAndView modelAndView = ctrl.showForm(request, response, null);
    assertNotNull(modelAndView);
    assertEquals("config", modelAndView.getViewName());
  }

  public void testShowFormConfigured() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    final Application application = new Application(new File(TEMPDIR), "filename");
    application.setConfigured(true);
    ctrl.setApplication(application);
    final ModelAndView modelAndView = ctrl.showForm(request, response, null);
    assertNotNull(modelAndView);
    assertEquals(null, modelAndView.getViewName());
  }

  //Test what happens if an instance is partially configured and the config view is invoked
  //this could happen if one started to configure sventon and then called repobrowser.svn.
  public void testShowFormConfiguredII() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    final InstanceConfiguration instanceConfig = new InstanceConfiguration("test");

    final Application application = new Application(new File(TEMPDIR), "filename");
    application.setConfigured(false);
    application.addInstance("firstinstance", instanceConfig);
    ctrl.setApplication(application);
    final ModelAndView modelAndView = ctrl.showForm(request, response, null);
    assertNotNull(modelAndView);
    assertEquals("confirmAddConfig", modelAndView.getViewName());
  }

  public void testProcessFormSubmissionConfigured() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    final Application application = new Application(new File(TEMPDIR), "filename");
    application.setConfigured(true);
    ctrl.setApplication(application);
    final ModelAndView modelAndView = ctrl.processFormSubmission(request, response, null, null);
    assertNotNull(modelAndView);
    assertTrue(modelAndView.getView() instanceof RedirectView);
    RedirectView rv = (RedirectView) modelAndView.getView();
    assertEquals("repobrowser.svn", rv.getUrl());
  }

  public void testProcessFormSubmissionNonConfiguredValidationError() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    ctrl.setApplication(new Application(new File(TEMPDIR), "filename"));
    final ConfigCommand command = new ConfigCommand();
    final BindException exception = new BindException(command, "test");
    exception.addError(new ObjectError("test", new String[]{}, new Object[]{}, "test message"));
    final ModelAndView modelAndView = ctrl.processFormSubmission(request, response, command, exception);
    assertNotNull(modelAndView);
    assertEquals("config", modelAndView.getViewName());
  }

  public void testProcessFormSubmissionNonConfigured() throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    final Application application = new Application(new File(TEMPDIR), "filename");
    assertEquals(0, application.getInstanceCount());
    assertFalse(application.isConfigured());
    ctrl.setApplication(application);
    final ConfigCommand command = new ConfigCommand();
    command.setName("testrepos");
    command.setRepositoryUrl("http://localhost");
    final BindException exception = new BindException(command, "test");
    final ModelAndView modelAndView = ctrl.processFormSubmission(request, response, command, exception);
    assertNotNull(modelAndView);
    assertEquals("confirmAddConfig", modelAndView.getViewName());
    assertEquals(1, application.getInstanceCount());
    assertFalse(application.isConfigured());
    final Map model = modelAndView.getModel();
    assertEquals("testrepos", ((Set) model.get("addedInstances")).iterator().next());
  }

  public void testProcessFormSubmissionNonConfiguredUserBasedAccessControl() throws Exception {
    final String instanceName = "testrepos";

    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final ConfigurationController ctrl = new ConfigurationController();
    final Application application = new Application(new File(TEMPDIR), "filename");
    assertEquals(0, application.getInstanceCount());
    assertFalse(application.isConfigured());
    ctrl.setApplication(application);
    final ConfigCommand command = new ConfigCommand();
    command.setName(instanceName);
    command.setRepositoryUrl("http://localhost");
    command.setEnableAccessControl(true);
    command.setZippedDownloadsAllowed(true);
    command.setConnectionTestUid("test uid");
    command.setConnectionTestPwd("test pwd");
    final BindException exception = new BindException(command, "test");
    final ModelAndView modelAndView = ctrl.processFormSubmission(request, response, command, exception);
    assertNotNull(modelAndView);
    assertEquals("confirmAddConfig", modelAndView.getViewName());
    assertEquals(1, application.getInstanceCount());
    assertFalse(application.isConfigured());
    final Map model = modelAndView.getModel();
    assertEquals(instanceName, ((Set) model.get("addedInstances")).iterator().next());

    //assert that the config was created correctly:
    final Instance instance = application.getInstance(instanceName);
    final InstanceConfiguration configuration = instance.getConfiguration();
    assertEquals("http://localhost", configuration.getRepositoryUrl());
    assertTrue(configuration.isAccessControlEnabled());
    assertTrue(configuration.isZippedDownloadsAllowed());
    assertNull(configuration.getUid()); //UID only for connection testing, not stored
    assertNull(configuration.getPwd()); //PWD only for connection testing, not stored

  }

}