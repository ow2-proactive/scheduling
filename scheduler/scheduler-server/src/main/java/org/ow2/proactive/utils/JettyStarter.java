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
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class JettyStarter {

    private static final String FOLDER_TO_DEPLOY = "/dist/war/";
    private static final String REST_CONFIG_PATH = "/config/rest/settings.ini";

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

        if ("true".equals(properties.getProperty("rest.deploy"))) {
            int restPort = Integer.parseInt(properties.getProperty("rest.port", "8080"));

            // for web portals
            setSystemPropertyIfNotDefined("rest.url", "http://localhost:" + restPort + "/rest");
            setSystemPropertyIfNotDefined("sched.rest.url", "http://localhost:" + restPort + "/rest/rest");
            setSystemPropertyIfNotDefined("rm.rest.url", "http://localhost:" + restPort + "/rest/rest");

            Server server = new Server(restPort);
            server.setStopAtShutdown(true);

            HandlerList handlerList = new HandlerList();
            addWarsToHanlderList(handlerList);
            server.setHandler(handlerList);

            startServer(server, restPort);
        }
    }

    private static void startServer(Server server, int restPort) {
        try {
            if (server.getHandler() == null) {
                logger.info("SCHEDULER_HOME/dist/war folder is empty, nothing is deployed");
            } else {
                server.start();
                if (server.isStarted()) {
                    HandlerList handlerList = (HandlerList) server.getHandler();
                    for (Handler handler : handlerList.getHandlers()) {
                        WebAppContext webAppContext = (WebAppContext) handler;
                        Throwable startException = webAppContext.getUnavailableException();
                        if (startException == null) {
                            logger.info("The web application " + webAppContext.getContextPath() +
                                " created on http://localhost:" + restPort + webAppContext.getContextPath());
                        } else {
                            logger.warn("Failed to start context " + webAppContext.getContextPath(),
                                    startException);
                        }
                    }
                } else {
                    logger.warn("Failed to start web modules (REST API, portals)");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to start web modules (REST API, portals)", e);
        }
    }

    private static void addWarsToHanlderList(HandlerList handlerList) {
        File warFolder = new File(getSchedulerHome() + FOLDER_TO_DEPLOY);
        File[] warFolderContent = warFolder.listFiles();
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
}
