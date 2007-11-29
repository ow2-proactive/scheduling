/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.common.scheduler;

import java.io.Serializable;
import java.util.Calendar;


/**
 * Provides some methods used by the scheduler or the GUI to display some tips properly.
 *
 * @author jlscheef - ProActiveTeam
 * @version 1.0, Jun 26, 2007
 * @since ProActive 3.2
 * @publicAPI
 */
public class Tools implements Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 2410081686855631412L;

    /**
     * Format the given integer 'toFormat' to a String containing 'nbChar'
     * characters
     *
     * @param toFormat
     *            the number to format
     * @param nbChar
     *            the number of characters of the formatted result string
     * @return the given integer formatted as a 'nbChar' length String.
     */
    public static String formatNChar(int toFormat, int nbChar) {
        String formatted = toFormat + "";

        while (formatted.length() < nbChar) {
            formatted = "0" + formatted;
        }

        return formatted;
    }

    /**
     * Format 2 long times into a single duration as a String. The string will
     * contains the duration in Days, hours, minutes, seconds, and millis.
     *
     * @param start
     *            the first date (time)
     * @param end
     *            the second date (time)
     * @return the duration as a formatted string.
     */
    public static String getFormattedDuration(long start, long end) {
        if ((start == -1) || (end == -1)) {
            return "Not yet";
        }

        long duration = Math.abs(start - end);
        String formatted = "";
        int tmp;
        // Millisecondes
        tmp = (int) duration % 1000;
        duration = duration / 1000;
        formatted = tmp + "ms" + formatted;
        // Secondes
        tmp = (int) duration % 60;
        duration = duration / 60;

        if (tmp > 0) {
            formatted = tmp + "s " + formatted;
        }

        // Minutes
        tmp = (int) duration % 60;
        duration = duration / 60;

        if (tmp > 0) {
            formatted = tmp + "m " + formatted;
        }

        // Hours
        tmp = (int) duration % 24;
        duration = duration / 24;

        if (tmp > 0) {
            formatted = tmp + "h " + formatted;
        }

        // Days
        tmp = (int) duration;

        if (tmp > 0) {
            formatted = tmp + " day" + ((tmp > 1) ? "s" : "") + " - " +
                formatted;
        }

        return formatted;
    }

    /**
     * Return the given date as a formatted string.
     *
     * @param time the date as a long.
     * @return the given date as a formatted string.
     */
    public static String getFormattedDate(long time) {
        if (time == -1) {
            return "Not yet";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        return String.format("%1$tT  %1$tD", calendar);
    }
}
