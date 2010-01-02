/*
 * ====================================================================
 * Copyright (c) 2005-2010 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.sventon.util;

import org.tmatesoft.svn.core.wc.SVNRevision;

import java.beans.PropertyEditorSupport;

/**
 * Custom property editor for repository name.
 *
 * @author jesper@sventon.org
 */
public class RevisionEditor extends PropertyEditorSupport {

  /**
   * {@inheritDoc}
   */
  @Override
  public void setAsText(final String text) {
    setValue(SVNRevision.parse(text));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAsText() {
    return super.getValue().toString();
  }

}