/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.web.WebProperties;


public class JettyStarter {

    protected static final String FOLDER_TO_DEPLOY = "/dist/war/";

    public static final String HTTP_CONNECTOR_NAME = "http";

    public static final String HTTPS_CONNECTOR_NAME = "https";

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
        initializeRestProperties();

        setSystemPropertyIfNotDefined("rm.url", rmUrl);
        setSystemPropertyIfNotDefined("scheduler.url", schedulerUrl);

        if (WebProperties.WEB_DEPLOY.getValueAsBoolean()) {
            logger.info("Starting the web applications...");

            int httpPort = getJettyHttpPort();
            int httpsPort = 443;
            if (WebProperties.WEB_HTTPS_PORT.isSet()) {
                httpsPort = WebProperties.WEB_HTTPS_PORT.getValueAsInt();
            }

            boolean httpsEnabled = WebProperties.WEB_HTTPS.getValueAsBoolean();
            boolean redirectHttpToHttps = WebProperties.WEB_REDIRECT_HTTP_TO_HTTPS.getValueAsBoolean();

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

            Server server = createHttpServer(httpPort, httpsPort, httpsEnabled, redirectHttpToHttps);

            server.setStopAtShutdown(true);

            HandlerList handlerList = new HandlerList();

            if (WebProperties.JETTY_LOG_FILE.isSet()) {
                NCSARequestLog requestLog = new NCSARequestLog(WebProperties.JETTY_LOG_FILE.getValueAsString());
                requestLog.setAppend(true);
                requestLog.setExtended(false);
                requestLog.setLogTimeZone("GMT");
                requestLog.setLogLatency(true);
                requestLog.setRetainDays(90);

                RequestLogHandler requestLogHandler = new RequestLogHandler();
                requestLogHandler.setRequestLog(requestLog);
                handlerList.addHandler(requestLogHandler);
            }


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

    public int getJettyHttpPort() {

        if (WebProperties.WEB_HTTP_PORT.isSet()) {
            return WebProperties.WEB_HTTP_PORT.getValueAsInt();
        }

        return 8080;
    }

    public Server createHttpServer(int httpPort, int httpsPort, boolean httpsEnabled, boolean redirectHttpToHttps) {

        int maxThreads = 100;
        if (WebProperties.WEB_MAX_THREADS.isSet()) {
            maxThreads = WebProperties.WEB_MAX_THREADS.getValueAsInt();
        }

        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads);
        Server server = new Server(threadPool);

        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSendDateHeader(false);
        httpConfiguration.setSendServerVersion(false);

        Connector[] connectors;

        if (httpsEnabled) {
            SslContextFactory sslContextFactory = new SslContextFactory();

            String httpsKeystore = WebProperties.WEB_HTTPS_KEYSTORE.getValueAsStringOrNull();
            String httpsKeystorePassword = WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD.getValueAsStringOrNull();

            checkPropertyNotNull(WebProperties.WEB_HTTPS_KEYSTORE.getKey(), httpsKeystore);
            checkPropertyNotNull(WebProperties.WEB_HTTPS_KEYSTORE_PASSWORD.getKey(), httpsKeystorePassword);

            sslContextFactory.setKeyStorePath(absolutePathOrRelativeToSchedulerHome(httpsKeystore));
            sslContextFactory.setKeyStorePassword(httpsKeystorePassword);

            if (WebProperties.WEB_HTTPS_TRUSTSTORE.isSet() && WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.isSet()) {
                String httpsTrustStore = WebProperties.WEB_HTTPS_TRUSTSTORE.getValueAsString();
                String httpsTrustStorePassword = WebProperties.WEB_HTTPS_TRUSTSTORE_PASSWORD.getValueAsString();
                sslContextFactory.setTrustStorePath(httpsTrustStore);
                sslContextFactory.setTrustStorePassword(httpsTrustStorePassword);
            }

            HttpConfiguration secureHttpConfiguration = new HttpConfiguration(httpConfiguration);
            secureHttpConfiguration.addCustomizer(new SecureRequestCustomizer());
            secureHttpConfiguration.setSecurePort(httpsPort);
            secureHttpConfiguration.setSecureScheme("https");
            secureHttpConfiguration.setSendDateHeader(false);
            secureHttpConfiguration.setSendServerVersion(false);

            // Connector to listen for HTTPS requests
            ServerConnector httpsConnector = new ServerConnector(server,
                                                                 new SslConnectionFactory(sslContextFactory,
                                                                                          HttpVersion.HTTP_1_1.toString()),
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

    private ServerConnector createHttpConnector(Server server, HttpConfiguration httpConfiguration, int httpPort) {
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
            logger.error("Failed to start web applications. Port " + restPort + " is already used", bindException);
            System.exit(2);
        } catch (Exception e) {
            logger.error("Failed to start web applications", e);
            System.exit(3);
        }
        return new ArrayList<>();
    }

    private String getApplicationUrl(String httpProtocol, String schedulerHost, int restPort,
            WebAppContext webAppContext) {
        return httpProtocol + "://" + schedulerHost + ":" + restPort + webAppContext.getContextPath();
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
            logger.info("*** Get started at " + httpProtocol + "://" + schedulerHost + ":" + restPort + " ***");
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
        // The following setting allows to avoid conflicts between server jackson jars and individual war jackson versions.
        webApp.addServerClass("com.fasterxml.jackson.");
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

    private void initializeRestProperties() {
        System.setProperty(WebProperties.REST_HOME.getKey(), getSchedulerHome());
        WebProperties.load();
        if (!getSchedulerHome().equals(WebProperties.REST_HOME.getValueAsString())) {
            throw new IllegalStateException("Rest home directory could not be initialized");
        }
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
