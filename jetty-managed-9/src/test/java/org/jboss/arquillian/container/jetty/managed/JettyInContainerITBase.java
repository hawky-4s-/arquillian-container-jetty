package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * Created by chris on 20.08.2015.
 */
public class JettyInContainerITBase {

  protected static final TestDeploymentFactory TEST_DEPLOYMENT_FACTORY = new TestDeploymentFactory();

  @Resource(name = "resourceInjectionTestName")
  protected String resourceInjectionTestValue;

  @Test
  @OperateOnDeployment(TestDeploymentFactory.ROOT_CONTEXT)
  public void shouldBeAbleToInjectMembersIntoTestClassOfRootWebApp(final TestBean testBean) {

    assertInjection(testBean);
  }

  @Test
  @OperateOnDeployment(TestDeploymentFactory.TEST_CONTEXT)
  public void shouldBeAbleToInjectMembersIntoTestClassOfTestWebApp(final TestBean testBean) {

    assertInjection(testBean);
  }

  protected void assertInjection(final TestBean testBean) {

    Assert.assertEquals("Hello World from an evn-entry", this.resourceInjectionTestValue);
    Assert.assertNotNull(testBean);
    Assert.assertEquals("Hello World from an evn-entry", testBean.getName());
  }

}
