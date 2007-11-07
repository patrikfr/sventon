package de.berlios.sventon;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getPackage().getName());
    suite.addTest(de.berlios.sventon.config.AllTests.suite());
    suite.addTest(de.berlios.sventon.colorer.AllTests.suite());
    suite.addTest(de.berlios.sventon.content.AllTests.suite());
    suite.addTest(de.berlios.sventon.web.command.AllTests.suite());
    suite.addTest(de.berlios.sventon.web.ctrl.AllTests.suite());
    suite.addTest(de.berlios.sventon.web.ctrl.xml.AllTests.suite());
    suite.addTest(de.berlios.sventon.web.model.AllTests.suite());
    suite.addTest(de.berlios.sventon.web.support.AllTests.suite());
    suite.addTest(de.berlios.sventon.diff.AllTests.suite());
    suite.addTest(de.berlios.sventon.service.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.cache.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.cache.entrycache.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.cache.logmessagecache.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.cache.revisioncache.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.cache.objectcache.AllTests.suite());
    suite.addTest(de.berlios.sventon.repository.export.AllTests.suite());
    suite.addTest(de.berlios.sventon.rss.AllTests.suite());
    suite.addTest(de.berlios.sventon.util.AllTests.suite());
    return suite;
  }

}