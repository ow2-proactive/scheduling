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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Helper class for converting ISO datetime strings of the following format:
 * "2008-03-01T13:00:00+01:00".
 * 
 * inspired by:
 * http://stackoverflow.com/questions/2201925/converting-iso8601-compliant
 * -string-to-java-util-date
 */
public class ISO8601DateUtil {

    /*
     * Provides thread-safe access for a SimpleDateFormat instance.
     */
    private static final ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }

    };

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
        String formatted = df.get().format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /**
     * Convert ISO8601 complaint string into a java.util.Date instance.
     * 
     * @param source
     *            ISO8601 complaint string
     * @return a java.util.Date instance
     * @throws ParseException
     *             if the string does not have required length
     */
    public static final Date toDate(final String source) throws ParseException {
        String s = source.replace("Z", "+00:00");
        try {
            s = s.substring(0, 22) + s.substring(23);
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Invalid length", 0);
        }
        return df.get().parse(s);

    }

}