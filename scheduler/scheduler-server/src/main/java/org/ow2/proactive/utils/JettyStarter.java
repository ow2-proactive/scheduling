/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;


public class JettyStarter {

    private static final String FOLDER_TO_DEPLOY = "/dist/war/";
    private static final String REST_CONFIG_PATH = "/config/web/settings.ini";

    private static Logger logger = Logger.getLogger(JettyStarter.class);

    /**
     * To run Jetty in standalone mode
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(".");
        JettyStarter.runWars("", "");
    }

    public static void runWars(String rmUrl, String schedulerUrl) {
        Properties properties = readRestProperties();

        setSystemPropertyIfNotDefined("rm.url", rmUrl);
        setSystemPropertyIfNotDefined("scheduler.url", schedulerUrl);

        if ("true".equals(properties.getProperty("web.deploy", "true"))) {
            logger.info("Starting the web applications...");
            int restPort = Integer.parseInt(properties.getProperty("web.port", "8080"));
            boolean httpsEnabled = Boolean.parseBoolean(properties.getProperty("web.https", "false"));
            String httpProtocol = httpsEnabled ? "https" : "http";

            // for web portals
            String defaultRestUrl = httpProtocol + "://localhost:" + restPort + "/rest";
            setSystemPropertyIfNotDefined("rest.url", defaultRestUrl);
            setSystemPropertyIfNotDefined("sched.rest.url", defaultRestUrl);
            setSystemPropertyIfNotDefined("rm.rest.url", defaultRestUrl);

            Server server = createHttpServer(properties, restPort, httpsEnabled);
            server.setStopAtShutdown(true);

            HandlerList handlerList = new HandlerList();
            addWarsToHandlerList(handlerList);
            server.setHandler(handlerList);

            String schedulerHost = ProActiveInet.getInstance().getHostname();
            startServer(server, schedulerHost, restPort, httpProtocol);
        }
    }

    private static Server createHttpServer(Properties properties, int restPort, boolean httpsEnabled) {
        Server server = new Server();
        if (httpsEnabled) {
            SslContextFactory httpsConfiguration = new SslContextFactory();
            httpsConfiguration.setKeyStorePath(absolutePathOrRelativeToSchedulerHome(properties
              .getProperty("web.https.keystore")));
            httpsConfiguration.setKeyStorePassword(properties.getProperty("web.https.keystore.password"));
            SslSelectChannelConnector ssl = new SslSelectChannelConnector(httpsConfiguration);
            ssl.setPort(restPort);
            server.addConnector(ssl);
        } else {
            SelectChannelConnector http = new SelectChannelConnector();
            http.setPort(restPort);
            server.addConnector(http);
        }
        return server;
    }

    private static void startServer(Server server, String schedulerHost, int restPort, String httpProtocol) {
        try {
            if (server.getHandler() == null) {
                logger.info("SCHEDULER_HOME/dist/war folder is empty, nothing is deployed");
            } else {
                server.start();
                if (server.isStarted()) {
                    printDeployedApplications(server, schedulerHost, restPort, httpProtocol);
                } else {
                    logger.error("Failed to start web modules (REST API, portals)");
                    System.exit(-1);
                }
            }
        } catch (BindException bindException) {
            logger.error(
              "Failed to start web modules (REST API, portals), port " + restPort + " is already used",
              bindException);
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Failed to start web modules (REST API, portals)", e);
            System.exit(-1);
        }
    }

    private static void printDeployedApplications(Server server, String schedulerHost, int restPort,
      String httpProtocol) {
        HandlerList handlerList = (HandlerList) server.getHandler();
        if (handlerList.getHandlers() != null) {
            for (Handler handler : handlerList.getHandlers()) {
                WebAppContext webAppContext = (WebAppContext) handler;
                Throwable startException = webAppContext.getUnavailableException();
                if (startException == null) {
                    if (!"/".equals(webAppContext.getContextPath())) {
                        logger.info("The web application " + webAppContext.getContextPath() +
                          " created on " + httpProtocol + "://" + schedulerHost + ":" + restPort +
                          webAppContext.getContextPath());
                    }
                } else {
                    logger.warn("Failed to start context " + webAppContext.getContextPath(),
                      startException);
                }
            }
            logger.info(
              "*** Get started at " + httpProtocol + "://" + schedulerHost + ":" + restPort + " ***");
        }
    }

    private static void addWarsToHandlerList(HandlerList handlerList) {
        File warFolder = new File(getSchedulerHome() + FOLDER_TO_DEPLOY);
        File[] warFolderContent = warFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !"getstarted".equals(name);
            }
        });
        if (warFolderContent != null) {
            for (File fileToDeploy : warFolderContent) {
                if (isExplodedWebApp(fileToDeploy)) {
                    addExplodedWebApp(handlerList, fileToDeploy);
                } else if (isWarFile(fileToDeploy)) {
                    addWarFile(handlerList, fileToDeploy);
                } else if (isStaticFolder(fileToDeploy)) {
                    addStaticFolder(handlerList, fileToDeploy);
                }
            }
        }
        addGetStartedApplication(handlerList, new File(warFolder, "getstarted"));
    }

    private static void addWarFile(HandlerList handlerList, File file) {
        String contextPath = "/" + FilenameUtils.getBaseName(file.getName());
        WebAppContext webApp = createWebAppContext(contextPath);
        webApp.setWar(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using war file " + file);
    }

    private static void addExplodedWebApp(HandlerList handlerList, File file) {
        String contextPath = "/" + file.getName();
        WebAppContext webApp = createWebAppContext(contextPath);
        webApp.setDescriptor(new File(file, "/WEB-INF/web.xml").getAbsolutePath());
        webApp.setResourceBase(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using exploded war " + file);
    }

    private static void addStaticFolder(HandlerList handlerList, File file) {
        String contextPath = "/" + file.getName();
        WebAppContext webApp = createWebAppContext(contextPath);
        webApp.setWar(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using folder " + file);
    }

    private static void addGetStartedApplication(HandlerList handlerList, File file) {
        if (file.exists()) {
            String contextPath = "/";
            WebAppContext webApp = createWebAppContext(contextPath);
            webApp.setWar(file.getAbsolutePath());
            handlerList.addHandler(webApp);
        }
    }

    private static WebAppContext createWebAppContext(String contextPath) {
        WebAppContext webApp = new WebAppContext();
        webApp.setParentLoaderPriority(true);
        webApp.setContextPath(contextPath);
        return webApp;
    }

    private static boolean isWarFile(File file) {
        return "war".equals(FilenameUtils.getExtension(file.getName()));
    }

    private static boolean isExplodedWebApp(File file) {
        return file.isDirectory() && new File(file, "WEB-INF").exists();
    }

    private static boolean isStaticFolder(File file) {
        return file.isDirectory();
    }

    private static Properties readRestProperties() {
        File restPropertiesFile = new File(getSchedulerHome() + REST_CONFIG_PATH);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(restPropertiesFile));
        } catch (IOException e) {
            logger.warn("Could not find REST properties" + restPropertiesFile, e);
        }
        properties.putAll(System.getProperties());
        return properties;
    }

    private static String getSchedulerHome() {
        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
        } else {
            return ".";
        }
    }

    private static void setSystemPropertyIfNotDefined(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String absolutePathOrRelativeToSchedulerHome(String path) {
        if (new File(path).isAbsolute()) {
            return path;
        } else {
            return new File(getSchedulerHome(), path).getPath();
        }
    }

    private static String getSchedulerHost(String schedulerUrl) {
        try {
            return new URI(schedulerUrl).getHost();
        } catch (URISyntaxException e) {
            logger.warn("Could not read host from Scheduler's URL", e);
            return "localhost";
        }
    }
}
