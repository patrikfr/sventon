package de.berlios.sventon.repository.cache.revisioncache;

import de.berlios.sventon.TestUtils;
import de.berlios.sventon.appl.RepositoryName;
import de.berlios.sventon.repository.cache.objectcache.ObjectCache;
import de.berlios.sventon.repository.cache.objectcache.ObjectCacheImpl;
import junit.framework.TestCase;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.ArrayList;
import java.util.List;

public class RevisionCacheUpdaterTest extends TestCase {

  private ObjectCache createMemoryCache() throws Exception {
    return new ObjectCacheImpl(new RepositoryName("sventonTestCache"), null, 1000, false, false, 0, 0, false, 0);
  }

  public void testUpdate() throws Exception {
    final ObjectCache objectCache = createMemoryCache();
    final RevisionCacheImpl cache = new RevisionCacheImpl(objectCache);

    try {
      final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
      logEntries.add(TestUtils.getLogEntryStub(123, "Log message for revision 123."));
      logEntries.add(TestUtils.getLogEntryStub(124, "Log message for revision 124."));

      final RevisionCacheUpdater revisionCacheUpdater = new RevisionCacheUpdater(null);
      revisionCacheUpdater.updateInternal(cache, logEntries);
      final SVNLogEntry result1 = cache.get(123);
      final SVNLogEntry result2 = cache.get(124);

      assertEquals(123, result1.getRevision());
      assertEquals(124, result2.getRevision());
    } finally {
      objectCache.shutdown();
    }
  }
}