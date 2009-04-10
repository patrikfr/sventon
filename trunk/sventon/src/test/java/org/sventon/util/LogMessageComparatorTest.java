package org.sventon.util;

import junit.framework.TestCase;
import org.sventon.model.LogMessage;
import org.tmatesoft.svn.core.SVNLogEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LogMessageComparatorTest extends TestCase {

  public void testCompare() throws Exception {
    List<LogMessage> entries = new ArrayList<LogMessage>();
    LogMessage c1 = new LogMessage(new SVNLogEntry(null, 1, "jesper", new Date(), "Message 1"));
    LogMessage c2 = new LogMessage(new SVNLogEntry(null, 12, "jesper", new Date(), "Message 2"));
    LogMessage c3 = new LogMessage(new SVNLogEntry(null, 123, "jesper", new Date(), "Message 3"));

    entries.add(c3);
    entries.add(c2);
    entries.add(c1);

    assertSame(c3, entries.get(0));

    Collections.sort(entries, new LogMessageComparator(LogMessageComparator.ASCENDING));
    assertSame(c1, entries.get(0));
    assertSame(c2, entries.get(1));
    assertSame(c3, entries.get(2));

    Collections.sort(entries, new LogMessageComparator(LogMessageComparator.DESCENDING));
    assertSame(c1, entries.get(2));
    assertSame(c2, entries.get(1));
    assertSame(c3, entries.get(0));

    try {
      new LogMessageComparator(6);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException iae) {
      // Expected
    }

    // null values are not OK
    LogMessageComparator comparator = new LogMessageComparator(LogMessageComparator.ASCENDING);
    try {
      comparator.compare(null, null);
      fail("NullPointerException expected");
    } catch (NullPointerException npe) {
      // Expected
    }
  }

}