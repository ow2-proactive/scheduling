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

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;


/**
 * A JULI (java.util.logging) handler that redirects java.util.logging messages to Log4J
 * http://wiki.apache.org/myfaces/Trinidad_and_Common_Logging
 */
public class JulToLog4jHandler extends Handler {

    @Override
    public void publish(LogRecord record) {
        org.apache.log4j.Logger log4j = getTargetLogger(record.getLoggerName());
        Priority priority = toLog4j(record.getLevel());
        log4j.log(priority, toLog4jMessage(record), record.getThrown());
    }

    static Logger getTargetLogger(String loggerName) {
        return Logger.getLogger(loggerName);
    }

    public static Logger getTargetLogger(Class clazz) {
        return getTargetLogger(clazz.getName());
    }

    private String toLog4jMessage(LogRecord record) {
        String message = record.getMessage();
        // Format message
        try {
            Object parameters[] = record.getParameters();
            if (parameters != null && parameters.length != 0) {
                // Check for the first few parameters ?
                if (message.contains("{0}") || message.contains("{1}") || message.contains("{2}") ||
                    message.contains("{3}")) {
                    message = MessageFormat.format(message, parameters);
                }
            }
        } catch (Exception ex) {
            // ignore Exception
        }
        return message;
    }

    private org.apache.log4j.Level toLog4j(Level level) {//converts levels
        if (Level.SEVERE == level) {
            return org.apache.log4j.Level.ERROR;
        } else if (Level.WARNING == level) {
            return org.apache.log4j.Level.WARN;
        } else if (Level.INFO == level) {
            return org.apache.log4j.Level.INFO;
        } else if (Level.OFF == level) {
            return org.apache.log4j.Level.OFF;
        }
        return org.apache.log4j.Level.OFF;
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }
}
