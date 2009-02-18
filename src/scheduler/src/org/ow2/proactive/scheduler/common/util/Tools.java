/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Provides some methods used by the scheduler or the GUI to display some tips properly.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class Tools implements Serializable {

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
            formatted = tmp + " day" + ((tmp > 1) ? "s" : "") + " - " + formatted;
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

    /**
     * Format the given string and return a long that correspond
     * to the time represented by the given string.<br />
     * If the string is not proper, 0 will be returned.
     *
     * @param pattern a time pattern that must be in [[HH:]MM:]SS
     * 			where HH, MM, and SS are numbers
     * @return a long corresponding to the given time.
     */
    public static long formatDate(String pattern) {
        String[] splitted = pattern.split(":");
        int[] factor = new int[] { 60 * 60 * 1000, 60 * 1000, 1000 };
        if (splitted.length < 0 || splitted.length > 3) {
            return 0;
        }
        long date = 0;
        try {
            for (int i = splitted.length - 1; i >= 0; i--) {
                date += Integer.parseInt(splitted[i]) * factor[i + 3 - splitted.length];
            }
            return date;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Parse a command line in order to split it into a string array.
     * This method provides the parsing as followed :<br />
     *  - It is split according to the 'white space' character.<br />
     *  - It is possible to escape white space character using the '%' character.<br />
     *  - To write this '%' special char, just escape it ( '%%' ).<br />
     * For example, the string "cmd arg1 arg% 2 arg%%% 3 arg4%% 5" will return the following string array :<br />
     *   [cmd,arg1,arg 2,arg% 3,arg4%,5]<br />
     * <br />This method can be mostlikely used for Runtime.exec(String[]) method.
     *
     * @param cmdLine The command line to parse.
     * @return a string array that represents the parsed command line.
     */
    public static String[] parseCommandLine(String cmdLine) {
        final char specialToken = '%';
        ArrayList<String> tokens = new ArrayList<String>();
        int i = 0;
        StringBuilder tmp = new StringBuilder();
        char[] cs = cmdLine.toCharArray();
        while (i < cs.length) {
            switch (cs[i]) {
                case specialToken:
                    if (i + 1 < cs.length) {
                        tmp.append(cs[i + 1]);
                        i++;
                    }
                    break;
                case ' ':
                    tokens.add(tmp.toString());
                    tmp = new StringBuilder();
                    break;
                default:
                    tmp.append(cs[i]);
            }
            i++;
        }
        if (tmp.length() > 0) {
            tokens.add(tmp.toString());
        }
        return tokens.toArray(new String[] {});
    }
}
