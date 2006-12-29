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
package de.berlios.sventon.web.model;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;

/**
 * Keeps information about available charsets.
 *
 * @author jesper@users.berlios.de
 */
public class AvailableCharsets {

  /**
   * Cached available character sets.
   */
  private Set<String> charsets;

  /**
   * Constructor.
   */
  public AvailableCharsets() {
    this.charsets = new TreeSet<String>(Charset.availableCharsets().keySet());
  }

  /**
   * Gets the character sets available in the JVM.
   *
   * @return Available charsets.
   */
  public Set<String> getCharsets() {
    return charsets;
  }

  /**
   * Checks if given character set is supported by the JVM.
   *
   * @param charset Charset
   * @return <tt>true</tt> if character set is supported by the JVM.
   */
  public boolean isSupported(final String charset) {
    return charsets.contains(charset);
  }
}
