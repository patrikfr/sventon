/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.web.command;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ConfigLoginCommand.
 * <p/>
 * Command class used to bind and pass servlet parameter arguments for sventon configuration.
 *
 * @author jesper@sventon.org
 */
public final class ConfigLoginCommand {

  /**
   * Password needed to access the configuration pages.
   */
  private String pwd;

  /**
   * Gets the password.
   *
   * @return Password.
   */
  public String getPwd() {
    return pwd;
  }

  /**
   * Sets the password.
   *
   * @param pwd Password.
   */
  public void setPwd(final String pwd) {
    this.pwd = pwd;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}