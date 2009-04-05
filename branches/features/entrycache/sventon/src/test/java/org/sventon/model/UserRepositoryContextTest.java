package org.sventon.model;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

public class UserRepositoryContextTest extends TestCase {

  public void testDefaults() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    assertNull(context.getCharset());
    assertEquals(1, context.getLatestRevisionsDisplayCount());
    assertNull(context.getSearchMode());
    assertNull(context.getSortMode());
    assertNull(context.getSortType());
  }

  public void testHasCredentials() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    assertFalse(context.hasCredentials());
    context.setCredentials(new Credentials("uid", "pwd"));
    assertTrue(context.hasCredentials());
  }

  public void testClearCredentials() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    context.setCredentials(new Credentials("uid", "pwd"));
    assertTrue(context.hasCredentials());
    context.clearCredentials();
    assertSame(Credentials.EMPTY, context.getCredentials());
  }

  public void testGetUserContext() throws Exception {
    final RepositoryName name = new RepositoryName("repository1");
    final HttpServletRequest request = new MockHttpServletRequest();
    assertNull(request.getSession().getAttribute("userContext"));
    final UserRepositoryContext userRepositoryContext = UserRepositoryContext.getContext(request, name);
    assertNotNull(request.getSession());
    final Object o = request.getSession().getAttribute("userContext");
    assertNotNull(o);
    assertTrue(o instanceof UserContext);
    final UserRepositoryContext contextFromSession = ((UserContext) o).getUserRepositoryContext(name);
    assertSame(contextFromSession, userRepositoryContext);
  }
}