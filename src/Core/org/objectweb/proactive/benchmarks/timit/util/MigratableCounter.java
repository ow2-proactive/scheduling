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
package org.objectweb.proactive.benchmarks.timit.util;


/**
 * A MigratableCounter is a TimerCounter which can be migrated using ProActive
 * migration.<br>
 * This counter use a network clock and is much slower than a classic
 * TimerCounter.<br>
 * Use with care.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk, Judicael Ribault
 *
 */
public class MigratableCounter extends TimerCounter {

    /**
     *
     */
    private static final long serialVersionUID = 2195039327751012564L;
    private TimItReductor netclock;
    private long elapsed;
    private long latency;

    /**
     * Create an instance of MigratableCounter
     *
     * @param s
     *            the name of the counter you want to create
     */
    public MigratableCounter(String s) {
        super(s);
    }

    /**
     * Used by the Timer to know if a counter can be migrated
     */
    @Override
    public boolean isMigratable() {
        return true;
    }

    /**
     * Invoked by TimItReductor to setup network clock (which is localized on
     * TimItReductor instance node
     *
     * @param tr
     *            the TimItReductor instance
     */
    public void setClock(TimItReductor tr) {
        this.netclock = tr;
    }

    /**
     * Start the counter (perform a distant method call)
     */
    @Override
    public void start() {
        this.latency = System.currentTimeMillis();
        this.elapsed = this.netclock.getCurrentTimeMillis(); // ask time to
                                                             // network clock

        this.latency = System.currentTimeMillis() - this.latency;
        this.elapsed -= (this.latency / 2); // adjust and consider an symetrical
                                            // connection
    }

    /**
     * Stop the counter (perform a distant method call)
     */
    @Override
    public void stop() {
        this.latency = System.currentTimeMillis();
        this.elapsed = this.netclock.getCurrentTimeMillis() - this.elapsed;
        this.latency = System.currentTimeMillis() - this.latency;
        this.elapsed -= (this.latency / 2); // adjust and consider an symetrical
                                            // connection

        super.setValue((int) this.elapsed);
    }
}
