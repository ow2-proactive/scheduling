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
package org.objectweb.proactive.ic2d.timit.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * This class contains all static methods used by different specific classes of this plugin
 * @author The ProActive Team
 *
 */
public final class Utils {
    private static final Format MILLISECONDS_FORMATTER = new SimpleDateFormat("SSS");
    private static final Format SECONDS_FORMATTER = new SimpleDateFormat("ss.SSS");
    private static final Format MINUTES_FORMATTER = new SimpleDateFormat("mm.ss.SSS");
    private static final Format HOURS_FORMATTER = new SimpleDateFormat("HH.mm.ss.SSS");

    /**
     * Returns a formatted string of the time given in milliseconds.
     * Example: XXX h XX m XX s XXX ms
     * @param aMillis The time to format
     * @return The formated time  
     */
    public static String formatMillis(long aMillis) {
        int hours = (int) (aMillis / (1000 * 60 * 60));
        long rest = aMillis % (1000 * 60 * 60);
        int minutes = (int) (rest / (1000 * 60));
        rest = rest % (1000 * 60);
        int seconds = (int) (rest / 1000);
        int millis = (int) (rest % 1000);
        return String.format("%02d h %02d m %02d s %02d ms", hours, minutes, seconds, millis);
    }

    /**
     * A static method to convert time in microseconds into its human-readable String representation.
     * @param timeInMicros The time in microseconds to convert
     * @return A String representation
     */
    public static final String convertTimeInMicrosToString(final long timeInMicros) {
        String result;
        if ((timeInMicros / 1000) < 1) {
            result = timeInMicros + "\u00b5s";
        } else {
            long timeInMillis = timeInMicros / 1000;
            long micros = timeInMicros % 1000;
            Date date = new Date(timeInMillis);
            if ((timeInMillis / 1000) < 1) {
                result = MILLISECONDS_FORMATTER.format(date) + "." + micros + "ms"; // use milliseconds
            } else {
                double timeInSeconds = timeInMillis / 1000; // Check if minutes is not ok
                if ((timeInSeconds / 60) < 1) {
                    result = SECONDS_FORMATTER.format(date) + "." + micros + "s"; // use seconds
                } else {
                    double timeInMinutes = timeInSeconds / 60; // Check if hours is not ok
                    if ((timeInMinutes / 60) < 1) {
                        result = MINUTES_FORMATTER.format(date) + "." + micros + "m"; // use minutes                      
                    } else {
                        result = HOURS_FORMATTER.format(date) + "." + micros + "h"; // use hours
                    }
                }
            }
        }
        return result;
    }

    /**
     * A static method to convert time in microseconds into its human-readable String representation.
     * @param timeInMicros The time in microseconds to convert
     * @return A String representation
     */
    public static final String convertTimeInMillisToString(final long timeInMillis) {
        String result;
        Date date = new Date(timeInMillis);
        if ((timeInMillis / 1000) < 1) {
            result = MILLISECONDS_FORMATTER.format(date) + "ms"; // use milliseconds
        } else {
            double timeInSeconds = timeInMillis / 1000; // Check if minutes is not ok
            if ((timeInSeconds / 60) < 1) {
                result = SECONDS_FORMATTER.format(date) + "s"; // use seconds
            } else {
                double timeInMinutes = timeInSeconds / 60; // Check if hours is not ok
                if ((timeInMinutes / 60) < 1) {
                    result = MINUTES_FORMATTER.format(date) + "m"; // use minutes                      
                } else {
                    result = HOURS_FORMATTER.format(date) + "h"; // use hours
                }
            }
        }

        return result;
    }

}
