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
package de.berlios.sventon.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.internal.wc.SVNTranslator;

import java.util.Map;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;

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

  private final Map<String, byte[]> keywordsMap;

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * Constructs the instance and computes keyword variables.
   *
   * @param url The full url to the repository entry
   */
  @SuppressWarnings({"unchecked"})
  public KeywordHandler(final Map properties, final String url) {

    final String author = (String) properties.get(SVNProperty.LAST_AUTHOR);
    final String date = (String) properties.get(SVNProperty.COMMITTED_DATE);
    final String revision = (String) properties.get(SVNProperty.COMMITTED_REVISION);
    final String keywords = (String) properties.get(SVNProperty.KEYWORDS);
    keywordsMap = SVNTranslator.computeKeywords(keywords, url, author, date, revision);
  }

  /**
   * Substitutes keywords in content.
   *
   * @param content  The content
   * @param encoding Encoding to use.
   * @return Content with substituted keywords.
   */
  public String substitute(final String content, final String encoding) throws UnsupportedEncodingException {
    logger.debug("Substituting keywords");
    String substitutedContent = content;
    for (String keyword : keywordsMap.keySet()) {
      final String value = new String(keywordsMap.get(keyword), encoding);
      logger.debug(keyword + "=" + value);
      final Pattern keywordPattern = Pattern.compile("\\$" + keyword + "\\$");
      substitutedContent = keywordPattern.matcher(substitutedContent).replaceAll("\\$"
          + keyword + ": " + value + " \\$");
    }
    return substitutedContent;
  }

}
