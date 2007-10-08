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
 * Exception thrown to indicate that a given file has an illegal file format.
 * Typically, this exception will be thrown if user tries to diff a binary file.
 *
 * @author jesper@users.berlios.de
 */
public final class IllegalFileFormatException extends DiffException {

  /**
   * Constructor.
   *
   * @param message Exception message text
   */
  public IllegalFileFormatException(final String message) {
    super(message);
  }

}
