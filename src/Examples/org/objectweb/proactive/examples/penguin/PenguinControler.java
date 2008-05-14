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
package org.objectweb.proactive.examples.penguin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class PenguinControler implements org.objectweb.proactive.RunActive, PenguinMessageReceiver,
        java.io.Serializable {
    //The image panel
    private transient PenguinApplet display;
    @SuppressWarnings("unchecked")
    protected CircularArrayList penguinList;
    String[] args;
    private MigrationStrategyManagerImpl myStrategyManager;

    public PenguinControler() {
    }

    @SuppressWarnings("unchecked")
    public PenguinControler(String[] args) {
        this.penguinList = new CircularArrayList(20);
        this.args = args;
        try {
            PAActiveObject.turnActive(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rebuild() {
        this.display = new PenguinApplet((PenguinControler) PAActiveObject.getStubOnThis(), penguinList);
    }

    public void clean() {
        if (display != null) {
            display.dispose();
            display = null;
        }
    }

    public void receiveMessage(String s) {
        display.receiveMessage(s);
    }

    public void receiveMessage(String s, java.awt.Color c) {
        display.receiveMessage(s, c);
    }

    public Penguin createPenguin(int n) {
        try {
            Penguin newPenguin = (Penguin) org.objectweb.proactive.api.PAActiveObject.newActive(Penguin.class
                    .getName(), new Object[] { new Integer(n) });
            newPenguin.initialize(args);
            newPenguin.setControler((PenguinControler) PAActiveObject.getStubOnThis());
            return newPenguin;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void runActivity(Body b) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(b);
        myStrategyManager = new MigrationStrategyManagerImpl(
            (org.objectweb.proactive.core.body.migration.Migratable) b);
        myStrategyManager.onDeparture("clean");
        rebuild();
        service.fifoServing();
        clean();
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();

        // Version with deployment descriptor
        GCMApplication proActiveDescriptor = null;
        try {
            proActiveDescriptor = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
            proActiveDescriptor.startDeployment();
            GCMVirtualNode vn1 = proActiveDescriptor.getVirtualNode("penguinNode");

            //Thread.sleep(15000);
            List<Node> currentNodes = vn1.getCurrentNodes();
            List<String> nodesURLs = new ArrayList<String>();
            for (Node node : currentNodes) {
                nodesURLs.add(node.getNodeInformation().getURL());
            }
            new PenguinControler(nodesURLs.toArray(new String[0]));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
