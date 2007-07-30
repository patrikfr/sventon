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
package de.berlios.sventon.rss;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedOutput;
import de.berlios.sventon.web.model.LogEntryActionType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Class to generate <code>RSS</code> feeds from Subversion log information.
 * Uses the <a href="https://rome.dev.java.net/">ROME</code> library.
 *
 * @author jesper@users.berlios.de
 */
public final class SyndFeedGenerator implements FeedGenerator {

  /**
   * The generated feed type, default set to <tt>rss_2.0</tt>.
   */
  private String feedType = "rss_2.0";

  /**
   * Number of characters in the abbreviated log message, default set to 40.
   */
  private int logMessageLength = 40;

  /**
   * Logging instance.
   */
  protected final Log logger = LogFactory.getLog(getClass());

  public static final String LOG_MESSAGE_KEY = "@LOG_MESSAGE@";
  public static final String ADDED_COUNT_KEY = "@ADDED_COUNT@";
  public static final String MODIFIED_COUNT_KEY = "@MODIFIED_COUNT@";
  public static final String REPLACED_COUNT_KEY = "@REPLACED_COUNT@";
  public static final String DELETED_COUNT_KEY = "@DELETED_COUNT@";
  public static final String CHANGED_PATHS_KEY = "@CHANGED_PATHS@";

  /**
   * Cached rss HTML body template.
   */
  private String bodyTemplate = null;

  /**
   * The rss body template file. Default set to <tt>rsstemplate.html</tt> in classpath root.
   */
  private String bodyTemplateFile = "/rsstemplate.html";

  /**
   * {@inheritDoc}
   */
  public void outputFeed(final String instanceName, final List<SVNLogEntry> logEntries, final String baseURL,
                         final Writer writer) throws Exception {

    final SyndFeed feed = new SyndFeedImpl();
    feed.setTitle("sventon feed - " + baseURL);
    feed.setLink(baseURL);
    feed.setDescription("sventon feed - " + logEntries.size() + " latest repository changes");
    feed.setEntries(createEntries(instanceName, logEntries, baseURL));
    feed.setFeedType(feedType);
    new SyndFeedOutput().output(feed, writer);
  }

  private List<SyndEntry> createEntries(final String instanceName, final List<SVNLogEntry> logEntries,
                                        final String baseURL) throws IOException {

    final List<SyndEntry> entries = new ArrayList<SyndEntry>();

    SyndEntry entry;
    SyndContent description;

    logger.debug("Generating [" + logEntries.size() + "] RSS feed items for instance [" + instanceName + "]");

    // One logEntry is one commit (or revision)
    for (final SVNLogEntry logEntry : logEntries) {
      entry = new SyndEntryImpl();
      entry.setTitle("Revision " + logEntry.getRevision() + " - "
          + getAbbreviatedLogMessage(logEntry.getMessage(), logMessageLength));
      entry.setAuthor(logEntry.getAuthor());
      entry.setLink(baseURL + "revinfo.svn?name=" + instanceName + "&revision=" + logEntry.getRevision());
      entry.setPublishedDate(logEntry.getDate());

      description = new SyndContentImpl();
      description.setType("text/html");
      description.setValue(createItemBody(getBodyTemplate(), logEntry));
      entry.setDescription(description);
      entries.add(entry);
    }
    return entries;
  }

