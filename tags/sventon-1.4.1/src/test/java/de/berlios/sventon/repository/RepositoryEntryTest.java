package de.berlios.sventon.repository;

import junit.framework.TestCase;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;

import java.util.Date;

public class RepositoryEntryTest extends TestCase {

  public void testGetFriendlyFullEntryNameLongFullName() throws Exception {
    SVNDirEntry e = new SVNDirEntry(null, null, "test.fil", SVNNodeKind.FILE, 1, false, 1, new Date(), "A");
    RepositoryEntry entry = new RepositoryEntry(e, "/source/com/bli/bla/blu/saaaaaoooooooooooaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/");
    assertEquals("...li/bla/blu/saaaaaoooooooooooaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/test.fil", entry.getFriendlyFullEntryName());
    assertEquals(RepositoryEntry.FULL_ENTRY_NAME_MAX_LENGTH, entry.getFriendlyFullEntryName().length());

    e = new SVNDirEntry(null, null, "reallylongfilenamejustfortestingpurposes.fil", SVNNodeKind.FILE, 1, false, 1, new Date(), "A");
    entry = new RepositoryEntry(e, "/source/com/bli/bla/blu/saaaaa/bbbbbb/ccccccccc/dddddddd/aaaaaaaaaaaaaaaaaa/");
    assertEquals("...ddd/aaaaaaaaaaaaaaaaaa/reallylongfilenamejustfortestingpurposes.fil", entry.getFriendlyFullEntryName());
    assertEquals(RepositoryEntry.FULL_ENTRY_NAME_MAX_LENGTH, entry.getFriendlyFullEntryName().length());
  }

  public void testGetFriendlyFullEntryNameShortName() throws Exception {
    SVNDirEntry e = new SVNDirEntry(null, null, "test.fil", SVNNodeKind.FILE, 1, false, 1, new Date(), "A");
    RepositoryEntry entry = new RepositoryEntry(e, "/source/com/bli/bla/blu/");
    assertEquals("/source/com/bli/bla/blu/test.fil", entry.getFriendlyFullEntryName());
  }

  public void testGetFriendlyFullEntryNameLongName() throws Exception {
    SVNDirEntry e = new SVNDirEntry(null, null, "thisisafilenamewithmorethanenoughcharactersinitreallynotperfectlookingontheweb.fil", SVNNodeKind.FILE, 1,
        false, 1, new Date(), "A");
    RepositoryEntry entry = new RepositoryEntry(e, "/source/com/bli/bla/blu/");
    assertEquals("...withmorethanenoughcharactersinitreallynotperfectlookingontheweb.fil", entry.getFriendlyFullEntryName());
    assertEquals(RepositoryEntry.FULL_ENTRY_NAME_MAX_LENGTH, entry.getFriendlyFullEntryName().length());

    e = new SVNDirEntry(null, null, "reallylongfilenamejustfortestingpurposesonly.fil", SVNNodeKind.FILE, 1, false, 1, new Date(), "A");
    entry = new RepositoryEntry(e, "/source/com/bli/bla/blu/saaaaa/bbbbbb/ccccccccc/dddddddd/aaaaaaaaaaaaaaaaaa/");
    assertEquals("...aaaaaaaaaaaaaaaaaa/reallylongfilenamejustfortestingpurposesonly.fil", entry.getFriendlyFullEntryName());
    assertEquals(RepositoryEntry.FULL_ENTRY_NAME_MAX_LENGTH, entry.getFriendlyFullEntryName().length());
  }

}
