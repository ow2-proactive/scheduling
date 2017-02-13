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
package org.ow2.proactive.scheduler.util.policy;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Helper class for converting ISO datetime strings of the following format:
 * "2008-03-01T13:00:00+01:00".
 * 
 * inspired by:
 * http://stackoverflow.com/questions/2201925/converting-iso8601-compliant
 * -string-to-java-util-date
 */
public class ISO8601DateUtil {

    // non-instantiable
    private ISO8601DateUtil() {
    }

    /**
     * Convert java.util.Date instance into a ISO8601 complaint string.
     * 
     * @param date
     *            a java.util.Date instance
     * @return ISO8601 complaint string
     */
    public static final String parse(final Date date) {
        return new DateTime(date).toString();
    }

    /**
     * Convert ISO8601 complaint string into a java.util.Date instance. In case
     * no timezone information is passed, the default local time will be used
     * 
     * @param source
     *            ISO8601 complaint string
     * @return a java.util.Date instance
     * @throws IllegalArgumentException
     *             if the string does not have a correct format
     */
    public static final Date toDate(final String source) throws IllegalArgumentException {
        return parseWithISO8601Time(source);
    }

    private static final Date parseWithISO8601Time(String dateString) {
        return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();

    }

}
