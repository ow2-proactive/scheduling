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
package org.ow2.proactive.utils.appenders;

import java.util.Collection;

import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;


/**
 * 
 * An appender that redirects logging events to all files
 * specified in "filenames" property in log4j context.
 *
 * It is used in the RM to during the selection script execution.
 * If the selection is performed for several tasks it writes logs
 * to all the tasks files.
 *
 */
public class MultipleFileAppender extends FileAppender {

    public static final String FILE_NAMES = "filenames";

    @Override
    public void append(LoggingEvent event) {
        Object value = MDC.get(FILE_NAMES);
        if (value != null) {
            Collection<String> names = (Collection<String>) value;
            for (String fileName : names) {
                append(fileName, event);
            }
        }
    }
}
