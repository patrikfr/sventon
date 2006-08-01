/*
 * ====================================================================
 * Copyright (c) 2005-2006 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.web.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ArchiveFile.
 *
 * @author jesper@users.berlios.de
 */
public class ArchiveFile extends AbstractFile {

  /**
   * Constructor.
   *
   * @param content The file contents.
   */
  public ArchiveFile(final byte[] content) throws IOException {
    final ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content));
    final List<ZipEntry> archiveEntries = new ArrayList<ZipEntry>();
    ZipEntry zipEntry;
    while ((zipEntry = zip.getNextEntry()) != null) {
      archiveEntries.add(zipEntry);
    }
    model.put("entries", archiveEntries);
  }

  /**
   * {@inheritDoc}
   */
  public String getContent() {
    throw new UnsupportedOperationException();
  }

}