  protected String createItemBody(final String bodyTemplate, final SVNLogEntry logEntry) {

    //noinspection unchecked
    final Map<String, SVNLogEntryPath> latestChangedPaths = logEntry.getChangedPaths();
    final List<String> latestPathsList = new ArrayList<String>(latestChangedPaths.keySet());

    String itemBody = bodyTemplate;

    int added = 0;
    int modified = 0;
    int replaced = 0;
    int deleted = 0;

    for (final String entryPath : latestPathsList) {
      final LogEntryActionType type = LogEntryActionType.parse(latestChangedPaths.get(entryPath).getType());
      switch (type) {
        case ADDED:
          added++;
          break;
        case MODIFIED:
          modified++;
          break;
        case REPLACED:
          replaced++;
          break;
        case DELETED:
          deleted++;
          break;
      }
    }

    itemBody = itemBody.replaceAll(LOG_MESSAGE_KEY, Matcher.quoteReplacement(logEntry.getMessage()));
    itemBody = itemBody.replaceAll(ADDED_COUNT_KEY, Matcher.quoteReplacement(String.valueOf(added)));
    itemBody = itemBody.replaceAll(MODIFIED_COUNT_KEY, Matcher.quoteReplacement(String.valueOf(modified)));
    itemBody = itemBody.replaceAll(REPLACED_COUNT_KEY, Matcher.quoteReplacement(String.valueOf(replaced)));
    itemBody = itemBody.replaceAll(DELETED_COUNT_KEY, Matcher.quoteReplacement(String.valueOf(deleted)));
    itemBody = itemBody.replaceAll(CHANGED_PATHS_KEY, Matcher.quoteReplacement(createChangedPathsTable(logEntry)));
    return itemBody;
  }

  protected String createChangedPathsTable(final SVNLogEntry logEntry) {
    final StringBuilder sb = new StringBuilder("<table border=\"0\">\n");
    sb.append("  <tr>\n");
    sb.append("    <th>Action</th>\n");
    sb.append("    <th>Path</th>\n");
    sb.append("  </tr>\n");

    //noinspection unchecked
    final Map<String, SVNLogEntryPath> latestChangedPaths = logEntry.getChangedPaths();
    final List<String> latestPathsList = new ArrayList<String>(latestChangedPaths.keySet());
    Collections.sort(latestPathsList);

    for (final String path : latestPathsList) {
      final SVNLogEntryPath logEntryPath = latestChangedPaths.get(path);
      final LogEntryActionType actionType = LogEntryActionType.parse(logEntryPath.getType());

      sb.append("  <tr>\n");
      sb.append("    <td valign=\"top\"><i>");
      sb.append(actionType);
      sb.append("</i></td>\n");
      sb.append("    <td>");
      sb.append(logEntryPath.getPath());
      sb.append("</td>\n");
      sb.append("  </tr>\n");
    }
    sb.append("</table>");
    return sb.toString();
  }

  /**
   * Gets the rss item HTML body template.
   *
   * @return The template.
   * @throws IOException if unable to load template.
   */
  protected String getBodyTemplate() throws IOException {
    if (bodyTemplate == null) {
      final StringBuilder sb = new StringBuilder();
      final InputStream is = this.getClass().getResourceAsStream(bodyTemplateFile);
      if (is == null) {
        throw new FileNotFoundException("Unable to find: " + bodyTemplateFile);
      }
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
      } finally {
        if (reader != null) {
          reader.close();
        }
      }
      bodyTemplate = sb.toString();
    }
    return bodyTemplate;
  }

  /**
   * Gets the abbreviated version of given log message.
   *
   * @param message The original log message
   * @param length  Length, shortened string length
   * @return The abbreviated log message
   */
  protected String getAbbreviatedLogMessage(final String message, final int length) {
    if (message != null && message.length() <= length) {
      return message;
    } else {
      return StringUtils.abbreviate(message, length);
    }

  }

  /**
   * Sets the file that should be used as the rss item body template.
   *
   * @param bodyTemplateFile Template file.
   */
  public void setBodyTemplateFile(final String bodyTemplateFile) {
    this.bodyTemplateFile = bodyTemplateFile;
  }

  /**
   * Sets the feed type. For available types check ROME documentation.
   *
   * @param feedType The feed type.
   * @link https://rome.dev.java.net/
   */
  public void setFeedType(final String feedType) {
    this.feedType = feedType;
  }

  /**
   * Sets the length of the log message to be displayed.
   *
   * @param length The length.
   */
  public void setLogMessageLength(final int length) {
    this.logMessageLength = length;
  }
}
