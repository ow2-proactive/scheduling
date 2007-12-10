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
package org.objectweb.proactive.extra.resourcemanager.test.simple;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMEvent;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMEventType;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMInitialState;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMNodeEvent;
import org.objectweb.proactive.extra.resourcemanager.common.event.RMNodeSourceEvent;
import org.objectweb.proactive.extra.resourcemanager.exception.RMException;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMConnection;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMEventListener;
import org.objectweb.proactive.extra.resourcemanager.frontend.RMMonitoring;


public class SimpleTestRMMonitoring implements RMEventListener, InitActive,
    Serializable {
    private static final long serialVersionUID = 2L;
    private RMMonitoring imMonitoring;

    /**
     * ProActive empty constructor
     */
    public SimpleTestRMMonitoring() {
    }

    public SimpleTestRMMonitoring(RMMonitoring imMonitoring) {
        this.imMonitoring = imMonitoring;
    }

    /**
     * Initialization part of NodeSource Active Object.
     * register itself to IMMonitoring as events listener
     */
    public void initActivity(Body body) {
        System.out.println("SimpleTestIMMonitoring.initActivity()");
        RMInitialState initState = this.imMonitoring.addRMEventListener((RMEventListener) PAActiveObject.getStubOnThis(),
                RMEventType.KILLED, RMEventType.NODE_ADDED,
                RMEventType.NODE_BUSY, RMEventType.NODE_DOWN,
                RMEventType.NODE_FREE, RMEventType.NODE_REMOVED,
                RMEventType.NODE_TO_RELEASE, RMEventType.NODESOURCE_CREATED,
                RMEventType.NODESOURCE_REMOVED, RMEventType.SHUTDOWN,
                RMEventType.SHUTTING_DOWN, RMEventType.STARTED);

        printInitialState(initState);
    }

    public static void main(String[] args) {
        System.out.println("# --oOo-- Simple Test  Monitoring --oOo-- ");

        try {
            String url;
            if (args.length > 0) {
                url = args[0];
            } else {
                url = "rmi://localhost:1099/" +
                    RMConstants.NAME_ACTIVE_OBJECT_RMMONITORING;
            }

            RMMonitoring imMonitoring = RMConnection.connectAsMonitor(url);

            SimpleTestRMMonitoring test = (SimpleTestRMMonitoring) PAActiveObject.newActive(SimpleTestRMMonitoring.class.getName(),
                    new Object[] { imMonitoring });
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (RMException e) {
            e.printStackTrace();
        }
    }

    private void printInitialState(RMInitialState state) {
        System.out.println("\n\n\t Infrastructure Manager - Initial state : ");
        ArrayList<RMNodeSourceEvent> nsList = state.getNodeSource();

        System.out.println("\n" + nsList.size() + " node sources :");
        for (RMNodeSourceEvent ns : nsList) {
            System.out.println("\ttype : " + ns.getSourceType() + " name : " +
                ns.getSourceName());
        }

        ArrayList<RMNodeEvent> freeNodes = state.getFreeNodes();
        ArrayList<RMNodeEvent> busyNodes = state.getBusyNodes();
        ArrayList<RMNodeEvent> toReleaseNodes = state.getToReleaseNodes();
        ArrayList<RMNodeEvent> downNodes = state.getDownNodes();

        System.out.println(freeNodes.size() + busyNodes.size() +
            toReleaseNodes.size() + downNodes.size() + " nodes :");

        for (RMNodeEvent ne : freeNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (RMNodeEvent ne : busyNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (RMNodeEvent ne : toReleaseNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (RMNodeEvent ne : downNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }
    }

    //----------------Events handling ---------------//    
    public void imKilledEvent(RMEvent evt) {
        System.out.println("imKilledEvent, RM : " + evt.getIMUrl());
    }

    public void imShutDownEvent(RMEvent evt) {
        System.out.println("imShutDownEvent, RM : " + evt.getIMUrl());
    }

    public void imShuttingDownEvent(RMEvent evt) {
        System.out.println("imShuttingDownEvent, RM : " + evt.getIMUrl());
    }

    public void imStartedEvent(RMEvent evt) {
        System.out.println("imStartedEvent RM : " + evt.getIMUrl());
    }

    public void nodeAddedEvent(RMNodeEvent n) {
        System.out.println("nodeAddedEvent " + n.getNodeUrl() + " status : " +
            n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeBusyEvent(RMNodeEvent n) {
        System.out.println("nodeBusyEvent " + n.getNodeUrl() + " status : " +
            n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeDownEvent(RMNodeEvent n) {
        System.out.println("nodeDownEvent " + n.getNodeUrl() + " status : " +
            n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeFreeEvent(RMNodeEvent n) {
        System.out.println("nodeFreeEvent " + n.getNodeUrl() + " status : " +
            n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeToReleaseEvent(RMNodeEvent n) {
        System.out.println("nodeToReleaseEvent " + n.getNodeUrl() +
            " status : " + n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeRemovedEvent(RMNodeEvent n) {
        System.out.println("nodeRemovedEvent " + n.getNodeUrl() + " status : " +
            n.getState() + " VMname : " + n.getVMName());
    }

    public void nodeSourceAddedEvent(RMNodeSourceEvent ns) {
        System.out.println("nodeSourceAddedEvent " + ns.getSourceName() +
            "RM : " + ns.getIMUrl());
    }

    public void nodeSourceRemovedEvent(RMNodeSourceEvent ns) {
        System.out.println("nodeSourceRemovedEvent " + ns.getSourceName() +
            "RM : " + ns.getIMUrl());
    }
}
