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
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.CircularArrayList;
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


public class PenguinControler implements org.objectweb.proactive.RunActive, PenguinMessageReceiver,
        java.io.Serializable {
    //The image panel
    private transient PenguinApplet display;
    protected CircularArrayList penguinList;
    String[] args;
    private MigrationStrategyManagerImpl myStrategyManager;

    public PenguinControler() {
    }

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

    //<<<<<<< PenguinControler.java
    public static void main(String[] args) {
        ProActiveConfiguration.load();
        // Version without descriptor
        //    try {
        //      // ProActive.newActive(AdvancedPenguinControler.class.getName(),null,(Node) null);
        //      new PenguinControler(args);
        //    } catch (Exception e) {
        //      e.printStackTrace();
        //    }
        // Version with descriptor
        GCMApplicationDescriptor proActiveDescriptor = null;
        try {
            proActiveDescriptor = PAGCMDeployment.getGCMApplicationDescriptor(new File(args[0]));
            proActiveDescriptor.startDeployment();
            GCMVirtualNode vn1 = proActiveDescriptor.getVirtualNode("penguinNode");

            //Thread.sleep(15000);
            Set<Node> currentNodes = vn1.getCurrentNodes();
            List<String> nodesURLs = new ArrayList<String>();
            for (Node node : currentNodes) {
                nodesURLs.add(node.getNodeInformation().getURL());
            }
            new PenguinControler(nodesURLs.toArray(new String[0]));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  public static void main(String args[]) {
    //      //args modification to replace relative name of nodes to them absolute name
    //      try {
    //	  java.net.InetAddress localhost = ProActiveInet.getInstance().getLocal();
    //	  for (int i=0; i<args.length; i++) {
    //	      if (args[i].startsWith("//localhost")) {
    //		  String nodeName;
    //		  int index = args[i].lastIndexOf('/');
    //		  if (index > 0 &&  index < args[i].length() - 1) {
    //		      nodeName = args[i].substring(index + 1);
    //		      args[i] = "//" + localhost.getHostName() + "/" + nodeName;
    //		  }
    //	      }
    //	  }
    //      } catch (java.net.UnknownHostException e) {
    //	  e.printStackTrace();
    //      }
    //      try {
    //	  // ProActive.newActive(AdvancedPenguinControler.class.getName(),null,(Node) null);
    //	  new PenguinControler(args);
    //      } catch (Exception e) {
    //	  e.printStackTrace();
    //      }
    //  }
    //>>>>>>> 1.6
}
