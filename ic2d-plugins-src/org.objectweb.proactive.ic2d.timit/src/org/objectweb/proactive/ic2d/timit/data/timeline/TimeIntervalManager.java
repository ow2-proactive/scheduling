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
package org.objectweb.proactive.ic2d.timit.data.timeline;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeIntervalManager {
    public static final int MINIMAL_TIMESTAMP_VALUE_IN_MICROSECONDS = 50000;
    private static final Format MILLISECONDS_FORMATTER = new SimpleDateFormat("SSS");
    private static final Format SECONDS_FORMATTER = new SimpleDateFormat("ss.SSS");
    private static final Format MINUTES_FORMATTER = new SimpleDateFormat("mm.ss.SSS");
    private static final Format HOURS_FORMATTER = new SimpleDateFormat("HH.mm.ss.SSS");

    /**
     * The time interval represented in microseconds.
     */
    protected long timeInterval;

    /**
     * The interval start represented in microseconds.
     */
    protected long beginTime; // since its always 0 ... for futur work on a sliding window

    /**
     * The end of the time interval represented in microseconds.
     */
    protected long endTime;

    /**
     * The current time step.
     */
    protected long timeStep;

    /**
     * A boolean to know if this time interval manager is initialized
     */
    protected boolean inited;

    public TimeIntervalManager() {
    }

    public TimeIntervalManager(long endTime) {
        this(0, endTime);
    }

    public TimeIntervalManager(long beginTime, long endTime) {
        this.init(beginTime, endTime);
    }

    /**
     * @param beginTime
     * @param endTime
     */
    public void init(long beginTime, long endTime) {
        this.beginTime = beginTime;
        this.endTime = endTime + ((endTime - beginTime) / 4);
        this.timeInterval = endTime - beginTime;
        this.inited = true;
    }

    /**
     * Gets the x position of the time relative to the axis interval.
     */
    public final int getXPosition(long instant, int clientWidth) {
        return (int) Math.floor(((double) (instant - this.beginTime) / this.timeInterval) * clientWidth);
    }

    public final long getTimePosition(int relativePixelPosition, int clientWidth) {
        return Math.round((((double) relativePixelPosition) / clientWidth) * timeInterval);
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(long timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * A static method to convert time in microseconds into its human-readable String representation.
     * @param timeInMicros The time in microseconds to convert
     * @return A String representation
     */
    public static final String convertTimeInMicrosToString(final long timeInMicros) {
        String result = "";
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

    public boolean isInited() {
        return inited;
    }
}
