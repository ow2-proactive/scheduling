/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.web.WebProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class JettyStarter {

    protected static final String FOLDER_TO_DEPLOY = "/dist/war/";

    protected static final String HTTP_CONNECTOR_NAME = "http";

    protected static final String HTTPS_CONNECTOR_NAME = "https";

    protected static final String REST_CONFIG_PATH = "/config/web/settings.ini";

    private static final Logger logger = Logger.getLogger(JettyStarter.class);

    /**
     * To run Jetty in standalone mode
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(".");

        new JettyStarter().deployWebApplications("", "");
    }

    public List<String> deployWebApplications(String rmUrl, String schedulerUrl) {
        Properties properties = readRestProperties();

        setSystemPropertyIfNotDefined("rm.url", rmUrl);
        setSystemPropertyIfNotDefined("scheduler.url", schedulerUrl);

        if ("true".equalsIgnoreCase(properties.getProperty(WebProperties.WEB_DEPLOY, "true"))) {
            logger.info("Starting the web applications...");

            int httpPort = getJettyHttpPort(properties);
            int httpsPort = Integer.parseInt(properties.getProperty(WebProperties.WEB_HTTPS_PORT, "443"));

            boolean httpsEnabled = isHttpsEnabled(properties);
            boolean redirectHttpToHttps = isHttpToHttpsRedirectionEnabled(properties);

            int restPort = httpPort;

            String httpProtocol;
            String[] defaultVirtualHost;
            String[] httpVirtualHost = new String[] { "@" + HTTP_CONNECTOR_NAME };

            if (httpsEnabled) {
                httpProtocol = "https";
                defaultVirtualHost = new String[] { "@" + HTTPS_CONNECTOR_NAME };
                restPort = httpsPort;
            } else {
                defaultVirtualHost = httpVirtualHost;
                httpProtocol = "http";
            }

            Server server =
                    createHttpServer(
                            properties, httpPort, httpsPort, httpsEnabled, redirectHttpToHttps);

            server.setStopAtShutdown(true);

            HandlerList handlerList = new HandlerList();

            if (httpsEnabled && redirectHttpToHttps) {
                ContextHandler redirectHandler = new ContextHandler();
                redirectHandler.setContextPath("/");
                redirectHandler.setHandler(new SecuredRedirectHandler());
                redirectHandler.setVirtualHosts(httpVirtualHost);
                handlerList.addHandler(redirectHandler);
            }

            addWarsToHandlerList(handlerList, defaultVirtualHost);
            server.setHandler(handlerList);

            String schedulerHost = ProActiveInet.getInstance().getHostname();
            return startServer(server, schedulerHost, restPort, httpProtocol);
        }
        return new ArrayList<>();
    }

    protected int getJettyHttpPort(Properties properties) {
        int result = 8080;

        String property = properties.getProperty(WebProperties.WEB_HTTP_PORT);

        if (property == null) {
            property = properties.getProperty(WebProperties.WEB_PORT);
        }

        if (property != null) {
            result = Integer.parseInt(property);
        }

        return result;
    }

    private boolean isHttpToHttpsRedirectionEnabled(Properties properties) {
        return properties.getProperty(
                WebProperties.WEB_REDIRECT_HTTP_TO_HTTPS, "true").equalsIgnoreCase("true");
    }

    private boolean isHttpsEnabled(Properties properties) {
        return properties.getProperty(WebProperties.WEB_HTTPS, "false").equalsIgnoreCase("true");
    }

    protected Server createHttpServer(
            Properties properties, int httpPort, int httpsPort, boolean httpsEnabled,
            boolean redirectHttpToHttps) {

        int maxThreads = Integer.parseInt(properties.getProperty(WebProperties.WEB_MAX_THREADS, "100"));

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads);
        Server server = new Server(threadPool);

        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendDateHeader(false);
        httpConfiguration.setSendServerVersion(false);

        Connector[] connectors;

        if (httpsEnabled) {
            SslContextFactory sslContextFactory = new SslContextFactory();

            String httpsKeystore = properties.getProperty(WebProperties.WEB_HTTPS_KEYSTORE);
            String httpsKeystorePassword = properties.getProperty(WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD);

            checkPropertyNotNull(WebProperties.WEB_HTTPS_KEYSTORE, httpsKeystore);
            checkPropertyNotNull(WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD, httpsKeystorePassword);

            sslContextFactory.setKeyStorePath(
                    absolutePathOrRelativeToSchedulerHome(
                            httpsKeystore));
            sslContextFactory.setKeyStorePassword(
                    httpsKeystorePassword);

            HttpConfiguration secureHttpConfiguration = new HttpConfiguration(httpConfiguration);
            secureHttpConfiguration.addCustomizer(new SecureRequestCustomizer());
            secureHttpConfiguration.setSecurePort(httpsPort);
            secureHttpConfiguration.setSecureScheme("https");
            secureHttpConfiguration.setSendDateHeader(false);
            secureHttpConfiguration.setSendServerVersion(false);

            // Connector to listen for HTTPS requests
            ServerConnector httpsConnector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString()),
                    new HttpConnectionFactory(secureHttpConfiguration));
            httpsConnector.setName(HTTPS_CONNECTOR_NAME);
            httpsConnector.setPort(httpsPort);

            if (redirectHttpToHttps) {
                // The next two settings allow !403 errors to be redirected to HTTPS
                httpConfiguration.setSecureScheme("https");
                httpConfiguration.setSecurePort(httpsPort);

                // Connector to listen for HTTP requests that are redirected to HTTPS
                ServerConnector httpConnector = createHttpConnector(server, httpConfiguration, httpPort);

                connectors = new Connector[] { httpConnector, httpsConnector };
            } else {
                connectors = new Connector[] { httpsConnector };
            }
        } else {
            ServerConnector httpConnector = createHttpConnector(server, httpConfiguration, httpPort);
            connectors = new Connector[] { httpConnector };
        }

        server.setConnectors(connectors);

        return server;
    }

    private void checkPropertyNotNull(String propertyName, String propertyValue) {
        if (propertyValue == null) {
            logger.error("You need to define property '" + propertyName + "'");
            System.exit(-1);
        }
    }

    private ServerConnector createHttpConnector(
            Server server, HttpConfiguration httpConfiguration, int httpPort) {
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfiguration));
        httpConnector.setName(HTTP_CONNECTOR_NAME);
        httpConnector.setPort(httpPort);
        return httpConnector;
    }

    private List<String> startServer(Server server, String schedulerHost, int restPort, String httpProtocol) {
        try {
            if (server.getHandler() == null) {
                logger.info("SCHEDULER_HOME/dist/war folder is empty, nothing is deployed");
            } else {
                server.start();
                if (server.isStarted()) {
                    return printDeployedApplications(server, schedulerHost, restPort, httpProtocol);
                } else {
                    logger.error("Failed to start web applications");
                    System.exit(1);
                }
            }
        } catch (BindException bindException) {
            logger.error("Failed to start web applications. Port " + restPort +
                    " is already used", bindException);
            System.exit(2);
        } catch (Exception e) {
            logger.error("Failed to start web applications", e);
            System.exit(3);
        }
        return new ArrayList<>();
    }

    private String getApplicationUrl(String httpProtocol, String schedulerHost, int restPort, WebAppContext webAppContext) {
        return httpProtocol + "://" + schedulerHost + ":" + restPort +
                webAppContext.getContextPath();
    }

    private List<String> printDeployedApplications(Server server, String schedulerHost, int restPort,
            String httpProtocol) {
        HandlerList handlerList = (HandlerList) server.getHandler();
        ArrayList<String> applicationsUrls = new ArrayList<>();
        if (handlerList.getHandlers() != null) {
            for (Handler handler : handlerList.getHandlers()) {
                if (!(handler instanceof WebAppContext)) {
                    continue;
                }

                WebAppContext webAppContext = (WebAppContext) handler;
                Throwable startException = webAppContext.getUnavailableException();
                if (startException == null) {
                    if (!"/".equals(webAppContext.getContextPath())) {
                        String applicationUrl = getApplicationUrl(httpProtocol, schedulerHost, restPort, webAppContext);
                        applicationsUrls.add(applicationUrl);
                        logger.info("The web application " + webAppContext.getContextPath() + " created on " +
                                applicationUrl);
                    }
                } else {
                    logger.warn("Failed to start context " + webAppContext.getContextPath(), startException);
                }
            }
            logger.info("*** Get started at " + httpProtocol + "://" + schedulerHost + ":" + restPort +
                    " ***");
        }
        return applicationsUrls;
    }

    private void addWarsToHandlerList(HandlerList handlerList, String[] virtualHost) {
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
                    addExplodedWebApp(handlerList, fileToDeploy, virtualHost);
                } else if (isWarFile(fileToDeploy)) {
                    addWarFile(handlerList, fileToDeploy, virtualHost);
                } else if (isStaticFolder(fileToDeploy)) {
                    addStaticFolder(handlerList, fileToDeploy, virtualHost);
                }
            }
        }

        addGetStartedApplication(handlerList, new File(warFolder, "getstarted"), virtualHost);
    }

    private void addWarFile(HandlerList handlerList, File file, String[] virtualHost) {
        String contextPath = "/" + FilenameUtils.getBaseName(file.getName());
        WebAppContext webApp = createWebAppContext(contextPath, virtualHost);
        webApp.setWar(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using war file " + file);
    }

    private void addExplodedWebApp(HandlerList handlerList, File file, String[] virtualHost) {
        String contextPath = "/" + file.getName();
        WebAppContext webApp = createWebAppContext(contextPath, virtualHost);

        // Don't scan classes for annotations. Saves 1 second at startup.
        webApp.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "^$");
        webApp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", "^$");

        webApp.setDescriptor(new File(file, "/WEB-INF/web.xml").getAbsolutePath());
        webApp.setResourceBase(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using exploded war " + file);
    }

    private void addStaticFolder(HandlerList handlerList, File file, String[] virtualHost) {
        String contextPath = "/" + file.getName();
        WebAppContext webApp = createWebAppContext(contextPath, virtualHost);
        webApp.setWar(file.getAbsolutePath());
        handlerList.addHandler(webApp);
        logger.debug("Deploying " + contextPath + " using folder " + file);
    }

    private void addGetStartedApplication(HandlerList handlerList, File file, String[] virtualHost) {
        if (file.exists()) {
            String contextPath = "/";
            WebAppContext webApp = createWebAppContext(contextPath, virtualHost);
            webApp.setWar(file.getAbsolutePath());
            handlerList.addHandler(webApp);
        }
    }

    private WebAppContext createWebAppContext(String contextPath, String[] virtualHost) {
        WebAppContext webApp = new WebAppContext();
        webApp.setParentLoaderPriority(true);
        webApp.setContextPath(contextPath);
        webApp.setVirtualHosts(virtualHost);
        return webApp;
    }

    private boolean isWarFile(File file) {
        return "war".equals(FilenameUtils.getExtension(file.getName()));
    }

    private boolean isExplodedWebApp(File file) {
        return file.isDirectory() && new File(file, "WEB-INF").exists();
    }

    private boolean isStaticFolder(File file) {
        return file.isDirectory();
    }

    private Properties readRestProperties() {
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

    private String getSchedulerHome() {
        if (PASchedulerProperties.SCHEDULER_HOME.isSet()) {
            return PASchedulerProperties.SCHEDULER_HOME.getValueAsString();
        } else {
            return ".";
        }
    }

    private void setSystemPropertyIfNotDefined(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    private String absolutePathOrRelativeToSchedulerHome(String path) {
        if (new File(path).isAbsolute()) {
            return path;
        } else {
            return new File(getSchedulerHome(), path).getPath();
        }
    }

}
