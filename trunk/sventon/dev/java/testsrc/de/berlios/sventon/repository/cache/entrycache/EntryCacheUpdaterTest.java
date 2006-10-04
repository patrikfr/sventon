package de.berlios.sventon.repository.cache.entrycache;

import de.berlios.sventon.repository.SVNRepositoryStub;
import de.berlios.sventon.repository.RevisionUpdate;
import de.berlios.sventon.service.RepositoryServiceImpl;
import de.berlios.sventon.config.ApplicationConfiguration;
import junit.framework.TestCase;
import org.tmatesoft.svn.core.*;

import java.util.*;

public class EntryCacheUpdaterTest extends TestCase {

  public void testUpdate() throws Exception {
    final EntryCache entryCache = new MemoryCache();
    //final EntryCacheManager cacheManager = new EntryCacheManager("/");
    //cacheManager.addCache("testCache", entryCache);
    //final EntryCacheUpdater cacheUpdater = new EntryCacheUpdater(cacheManager);
    //cacheUpdater.setRepository(new TestRepository());

    assertEquals(0, entryCache.getSize());

    final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
    final Map<String, SVNLogEntryPath> changedPaths1 = new HashMap<String, SVNLogEntryPath>();
    changedPaths1.put("/file1.java", new SVNLogEntryPath("/file1.java", 'M', null, 1));
    changedPaths1.put("/file2.html", new SVNLogEntryPath("/file2.html", 'D', null, 1));
    changedPaths1.put("/file3.abc", new SVNLogEntryPath("/file3.abc", 'A', null, 1));
    changedPaths1.put("/file4.def", new SVNLogEntryPath("/file4.def", 'R', null, 1));
    logEntries.add(new SVNLogEntry(changedPaths1, 123, "jesper", new Date(), "Log message for revision 123."));

    final Map<String, SVNLogEntryPath> changedPaths2 = new HashMap<String, SVNLogEntryPath>();
    changedPaths2.put("/file1.java", new SVNLogEntryPath("/file1.java", 'M', null, 1));
    logEntries.add(new SVNLogEntry(changedPaths2, 124, "jesper", new Date(), "Log message for revision 124."));

    assertEquals(0, entryCache.getSize());
    new EntryCacheUpdater(null, new ApplicationConfiguration(), new RepositoryServiceImpl()).updateInternal(entryCache,
        new TestRepository(), new RevisionUpdate("defaultsvn", logEntries));
    //TODO: Fix this test - all repository.info()-calls returns the same value now.
    assertEquals(1, entryCache.getSize());
  }

  class TestRepository extends SVNRepositoryStub {
    public TestRepository() throws SVNException {
      super(SVNURL.parseURIDecoded("http://localhost/"), null);
    }

    public SVNDirEntry info(String path, long revision) throws SVNException {
      return new SVNDirEntry(null, "file999.java", SVNNodeKind.FILE, 12345, false, 1, new Date(), "jesper");
    }
  }
}