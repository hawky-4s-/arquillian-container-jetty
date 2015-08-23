package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Created by chris on 20.08.2015.
 */
public class Jetty9ManagedExtension implements LoadableExtension {

  @Override
  public void register(ExtensionBuilder extensionBuilder) {
    extensionBuilder.service(DeployableContainer.class, Jetty9ManagedContainer.class);
  }

}
