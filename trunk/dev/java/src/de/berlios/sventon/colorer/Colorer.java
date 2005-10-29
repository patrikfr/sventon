package de.berlios.sventon.colorer;

/**
 * Colorer interface.
 *
 * @author jesper@users.berlios.de
 */
public interface Colorer {

  /**
   * Converts given contents into colorized HTML code.
   * @param content The contents.
   * @param filename The filename, used to determine formatter.
   * @return The colorized string.
   */
  String getColorizedContent(final String content, final String filename);
  //TODO: Method should not take filename as a parameter.
  // Better use another type of identifier instead.
}
