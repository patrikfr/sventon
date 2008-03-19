package de.berlios.sventon.repository.cache.revisioncache;

import de.berlios.sventon.appl.RepositoryName;
import de.berlios.sventon.repository.cache.objectcache.ObjectCache;
import de.berlios.sventon.repository.cache.objectcache.ObjectCacheImpl;
import junit.framework.TestCase;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import java.util.*;

public class RevisionCacheUpdaterTest extends TestCase {

  private ObjectCache createMemoryCache() throws Exception {
    return new ObjectCacheImpl(new RepositoryName("sventonTestCache"), null, 1000, false, false, 0, 0, false, 0);
  }

  public void testUpdate() throws Exception {
    final ObjectCache objectCache = createMemoryCache();
    final RevisionCacheImpl cache = new RevisionCacheImpl(objectCache);

    try {
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

      final RevisionCacheUpdater revisionCacheUpdater = new RevisionCacheUpdater(null);
      revisionCacheUpdater.updateInternal(cache, logEntries);
      final SVNLogEntry result1 = cache.get(123);
      final SVNLogEntry result2 = cache.get(124);

      assertEquals(4, result1.getChangedPaths().size());
      assertEquals(1, result2.getChangedPaths().size());
    } finally {
      objectCache.shutdown();
    }
  }
}