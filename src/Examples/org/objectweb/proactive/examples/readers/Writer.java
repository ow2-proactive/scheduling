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
package org.objectweb.proactive.examples.readers;

public class Writer implements org.objectweb.proactive.RunActive {
    private ReaderDisplay display;
    private ReaderWriter rw;
    private boolean done;
    private boolean autopilot;
    private boolean writing;
    private int id;

    /**
     * The no args constructor as commanded by PaPdc
     */
    public Writer() {
    }

    /**
     * The real constructor
     */
    public Writer(ReaderDisplay display, ReaderWriter rw, int id) {
        this.display = display;
        this.rw = rw;
        this.id = id;

        done = false;
        autopilot = true;
        writing = false;
    }

    public void stopIt() {
        done = true;
    }

    private void startWrite() {
        writing = true;
        display.setWait(id, false);
        rw.startWrite();
        display.setWrite(id, true);
    }

    private void stopWrite() {
        writing = false;
        rw.endWrite();
        display.setWrite(id, false);
    }

    /**
     * The live method.
     * @param body the body of the Active object
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (!done) {
            service.serveOldest();
            if (writing) {
                stopWrite();
            } else {
                startWrite();
            }
            try {
                Thread.sleep((long) 700 + (long) (Math.random() * 1500));
            } catch (InterruptedException e) {
            }
        }
    }
}
