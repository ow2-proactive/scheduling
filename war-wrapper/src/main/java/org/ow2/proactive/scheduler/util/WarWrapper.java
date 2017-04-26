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
package org.ow2.proactive.scheduler.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.scheduler.SchedulerFactory;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.wrapper.WrapperWebConfiguration;


/**
 *  WarWrapper is a class that enables running ProActive as a Web Application. It extends SchedulerStarter
 *  by properly configuring the Scheduler and RM homes inside a Web archive (WAR), and disables running
 *  the jetty server embedded in ProActive (as the WAR is to be deployed in an application server).
 *  It further defines methods to shutdown ProActive (by shutting down the Scheduler and RM).
 *  The methods launchProactive() and shutdownProactive() of WarWrapper are respectively called
 *  by the WrapperContextListener to run ProActive (when the web application context is initialized)
 *  and shut it down (when the web application context is destroyed).
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 7.27.0
 */

public class WarWrapper extends SchedulerStarter {

    private static final Logger logger = Logger.getLogger(WarWrapper.class);

    private static final String SCHEDULER_RM_DEFAULT_HOME = ".";

    private static String schedulerUri;

    private static CommandLine cmd = null;

    /**
     * The main method called when ProActive is deployed as a web or enterprise application (WAR/EAR)
     */
    public void launchProactive() {

        String[] args = { "--no-rest" };

        configureSchedulerAndRMAndPAHomes();
        configureSecurityManager();
        configureLogging();
        configureDerby();

        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        Options options = getOptions();

        try {
            cmd = getCommandLine(args, options);

            start(cmd);

            configureURLs();

        } catch (Exception e) {
            logger.error("Error when starting the scheduler", e);
        }

    }

    /**
     * Configures the Scheduler and RM homes inside a Web archive (WAR)
     */
    protected void configureSchedulerAndRMAndPAHomes() {

        String schedHome = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() +
                           SCHEDULER_RM_DEFAULT_HOME;

        setPropIfNotAlreadySet(PASchedulerProperties.SCHEDULER_HOME.getKey(), schedHome);
        schedHome = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());

        setPropIfNotAlreadySet(PASchedulerProperties.SCHEDULER_HOME.getKey(), schedHome);
        setPropIfNotAlreadySet(PAResourceManagerProperties.RM_HOME.getKey(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_HOME.getName(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                               schedHome + "/config/network/server.ini");
    }

    /**
     * Configures the variables that are traditionally set by Jetty Starter
     */
    private void configureURLs() {
        //
        if (null != rmURL) {
            setPropIfNotAlreadySet("rm.url", rmURL);
        } else {
            logger.warn("System property 'rm.url' not set: ");
        }

        if (null != schedAuthInter) {
            schedulerUri = schedAuthInter.getHostURL();
            setPropIfNotAlreadySet("scheduler.url", schedulerUri);
        } else {
            logger.warn("System property 'scheduler.url' not set");
        }

        String getStartedUrl = WrapperWebConfiguration.getStartedUrl();
        if (null != getStartedUrl) {
            PASchedulerProperties.SCHEDULER_REST_URL.updateProperty(getStartedUrl);
            logger.info("*** Get started at: " + getStartedUrl + " ***");
        } else {
            logger.warn("PA property 'SCHEDULER_REST_URL' not set");
        }

    }

    /**
     * The main method called when stopping or removing ProActive
     */
    public void shutdownProactive() {

        try {

            Credentials credentials = getCredentials();

            if (stopScheduler(credentials)) {
                logger.info("Scheduler on " + schedulerUri + " correctly stopped");
            } else {
                logger.warn("Scheduler on " + schedulerUri + " is not stopped");
            }

            if (stopRM(credentials)) {
                logger.info("RM on " + rmURL + " correctly stopped");
            } else {
                logger.warn("RM on " + rmURL + " is not stopped");
            }

        } catch (KeyException e) {
            logger.error("ERROR while stopping ProActive: Cannot  acquire credentials");
        } catch (Exception e) {
            logger.error("ERROR while stopping ProActive: " + e.getMessage());
        }
    }

    protected boolean stopRM(Credentials credentials) {

        boolean rmStopped = false;

        try {
            RMAuthentication rmAuthentication = connectToRM();
            ResourceManager rm = rmAuthentication.login(credentials);

            //Do not kill Runtime when stopping RM
            PAResourceManagerProperties.RM_SHUTDOWN_KILL_RUNTIME.updateProperty("false");

            BooleanWrapper booleanWrapper = rm.shutdown(true);
            rmStopped = booleanWrapper.getBooleanValue();

        } catch (Exception e) {
            logger.error("ERROR while stopping RM on " + rmURL + ": " + e.getMessage());
        }

        return rmStopped;
    }

    protected boolean stopScheduler(Credentials credentials) {

        boolean schedulerStopped = false;

        try {

            Scheduler scheduler = schedAuthInter.login(credentials);
            schedulerStopped = scheduler.shutdown();

            if (schedulerStopped && scheduler.getStatus().isDown()) {
                SchedulerFactory.setSchedulerStarted(false);
            }

        } catch (Exception e) {
            logger.error("ERROR while stopping to the scheduler on " + schedulerUri + ": " + e.getMessage());
        }

        return schedulerStopped;
    }

    private static RMAuthentication connectToRM() throws URISyntaxException, RMException {

        RMAuthentication rmAuthentication = null;

        if (rmURL != null) {
            rmAuthentication = SchedulerFactory.waitAndJoinRM(new URI(rmURL));
        }

        return rmAuthentication;
    }

    protected Credentials getCredentials() throws KeyException {
        return Credentials.getCredentialsBase64(credentials);
    }

}
