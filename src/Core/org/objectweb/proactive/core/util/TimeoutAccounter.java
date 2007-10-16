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
package org.objectweb.proactive.core.util;


/**
 * All time are in milliseconds
 * We use System.nanoTime() as it tries to be monotonic
 */
public class TimeoutAccounter {
    private final long timeout;
    private long start;
    private static final TimeoutAccounter NO_TIMEOUT = new TimeoutAccounter(0);

    private TimeoutAccounter(long timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout: " + timeout);
        }
        this.timeout = timeout;
        if (this.timeout != 0) {
            this.start = System.nanoTime() / 1000000;
        }
    }

    public static TimeoutAccounter getAccounter(long timeout) {
        if (timeout == 0) {
            return NO_TIMEOUT;
        }

        return new TimeoutAccounter(timeout);
    }

    public boolean isTimeoutElapsed() {
        return (this.timeout != 0) &&
        (((System.nanoTime() / 1000000) - this.start) >= this.timeout);
    }

    /**
     * Will never return 0 if a timeout was originally specified,
     * that's why you must check isTimeoutElapsed() before.
     */
    public long getRemainingTimeout() {
        long remainingTimeout = 0;
        if (this.timeout != 0) {
            long elapsedTime = (System.nanoTime() / 1000000) - start;
            remainingTimeout = timeout - elapsedTime;
            if (remainingTimeout <= 0) {
                /* Returning a timeout of 0 would mean infinite timeout */
                remainingTimeout = 1;
            }
        }
        return remainingTimeout;
    }
}
