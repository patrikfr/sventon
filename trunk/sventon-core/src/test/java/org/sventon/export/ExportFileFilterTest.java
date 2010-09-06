package org.sventon.export;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExportFileFilterTest {

  @Test
  public void testAccept() throws Exception {
    final ExportFileFilter filter = new ExportFileFilter();
    assertTrue(filter.accept(null, "berlios-20060821234551243.zip"));
    assertTrue(filter.accept(null, "test-20100821234551243.zip"));
    assertTrue(filter.accept(null, "åäö-20100821234551243.zip"));
    assertFalse(filter.accept(null, "t st-20100821234551243.zip"));
    assertFalse(filter.accept(null, "test-20100821234551243zip"));
    assertFalse(filter.accept(null, "test-20100821234551243.zi"));
    assertFalse(filter.accept(null, "test-2010082123455124.zip"));
    assertFalse(filter.accept(null, "sventon.zip"));
    assertFalse(filter.accept(null, ""));

    try {
      filter.accept(null, null);
      fail("Should throw NPE");
    } catch (NullPointerException npe) {
      // expected
    }
  }
}