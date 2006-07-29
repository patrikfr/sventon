package de.berlios.sventon.util;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(AllTests.suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getPackage().getName());
    suite.addTestSuite(ByteFormatterTest.class);
    suite.addTestSuite(ImageScalerTest.class);
    suite.addTestSuite(ImageUtilTest.class);
    suite.addTestSuite(PathUtilTest.class);
    return suite;
  }

}
