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
package de.berlios.sventon.repository;

/**
 * CommitMessage.
 *
 * @author jesper@users.berlios.de
 */
public class CommitMessage {

  final private long revision;
  final private String message;

  /**
   * Constructor.
   *
   * @param revision The revision
   * @param message The commit message
   */
  public CommitMessage(final long revision, final String message) {
    this.revision = revision;
    this.message = message;
  }

  /**
   * Gets the revision for this commit message.
   *
   * @return The revision
   */
  public long getRevision() {
    return revision;
  }

  /**
   * Gets the commit message.
   *
   * @return The message.
   */
  public String getMessage() {
    return message;
  }

}
