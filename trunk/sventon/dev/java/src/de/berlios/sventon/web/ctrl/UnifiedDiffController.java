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
package de.berlios.sventon.web.ctrl;

import de.berlios.sventon.config.InstanceConfiguration;
import de.berlios.sventon.diff.DiffCreator;
import de.berlios.sventon.diff.DiffException;
import de.berlios.sventon.diff.DiffProducer;
import de.berlios.sventon.web.command.DiffCommand;
import de.berlios.sventon.web.model.RawTextFile;
import org.springframework.web.servlet.mvc.Controller;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a unified diff between two repository entries.
 *
 * @author jesper@users.berlios.de
 */
public class UnifiedDiffController extends DiffController implements Controller {

  /**
   * {@inheritDoc}
   */
  protected Map<String, Object> diffInternal(final SVNRepository repository, final DiffCommand diffCommand,
                                               InstanceConfiguration configuration) throws DiffException, SVNException {

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final Map<String, Object> model = new HashMap<String, Object>();
    final RawTextFile leftFile;
    final RawTextFile rightFile;

    final boolean isLeftFileTextType = getRepositoryService().isTextFile(repository, diffCommand.getFromPath(),
        diffCommand.getFromRevision().getNumber());
    final boolean isRightFileTextType = getRepositoryService().isTextFile(repository, diffCommand.getToPath(),
        diffCommand.getToRevision().getNumber());

    if (isLeftFileTextType || isRightFileTextType) {
      model.put("isBinary", false);

      // Get content of oldest file (left).
      logger.debug("Getting file content for (from) revision "
          + diffCommand.getFromRevision() + ", path: " + diffCommand.getFromPath());
      getRepositoryService().getFile(repository, diffCommand.getFromPath(), diffCommand.getFromRevision().getNumber(),
          outStream);
      leftFile = new RawTextFile(outStream.toString(), true);

      // Re-initialize stream
      outStream = new ByteArrayOutputStream();

      // Get content of newest file (right).
      logger.debug("Getting file content for (to) revision "
          + diffCommand.getToRevision() + ", path: " + diffCommand.getToPath());

      getRepositoryService().getFile(repository, diffCommand.getToPath(), diffCommand.getToRevision().getNumber(),
          outStream);
      rightFile = new RawTextFile(outStream.toString(), true);

      final ByteArrayOutputStream diffResult = new ByteArrayOutputStream();
      final DiffProducer diffProducer = new DiffProducer(new ByteArrayInputStream(leftFile.getContent().getBytes()),
          new ByteArrayInputStream(rightFile.getContent().getBytes()), DiffCreator.ENCODING);
      try {
        diffProducer.doUniDiff(diffResult);
      } catch (final IOException ioex) {
        throw new DiffException("Unable to procude unified diff");
      }

      final String diffResultString = diffResult.toString();
      if ("".equals(diffResultString)) {
        throw new DiffException("Files are identical.");
      }

      model.put("diffResult", diffResultString);
    } else {
      model.put("isBinary", true);  // Indicates that the file is in binary format.
      logger.info("One or both files selected for diff is in binary format. "
          + "Diff will not be performed");
    }
    return model;
  }

}
