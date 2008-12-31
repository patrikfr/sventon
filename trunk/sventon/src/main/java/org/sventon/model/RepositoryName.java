/*
 * ====================================================================
 * Copyright (c) 2005-2009 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * Represents a repository name.
 *
 * @author jesper@sventon.org
 */
public final class RepositoryName implements Serializable, Comparable {

  private static final long serialVersionUID = 5044457892450351810L;

  /**
   * Name of the repository.
   */
  private final String name;

  /**
   * Constructor.
   *
   * @param name Repository name.
   */
  public RepositoryName(final String name) {
    if (isValid(name)) {
      this.name = name;
    } else {
      throw new IllegalArgumentException("Illegal name: " + name);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  /**
   * Validates given repository name.
   *
   * @param name Repository name to validate.
   * @return True if valid, false if not.
   */
  public static boolean isValid(final String name) {
    return !(name == null || name.length() == 0) && !StringUtils.containsWhitespace(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(Object o) {
    return name.compareTo(o.toString());
  }
}
