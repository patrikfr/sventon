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
package de.berlios.sventon;

/**
 * SventonException.
 *
 * @author jesper@users.berlios.de
 */
public class SventonException extends Exception {

  /**
   * {@inheritDoc}
   */
  public SventonException(final String message) {
    super(message);
  }

  /**
   * {@inheritDoc}
   */
  public SventonException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * {@inheritDoc}
   */
  public SventonException(final Throwable cause) {
    super(cause);
  }
}
