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
package org.ow2.proactive.wrapper;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


/**
 * BootstrapLogger is a class that uses the root logger to log bootstrap events in the system console.
 * As the logging is properly configured in the class SchedulerStarter, all the code executed
 * before reaching SchedulerStarter uses BootstrapLogger.
 */

public class BootstrapLogger {

    private static final Logger logger = Logger.getRootLogger();

    private BootstrapLogger() {
    }

    // While logger is not configured and it not set with sys properties, use Console logger
    static {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            logger.getLoggerRepository().resetConfiguration();
            BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
            logger.setLevel(Level.INFO);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
