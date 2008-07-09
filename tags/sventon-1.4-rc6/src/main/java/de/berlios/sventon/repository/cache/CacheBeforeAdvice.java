/*
 * ====================================================================
 * Copyright (c) 2005-2008 sventon project. All rights reserved.
 *
 * This software is licensed as described in the file LICENSE, which
 * you should have received as part of this distribution. The terms
 * are also available at http://www.sventon.org.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package de.berlios.sventon.repository.cache;

import de.berlios.sventon.appl.RevisionObservable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * Before advice that checks if the cache needs to be updated before any method
 * of class {@link CacheGatewayImpl} is execuded.
 *
 * @author jesper@users.berlios.de
 */
public final class CacheBeforeAdvice implements MethodBeforeAdvice {

  /**
   * The logging instance.
   */
  private final Log logger = LogFactory.getLog(getClass());

  /**
   * The Observable. Used to trigger cache updates.
   */
  private RevisionObservable revisionObservable;

  /**
   * Sets the observable. Needed to trigger cache updates.
   *
   * @param revisionObservable The observable
   */
  public void setRevisionObservable(final RevisionObservable revisionObservable) {
    this.revisionObservable = revisionObservable;
  }

  /**
   * {@inheritDoc}
   */
  public void before(final Method method, final Object[] args, final Object target) throws Throwable {
    final String instanceName = (String) args[0];
    logger.debug("Updating cache for instance [" + instanceName + "] (if needed)");
    revisionObservable.update(instanceName, false);
  }
}
