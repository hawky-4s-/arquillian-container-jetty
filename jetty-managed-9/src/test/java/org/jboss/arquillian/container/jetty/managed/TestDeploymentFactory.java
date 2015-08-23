package org.jboss.arquillian.container.jetty.managed;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Created by chris on 20.08.2015.
 */
public class TestDeploymentFactory {

  public static final String TEST_SERVLET_PATH = "/Test";

  public static final Class<?> TEST_SERVLET_CLASS = TestServlet.class;

  public static final String TEST_SERVLET_CLASS_NAME = TEST_SERVLET_CLASS.getName();

  public static final String TEST_SERVLET_NAME = TEST_SERVLET_CLASS.getSimpleName();

  public static final String TEST_WELCOME_FILE = "index.jsp";

  public static final String SERVLET_2_4 = "2.4";

  public static final String SERVLET_2_5 = "2.5";

  public static final String SERVLET_3_0 = "3.0";

  public static final String ROOT_CONTEXT = "ROOT";

  public static final String TEST_CONTEXT = "test";

  public WebArchive createWebAppClientDeployment(final String contextRoot, final String webAppVersion) {

    final String archiveName = getArchiveName(contextRoot);

    final WebArchive war =
        ShrinkWrap.create(WebArchive.class, archiveName).addClass(TestServlet.class).addAsResource("logging.properties")
            .addAsWebResource(TEST_WELCOME_FILE).setWebXML("web-" + webAppVersion + ".xml");

    return war;
  }

  public WebArchive createWebAppInContainerDeployment(final String contextRoot, final String webAppVersion) {

    final String archiveName = getArchiveName(contextRoot);

    final WebArchive war =
        ShrinkWrap
            .create(WebArchive.class, archiveName)
            .addClasses(TestServlet.class, TestBean.class, JettyInContainerITBase.class, this.getClass())
            .addAsResource("logging.properties")
//            .addAsLibraries(
//                Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
//                    .resolve("org.jboss.weld.servlet:weld-servlet").withTransitivity().asFile())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").setWebXML("in-container-web-" + webAppVersion + ".xml");

    return war;
  }

  private String getArchiveName(final String contextRoot) {

    final String archiveName = contextRoot + ".war";

    return archiveName;
  }

}
