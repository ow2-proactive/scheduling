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
package org.objectweb.proactive.examples.penguin;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProMobileAgent;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.migration.Destination;
import org.objectweb.proactive.core.migration.MigrationStrategyImpl;
import org.objectweb.proactive.core.node.NodeFactory;


public class Penguin implements org.objectweb.proactive.RunActive,
    java.io.Serializable {
    private boolean onItinerary;
    private boolean initialized;
    private transient PenguinFrame myFrame;
    private PenguinMessageReceiver controler;
    private Penguin otherPenguin;
    private javax.swing.ImageIcon face;
    private org.objectweb.proactive.core.migration.MigrationStrategy myStrategy;
    private org.objectweb.proactive.core.migration.MigrationStrategyManager myStrategyManager;
    private int index;
    private String name;
    private String[] itinerary;

    /**
     * Empty constructor for ProActive
     */
    public Penguin() {
    }

    public Penguin(Integer ind) {
        this.index = ind.intValue();
        this.name = "Agent " + index;
    }

    public void loop() {
        rebuild();
    }

    public void rebuild() {
        Body body = ProActiveObject.getBodyOnThis();
        myFrame = new PenguinFrame(face, body.getNodeURL(), index);
        //System.out.println("Penguin is here");
        sendMessageToControler("I just got in node " +
            ProActiveObject.getBodyOnThis().getNodeURL());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public void clean() {
        if (myFrame != null) {
            myFrame.dispose();
            myFrame = null;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void initialize(String[] s) {
        try {
            //With this we can load the image from the same location than
            //the classes
            ClassLoader cl = this.getClass().getClassLoader();
            java.net.URL u = cl.getResource(
                    "org/objectweb/proactive/examples/penguin/PenguinSmall.jpg");
            face = new javax.swing.ImageIcon(u);
        } catch (Exception e) {
            e.printStackTrace();
        }
        myStrategyManager = new org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl((org.objectweb.proactive.core.body.migration.Migratable) org.objectweb.proactive.api.ProActiveObject.getBodyOnThis());
        myStrategyManager.onDeparture("clean");
        myStrategy = new MigrationStrategyImpl();
        itinerary = s;
        for (int i = 0; i < s.length; i++)
            myStrategy.add(s[i], null);
    }

    public void setControler(PenguinMessageReceiver c) {
        this.controler = c;
        this.initialized = true;
    }

    public void setOther(Penguin penguin) {
        this.otherPenguin = penguin;
    }

    public Destination nextHop() {
        Destination r = myStrategy.next();
        if (r == null) {
            myStrategy.reset();
            r = myStrategy.next();
        }
        return r;
    }

    public void runActivity(Body b) {
        Service service = new Service(b);
        if (!initialized) {
            service.blockingServeOldest();
        }
        rebuild();
        Destination r = null;

        //first we empty the RequestQueue
        while (b.isActive()) {
            while (service.hasRequestToServe()) {
                service.serveOldest();
            }
            if (onItinerary) {
                r = nextHop();
                try {
                    //Non migration to the same node or a null node
                    if ((r != null) &&
                            !NodeFactory.isNodeLocal(NodeFactory.getNode(
                                    r.getDestination()))) {
                        ProMobileAgent.migrateTo(r.getDestination());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                service.blockingServeOldest();
            }
        }
    }

    public String[] getItinerary() {
        return itinerary;
    }

    public void setItinerary(String[] newItinerary) {
        sendMessageToControler("I changed my itinerary", java.awt.Color.blue);
        myStrategy = new MigrationStrategyImpl();
        itinerary = newItinerary;
        for (int i = 0; i < newItinerary.length; i++)
            myStrategy.add(newItinerary[i], null);
    }

    public void start() {
        sendMessageToControler("I am starting my itinerary",
            new java.awt.Color(0, 150, 0));
        myStrategy.reset();
        onItinerary = true;
    }

    public void suspend() {
        if (!onItinerary) {
            sendMessageToControler("I'm already suspended");
        } else {
            sendMessageToControler("I suspended my itinerary",
                java.awt.Color.red);
            onItinerary = false;
        }
    }

    public void resume() {
        if (onItinerary) {
            sendMessageToControler("I'm already on my itinerary");
        } else {
            sendMessageToControler("I'm resuming my itinerary",
                new java.awt.Color(0, 150, 0));
            onItinerary = true;
        }
    }

    public String call() {
        return "[" + name + "] : I am working on node " +
        ProActiveObject.getBodyOnThis().getNodeURL();
    }

    public void chainedCall() {
        if (otherPenguin != null) {
            sendMessageToControler("I'm calling my peer agent");
            otherPenguin.chainedCall();
        } else {
            sendMessageToControler("I don't have a peer agent to call");
        }
    }

    private void sendMessageToControler(String message) {
        if (controler != null) {
            controler.receiveMessage("[" + name + "] : " + message);
        }
    }

    private void sendMessageToControler(String message, java.awt.Color color) {
        if (controler != null) {
            controler.receiveMessage("[" + name + "] : " + message, color);
        }
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        if (!(args.length > 1)) {
            System.out.println(
                "Usage: java org.objectweb.proactive.examples.penguin.Penguin hostname1/NodeName1 hostname2/NodeName2 ");
            System.exit(-1);
        }
        try {
            Penguin n = (Penguin) org.objectweb.proactive.api.ProActiveObject.newActive(Penguin.class.getName(),
                    null);

            //	Penguin n2 = (Penguin)org.objectweb.proactive.ProActive.newActive(Penguin.class.getName(), null);
            n.initialize(args);
            //	n2.initialize(args);
            //	n.startItinerary();
            Object[] param = new Object[1];
            param[0] = n;
            org.objectweb.proactive.api.ProActiveObject.newActive(PenguinMessageReceiver.class.getName(),
                param, (org.objectweb.proactive.core.node.Node) null);
            //	n.setOther(n2);
            //	n2.setOther(null);
            //	n2.startItinerary();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
