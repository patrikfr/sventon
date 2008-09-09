package de.berlios.sventon.repository;

import junit.framework.TestCase;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNNodeKind;

import java.util.ArrayList;
import java.util.List;

public class RepositoryEntryKindFilterTest extends TestCase {

  public void testFilter() throws Exception {

    final List<RepositoryEntry> list = new ArrayList<RepositoryEntry>();
    list.add(new RepositoryEntry(new SVNDirEntry(null, "test.abC", SVNNodeKind.FILE, 0, false, 0, null, null), "/", null));
    list.add(new RepositoryEntry(new SVNDirEntry(null, "/trunk/test", SVNNodeKind.DIR, 0, false, 0, null, null), "/", null));
    list.add(new RepositoryEntry(new SVNDirEntry(null, "/trunk", SVNNodeKind.DIR, 0, false, 0, null, null), "/", null));

    List<RepositoryEntry> filteredList;

    try {
      filteredList = new RepositoryEntryKindFilter(RepositoryEntry.Kind.none).filter(list);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }

    try {
      filteredList = new RepositoryEntryKindFilter(RepositoryEntry.Kind.any).filter(list);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }

    try {
      filteredList = new RepositoryEntryKindFilter(RepositoryEntry.Kind.unknown).filter(list);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }

    filteredList = new RepositoryEntryKindFilter(RepositoryEntry.Kind.dir).filter(list);
    assertEquals(2, filteredList.size());

    filteredList = new RepositoryEntryKindFilter(RepositoryEntry.Kind.file).filter(list);
    assertEquals(1, filteredList.size());

    try {
      filteredList = new RepositoryEntryKindFilter(null).filter(list);
      fail("Should throw IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }
}