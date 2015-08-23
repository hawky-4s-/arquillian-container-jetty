package org.jboss.arquillian.container.jetty.managed;

import org.jboss.arquillian.container.jetty.Validate;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by chris on 20.08.2015.
 */
public class JettyManagedContainer implements DeployableContainer<JettyManagedConfiguration> {

  private static final Logger log = Logger.getLogger(JettyManagedContainer.class.getName());

  private final ProtocolDescription protocolDescription;

  public JettyManagedContainer(ProtocolDescription protocolDescription) {
    this.protocolDescription = protocolDescription;
  }

  /**
   * Tomcat container configuration
   */
  private JettyManagedConfiguration configuration;

  private JettyManager<? extends JettyManagedContainer> manager;

  private Thread shutdownThread;

  private Process startupProcess;

  @Override
  public Class<JettyManagedConfiguration> getConfigurationClass() {
    return JettyManagedConfiguration.class;
  }

  @Override
  public ProtocolDescription getDefaultProtocol() {
    return protocolDescription;
  }

  @Override
  public void setup(JettyManagedConfiguration configuration) {
    this.configuration = configuration;
//    this.manager = new TomcatManager<JettyManagedContainer>(configuration, tomcatManagerCommandSpec);
  }

  @Override
  public void start() throws LifecycleException {

    if (manager.isRunning()) {
      throw new LifecycleException("The server is already running! "
          + "Managed containers does not support connecting to running server instances due to the "
          + "possible harmful effect of connecting to the wrong server. Please stop server before running or "
          + "change to another type of container.\n"
          + "To disable this check and allow Arquillian to connect to a running server, "
          + "set allowConnectingToRunningServer to true in the container configuration");
    }

    try {
      final String JETTY_HOME = configuration.getJettyHome();
      final String JETTY_BASE = configuration.getJettyBase();
      final String ADDITIONAL_JAVA_OPTS = configuration.getJavaVmArguments();

      final String absoluteJettyHomePath = new File(JETTY_HOME).getAbsolutePath();
      final String absoluteJettyBasePath = new File(JETTY_BASE).getAbsolutePath();

      final String javaCommand = getJavaCommand();

      // construct a command to execute
      final List<String> cmd = new ArrayList<String>();

      cmd.add(javaCommand);

      cmd.add("-Djava.util.logging.config.file=" + absoluteJettyBasePath + "/conf/"
          + configuration.getLoggingProperties());
      cmd.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");

      cmd.add("-Dcom.sun.management.jmxremote.port=" + configuration.getJmxPort());
      cmd.add("-Dcom.sun.management.jmxremote.ssl=false");
      cmd.add("-Dcom.sun.management.jmxremote.authenticate=false");

      cmd.addAll(AdditionalJavaOptionsParser.parse(ADDITIONAL_JAVA_OPTS));

      String CLASS_PATH = absoluteJettyHomePath + "/bin/bootstrap.jar" + System.getProperty("path.separator");
      CLASS_PATH += absoluteJettyHomePath + "/bin/tomcat-juli.jar";

      cmd.add("-classpath");
      cmd.add(CLASS_PATH);
      cmd.add("-Djava.endorsed.dirs=" + absoluteJettyHomePath + "/endorsed");
      cmd.add("-Dcatalina.base=" + absoluteJettyBasePath);
      cmd.add("-Dcatalina.home=" + absoluteJettyHomePath);
      cmd.add("-Djava.io.tmpdir=" + absoluteJettyBasePath + "/temp");
      cmd.add("org.apache.catalina.startup.Bootstrap");
      cmd.add("-config");
      cmd.add(absoluteJettyBasePath + "/conf/" + configuration.getServerConfig());
      cmd.add("start");

      // execute command
      final ProcessBuilder startupProcessBuilder = new ProcessBuilder(cmd);
      startupProcessBuilder.redirectErrorStream(true);
      startupProcessBuilder.directory(new File(configuration.getJettyHome() + "/bin"));
      log.info("Starting Tomcat with: " + cmd.toString());
      startupProcess = startupProcessBuilder.start();
      new Thread(new ConsoleConsumer(configuration.isOutputToConsole())).start();
      final Process proc = startupProcess;

      shutdownThread = new Thread(new Runnable() {

        @Override
        public void run() {

          if (proc != null) {
            proc.destroy();
            try {
              proc.waitFor();
            } catch (final InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }
      });
      Runtime.getRuntime().addShutdownHook(shutdownThread);

      final long startupTimeout = configuration.getStartupTimeoutInSeconds();
      long timeout = startupTimeout * 1000;
      boolean serverAvailable = false;
      while (timeout > 0 && serverAvailable == false) {
        serverAvailable = manager.isRunning();
        if (!serverAvailable) {
          Thread.sleep(100);
          timeout -= 100;
        }
      }
      if (!serverAvailable) {
        destroystartupProcess();
        throw new TimeoutException(String.format("Managed server was not started within [%d] s", startupTimeout));
      }

    } catch (final Exception ex) {

      throw new LifecycleException("Could not start container", ex);
    }

  }

  @Override
  public void stop() throws LifecycleException {

    if (shutdownThread != null) {
      Runtime.getRuntime().removeShutdownHook(shutdownThread);
      shutdownThread = null;
    }
    try {
      if (startupProcess != null) {
        startupProcess.destroy();
        startupProcess.waitFor();
        startupProcess = null;
      }
    } catch (final Exception e) {
      throw new LifecycleException("Could not stop container", e);
    }
  }

  /**
   * Deploys to remote Tomcat using it's /manager web-app's org.apache.catalina.manager.ManagerServlet.
   *
   * @param archive
   * @return
   * @throws org.jboss.arquillian.container.spi.client.container.DeploymentException
   */
  @Override
  public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {

    Validate.notNull(archive, "Archive must not be null");

    final String archiveName = manager.normalizeArchiveName(archive.getName());
    final URL archiveURL = ShrinkWrapUtil.toURL(archive);
    try {
      manager.deploy("/" + archiveName, archiveURL);
    } catch (final IOException e) {
      throw new DeploymentException("Unable to deploy an archive " + archive.getName(), e);
    }

    final ProtocolMetadataParser<JettyManagedContainer> parser =
        new ProtocolMetadataParser<JettyManagedContainer>(configuration);
    return parser.retrieveContextServletInfo(archiveName);
  }

  @Override
  public void undeploy(final Archive<?> archive) throws DeploymentException {

    Validate.notNull(archive, "Archive must not be null");

    final String archiveName = manager.normalizeArchiveName(archive.getName());
    try {
      manager.undeploy("/" + archiveName);
    } catch (final IOException e) {
      throw new DeploymentException("Unable to undeploy an archive " + archive.getName(), e);
    }
  }

  @Override
  public void deploy(final Descriptor descriptor) throws DeploymentException {

    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void undeploy(final Descriptor descriptor) throws DeploymentException {

    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Runnable that consumes the output of the startupProcess. If nothing consumes the output the AS will hang on some
   * platforms
   *
   * @author Stuart Douglas
   */
  private class ConsoleConsumer implements Runnable {

    private final boolean writeOutput;

    ConsoleConsumer(final boolean writeOutput) {

      this.writeOutput = writeOutput;
    }

    @Override
    public void run() {

      final InputStream stream = startupProcess.getInputStream();
      final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      String line = null;
      try {
        while ((line = reader.readLine()) != null) {
          if (writeOutput) {
            System.out.println(line);
          }
        }
      } catch (final IOException e) {
      }
    }

  }

  private int destroystartupProcess() {

    if (startupProcess == null)
      return 0;
    startupProcess.destroy();
    try {
      return startupProcess.waitFor();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  String getJavaCommand() {

    if (configuration == null) {
      throw new IllegalStateException("setup not called");
    }

    return configuration.getJavaHome() + File.separator + "bin" + File.separator + "java";
  }
}
