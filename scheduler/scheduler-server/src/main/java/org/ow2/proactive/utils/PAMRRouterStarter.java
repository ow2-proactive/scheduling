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
import java.io.IOException;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pamr.router.Main;
import org.ow2.proactive.scheduler.util.SchedulerStarter;
import org.apache.log4j.PropertyConfigurator;


public class PAMRRouterStarter {

    public static void main(String[] args) throws IOException {
        configureLogging();
        Main.main(args);
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            String schedulerHomeFromJarOrCurrentFolder = findSchedulerHomeFromJarOrCurrentFolder();
            String defaultLog4jConfig = schedulerHomeFromJarOrCurrentFolder + "/config/log/router.properties";
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig);
            System.setProperty(CentralPAPropertyRepository.PA_HOME.getName(), schedulerHomeFromJarOrCurrentFolder);
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    private static String findSchedulerHomeFromJarOrCurrentFolder() {
        try {
            String jarPath = SchedulerStarter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (new File(jarPath).getParentFile().getParentFile().getName().equals("dist")) {
                return new File(jarPath).getParentFile().getParentFile().getParent();
            } else {
                return ".";
            }
        } catch (Exception e) {
            return ".";
        }
    }
}
