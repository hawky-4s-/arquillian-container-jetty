package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * Created by chris on 20.08.2015.
 */
@RunWith(Arquillian.class)
public class Jetty9ManagedInContainerIT extends JettyInContainerITBase {

  @Deployment(name = TestDeploymentFactory.ROOT_CONTEXT)
  public static WebArchive createRootDeployment() {

    return TEST_DEPLOYMENT_FACTORY.createWebAppInContainerDeployment(TestDeploymentFactory.ROOT_CONTEXT, TestDeploymentFactory.SERVLET_3_0);
  }

  @Deployment(name = TestDeploymentFactory.TEST_CONTEXT)
  public static WebArchive createTestDeployment() {

    return TEST_DEPLOYMENT_FACTORY.createWebAppInContainerDeployment(TestDeploymentFactory.TEST_CONTEXT, TestDeploymentFactory.SERVLET_3_0);
  }

}
