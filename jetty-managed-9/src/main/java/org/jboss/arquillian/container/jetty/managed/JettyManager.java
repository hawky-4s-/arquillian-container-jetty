package org.jboss.arquillian.container.jetty.managed;

/**
 * Created by chris on 20.08.2015.
 */
public class JettyManager<T> {

  private boolean running;

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }
}
