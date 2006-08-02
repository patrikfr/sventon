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
package de.berlios.sventon.config;

import de.berlios.sventon.logging.SVNLog4JAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.util.SVNDebugLog;

import java.io.File;

/**
 * Sventon application configuration class holding configuration parameters
 * and repository connection information.
 * <p/>
 * The class also performs JavaSVN initialization, such as setting up logging
 * and repository access. It should be instanciated once (and only once), when
 * the application starts.
 * <p/>
 * This class is preferably configured using Spring.
 *
 * @author patrikfr@user.berlios.de
 * @see <a href="http://tmate.org/svn">TMate JavaSVN</a>
 * @see <a href="http://www.springframework">Spring framework</a>
 */
public class ApplicationConfiguration {

  /**
   * The logging instance.
   */
  protected final Log logger = LogFactory.getLog(getClass());

  /**
   * Will be <code>true</code> if all parameters are ok.
   */
  private boolean configured;

  /**
   * The url.
   */
  private String repositoryURL;

  /**
   * The repository location.
   */
  private SVNURL svnURL;

  /**
   * Path to the Subversion configuration libraries
   */
  private String SVNConfigurationPath;

  /**
   * If a global user is configured for repository browsing, this property
   * should be set.
   */
  private String configuredUID;

  /**
   * If a global user is configured for repository browsing, this property
   * should be set.
   */
  private String configuredPWD;

  /**
   * Decides whether the caching feature will be used.
   */
  private Boolean useCache;

  /**
   * Decides whether <i>download as zip</i> is allowed.
   */
  private boolean zipDownloadsAllowed;

  /**
   * Configures and initializes the repository.
   */
  public ApplicationConfiguration() {
    configureLogging();
    logger.info("Configuring SVN Repository");
    SVNRepositoryFactoryImpl.setup();
    DAVRepositoryFactory.setup();
    FSRepositoryFactory.setup();
  }

  /**
   * Sets the repository root. Root URL will never end with a slash.
   *
   * @param repositoryRoot The root url. <code>Null</code> or
   *                       empty value will be ignored, as it indicates sventon has
   *                       not been configured yet. A well formed URL is needed for
   *                       the method {@link #isConfigured()} to return <code>true</code>.
   */
  public void setRepositoryRoot(final String repositoryRoot) {

    if (StringUtils.isEmpty(repositoryRoot)) {
      logger.debug("Ignoring empty repository root url");
      return;
    }

    logger.debug("Repository URL: " + repositoryURL);

    // Strip last slash if any.
    this.repositoryURL = repositoryRoot;
    if (repositoryRoot.endsWith("/")) {
      logger.debug("Removing trailing slash from url");
      this.repositoryURL = repositoryRoot.substring(0,
          repositoryRoot.length() - 1);
    }

    try {
      svnURL = SVNURL.parseURIDecoded(repositoryURL);
      configured = true;
    } catch (SVNException ex) {
      logger.warn("Unable to parse URL [" + repositoryRoot + "]");
    }
    logger.debug("sventon is configured: " + configured);
  }

  /**
   * Get configured Password, if any.
   *
   * @return Returns the configuredPWD.
   */
  public String getConfiguredPWD() {
    return configuredPWD;
  }

  /**
   * Set a configured password. This password will be used for repository
   * access, together with configured user ID, {@see #setConfiguredUID(String)}
   *
   * @param configuredPWD The configuredPWD to set, may be <code>null</code>.
   */
  public void setConfiguredPWD(final String configuredPWD) {
    this.configuredPWD = configuredPWD;
  }

  /**
   * Get configured user ID, if any.
   *
   * @return Returns the configuredUID.
   */
  public String getConfiguredUID() {
    return configuredUID;
  }

  /**
   * Set a configured user ID. This user ID will be used for repository access,
   * together with configured password, {@see #setConfiguredPWD(String)}
   *
   * @param configuredUID The configuredUID to set, may be <code>null</code>
   */
  public void setConfiguredUID(final String configuredUID) {
    this.configuredUID = configuredUID;
  }

  /**
   * Set SVN configuration path, this is a directory where Subversion
   * configuration is stored. The user running the servlet container running
   * sventon needs read and write access to this directory.
   *
   * @return SVN configuration path.
   */
  public String getSVNConfigurationPath() {
    return SVNConfigurationPath;
  }

  /**
   * Get SVN configuration path.
   *
   * @param configurationPath ApplicationConfiguration path.
   * @throws IllegalArgumentException if argument is not a directory.
   *                                  <code>Null</code> or empty will be ignored.
   */
  public void setSVNConfigurationPath(final String configurationPath) {
    if (StringUtils.isEmpty(configurationPath)) {
      return;
    }

    if (!new File(configurationPath).isDirectory()) {
      throw new IllegalArgumentException("Given path, [" + configurationPath + "] is not a directory");
    }
    SVNConfigurationPath = configurationPath;
  }

  /**
   * Get the configured repository URL.
   *
   * @return Returns the repository url.
   */
  public String getUrl() {
    return repositoryURL;
  }

  /**
   * Get the SVNURL, this is the typed version of the URL set using method
   * {@link #setRepositoryRoot(String)}
   *
   * @return Returns the location.
   */
  public SVNURL getSVNURL() {
    return svnURL;
  }

  private void configureLogging() {
    SVNDebugLog.setLogger(new SVNLog4JAdapter("sventon.javasvn"));
  }

  /**
   * Gets configuration status.
   *
   * @return True if repository is configured ok, false if not.
   */
  public boolean isConfigured() {
    return configured;
  }

  /**
   * Sets the 'useCache' flag.
   *
   * @param useCache <code>true</code> if cache should be enabled, <code>false</code> if not.
   */
  public void setCacheUsed(final boolean useCache) {
    this.useCache = useCache;
  }

  /**
   * Checks if the cache should be used.
   *
   * @return <code>true</code> if cache is enabled, <code>false</code> if not.
   */
  public boolean isCacheUsed() {
    return this.useCache;
  }

  /**
   * Sets the 'zipDownloadsAllowed' flag.
   *
   * @param zipDownloadsAllowed <code>true</code> if <i>download as zip</i> is allowed.
   */
  public void setZippedDownloadsAllowed(final boolean zipDownloadsAllowed) {
    this.zipDownloadsAllowed = zipDownloadsAllowed;
  }

  /**
   * Checks if <i>download as zip</i> is allowed.
   *
   * @return <code>true</code> if zipped downloads are allowed.
   */
  public boolean isZippedDownloadsAllowed() {
    return this.zipDownloadsAllowed;
  }

}
