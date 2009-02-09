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
package org.sventon.web.command;

import org.apache.commons.lang.Validate;
import org.sventon.model.DiffStyle;
import org.sventon.util.PathUtil;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.Arrays;
import java.util.Comparator;

/**
 * DiffCommand.
 * <p/>
 * Command class used to parse and bundle diffing from/to details.
 * <p/>
 * A diff can be made between two arbitrary entries or by a single entry and
 * it's previous revision. In the first case, the method
 * {@link #setEntries(org.tmatesoft.svn.core.io.SVNFileRevision[])} setEntries} will be used.
 * A diff with previous revision will use the main path set by calling {@link #setPath(String)} }
 *
 * @author jesper@sventon.org
 */
public final class DiffCommand extends BaseCommand {

  /**
   * From revision.
   */
  private SVNFileRevision fromFileRevision;

  /**
   * To revision.
   */
  private SVNFileRevision toFileRevision;

  /**
   * The requested diff style.
   */
  private DiffStyle style = DiffStyle.unspecified;

  /**
   * Sets the requested diff style.
   *
   * @param style Style
   */
  public void setStyle(final DiffStyle style) {
    Validate.notNull(style);
    this.style = style;
  }

  /**
   * Used when diffing two arbitrary entries.
   *
   * @param entries Array containing two <code>SVNFileRevision</code> objects.
   * @throws IllegalArgumentException if given list does not contain two entries.
   */
  public void setEntries(final SVNFileRevision[] entries) {
    Validate.notNull(entries);

    if (entries.length < 2) {
      throw new IllegalArgumentException("The entry does not have a history.");
    }

    Arrays.sort(entries, new Comparator<SVNFileRevision>() {
      public int compare(SVNFileRevision o1, SVNFileRevision o2) {
        return (o2.getRevision() < o1.getRevision() ? -1 : (o2.getRevision() == o1.getRevision() ? 0 : 1));
      }
    });
    toFileRevision = entries[0];
    fromFileRevision = entries[1];
  }

  /**
   * @return True if entries has been set (using {@link #setEntries(org.tmatesoft.svn.core.io.SVNFileRevision[])}).
   */
  public boolean hasEntries() {
    return toFileRevision != null && fromFileRevision != null;
  }

  /**
   * Gets the requested diff style.
   *
   * @return Style
   */
  public DiffStyle getStyle() {
    return style;
  }

  /**
   * Gets the diff <i>from</i> path.
   *
   * @return The path.
   */
  public String getFromPath() {
    return fromFileRevision != null ? fromFileRevision.getPath() : "";
  }

  /**
   * Gets the from target.
   *
   * @return From target, i.e. file name without path.
   */
  public String getFromTarget() {
    return PathUtil.getTarget(getFromPath());
  }

  /**
   * Gets the diff <i>from</i> revision.
   *
   * @return The revision.
   */
  public SVNRevision getFromRevision() {
    return fromFileRevision != null ? SVNRevision.create(fromFileRevision.getRevision()) : SVNRevision.UNDEFINED;
  }

  /**
   * Gets the diff <i>to</i> path.
   *
   * @return The path.
   */
  public String getToPath() {
    return toFileRevision != null ? toFileRevision.getPath() : "";
  }

  /**
   * Gets the to target.
   *
   * @return To target, i.e. file name without path.
   */
  public String getToTarget() {
    return PathUtil.getTarget(getToPath());
  }

  /**
   * Gets the diff <i>to</i> revision.
   *
   * @return The revision.
   */
  public SVNRevision getToRevision() {
    return toFileRevision != null ? SVNRevision.create(toFileRevision.getRevision()) : SVNRevision.UNDEFINED;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DiffCommand {");
    if (hasEntries()) {
      sb.append("from: ");
      sb.append(getFromPath());
      sb.append("@");
      sb.append(getFromRevision());
      sb.append(", to: ");
      sb.append(getToPath());
      sb.append("@");
      sb.append(getToRevision());
    } else {
      sb.append("from: ");
      sb.append(getPath());
      sb.append("@");
      sb.append(getRevision());
      sb.append(", to: ");
      sb.append(getPath());
      sb.append("@");
      sb.append(getRevision());
      sb.append("-1");
    }
    sb.append(", style: ");
    sb.append(getStyle());
    sb.append("}");
    return sb.toString();
  }
}
