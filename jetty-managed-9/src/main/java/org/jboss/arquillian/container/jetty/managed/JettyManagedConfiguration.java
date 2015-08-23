package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.jetty.Validate;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Created by chris on 20.08.2015.
 */
public class JettyManagedConfiguration implements ContainerConfiguration {

  static final String JAVA_HOME_SYSTEM_PROPERTY = "java.home";

  private boolean outputToConsole = true;

  private String catalinaHome = System.getenv("JETTY_HOME");

  private String catalinaBase = System.getenv("JETTY_BASE");

  private String javaHome = System.getProperty(JAVA_HOME_SYSTEM_PROPERTY);

  private String javaVmArguments = "-Xmx512m -XX:MaxPermSize=128m";

  private int startupTimeoutInSeconds = 120;

  private int shutdownTimeoutInSeconds = 45;

  private String workDir = null;

  private String serverConfig = "server.xml";

  private String loggingProperties = "logging.properties";

  private int jmxPort = 8089;

  @Override
  public void validate() throws ConfigurationException {

//    super.validate();

    Validate.configurationDirectoryExists(catalinaHome,
        "Either CATALINA_HOME environment variable or catalinaHome property in Arquillian configuration "
            + "must be set and point to a valid directory! " + catalinaHome + " is not valid directory!");

    Validate.configurationDirectoryExists(javaHome,
        "Either \"java.home\" system property or javaHome property in Arquillian configuration "
            + "must be set and point to a valid directory! " + javaHome + " is not valid directory!");

    Validate.isValidFile(getJettyBase() + "/conf/" + serverConfig,
        "The server configuration file denoted by serverConfig property has to exist! This file: " + getJettyBase()
            + "/conf/" + serverConfig + " does not!");

    // set write output to console
    this.setOutputToConsole(AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

      @Override
      public Boolean run() {

        // By default, redirect to stdout unless disabled by this property
        final String val = System.getProperty("org.apache.tomcat.writeconsole");
        return val == null || !"false".equals(val);
      }
    }));

  }

  public String getJettyHome() {

    return catalinaHome;
  }

  public void setCatalinaHome(final String catalinaHome) {

    this.catalinaHome = catalinaHome;
  }

  public String getJettyBase() {

    if (catalinaBase == null || "".equals(catalinaBase)) {
      return catalinaHome;
    } else {
      return catalinaBase;
    }
  }

  public void setCatalinaBase(final String catalinaBase) {

    this.catalinaBase = catalinaBase;
  }

  public String getJavaHome() {

    return javaHome;
  }

  public void setJavaHome(final String javaHome) {

    this.javaHome = javaHome;
  }

  public String getJavaVmArguments() {

    return javaVmArguments;
  }

  /**
   * This will override the default ("-Xmx512m -XX:MaxPermSize=128m") startup JVM arguments.
   *
   * @param javaVmArguments use as start up arguments
   */
  public void setJavaVmArguments(final String javaVmArguments) {

    this.javaVmArguments = javaVmArguments;
  }

  public int getStartupTimeoutInSeconds() {

    return startupTimeoutInSeconds;
  }

  public void setStartupTimeoutInSeconds(final int startupTimeoutInSeconds) {

    this.startupTimeoutInSeconds = startupTimeoutInSeconds;
  }

  public int getShutdownTimeoutInSeconds() {

    return shutdownTimeoutInSeconds;
  }

  public void setShutdownTimeoutInSeconds(final int shutdownTimeoutInSeconds) {

    this.shutdownTimeoutInSeconds = shutdownTimeoutInSeconds;
  }

  public String getWorkDir() {

    return workDir;
  }

  /**
   * @param workDir the directory where the compiled JSP files and session serialization data is stored
   */
  public void setWorkDir(final String workDir) {

    this.workDir = workDir;
  }

  public String getServerConfig() {

    return serverConfig;
  }

  public void setServerConfig(final String serverConfig) {

    this.serverConfig = serverConfig;
  }

  /**
   * @return the loggingProperties
   */
  public String getLoggingProperties() {

    return loggingProperties;
  }

  /**
   * @param loggingProperties the loggingProperties to set
   */
  public void setLoggingProperties(final String loggingProperties) {

    this.loggingProperties = loggingProperties;
  }

  /**
   * @param outputToConsole the outputToConsole to set
   */
  public void setOutputToConsole(final boolean outputToConsole) {

    this.outputToConsole = outputToConsole;
  }

  /**
   * @return the outputToConsole
   */
  public boolean isOutputToConsole() {

    return outputToConsole;
  }

  public String getJmxPort() {
    return jmxPort;
  }

  public void setJmxPort(String jmxPort) {
    this.jmxPort = jmxPort;
  }

}
