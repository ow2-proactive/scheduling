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
package org.ow2.proactive.scheduler.common.util.logforwarder;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;


public class LoggingEventProcessor {
    Hierarchy h = new NoWarningHierarchy();

    public void addAppender(String loggerName, Appender appender) {
        h.getLogger(loggerName).addAppender(appender);
    }

    public void removeAllAppenders(String loggerName) {
        h.getLogger(loggerName).removeAllAppenders();
    }

    public void removeAllAppenders() {
        h.getRootLogger().removeAllAppenders();
    }

    public void processEvent(LoggingEvent event) {
        h.getLogger(event.getLoggerName()).callAppenders(event);
    }

    private static class NoWarningHierarchy extends Hierarchy {
        public NoWarningHierarchy() {
            super(new RootLogger(Level.ALL));
        }

        @Override
        public void emitNoAppenderWarning(Category cat) {

        }
    }
}
