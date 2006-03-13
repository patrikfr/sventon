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
package de.berlios.sventon.colorer;

import com.uwyn.jhighlight.renderer.Renderer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

/**
 * Colorizes given input using the JHighlight syntax highlighting library.
 *
 * @author jesper@users.berlios.de
 * @link http://jhighlight.dev.java.net
 */
public class JHighlightColorer implements Colorer {

  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The encoding, default set to <code>ISO-8859-1</code>.
   * <p/>
   * TODO: Use UTF-8 as default instead?
   */
  private String encoding = "ISO-8859-1";

  private Properties rendererMappings;

  /**
   * Constructor.
   */
  public JHighlightColorer() {
  }

  /**
   * {@inheritDoc}
   */
  public String getColorizedContent(final String content, final String fileExtension) {
    logger.debug("Colorizing content, file extension: " + fileExtension);

    Renderer renderer = getRenderer(fileExtension);
    StringBuilder sb = new StringBuilder();

    if (renderer == null) {
      return StringEscapeUtils.escapeXml(content);
    }

    try {
      sb.append(renderer.highlight(null, content, encoding, true, true));
    } catch (Exception ioex) {
      logger.error(ioex);
    }
    return sb.toString();
  }

  /**
   * Gets the <code>Renderer</code> instance for given file extension,
   * based on it's extension.
   *
   * @param fileExtension The file extension.
   * @return The JHighlight <code>Renderer</code> instance.
   */
  protected Renderer getRenderer(final String fileExtension) {
    if (fileExtension == null) {
      throw new IllegalArgumentException("File extension cannot be null");
    }
    return (Renderer) rendererMappings.get(fileExtension.toLowerCase());
  }

  /**
   * Sets the file extension / renderer mapping
   *
   * @param rendererMappings The mappings
   */
  public void setRendererMappings(Properties rendererMappings) {
    this.rendererMappings = rendererMappings;
  }

  /**
   * Sets the encoding.
   *
   * @param encoding The encoding.
   */
  public void setEncoding(final String encoding) {
    this.encoding = encoding;
  }

}
