package de.berlios.sventon.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(AllTests.suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for de.berlios.sventon.util");
    suite.addTestSuite(ByteFormatterTest.class);
    suite.addTestSuite(ImageUtilTest.class);
    suite.addTestSuite(PathUtilTest.class);
    suite.addTestSuite(ZipUtilTest.class);
    return suite;
  }

}
