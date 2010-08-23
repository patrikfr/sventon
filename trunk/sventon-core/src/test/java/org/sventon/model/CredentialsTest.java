package org.sventon.model;

import junit.framework.TestCase;

public class CredentialsTest extends TestCase {

  public void testCredentials() throws Exception {
    Credentials credentials = new Credentials("userid", null);
    assertEquals("userid", credentials.getUserName());
    assertEquals(null, credentials.getPassword());

    credentials = new Credentials(null, "password");
    assertEquals(null, credentials.getUserName());
    assertEquals("password", credentials.getPassword());

    credentials = new Credentials(null, null);
    assertEquals(null, credentials.getUserName());
    assertEquals(null, credentials.getPassword());
  }

  //Test toString to make sure pwd and uid is not outputted
  public void testToString() throws Exception {

    String testString = "Credentials[" +
        "userName=*****," +
        "password=*****]";

    Credentials credentials = new Credentials("uid", "pwd");
    assertEquals(testString, credentials.toString());

    testString = "Credentials[" +
        "userName=<null>," +
        "password=*****]";
    credentials = new Credentials(null, "pwd");
    assertEquals(testString, credentials.toString());

    testString = "Credentials[" +
        "userName=<null>," +
        "password=<null>]";
    credentials = new Credentials(null, null);
    assertEquals(testString, credentials.toString());
  }

  public void testIsEmpty() throws Exception {
    Credentials credentials = new Credentials(null, null);
    assertTrue(credentials.isEmpty());

    credentials = new Credentials("", "");
    assertTrue(credentials.isEmpty());
  }
}
