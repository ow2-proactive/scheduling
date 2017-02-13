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

import static org.ow2.proactive.utils.ClasspathUtils.findSchedulerHome;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pamr.router.Main;


public class PAMRRouterStarter {

    public static final String PATH_TO_ROUTER_CONFIG_FILE = File.separator + "config" + File.separator + "router" +
                                                            File.separator + "router.ini";

    public static void main(String[] args) throws IOException {
        configureLogging();
        Main.mainWithDefaults(args, defaultConfigurationFile());
    }

    private static String defaultConfigurationFile() {
        return findSchedulerHome() + PATH_TO_ROUTER_CONFIG_FILE;
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String schedulerHomeFromJarOrCurrentFolder = findSchedulerHome();
            String defaultLog4jConfig = schedulerHomeFromJarOrCurrentFolder + "/config/log/router.properties";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig);
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), schedulerHomeFromJarOrCurrentFolder);
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

}
