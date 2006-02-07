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
package de.berlios.sventon.diff;

import de.berlios.sventon.svnsupport.KeywordHandler;
import de.berlios.sventon.svnsupport.LineNumberAppender;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * String differ.
 *
 * @author jesper@users.berlios.de
 */
public class Diff {

  private List<SourceLine> leftLinesList;
  private List<SourceLine> rightLinesList;

  public static final String ENCODING = "UTF-8";
  private String diffResultString = "";

  /**
   * Constructor.
   *
   * @param leftContent         Left (old/from) string content to diff.
   * @param leftKeywordHandler  Left file's keyword handler.
   * @param rightContent        Right (new/to) string content to diff.
   * @param rightKeywordHandler Right file's keyword handler.
   * @throws DiffException if IO error occurs.
   */
  public Diff(final String leftContent, final KeywordHandler leftKeywordHandler,
              final String rightContent, final KeywordHandler rightKeywordHandler)
      throws DiffException {

    String tempLine;
    BufferedReader reader;

    List<String> leftSourceLines = new ArrayList<String>();
    List<String> rightSourceLines = new ArrayList<String>();

    InputStream leftStream = new ByteArrayInputStream(leftContent.getBytes());
    InputStream rightStream = new ByteArrayInputStream(rightContent.getBytes());

    try {
      ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
      DiffProducer diffProducer = new DiffProducer(leftStream, rightStream, Diff.ENCODING);
      diffProducer.doNormalDiff(diffResult);
      diffResultString = diffResult.toString();

      if ("".equals(diffResultString)) {
        throw new DiffException("Files are identical.");
      }

      String leftString;
      String rightString;
      // Append keywords, if any.
      if (leftKeywordHandler != null) {
        leftString = leftKeywordHandler.substitute(leftContent);
      } else {
        leftString = leftContent;
      }
      if (rightKeywordHandler != null) {
        rightString = rightKeywordHandler.substitute(rightContent);
      } else {
        rightString = rightContent;
      }

      // Append line numbers
      LineNumberAppender appender = new LineNumberAppender();
      appender.setEmbedStart("<span class=\"sventonLineNo\">");
      appender.setEmbedEnd(":&nbsp;</span>");
      appender.setPadding(5);

      reader = new BufferedReader(new StringReader(appender.appendTo(leftString)));
      while ((tempLine = reader.readLine()) != null) {
        leftSourceLines.add(tempLine);
      }
      reader = new BufferedReader(new StringReader(appender.appendTo(rightString)));
      while ((tempLine = reader.readLine()) != null) {
        rightSourceLines.add(tempLine);
      }
    } catch (IOException ioex) {
      throw new DiffException("Unable to produce diff.");
    }

    List<DiffSegment> diffActions = DiffResultParser.parseNormalDiffResult(diffResultString);

    leftLinesList = processLeft(leftSourceLines, diffActions);
    rightLinesList = processRight(rightSourceLines, diffActions);

    if (leftLinesList.size() != rightLinesList.size()) {
      StringBuffer sb = new StringBuffer("Error while applying diff result!");
      sb.append("\nLine diff count: ");
      sb.append(leftLinesList.size() - rightLinesList.size());
      sb.append("\nDiffresult:\n");
      sb.append(diffResultString);
      sb.append("\nLeft:\n");
      sb.append(leftLinesList);
      sb.append("\nRight:\n");
      sb.append(rightLinesList);
      throw new RuntimeException(sb.toString());
    }
  }

