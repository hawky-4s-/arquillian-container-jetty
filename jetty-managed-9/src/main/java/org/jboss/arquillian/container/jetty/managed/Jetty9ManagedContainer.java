package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;

/**
 * Created by chris on 20.08.2015.
 */
public class Jetty9ManagedContainer extends JettyManagedContainer {

  public Jetty9ManagedContainer() {
    super(new ProtocolDescription("Servlet 3.0"));
  }

}
