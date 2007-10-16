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
package org.objectweb.proactive.examples.philosophers;

public class Philosopher implements org.objectweb.proactive.RunActive {

    /**
     * The philosopher's ID
     */
    protected int id;

    /**
     * set to false if the philosopher is interface-driven
     */
    protected boolean autopilot;

    /**
     * True if the philosopher has both forks
     */
    protected boolean hasBothForks;

    /**
     * The table's stub
     */
    protected Table table;

    /**
     * A reference to the layout\
     */
    protected DinnerLayout layout;

    /**
     * No arg constructor
     */
    public Philosopher() {
    }

    /**
     * Real constructor
     */
    public Philosopher(int id, Table table, DinnerLayout layout) {
        this.id = id;
        this.table = table;
        this.layout = layout;
        autopilot = false;
        hasBothForks = false;
    }

    /**
     * getForks
     */
    public void getForks() {
        layout.update(id, 1); // set to waiting
                              // do a synchronous call

        table.getForks(id);
        hasBothForks = true;
    }

    /**
     * putForks
     */
    public void putForks() {
        table.putForks(id);
        hasBothForks = false;
    }

    /**
     *
     */
    public void toggle() {
        autopilot = !autopilot;
    }

    /**
     * The live method
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            // Check wether the philosopher is UI-driven or not
            if (autopilot) {
                if (hasBothForks) {
                    // drop the forks
                    putForks();
                } else {
                    // Let's take the forks
                    getForks();
                }
                service.serveOldest("toggle"); // Serve any toggle
                try {
                    Thread.sleep((int) (Math.random() * (double) 2000) + 1000);
                } catch (InterruptedException e) {
                }
            } else {
                // manual mode
                service.serveOldest();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }
}
//:~Philosopher
