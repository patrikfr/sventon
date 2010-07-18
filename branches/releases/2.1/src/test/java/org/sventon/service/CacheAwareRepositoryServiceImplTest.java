package org.sventon.service;

import junit.framework.TestCase;

public class CacheAwareRepositoryServiceImplTest extends TestCase {

  public void testCalculateRevisionsToFetch() throws Exception {
    final CacheAwareRepositoryServiceImpl service = new CacheAwareRepositoryServiceImpl();
    assertEquals("[3, 2]", service.calculateRevisionsToFetch(3, 2).toString());
    assertEquals("[3, 2, 1]", service.calculateRevisionsToFetch(3, 3).toString());
    assertEquals("[3, 2, 1]", service.calculateRevisionsToFetch(3, 4).toString());
  }

}
