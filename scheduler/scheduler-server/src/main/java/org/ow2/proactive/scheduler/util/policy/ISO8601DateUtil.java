/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
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