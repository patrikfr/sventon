package de.berlios.sventon.repository.export;

import junit.framework.TestCase;

import java.util.Date;

public class ExportDirectoryTest extends TestCase {

  public void testCreateTempFilename() throws Exception {
    final Date now = new Date(1111111111111L);
    final ExportDirectory exportDirectory = new ExportDirectory("defaultsvn", System.getProperty("java.io.tmpdir"));
    assertEquals("defaultsvn-20050318025831111.zip", exportDirectory.createTempFilename(now));
  }
  
}