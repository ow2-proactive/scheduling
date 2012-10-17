/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.utils;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Provides some methods used by the scheduler or the GUI to display some tips properly.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class Tools {

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
    public static String formatNChar(int toFormat, int nbChar, char replacement) {
        String formatted = toFormat + "";

        while (formatted.length() < nbChar) {
            formatted = replacement + formatted;
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
        if ((start < 0) || (end < 0)) {
            return "Not yet";
        }

        long duration = Math.abs(start - end);
        String formatted = "";
        int tmp;
        // Millisecondes
        tmp = (int) duration % 1000;
        duration = duration / 1000;
        formatted = formatNChar(tmp, 3, ' ') + "ms" + formatted;
        // Secondes
        tmp = (int) duration % 60;
        duration = duration / 60;

        if (tmp > 0) {
            formatted = formatNChar(tmp, 2, ' ') + "s " + formatted;
        }

        // Minutes
        tmp = (int) duration % 60;
        duration = duration / 60;

        if (tmp > 0) {
            formatted = formatNChar(tmp, 2, ' ') + "m " + formatted;
        }

        // Hours
        tmp = (int) duration % 24;
        duration = duration / 24;

        if (tmp > 0) {
            formatted = formatNChar(tmp, 2, ' ') + "h " + formatted;
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
        if (time < 0) {
            return "Not yet";
        }

        SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(new Date(time));
    }

    public static String getElapsedTime(long time) {
        long seconds = (System.currentTimeMillis() - time) / 1000;
        long day, hou, min, sec;
        StringBuilder ret = new StringBuilder();

        day = seconds / (3600 * 24);
        seconds -= day * (3600 * 24);
        hou = seconds / 3600;
        seconds -= hou * 3600;
        min = seconds / 60;
        sec = seconds % 60;

        if (day > 0) {
            ret.append(day + "d ");
            ret.append(hou + "h ");
        } else if (hou > 0) {
            ret.append(hou + "h");
            ret.append(min + "mn ");
        } else if (min > 0) {
            ret.append(min + "mn ");
        } else {
            ret.append(sec + "s ");
        }

        ret.append("ago");

        return ret.toString();
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
     * Normalize the given URL into an URL that only contains protocol://host:port/
     *
     * @param url the url to transform
     * @return an URL that only contains protocol://host:port/
     */
    public static String getHostURL(String url) {
        URI uri = URI.create(url);
        String scheme = (uri.getScheme() == null) ? "rmi" : uri.getScheme();
        String host = (uri.getHost() == null) ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            return scheme + "://" + host + "/";
        } else {
            return scheme + "://" + host + ":" + port + "/";
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

    /**
     * Return the extension of shell script depending the current OS
     *
     * @return the extension of shell script depending the current OS
     */
    public static String shellExtension() {
        if (System.getProperty("os.name").contains("Windows")) {
            return ".bat";
        } else {
            return "";
        }
    }

    /**
     * Get the columned string according to the given ObjectArrayFormatter descriptor.
     *
     * @param oaf the ObjectArrayFormatter describing how to print the array.
     * @return the columned string according the given descriptor
     */
    public static String getStringAsArray(ObjectArrayFormatter oaf) {
        return oaf.getAsString();
    }

    /**
     * Translates time period string into milliseconds. Period string should
     * contain symbol indication length of the period: 
     * <ul>
     * <li>'s' - seconds
     * <li>'m' - minutes
     * <li>'h' - hours
     * <li>'d' - days
     * </ul> 
     * 
     * Examples of valid time period expressions:
     * <ul>
     * <li>'1m' - one minute
     * <li>'1d 10h' - one day and ten hours
     * </ul>
     */
    public static long parsePeriod(String periodString) {
        periodString = periodString.trim();
        if (periodString.isEmpty()) {
            throw new IllegalArgumentException("Period string is empty");
        }

        long total = 0;

        StringBuilder numberStr = new StringBuilder();
        for (int i = 0; i < periodString.length(); i++) {
            char ch = periodString.charAt(i);
            if (Character.isSpaceChar(ch)) {
                continue;
            } else if (Character.isDigit(ch)) {
                numberStr.append(ch);
            } else {
                long millis;
                if (ch == 's') {
                    millis = 1000;
                } else if (ch == 'm') {
                    millis = 1000 * 60;
                } else if (ch == 'h') {
                    millis = 1000 * 60 * 60;
                } else if (ch == 'd') {
                    millis = 1000 * 60 * 60 * 24;
                } else {
                    throw new IllegalArgumentException("Invalid period string: " + ch);
                }
                if (numberStr.length() == 0) {
                    throw new IllegalArgumentException("Period length isn't specified");
                }

                total += Long.valueOf(numberStr.toString()) * millis;
                numberStr = new StringBuilder();
            }
        }

        if (numberStr.length() != 0) {
            throw new IllegalArgumentException("Period string isn't specified");
        }

        return total;
    }
}
