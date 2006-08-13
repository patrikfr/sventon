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
package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.config.ApplicationConfiguration;
import de.berlios.sventon.config.InstanceConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Controller for persisting application configuration.
 * Called after one or more instances have been submitted to {@link ConfigurationController}.
 *
 * @author jesper@users.berlios.de
 */
public class ConfigurationSubmissionController extends AbstractController {

  /**
   * Application configuration.
   */
  private ApplicationConfiguration configuration;

  /**
   * The scheduler instance. Used to fire cache update job.
   */
  private Scheduler scheduler;

  /**
   * Logger for this class and subclasses.
   */
  private final Log logger = LogFactory.getLog(getClass());

  public static final String SVENTON_PROPERTIES = "sventon.properties";
  public static final String PROPERTY_KEY_REPOSITORY_URL = ".root";
  public static final String PROPERTY_KEY_USERNAME = ".uid";
  public static final String PROPERTY_KEY_PASSWORD = ".pwd";
  public static final String PROPERTY_KEY_USE_CACHE = ".useCache";
  public static final String PROPERTY_KEY_ALLOW_ZIP_DOWNLOADS = ".allowZipDownloads";

  /**
   * Sets application configuration.
   *
   * @param configuration ApplicationConfiguration
   */
  public void setConfiguration(final ApplicationConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Sets scheduler instance.
   * The scheduler is used to fire cache update job after configuration has been done.
   *
   * @param scheduler The scheduler
   */
  public void setScheduler(final Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
      throws Exception {

    if (configuration.isConfigured()) {
      throw new IllegalStateException("sventon is already configured!");
    }

    if (configuration.getInstanceCount() == 0) {
      throw new IllegalStateException("No instance has been configured!");
    }

    final String classPath = getServletContext().getRealPath("/WEB-INF/classes");
    final File propertyFile = new File(classPath + System.getProperty("file.separator") + SVENTON_PROPERTIES);
    logger.debug("Storing configuration properties in: " + propertyFile.getAbsolutePath());

    final FileOutputStream fileOutputStream = new FileOutputStream(propertyFile);

    for (final Properties properties : getConfigurationAsProperties(configuration)) {
      logger.debug("Storing: " + properties);
      properties.store(fileOutputStream, null);
      fileOutputStream.flush();
    }
    fileOutputStream.close();

    configuration.setConfigured(true);

    try {
      logger.debug("Starting up caches");
      scheduler.triggerJob("cacheUpdateJobDetail", Scheduler.DEFAULT_GROUP);
    } catch (SchedulerException sx) {
      logger.warn(sx);
    }

    final ModelAndView modelAndView;
    if (configuration.getInstanceCount() > 1) {
      modelAndView = new ModelAndView(new RedirectView("listinstances.svn"));
    } else {
      final String instanceName = configuration.getInstanceNames().iterator().next();
      modelAndView = new ModelAndView(new RedirectView("repobrowser.svn?name=" + instanceName));
    }
    return modelAndView;
  }

  /**
   * Creates and populates a List of <code>Properties</code> instances with relevant configuration values
   * extracted from given <code>ApplicationConfiguration</code>.
   *
   * @param configuration The application configuration
   * @return List of populated Properties.
   */
  protected List<Properties> getConfigurationAsProperties(final ApplicationConfiguration configuration) {
    final List<Properties> propertyList = new ArrayList<Properties>();
    final Set<String> instanceNames = configuration.getInstanceNames();

    for (final String instanceName : instanceNames) {
      final Properties properties = new Properties();
      final InstanceConfiguration config = configuration.getInstanceConfiguration(instanceName);
      properties.put(instanceName + PROPERTY_KEY_REPOSITORY_URL, config.getUrl());
      properties.put(instanceName + PROPERTY_KEY_USERNAME, config.getConfiguredUID());
      properties.put(instanceName + PROPERTY_KEY_PASSWORD, config.getConfiguredPWD());
      properties.put(instanceName + PROPERTY_KEY_USE_CACHE, config.isCacheUsed() ? "true" : "false");
      properties.put(instanceName + PROPERTY_KEY_ALLOW_ZIP_DOWNLOADS, config.isZippedDownloadsAllowed() ? "true" : "false");
      propertyList.add(properties);
    }
    return propertyList;
  }

}