  private List<SourceLine> processLeft(final List<String> sourceLines, final List<DiffSegment> diffActions) {
    List<SourceLine> resultLines = new ArrayList<SourceLine>();
    for (String tempLine : sourceLines) {
      resultLines.add(new SourceLine(DiffAction.u, tempLine));
    }

    int offset = 0;

    for (DiffSegment diffAction : diffActions) {
      if (DiffAction.a == diffAction.getAction()) {
        // Apply diff action ADD
        int addedLines = 0;
        int startLine = diffAction.getLeftLineIntervalStart() + offset;
        for (int i = diffAction.getRightLineIntervalStart(); i <= diffAction.getRightLineIntervalEnd(); i++) {
          resultLines.add(startLine++ - 1, new SourceLine(DiffAction.a, ""));
          addedLines++;
        }
        offset += addedLines;
      } else if (DiffAction.d == diffAction.getAction()) {
        // Apply diff action DELETE
        for (int i = diffAction.getLeftLineIntervalStart(); i <= diffAction.getLeftLineIntervalEnd(); i++) {
          resultLines.set(i - 1, new SourceLine(DiffAction.d, resultLines.get(i - 1).getLine()));
        }
      } else if (DiffAction.c == diffAction.getAction()) {
        // Apply diff action CHANGE
        int changedLines = 0;
        for (int i = diffAction.getRightLineIntervalStart(); i <= diffAction.getRightLineIntervalEnd(); i++) {
          resultLines.set(i - 1 + offset, new SourceLine(DiffAction.c, resultLines.get(i - 1 + offset).getLine()));
          changedLines++;
        }
        int addedLines = 0;
        for (int i = diffAction.getLeftLineIntervalStart() + changedLines; i <= diffAction.getLeftLineIntervalEnd(); i++) {
          resultLines.add(diffAction.getRightLineIntervalEnd() + offset, new SourceLine(DiffAction.c, ""));
          changedLines++;
          addedLines++;
        }
        offset += addedLines;
      }
    }
    return resultLines;
  }

  private List<SourceLine> processRight(final List<String> sourceLines, final List<DiffSegment> diffActions) {
    List<SourceLine> resultLines = new ArrayList<SourceLine>();
    for (String tempLine : sourceLines) {
      resultLines.add(new SourceLine(DiffAction.u, tempLine));
    }

    int offset = 0;

    for (DiffSegment diffAction : diffActions) {
      if (DiffAction.a == diffAction.getAction()) {
        // Apply diff action ADD
        for (int i = diffAction.getRightLineIntervalStart(); i <= diffAction.getRightLineIntervalEnd(); i++) {
          resultLines.set(i - 1 + offset, new SourceLine(DiffAction.a, resultLines.get(i - 1 + offset).getLine()));
        }
      } else if (DiffAction.d == diffAction.getAction()) {
        // Apply diff action DELETE
        int deletedLines = 0;
        for (int i = diffAction.getLeftLineIntervalStart(); i <= diffAction.getLeftLineIntervalEnd(); i++) {
          resultLines.add(i - 1, new SourceLine(DiffAction.d, ""));
          deletedLines++;
        }
        offset += deletedLines;
      } else if (DiffAction.c == diffAction.getAction()) {
        // Apply diff action CHANGE
        int changedLines = 0;
        for (int i = diffAction.getLeftLineIntervalStart(); i <= diffAction.getLeftLineIntervalEnd(); i++) {
          resultLines.set(i - 1 + offset, new SourceLine(DiffAction.c, resultLines.get(i - 1 + offset).getLine()));
          changedLines++;
        }
        int addedLines = 0;
        for (int i = diffAction.getRightLineIntervalStart() + changedLines; i <= diffAction.getRightLineIntervalEnd(); i++) {
          resultLines.add(diffAction.getLeftLineIntervalEnd() + offset, new SourceLine(DiffAction.c, ""));
          addedLines++;
        }
        offset += addedLines;
      }
    }
    return resultLines;
  }

  /**
   * Gets the diff result string.
   *
   * @return The result string
   */
  public String getDiffResultString() {
    return diffResultString;
  }

  /**
   * Gets the left lines.
   *
   * @return The list containing the left lines
   */
  public List<SourceLine> getLeft() {
    return leftLinesList;
  }

  /**
   * Gets the right lines.
   *
   * @return The list containing the right lines
   */
  public List<SourceLine> getRight() {
    return rightLinesList;
  }

}
