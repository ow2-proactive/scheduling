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
package org.ow2.tests;

import java.security.Policy;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * Just to set the security manager without a system property in the command
 * line.
 */
public class ProActiveTest {

    static {
        configureSecurityManager();
        configurePAHome();
        configureLog4j();
    }

    private static void configureLog4j() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    private static void configureSecurityManager() {
        if (System.getProperty("java.security.policy") == null) {
            System.setProperty("java.security.policy",
                               System.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) +
                                                       "/config/security.java.policy-server");

            Policy.getPolicy().refresh();
        }
    }

    private static void configurePAHome() {
        String rmHome = System.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
        if (System.getProperty(CentralPAPropertyRepository.PA_HOME.getName()) == null && rmHome != null) {
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), rmHome);
        }
        if (System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey()) == null && rmHome != null) {
            System.setProperty(PASchedulerProperties.SCHEDULER_HOME.getKey(), rmHome);
        }
    }

}
