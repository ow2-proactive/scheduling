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
package org.objectweb.proactive.examples.boundedbuffer;

public class ActiveDisplay implements ConsumerProducerListener {
    transient private AppletBuffer applet;
    transient private BoundedBuffer buffer;
    transient private Consumer consumer;
    transient private Producer producer;
    transient private int size;

    public ActiveDisplay() {
    }

    public ActiveDisplay(int size, AppletBuffer applet) {
        //System.out.println("applet = " + applet);
        this.applet = applet;
        this.size = size;
        displayMessage("Active display created");
    }

    public void start() {
        displayMessage("ActiveDisplay start : Creating active objects");

        // Active objects creation
        Object o = org.objectweb.proactive.api.ProActiveObject.getStubOnThis();

        // Active Buffer creation
        try {
            buffer = (BoundedBuffer) org.objectweb.proactive.api.ProActiveObject.newActive(BoundedBuffer.class.getName(),
                    new Object[] { new Integer(size), o });
        } catch (Exception e) {
        }

        if (buffer == null) {
            displayMessage("Error while creating active objects");
            applet.kill();
        }

        // Producer
        displayMessage("Creating producer...");
        try {
            producer = (Producer) org.objectweb.proactive.api.ProActiveObject.newActive(Producer.class.getName(),
                    new Object[] { o, buffer });
        } catch (Exception e) {
        }

        // Consumer
        displayMessage("Creating Consumer...");
        try {
            consumer = (Consumer) org.objectweb.proactive.api.ProActiveObject.newActive(Consumer.class.getName(),
                    new Object[] { o, buffer });
        } catch (Exception e) {
        }
        displayMessage("Remote objects created...");
    }

    public void done() {
        producer.done();
        consumer.done();
        producer = null;
        consumer = null;
        buffer = null;
    }

    public void update(int pos, String str) {
        if (str == null) {
            applet.setOut(pos, false);
        } else {
            applet.setIn(pos, false);
        }
        applet.setCell(pos, str);
    }

    public void toggleCons() {
        applet.receiveMessage("toggle Consumer");
        consumer.toggle();
    }

    public void toggleProd() {
        displayMessage("toggle Producer");
        producer.toggle();
    }

    public void setOut(int pos) {
        applet.setOut(pos, true);
    }

    public void setIn(int pos) {
        applet.setIn(pos, true);
    }

    //
    // -- implements ConsumerProducerListener ----------------------------------------------------------------------
    // 
    public void displayMessage(String msg) {
        applet.receiveMessage(msg);
    }

    public void consumerStartRunning() {
        applet.consumerStartRunning();
    }

    public void consumerStopRunning() {
        applet.consumerStopRunning();
    }

    public void producerStartRunning() {
        applet.producerStartRunning();
    }

    public void producerStopRunning() {
        applet.producerStopRunning();
    }
}
