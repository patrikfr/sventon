package org.sventon.model;

import junit.framework.TestCase;
import org.sventon.colorer.Colorer;
import org.sventon.colorer.JHighlightColorer;

public class TextFileTest extends TestCase {

  private static final String BR = System.getProperty("line.separator");

  public void testTextFilePlain() throws Exception {
    final String content = "Line one" + BR + "Line two";
    final TextFile textFile = new TextFile(content);
    assertEquals(2, textFile.size());
    assertEquals("Line one" + BR + "Line two" + BR, textFile.getContent());
  }

  public void testTextFilePlainInitialBR() throws Exception {
    final String content = BR + "Line one" + BR + "Line two";
    final TextFile textFile = new TextFile(content);
    assertEquals(3, textFile.size());
    assertEquals(BR + "Line one" + BR + "Line two" + BR, textFile.getContent());
  }

  public void testTextFilePlainWebsafe() throws Exception {
    final String content = "Line&one" + BR + "Line<two>";
    final TextFile textFile = new TextFile(content);
    assertEquals(2, textFile.size());
    assertEquals("Line&amp;one" + BR + "Line&lt;two&gt;" + BR, textFile.getContent());
  }

  public void testTextFilePlainWebsafeExpandedKeywords() throws Exception {
    final Properties props = new Properties();
    props.put(Property.KEYWORDS, new PropertyValue("Author Date Revision URL"));
    props.put(Property.LAST_AUTHOR, new PropertyValue("domain\\user"));
    props.put(Property.COMMITTED_DATE, new PropertyValue("2005-09-05T18:27:48.718750Z"));
    props.put(Property.COMMITTED_REVISION, new PropertyValue("1234"));

    final String content = "$Revision$" + BR + "$Author$";
    final TextFile textFile = new TextFile(content, "Test.java", "UTF-8", null, props, "http://localhost/svn");

    assertEquals(2, textFile.size());
    assertEquals("$Revision: 1234 $" + BR + "$Author: domain\\user $" + BR, textFile.getContent());
  }

  public void testTextFileColorized() throws Exception {
    final String content = "class Test {" + BR + "// <test> " + BR + "}";
    final TextFile textFile = new TextFile(content, "Test.java", "UTF-8", getColorer(), null, null);
    assertEquals(3, textFile.size());
    assertEquals("<span class=\"java_keyword\">class</span><span class=\"java_plain\">&#160;</span>" +
        "<span class=\"java_type\">Test</span><span class=\"java_plain\">&#160;</span>" +
        "<span class=\"java_separator\">{</span><span class=\"java_plain\"></span>" + BR +
        "<span class=\"java_comment\">//&#160;&lt;test&gt;&#160;</span>" + BR +
        "<span class=\"java_separator\">}</span><span class=\"java_plain\"></span>" + BR, textFile.getContent());
  }

  private Colorer getColorer() {
    final JHighlightColorer col = new JHighlightColorer();
    final java.util.Properties mappings = new java.util.Properties();
    mappings.put("java", new com.uwyn.jhighlight.renderer.JavaXhtmlRenderer());
    mappings.put("html", new com.uwyn.jhighlight.renderer.XmlXhtmlRenderer());
    mappings.put("xml", new com.uwyn.jhighlight.renderer.XmlXhtmlRenderer());
    col.setRendererMappings(mappings);
    return col;
  }

}