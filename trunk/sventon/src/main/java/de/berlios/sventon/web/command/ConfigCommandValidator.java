/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.web.command;

import de.berlios.sventon.appl.RepositoryConfiguration;
import de.berlios.sventon.appl.RepositoryName;
import de.berlios.sventon.repository.RepositoryFactory;
import static de.berlios.sventon.web.command.ConfigCommand.AccessMethod.USER;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * ConfigCommandValidator.
 *
 * @author jesper@users.berlios.de
 * @author patrik@sventon.org
 */
public final class ConfigCommandValidator implements Validator {

  /**
   * Logger for this class.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * Controls whether repository connection should be tested or not.
   */
  private boolean testConnection = true;

  /**
   * The repository factory.
   */
  private RepositoryFactory repositoryFactory;

  /**
   * Constructor.
   */
  public ConfigCommandValidator() {
  }

  /**
   * Constructor for testing purposes.
   *
   * @param testConnection If <tt>false</tt> repository
   *                       connection will not be tested.
   */
  protected ConfigCommandValidator(final boolean testConnection) {
    this.testConnection = testConnection;
  }

  /**
   * {@inheritDoc}
   */
  public boolean supports(final Class clazz) {
    return clazz.equals(ConfigCommand.class);
  }

  /**
   * Sets the repository factory instance.
   *
   * @param repositoryFactory Factory.
   */
  public void setRepositoryFactory(final RepositoryFactory repositoryFactory) {
    this.repositoryFactory = repositoryFactory;
  }

  /**
   * {@inheritDoc}
   */
  public void validate(final Object obj, final Errors errors) {
    final ConfigCommand command = (ConfigCommand) obj;

    // Validate 'repository name'
    final String repositoryName = command.getName();
    if (repositoryName != null && !RepositoryName.isValid(repositoryName)) {
      errors.rejectValue("name", "config.error.illegal-name");
      return;
    }

    // Validate 'repositoryUrl', 'username' and 'password'
    final String repositoryUrl = command.getRepositoryUrl();

    if (repositoryUrl != null) {
      final String trimmedURL = repositoryUrl.trim();
      SVNURL url = null;
      try {
        url = SVNURL.parseURIDecoded(trimmedURL);
      } catch (SVNException ex) {
        errors.rejectValue("repositoryUrl", "config.error.illegal-url");
      }
      if (url != null && testConnection) {
        final RepositoryConfiguration configuration = new RepositoryConfiguration(repositoryName);
        configuration.setRepositoryUrl(trimmedURL);
        configuration.setUid(command.getAccessMethod() == USER
            ? command.getConnectionTestUid() : command.getUid());
        configuration.setPwd(command.getAccessMethod() == USER
            ? command.getConnectionTestPwd() : command.getPwd());

        SVNRepository repository = null;
        try {
          repository = repositoryFactory.getRepository(new RepositoryName(repositoryName), configuration.getSVNURL(),
              configuration.getUid(), configuration.getPwd());
          repository.testConnection();
        } catch (SVNAuthenticationException e) {
          logger.warn("Repository authentication failed");
          errors.rejectValue("accessMethod", "config.error.authentication-error");
        } catch (SVNException e) {
          logger.warn("Unable to connect to repository", e);
          errors.rejectValue("repositoryUrl", "config.error.connection-error", new String[]{trimmedURL},
              "Unable to connect to repository [" + trimmedURL + "]. Check URL.");
        } finally {
          if (repository != null) {
            repository.closeSession();
          }
        }
      }
    }
  }

}
