/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package testsuite.timer.micro;

import testsuite.timer.Timeable;


/**
 * @author Alexandre di Costanzo
 *
 */
public class MicroTimer implements Timeable {

    static {
        System.loadLibrary("Testsuite_timer_micro_MicroTimer");
    }

    private long[] cumulatedTime;
    private long[] startTime;
    private long[] endTime;

    public native long[] currentTime();

    public MicroTimer() {
    }

    public void start() {
        this.startTime = currentTime();
        this.endTime = currentTime();
        this.cumulatedTime = new long[2];
    }

    /**
     * Stop the timer and
     * returns the cumulated time
     */
    public void stop() {
        this.endTime = currentTime();
    }

    /**
     * Return the newly computed cumulated time
     * as measured by this MicroTimer
     * in microseconds
     */
    public long getCumulatedTime() {
        long[] tmp = this.updateCumulatedTime(startTime, endTime); //this.stop();

        // this.resume();
        return (tmp[0] * 1000000) + tmp[1];
    }

    protected long[] updateCumulatedTime(long[] t1, long[] t2) {
        long[] tmp = new long[2]; // this.cumulatedTime;
        tmp[0] = t2[0] - t1[0];

        if ((t2[1] - t1[1]) < 0) {
            //one second has gone
            tmp[0]--;
            tmp[1] = (t2[1] + 1000000) - t1[1];
        } else {
            tmp[1] += (t2[1] - t1[1]);
        }
        return tmp;
    }

    /**
     * @see testsuite.timer.Timeable#getUnit()
     */
    public String getUnit() {
        return "micros";
    }

    public static void main(String[] args) {
        MicroTimer micro = new MicroTimer();
        System.out.println("Debut du test");
        micro.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        micro.stop();
        System.out.println("Apres 1000ms : " + micro.getCumulatedTime() +
            micro.getUnit());
    }
}
