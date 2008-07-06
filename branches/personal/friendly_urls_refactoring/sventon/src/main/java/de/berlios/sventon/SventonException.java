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
package de.berlios.sventon;

/**
 * SventonException.
 *
 * @author jesper@users.berlios.de
 */
public class SventonException extends Exception {

  /**
   * Constructor.
   *
   * @param message Exception message.
   */
  public SventonException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   Cause.
   */
  public SventonException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
