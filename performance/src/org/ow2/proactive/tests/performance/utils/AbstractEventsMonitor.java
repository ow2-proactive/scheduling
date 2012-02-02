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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.utils;

import java.util.ArrayList;
import java.util.List;


public class AbstractEventsMonitor<T extends AbstractWaitCondition> {

    protected List<T> waitConditions = new ArrayList<T>();

    public T addWaitCondition(T waitCondition) {
        synchronized (waitConditions) {
            waitConditions.add(waitCondition);
        }
        return waitCondition;
    }

    public final boolean waitFor(T waitCondition, long timeout) throws InterruptedException {
        synchronized (waitConditions) {
            if (!waitConditions.contains(waitCondition)) {
                throw new IllegalArgumentException("Condition isn't related to this monitor");
            }
        }
        try {
            long endTime = System.currentTimeMillis() + timeout;
            try {
                boolean stopWait;
                synchronized (waitCondition) {
                    while (!(stopWait = waitCondition.stopWait())) {
                        long waitTime = endTime - System.currentTimeMillis();
                        if (waitTime > 0) {
                            waitCondition.wait(waitTime);
                        } else {
                            break;
                        }
                    }
                }

                // System.out.println("All events:\n" + waitCondition.getEventsLog());

                if (stopWait) {
                    return true;
                } else {
                    System.out.println("Waiting failed with timeout, all events:\n" +
                        waitCondition.getEventsLog());
                    return false;
                }
            } catch (WaitFailedException e) {
                System.out.println("Waiting failed with error: " + e.getMessage() + ", all events:\n" +
                    waitCondition.getEventsLog());
                return false;
            }
        } finally {
            synchronized (waitConditions) {
                waitConditions.remove(waitCondition);
            }
        }
    }

}
