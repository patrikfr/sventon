/*
 * ====================================================================
 * Copyright (c) 2005-2007 Sventon Project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://sventon.berlios.de.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.colorer;

import com.uwyn.jhighlight.renderer.Renderer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Colorizes given input using the JHighlight syntax highlighting library.
 *
 * @author jesper@users.berlios.de
 * @link http://jhighlight.dev.java.net
 */
public final class JHighlightColorer implements Colorer {

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * File type <-> renderer mappings.
   */
  private Properties rendererMappings;

  /**
   * {@inheritDoc}
   */
  public String getColorizedContent(final String content, final String fileExtension, final String encoding) throws IOException {

    logger.debug("Colorizing content, file extension: " + fileExtension);

    final Renderer renderer = getRenderer(fileExtension);

    if (content == null) {
      return "";
    }

    if (renderer == null) {
      return StringEscapeUtils.escapeXml(content);
    }
    // Sorry Geert, but we need to remove the trailing BR-tags
    // and the initial comment, as it messes up the line numbering.
    return renderer.highlight(null, content, encoding, true).substring(73).replace("<br />", "").trim();
  }

  /**
   * Gets the <code>Renderer</code> instance for given file extension,
   * based on it's extension.
   *
   * @param fileExtension The file extension.
   * @return The JHighlight <code>Renderer</code> instance.
   * @throws IllegalArgumentException if file extension is null.
   */
  protected Renderer getRenderer(final String fileExtension) {
    Validate.notNull(fileExtension, "File extension cannot be null");
    return (Renderer) rendererMappings.get(fileExtension.toLowerCase());
  }

  /**
   * Sets the file extension / renderer mapping
   *
   * @param rendererMappings The mappings
   */
  public void setRendererMappings(final Properties rendererMappings) {
    this.rendererMappings = rendererMappings;
  }

}
