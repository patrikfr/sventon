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
package de.berlios.sventon.repository.cache;

import de.berlios.sventon.SventonException;

/**
 * Exception thrown by <code>Cache</code>.
 *
 * @author jesper@users.berlios.de
 */
public class CacheException extends SventonException {

  /**
   * Constructor.
   *
   * @param message Exception message.
   */
  public CacheException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   Cause
   */
  public CacheException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
