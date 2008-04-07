package de.berlios.sventon.web.command;

import de.berlios.sventon.repository.SVNRepositoryStub;
import junit.framework.TestCase;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.Date;

public class SVNBaseCommandTest extends TestCase {

  public void testDefaultValues() {
    final SVNBaseCommand command = new SVNBaseCommand();
    assertEquals("/", command.getPath());
    assertEquals(SVNRevision.HEAD, command.getRevision());
  }

  public void testSetPath() {
    final SVNBaseCommand command = new SVNBaseCommand();

    //null is OK, will be converted to "/"
    command.setPath(null);
    assertEquals("/", command.getPath());

    //"" (empty string) will also be converted to "/"
    command.setPath("");
    assertEquals("/", command.getPath());

    command.setPath("Asdf.java");
    assertEquals("/Asdf.java", command.getPath());
  }

  public void testSetRevision() {
    final SVNBaseCommand command = new SVNBaseCommand();
    try {
      command.setRevision(null);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    assertEquals(SVNRevision.HEAD, command.getRevision());

    command.setRevision(SVNRevision.parse("2"));
    assertEquals(SVNRevision.create(2), command.getRevision());

    //Drutten is accepted as a revision here, but not by the SVNBaseCommandValidator
    command.setRevision(SVNRevision.parse("Drutten"));
    assertEquals(SVNRevision.UNDEFINED, command.getRevision());

    //HEAD in different cases are converted to HEAD
    command.setRevision(SVNRevision.parse("HEAD"));
    assertEquals(SVNRevision.HEAD, command.getRevision());

    command.setRevision(SVNRevision.parse("head"));
    assertEquals(SVNRevision.HEAD, command.getRevision());

    command.setRevision(SVNRevision.parse("HEad"));
    assertEquals(SVNRevision.HEAD, command.getRevision());
  }

  public void testGetCompletePath() {
    final SVNBaseCommand command = new SVNBaseCommand();
    command.setPath("trunk/src/File.java");
    assertEquals("/trunk/src/File.java", command.getPath());
  }

  public void testTranslateRevision() throws Exception {
    final SVNBaseCommand command = new SVNBaseCommand();
    command.setRevision(SVNRevision.parse("head"));
    command.translateRevision(100, null);
    assertEquals(SVNRevision.HEAD, command.getRevision());
    assertEquals(100, command.getRevisionNumber());

    command.setRevision(SVNRevision.parse(""));
    command.translateRevision(100, null);
    assertEquals(SVNRevision.UNDEFINED, command.getRevision());
    assertEquals(100, command.getRevisionNumber());

    command.setRevision(SVNRevision.parse("123"));
    command.translateRevision(200, null);
    assertEquals(SVNRevision.create(123), command.getRevision());

    command.setRevision(SVNRevision.parse("{2007-01-01}"));
    command.translateRevision(200, new SVNRepositoryStub(null, null) {
      public long getDatedRevision(final Date date) throws SVNException {
        return 123;
      }
    });
    assertEquals(123, command.getRevisionNumber());
  }

}
