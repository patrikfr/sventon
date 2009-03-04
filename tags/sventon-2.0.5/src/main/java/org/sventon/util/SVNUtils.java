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
package org.sventon.util;

import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * Misc SVNKit utilities.
 *
 * @author jesper@sventon.org
 */
public final class SVNUtils {

  /**
   * Private.
   */
  private SVNUtils() {
  }

  /**
   * Checks if given log entry contains accessible information, i.e. it was
   * fetched from the repository by a user with access to the affected paths.
   *
   * @param logEntry Log entry.
   * @return True if accessible, false if not.
   */
  public static boolean isAccessible(final SVNLogEntry logEntry) {
    return logEntry != null
        && logEntry.getDate() != null
        && (logEntry.getChangedPaths() != null
        && !logEntry.getChangedPaths().isEmpty());
  }
}
