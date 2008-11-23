package org.sventon.model;

import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;

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
    context.setUid("uid");
    assertFalse(context.hasCredentials());
    context.setPwd("pwd");
    assertTrue(context.hasCredentials());
  }

  public void testSetUid() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    context.setUid("userid");
    assertEquals("userid", context.getUid());

    //test null case
    context.setUid(null);
    assertEquals(null, context.getUid());
  }

  public void testSetPwd() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    context.setPwd("password");
    assertEquals("password", context.getPwd());

    //test null case
    context.setPwd(null);
    assertEquals(null, context.getPwd());
  }

  public void testClearCredentials() throws Exception {
    final UserRepositoryContext context = new UserRepositoryContext();
    context.setPwd("password");
    context.setUid("user");
    assertEquals("password", context.getPwd());
    assertEquals("user", context.getUid());

    context.clearCredentials();
    assertNull(context.getPwd());
    assertNull(context.getUid());
  }

  //Test toString to make sure pwd and uid is not outputted
  public void testToString() throws Exception {

    String testString = "UserRepositoryContext[sortType=<null>," +
       "sortMode=<null>," +
       "latestRevisionsDisplayCount=1," +
       "charset=<null>," +
       "uid=*****," +
       "pwd=*****]";

    final UserRepositoryContext context = new UserRepositoryContext();
    context.setUid("uid");
    context.setPwd("pwd");
    assertEquals(testString, context.toString());

    testString = "UserRepositoryContext[sortType=<null>," +
       "sortMode=<null>," +
       "latestRevisionsDisplayCount=1," +
       "charset=<null>," +
       "uid=<null>," +
       "pwd=*****]";
    context.setUid(null);
    assertEquals(testString, context.toString());

    testString = "UserRepositoryContext[sortType=<null>," +
       "sortMode=<null>," +
       "latestRevisionsDisplayCount=1," +
       "charset=<null>," +
       "uid=<null>," +
       "pwd=<null>]";
    context.setPwd(null);
    assertEquals(testString, context.toString());
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