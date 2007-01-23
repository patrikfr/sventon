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

import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.List;

/**
 * A RevisionUpdate object will be created when a repository change has been detected.
 * The object will be communicated to all {@link RevisionObserver}s and contains the
 * new revisions including repository instance information.
 *
 * @author jesper@users.berlios.de
 */
public class RevisionUpdate {

  private final String instanceName;
  private final List<SVNLogEntry> logEntries;
  private boolean flushAfterUpdate;

  /**
   * Constructor.
   *
   * @param instanceName     Instance name
   * @param logEntries       The new log entries
   * @param flushAfterUpdate If <tt>true</tt>, caches will be flushed after update.
   */
  public RevisionUpdate(final String instanceName, final List<SVNLogEntry> logEntries, final boolean flushAfterUpdate) {
    this.instanceName = instanceName;
    this.logEntries = logEntries;
    this.flushAfterUpdate = flushAfterUpdate;
  }

  /**
   * Gets the updates log entries, one per revision.
   *
   * @return The log entries.
   */
  public List<SVNLogEntry> getRevisions() {
    return logEntries;
  }

  /**
   * Gets the instance name.
   *
   * @return The instance name
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * Gets the flush-after-update status.
   *
   * @return If <tt>true</tt>, caches must be flushed after update.
   */
  public boolean isFlushAfterUpdate() {
    return flushAfterUpdate;
  }

}
