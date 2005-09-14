package de.berlios.sventon.ctrl;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * SVNBaseCommand.
 * <p>
 * Command class used to bind and pass servlet parameter arguments in sventon.
 * <p>
 * A newly created instance is initialized to have path <code>/</code> and
 * revision <code>null</code>.
 * 
 * @author patrikfr@users.berlios.de
 */
public class SVNBaseCommand {

  /** The full path. */
  private String path = "/";

  /** The revision. */
  private String revision = null;

  /** Mount point. */
  private String mountPoint;

  public String getPath() {
    return path;
  }

  /**
   * Set path. <code>null</code> and <code>""</code> arguments will be
   * converted <code>/</code>
   * 
   * @param path The path to set.
   */
  public void setPath(final String path) {
    if (path == null || "".equals(path)) {
      this.path = "/";
    } else {
      this.path = path.trim();
    }
  }

  /**
   * @return Returns the revision.
   */
  public String getRevision() {
    return revision;
  }

  /**
   * Set revision. Any revision is legal here (but may be rejected by the
   * validator, {@link SVNBaseCommandValidator}).
   * <p>
   * All case variations of the logical name "HEAD" will be converted to HEAD,
   * all other revision arguments will be set as is.
   * 
   * @param revision The revision to set.
   */
  public void setRevision(final String revision) {
    if (revision != null && "HEAD".equalsIgnoreCase(revision)) {
      this.revision = "HEAD";
    } else {
      this.revision = revision;
    }
  }

  public void setMountPoint(final String mountPoint) {
    this.mountPoint = mountPoint;
  }

  public String getMountPoint(final boolean stripSplash) {
    if (stripSplash) {
      return StringUtils.removeStart(mountPoint, "/");
    } else {
      return mountPoint;
    }
  }

  /**
   * @return Returns the path including the mount point offset, if any.
   */
  public String getCompletePath() {
    if (mountPoint != null) {
      return mountPoint + path;
    } else {
      return path;
    }
  }

  /**
   * Get target (leaf/end) part of the <code>path</code>, it could be a file
   * or a directory.
   * <p>
   * The returned string will have no final "/", even if it is a directory.
   * 
   * @return Target part of the path.
   */
  public String getTarget() {

    String[] splittedString = getPath().split("/");
    int length = splittedString.length;
    if (length == 0) {
      return "";
    } else {
      return splittedString[splittedString.length - 1];
    }

  }

  /**
   * Get path, excluding the end/leaf. For complete path including target,see
   * {@link SVNBaseCommand#getCompletePath()}. Mountpoint offset will be
   * included.
   * <p>
   * The returned string will have a final "/", if the path info is empty, ""
   * (empty string) will be returned.
   * 
   * @return Path excluding taget (end/leaf)
   */
  public String getPathPart() {
    String work = getPath();
    if (work.endsWith("/")) {
      work = work.substring(0, work.length() - 1);
    }

    int lastIndex = work.lastIndexOf('/');
    if (lastIndex == -1) {
      return "";
    } else {
      return work.substring(0, lastIndex) + "/";
    }
  }

  /**
   * Return the contents of this object as a map model.
   * <p>
   * Model data keys:
   * <ul>
   * <li><code>completePath</code></li>
   * <li><code>revision</code></li>
   * <li><code>path</code></li>
   * </ul>
   * 
   * @return The model map.
   */
  public Map<String, Object> asModel() {
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("completePath", getCompletePath());
    m.put("revision", getRevision());
    m.put("path", getPath());
    return m;
  }

  /**
   * Gets the file extension.
   * 
   * @return The file extension if any. Empty string otherwise.
   */
  public String getFileExtension() {
    String fileExtension = "";
    if (getTarget().lastIndexOf(".") > -1) {
      fileExtension = getTarget().substring(getTarget().lastIndexOf(".") + 1);
    }
    return fileExtension;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return "SVNBaseCommand{path='" + path + "', " + 
    "completePah='" + getCompletePath() + "', " + 
    "revision='" + revision + "', " +
    "mountPoint='" + mountPoint 
        + "'}";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj instanceof SVNBaseCommand) {
      SVNBaseCommand o = (SVNBaseCommand) obj;
      return (StringUtils.equals(o.path, path) && StringUtils.equals(o.revision, revision));
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    // TODO: Impelement!
    return super.hashCode();
  }

}
