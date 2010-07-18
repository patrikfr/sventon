/*
 * ====================================================================
 * Copyright (c) 2005-2010 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon;

import org.apache.commons.lang.Validate;
import org.sventon.appl.ConfigDirectory;
import org.sventon.model.Credentials;
import org.sventon.model.RepositoryName;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;

/**
 * Factory class responsible for creating subversion repository connections.
 *
 * @author jesper@sventon.org
 */
public class RepositoryConnectionFactoryImpl implements RepositoryConnectionFactory {

  /**
   * Root directory where to place the svn config files.
   */
  private final ConfigDirectory configurationDirectory;

  /**
   * Constructor.
   *
   * @param configDirectory Root directory where to place the svn config files.
   *                        If the directory does not exist, it will be created.
   */
  public RepositoryConnectionFactoryImpl(final ConfigDirectory configDirectory) {
    Validate.notNull(configDirectory, "Configuration directory cannot be null!");
    this.configurationDirectory = configDirectory;
  }

  /**
   * Note: Be sure to call <code>repository.closeSession()</code> when connection is not needed anymore.
   */
  public SVNRepository createConnection(final RepositoryName repositoryName, final SVNURL svnUrl,
                                        final Credentials credentials) throws SVNException {

    if (svnUrl == null) {
      return null;
    }

    final SVNRepository repository = SVNRepositoryFactory.create(svnUrl);
    final File configDirectory = new File(configurationDirectory.getRepositoriesDirectory(), repositoryName.toString());
    repository.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(
        configDirectory, credentials.getUserName(), credentials.getPassword(), false));
    repository.setTunnelProvider(SVNWCUtil.createDefaultOptions(true));
    return repository;
  }
}
