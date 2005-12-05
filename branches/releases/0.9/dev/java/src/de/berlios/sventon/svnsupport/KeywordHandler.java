/*
 * ====================================================================
 * Copyright (c) 2005 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.svnsupport;

import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNFormatUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNTimeUtil;
import org.tmatesoft.svn.core.SVNProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Handler class for subversion keyword substitution.
 * <p/>
 * Valid keywords are:
 * <table>
 * <tr><td>Date</td></tr>
 * <tr><td>LastChangedDate</td></tr>
 * <tr><td>Rev</td></tr>
 * <tr><td>Revision</td></tr>
 * <tr><td>LastChangedRevision</td></tr>
 * <tr><td>Author</td></tr>
 * <tr><td>LastChangedBy</td></tr>
 * <tr><td>URL</td></tr>
 * <tr><td>HeadURL</td></tr>
 * <tr><td>Id</td></tr>
 * </table>
 *
 * @author jesper@users.berlios.de
 */
public final class KeywordHandler {

  public static final String KEYWORD_LAST_CHANGED_DATE = "LastChangedDate";
  public static final String KEYWORD_DATE = "Date";
  public static final String KEYWORD_LAST_CHANGED_REVISION = "LastChangedRevision";
  public static final String KEYWORD_REVISION = "Revision";
  public static final String KEYWORD_REV = "Rev";
  public static final String KEYWORD_LAST_CHANGED_BY = "LastChangedBy";
  public static final String KEYWORD_AUTHOR = "Author";
  public static final String KEYWORD_HEAD_URL = "HeadURL";
  public static final String KEYWORD_URL = "URL";
  public static final String KEYWORD_ID = "Id";

  private final Map<String, String> keywordsMap;

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * Constructs the instance and computes keyword variables.
   * This method is based on the method <tt>computeKeywords</tt> in
   * <code>org.tmatesoft.svn.core.internal.wc.SVNTranslator</code>.
   *
   * @param url      The full url to the repository entry
   */
  @SuppressWarnings({"unchecked"})
  public KeywordHandler(final Map properties, final String url) {

    String author = (String) properties.get(SVNProperty.LAST_AUTHOR);
    String date = (String) properties.get(SVNProperty.COMMITTED_DATE);
    String revision = (String) properties.get(SVNProperty.COMMITTED_REVISION);
    String keywords = (String) properties.get(SVNProperty.KEYWORDS);

    if (keywords == null) {
      keywordsMap = Collections.EMPTY_MAP;
      return;
    }

    boolean expand = url != null;
    String computedDate = null;
    String computedUrl = null;
    String computedRevision = null;
    String computedAuthor = null;
    String computedName = null;
    String computedId = null;

    Date jDate = date == null ? null : SVNTimeUtil.parseDate(date);

    keywordsMap = new HashMap<String, String>();
    for (StringTokenizer tokens = new StringTokenizer(keywords,
        " \t\n\b\r\f"); tokens.hasMoreTokens();) {
      String token = tokens.nextToken();
      if (KEYWORD_LAST_CHANGED_DATE.equals(token) || KEYWORD_DATE.equals(token)) {
        computedDate = expand && computedDate == null ? SVNFormatUtil.formatDate(jDate, false) : computedDate;
        keywordsMap.put(KEYWORD_LAST_CHANGED_DATE, computedDate);
        keywordsMap.put(KEYWORD_DATE, computedDate);
      } else if (KEYWORD_LAST_CHANGED_REVISION.equals(token)
          || KEYWORD_REVISION.equals(token) || KEYWORD_REV.equals(token)) {
        computedRevision = expand && computedRevision == null ? revision : computedRevision;
        keywordsMap.put(KEYWORD_LAST_CHANGED_REVISION, computedRevision);
        keywordsMap.put(KEYWORD_REVISION, computedRevision);
        keywordsMap.put(KEYWORD_REV, computedRevision);
      } else if (KEYWORD_LAST_CHANGED_BY.equals(token)
          || KEYWORD_AUTHOR.equals(token)) {
        computedAuthor = expand && computedAuthor == null ? (author == null ? "" : author) : computedAuthor;
        keywordsMap.put(KEYWORD_LAST_CHANGED_BY, computedAuthor);
        keywordsMap.put(KEYWORD_AUTHOR, computedAuthor);
      } else if (KEYWORD_HEAD_URL.equals(token) || KEYWORD_URL.equals(token)) {
        computedUrl = expand && computedUrl == null ? SVNEncodingUtil.uriDecode(url) : computedUrl;
        keywordsMap.put(KEYWORD_HEAD_URL, computedUrl);
        keywordsMap.put(KEYWORD_URL, computedUrl);
      } else if (KEYWORD_ID.equals(token)) {
        if (expand && computedId == null) {
          computedRevision = computedRevision == null ? revision : computedRevision;
          computedDate = computedDate == null ? SVNFormatUtil.formatDate(jDate, false) : computedDate;
          computedName = computedName == null ? SVNEncodingUtil.uriDecode(SVNPathUtil.tail(url)) : computedName;
          computedAuthor = computedAuthor == null ? (author == null ? "" : author) : computedAuthor;
          StringBuffer sb = new StringBuffer();
          sb.append(computedName);
          sb.append(' ');
          sb.append(computedRevision);
          sb.append(' ');
          sb.append(computedDate);
          sb.append(' ');
          sb.append(computedAuthor);
          computedId = sb.toString();
        }
        keywordsMap.put(KEYWORD_ID, expand ? computedId : null);
      }
    }
  }

  /**
   * Substitutes keywords in content.
   *
   * @param content     The content
   * @return Content with substituted keywords.
   */
  public String substitute(String content) {
    logger.debug("Substituting keywords: " + keywordsMap);
    for (Object o : keywordsMap.keySet()) {
      String keyword = (String) o;
      Pattern keywordPattern = Pattern.compile("\\$" + keyword + "\\$");
      String expandedKeyword = (String) keywordsMap.get(keyword);
      content = keywordPattern.matcher(content).replaceAll("\\$" + keyword + ": " + expandedKeyword + " \\$");
    }
    return content;
  }

}
