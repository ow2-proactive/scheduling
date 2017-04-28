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

import org.apache.commons.cli.*;
import org.apache.log4j.*;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.wrapper.WrapperWebConfiguration;


/**
 * Created by root on 11/03/17.
 */
public class WarWrapper extends SchedulerStarter {

    private static Logger logger = Logger.getLogger(WarWrapper.class);

    public static void main(String[] args) {
        new WarWrapper().launchProactiveServer();
    }

    public void launchProactiveServer() {

        String[] args = { "--no-rest" };

        configureSchedulerAndRMAndPAHomes();
        configureSecurityManager();
        configureLogging();
        configureDerby();

        args = JVMPropertiesPreloader.overrideJVMProperties(args);

        Options options = getOptions();

        try {
            CommandLine cmd = getCommandLine(args, options);

            start(cmd);

            // the variable set by Jetty Starter
            if (null != rmURL)
                setPropIfNotAlreadySet("rm.url", rmURL);
            else
                logger.warn("System property 'rm.url' not set: ");

            if (null != schedAuthInter)
                setPropIfNotAlreadySet("scheduler.url", schedAuthInter.getHostURL());
            else
                logger.warn("System property 'scheduler.url' not set");

            String getStartedUrl = WrapperWebConfiguration.getStartedUrl();
            if (null != getStartedUrl) {
                PASchedulerProperties.SCHEDULER_REST_URL.updateProperty(getStartedUrl);
                logger.info("*** Get started at: " + getStartedUrl + " ***");
            } else
                logger.warn("PA property 'SCHEDULER_REST_URL' not set");

        } catch (Exception e) {
            logger.error("Error when starting the scheduler", e);
        }

    }

    protected void configureSchedulerAndRMAndPAHomes() {

        String schedHome = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + ".";

        setPropIfNotAlreadySet(PASchedulerProperties.SCHEDULER_HOME.getKey(), schedHome);
        schedHome = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());

        setPropIfNotAlreadySet(PASchedulerProperties.SCHEDULER_HOME.getKey(), schedHome);
        setPropIfNotAlreadySet(PAResourceManagerProperties.RM_HOME.getKey(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_HOME.getName(), schedHome);
        setPropIfNotAlreadySet(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                               schedHome + "/config/network/server.ini");
    }
}
