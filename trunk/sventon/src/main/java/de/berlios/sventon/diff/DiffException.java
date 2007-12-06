/*
 * ====================================================================
 * Copyright (c) 2005-2007 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.diff;

/**
 * Exception thrown if diff exceptions occurs.
 *
 * @author jesper@users.berlios.de
 */
public class DiffException extends Exception {

  /**
   * Constructor.
   *
   * @param message Exception message.
   */
  public DiffException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   Cause.
   */
  public DiffException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
