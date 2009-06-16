package org.sventon.cache.revisioncache;


import junit.framework.TestCase;
import org.sventon.TestUtils;
import org.sventon.cache.objectcache.ObjectCache;
import org.sventon.cache.objectcache.ObjectCacheImpl;
import org.tmatesoft.svn.core.SVNLogEntry;

public class RevisionCacheImplTest extends TestCase {

  private ObjectCache createMemoryCache() throws Exception {
    return new ObjectCacheImpl("sventonTestCache", null, 1000, false, false, 0, 0, false, 0);
  }

  public void testGetAndAdd() throws Exception {
    final ObjectCache cache = createMemoryCache();
    final RevisionCacheImpl revisionCache = new RevisionCacheImpl(cache);
    try {
      assertNull(revisionCache.get(123));
      revisionCache.add(TestUtils.getLogEntryStub());
      final SVNLogEntry result = revisionCache.get(123);
      assertNotNull(result);
      assertEquals(123, result.getRevision());
      assertEquals("TestAuthor", result.getAuthor());
      assertNotNull(result.getDate());
      assertEquals("TestMessage", result.getMessage());
    } finally {
      cache.shutdown();
    }
  }

}