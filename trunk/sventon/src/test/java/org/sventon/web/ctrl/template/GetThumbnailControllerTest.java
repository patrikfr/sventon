package org.sventon.web.ctrl.template;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.sventon.TestUtils;
import org.sventon.appl.Application;
import org.sventon.appl.ConfigDirectory;
import org.sventon.appl.RepositoryConfiguration;
import org.sventon.cache.CacheException;
import org.sventon.cache.objectcache.ObjectCache;
import org.sventon.cache.objectcache.ObjectCacheImpl;
import org.sventon.cache.objectcache.ObjectCacheManager;
import org.sventon.model.RepositoryName;
import org.sventon.service.RepositoryService;
import org.sventon.util.ImageScaler;
import org.sventon.util.WebUtils;
import org.sventon.web.command.SVNBaseCommand;
import org.tmatesoft.svn.core.io.SVNRepository;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

public class GetThumbnailControllerTest extends TestCase {

  public void testCacheNotUsed() throws Exception {
    final RepositoryService repositoryServiceMock = EasyMock.createMock(RepositoryService.class);

    final RepositoryName repositoryName = new RepositoryName("test");
    final HttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();

    final GetThumbnailController ctrl = new GetThumbnailController();

    final RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration(repositoryName.toString());
    repositoryConfiguration.setCacheUsed(false);

    final ImageScaler imageScaler = new ImageScaler() {
      @Override
      public BufferedImage getThumbnail(BufferedImage image, int maxSize) {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      }
    };

    final ConfigDirectory configDirectory = TestUtils.getTestConfigDirectory();
    configDirectory.setCreateDirectories(false);
    final MockServletContext servletContext = new MockServletContext();
    servletContext.setContextPath("sventon-test");
    configDirectory.setServletContext(servletContext);
    final Application application = new Application(configDirectory, TestUtils.CONFIG_FILE_NAME);
    application.setConfigured(true);
    application.addRepository(repositoryConfiguration);

    final ConfigurableMimeFileTypeMap mftm = new ConfigurableMimeFileTypeMap();
    mftm.afterPropertiesSet();

    ctrl.setApplication(application);
    ctrl.setRepositoryService(repositoryServiceMock);
    ctrl.setMimeFileTypeMap(mftm);
    ctrl.setImageScaler(imageScaler);
    ctrl.setImageFormatName("jpg");

    final SVNBaseCommand command = new SVNBaseCommand();
    command.setName(repositoryName);
    command.setPath("/test/target.jpg");

    repositoryServiceMock.getFile((SVNRepository) EasyMock.isNull(), EasyMock.matches(command.getPath()),
        EasyMock.eq(-1L), (OutputStream) EasyMock.anyObject());

    assertEquals(0, response.getContentAsByteArray().length);

    EasyMock.replay(repositoryServiceMock);
    ctrl.svnHandle(null, command, 100, null, request, response, null);
    EasyMock.verify(repositoryServiceMock);

    assertTrue(((String) response.getHeader(WebUtils.CONTENT_DISPOSITION_HEADER)).contains("target.jpg"));
    assertEquals(622, response.getContentAsByteArray().length);
  }

  public void testCacheUsed() throws Exception {
    final ConfigDirectory configDirectory = TestUtils.getTestConfigDirectory();
    configDirectory.setCreateDirectories(false);
    final MockServletContext servletContext = new MockServletContext();
    servletContext.setContextPath("sventon-test");
    configDirectory.setServletContext(servletContext);

    final RepositoryName repositoryName = new RepositoryName("test");

    final RepositoryService repositoryServiceMock = EasyMock.createMock(RepositoryService.class);

    final ObjectCacheManager objectCacheManager = new ObjectCacheManager(
        configDirectory, 0, false, false, 0, 0, false, 0) {
      @Override
      protected ObjectCache createCache(RepositoryName cacheName) throws CacheException {
        return new ObjectCacheImpl(cacheName.toString(), null, 1000, false, false, 0, 0, false, 0);
      }
    };
    objectCacheManager.register(repositoryName);

    final HttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();

    final GetThumbnailController ctrl = new GetThumbnailController();

    final RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration(repositoryName.toString());
    repositoryConfiguration.setCacheUsed(true);

    final ImageScaler imageScaler = new ImageScaler() {
      @Override
      public BufferedImage getThumbnail(BufferedImage image, int maxSize) {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      }
    };

    final Application application = new Application(configDirectory, TestUtils.CONFIG_FILE_NAME);

    application.setConfigured(true);
    application.addRepository(repositoryConfiguration);

    final ConfigurableMimeFileTypeMap mftm = new ConfigurableMimeFileTypeMap();
    mftm.afterPropertiesSet();

    ctrl.setApplication(application);
    ctrl.setRepositoryService(repositoryServiceMock);
    ctrl.setMimeFileTypeMap(mftm);
    ctrl.setImageScaler(imageScaler);
    ctrl.setImageFormatName("png");
    ctrl.setObjectCacheManager(objectCacheManager);

    final SVNBaseCommand command = new SVNBaseCommand();
    command.setName(repositoryName);
    command.setPath("/test/target.png");

    EasyMock.expect(repositoryServiceMock.getFileChecksum(null, command.getPath(), -1L)).andStubReturn("checksum");

    repositoryServiceMock.getFile((SVNRepository) EasyMock.isNull(), EasyMock.matches(command.getPath()),
        EasyMock.eq(-1L), (OutputStream) EasyMock.anyObject());

    assertEquals(0, response.getContentAsByteArray().length);

    EasyMock.replay(repositoryServiceMock);
    ctrl.svnHandle(null, command, 100, null, request, response, null);
    EasyMock.verify(repositoryServiceMock);

    assertTrue(((String) response.getHeader(WebUtils.CONTENT_DISPOSITION_HEADER)).contains("target.png"));
    assertEquals(68, response.getContentAsByteArray().length);

    EasyMock.reset(repositoryServiceMock);

    // Thumbnail is now cached - verify that it's used

    EasyMock.expect(repositoryServiceMock.getFileChecksum(null, command.getPath(), -1L)).andStubReturn("checksum");

    EasyMock.replay(repositoryServiceMock);
    ctrl.svnHandle(null, command, 100, null, request, response, null);
    EasyMock.verify(repositoryServiceMock);
  }

}