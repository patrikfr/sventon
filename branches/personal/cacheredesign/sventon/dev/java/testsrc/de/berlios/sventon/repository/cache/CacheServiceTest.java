package de.berlios.sventon.repository.cache;

import de.berlios.sventon.cache.ObjectCache;
import de.berlios.sventon.repository.RepositoryConfiguration;
import de.berlios.sventon.repository.RepositoryEntry;
import de.berlios.sventon.repository.SVNRepositoryStub;
import de.berlios.sventon.repository.CommitMessage;
import de.berlios.sventon.repository.cache.commitmessagecache.CommitMessageCache;
import de.berlios.sventon.repository.cache.commitmessagecache.CommitMessageCacheImpl;
import de.berlios.sventon.repository.cache.entrycache.EntryCache;
import de.berlios.sventon.repository.cache.entrycache.EntryCacheWriter;
import de.berlios.sventon.repository.cache.entrycache.MemoryCache;
import junit.framework.TestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.tmatesoft.svn.core.*;

import java.util.*;

public class CacheServiceTest extends TestCase {

  public void testFindEntry() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(false); // Prevent physical connection during update check
    config.setRepositoryRoot("http://localhost");
    final EntryCache entryCache = new MemoryCache();
    final EntryCacheWriter writer = new EntryCacheWriter(entryCache);
    writer.add(getEntryTemplateList());

    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setRepositoryConfiguration(config);
    cacheService.setEntryCache(entryCache);
    cacheService.initialize();
    assertEquals(4, cacheService.findEntry("java").size());
  }

  public void testFindEntryInPath() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(false); // Prevent physical connection during update check
    config.setRepositoryRoot("http://localhost");
    final EntryCache entryCache = new MemoryCache();
    final EntryCacheWriter writer = new EntryCacheWriter(entryCache);
    writer.add(getEntryTemplateList());

    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setRepositoryConfiguration(config);
    cacheService.setEntryCache(entryCache);
    cacheService.initialize();
    assertEquals(1, cacheService.findEntry("html", "/trunk/src/").size());
  }

  public void testFindEntryWithLimit() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(false); // Prevent physical connection during update check
    config.setRepositoryRoot("http://localhost");
    final EntryCache entryCache = new MemoryCache();
    final EntryCacheWriter writer = new EntryCacheWriter(entryCache);
    writer.add(getEntryTemplateList());

    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setRepositoryConfiguration(config);
    cacheService.setEntryCache(entryCache);
    cacheService.initialize();
    assertEquals(2, cacheService.findEntry("java", "/", 2).size());

    assertEquals(4, cacheService.findEntry("java", "/", null).size());
    assertEquals(4, cacheService.findEntry("java", "/", 8).size());
    assertEquals(4, cacheService.findEntry("java", "/", 0).size());
    assertEquals(1, cacheService.findEntry("java", "/", 1).size());
  }

  public void testFindDirectories() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(false); // Prevent physical connection during update check
    config.setRepositoryRoot("http://localhost");
    final EntryCache entryCache = new MemoryCache();
    final EntryCacheWriter writer = new EntryCacheWriter(entryCache);
    writer.add(getEntryTemplateList());

    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setRepositoryConfiguration(config);
    cacheService.setEntryCache(entryCache);
    cacheService.initialize();
    assertEquals(3, cacheService.findDirectories("/").size());

    assertEquals(1, cacheService.findDirectories("/trunk/").size());
  }

  public void testPopulateCaches() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(true);
    config.setRepositoryRoot("http://localhost");

    final EntryCache entryCache = new MemoryCache();

    final Directory directory = new RAMDirectory();
    final CommitMessageCache messageCache = new CommitMessageCacheImpl(directory);

    final TestRepository repos = new TestRepository();
    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setObjectCache(new TestObjectCache());
    cacheService.setRepositoryConfiguration(config);
    cacheService.setRepository(repos);
    cacheService.setEntryCache(entryCache);
    cacheService.setCommitMessageCache(messageCache);
    cacheService.initialize();

    assertEquals(1, cacheService.find("revision").size());
    assertEquals(8, cacheService.findEntry(".*").size());
  }

  public void testUpdateCaches() throws Exception {
    final RepositoryConfiguration config = new RepositoryConfiguration();
    config.setCacheUsed(true);
    config.setRepositoryRoot("http://localhost");

    final EntryCache entryCache = new MemoryCache();
    final EntryCacheWriter writer = new EntryCacheWriter(entryCache);
    writer.setCachedRevision(100);
    writer.setRepositoryURL("http://localhost");
    writer.add(getEntryTemplateList());

    final Directory directory = new RAMDirectory();
    final CommitMessageCache messageCache = new CommitMessageCacheImpl(directory);

    final TestRepository repos = new TestRepository();
    final CacheServiceImpl cacheService = new CacheServiceImpl();
    cacheService.setObjectCache(new TestObjectCache());
    cacheService.setRepositoryConfiguration(config);
    cacheService.setRepository(repos);
    cacheService.setEntryCache(entryCache);
    cacheService.setCommitMessageCache(messageCache);
    cacheService.initialize();

    cacheService.updateCaches(); // Triggers the populationn
//    repos.setLatestRevision(repos.getLatestRevision() + 1);
    assertEquals(11, cacheService.findEntry(".*").size());
    List<CommitMessage> msgs = cacheService.find("revision");
    assertEquals(2, msgs.size());  // Triggers the update

  }

  private List<RepositoryEntry> getEntryTemplateList() {
    final List<RepositoryEntry> entries = new ArrayList<RepositoryEntry>();
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "trunk", SVNNodeKind.DIR, 0, false, 1, new Date(), "jesper"), "/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "tags", SVNNodeKind.DIR, 0, false, 1, new Date(), "jesper"), "/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "file1.java", SVNNodeKind.FILE, 64000, false, 1, new Date(), "jesper"), "/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "file2.html", SVNNodeKind.FILE, 32000, false, 2, new Date(), "jesper"), "/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "File3.java", SVNNodeKind.FILE, 16000, false, 3, new Date(), "jesper"), "/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "src", SVNNodeKind.DIR, 0, false, 1, new Date(), "jesper"), "/trunk/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "DirFile2.html", SVNNodeKind.FILE, 3200, false, 2, new Date(), "jesper"), "/trunk/src/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "file1_in_trunk.java", SVNNodeKind.FILE, 6400, false, 1, new Date(), "jesper"), "/trunk/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "DirFile3.java", SVNNodeKind.FILE, 1600, false, 3, new Date(), "jesper"), "/trunk/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "tagfile.txt", SVNNodeKind.FILE, 3200, false, 2, new Date(), "jesper"), "/tags/", null));
    entries.add(new RepositoryEntry(new SVNDirEntry(null, "tagfile2.txt", SVNNodeKind.FILE, 1600, false, 3, new Date(), "jesper"), "/tags/", null));
    return entries;
  }

  class TestObjectCache implements ObjectCache {
    private HashMap cache = new HashMap();

    public void put(final Object cacheKey, final Object value) {
      cache.put(cacheKey, value);
    }

    public Object get(final Object cacheKey) {
      return cache.get(cacheKey);
    }

    public long getHitCount() {
      return 0;
    }

    public long getMissCount() {
      return 0;
    }

    public void shutdown() throws Exception {
    }
  }

  class TestRepository extends SVNRepositoryStub {

    private HashMap<String, Collection> repositoryEntries = new HashMap<String, Collection>();
    private List<SVNLogEntry> logEntriesForPopulation = new ArrayList<SVNLogEntry>();
    private List<SVNLogEntry> logEntriesForUpdate = new ArrayList<SVNLogEntry>();
    private SVNDirEntry infoEntry;
    private long headRevision = 123;

    public TestRepository() throws SVNException {
      super(SVNURL.parseURIDecoded("http://localhost/"), null);

      final List<SVNDirEntry> entries1 = new ArrayList<SVNDirEntry>();
      entries1.add(new SVNDirEntry(null, "file1.java", SVNNodeKind.FILE, 64000, false, 1, new Date(), "jesper"));
      entries1.add(new SVNDirEntry(null, "file2.html", SVNNodeKind.FILE, 32000, false, 2, new Date(), "jesper"));
      entries1.add(new SVNDirEntry(null, "dir1", SVNNodeKind.DIR, 0, false, 1, new Date(), "jesper"));
      entries1.add(new SVNDirEntry(null, "File3.java", SVNNodeKind.FILE, 16000, false, 3, new Date(), "jesper"));
      final List<SVNDirEntry> entries2 = new ArrayList<SVNDirEntry>();
      entries2.add(new SVNDirEntry(null, "dir2", SVNNodeKind.DIR, 0, false, 1, new Date(), "jesper"));
      entries2.add(new SVNDirEntry(null, "file1.java", SVNNodeKind.FILE, 6400, false, 1, new Date(), "jesper"));
      entries2.add(new SVNDirEntry(null, "DirFile2.html", SVNNodeKind.FILE, 3200, false, 2, new Date(), "jesper"));
      entries2.add(new SVNDirEntry(null, "DirFile3.java", SVNNodeKind.FILE, 1600, false, 3, new Date(), "jesper"));
      repositoryEntries.put("/", entries1);
      repositoryEntries.put("/dir1/", entries2);
      repositoryEntries.put("/dir1/dir2/", new ArrayList());

      final Map<String, SVNLogEntryPath> changedPathsPopulation = new HashMap<String, SVNLogEntryPath>();
      changedPathsPopulation.put("/file1.java", new SVNLogEntryPath("/file1.java", 'M', null, 1));
      changedPathsPopulation.put("/file2.html", new SVNLogEntryPath("/file2.html", 'D', null, 1));
      changedPathsPopulation.put("/file3.abc", new SVNLogEntryPath("/file3.abc", 'A', null, 1));
      changedPathsPopulation.put("/file4.def", new SVNLogEntryPath("/file4.def", 'R', null, 1));
      logEntriesForPopulation.add(new SVNLogEntry(changedPathsPopulation, 123, "jesper", new Date(), "Commit message for revision 123."));

      final Map<String, SVNLogEntryPath> changedPathsUpdate = new HashMap<String, SVNLogEntryPath>();
      changedPathsUpdate.put("/file1.java", new SVNLogEntryPath("/file1.java", 'M', null, 1));
      logEntriesForUpdate.add(new SVNLogEntry(changedPathsUpdate, 124, "jesper", new Date(), "Commit message for revision 124."));

      infoEntry = new SVNDirEntry(null, "file999.java", SVNNodeKind.FILE, 12345, false, 1, new Date(), "jesper");
    }

    public long getLatestRevision() {
      return headRevision;
    }

    public void setLatestRevision(final long headRevision) {
      this.headRevision = headRevision;
    }

    public Collection getDir(String path, long revision, Map properties, Collection dirEntries) throws SVNException {
      return repositoryEntries.get(path);
    }

    public Collection log(String[] targetPaths, Collection entries, long startRevision, long endRevision, boolean changedPath, boolean strictNode) throws SVNException {
      return logEntriesForUpdate;
    }

    public long log(String[] targetPaths, long startRevision, long endRevision, boolean changedPath, boolean strictNode, ISVNLogEntryHandler handler) throws SVNException {
      for (final SVNLogEntry svnLogEntry : logEntriesForPopulation) {
        handler.handleLogEntry(svnLogEntry);
      }
      return logEntriesForPopulation.size();
    }

    public SVNDirEntry info(String path, long revision) throws SVNException {
      return infoEntry;
    }

  }

}