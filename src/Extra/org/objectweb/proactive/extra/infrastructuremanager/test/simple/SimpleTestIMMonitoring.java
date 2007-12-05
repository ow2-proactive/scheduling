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
package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMConstants;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMInitialState;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeEvent;
import org.objectweb.proactive.extra.infrastructuremanager.common.IMNodeSourceEvent;
import org.objectweb.proactive.extra.infrastructuremanager.exception.IMException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMConnection;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMEventListener;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;


public class SimpleTestIMMonitoring implements IMEventListener, InitActive,
    Serializable {
    private static final long serialVersionUID = 2L;
    private IMMonitoring imMonitoring;

    /**
     * ProActive empty constructor
     */
    public SimpleTestIMMonitoring() {
    }

    public SimpleTestIMMonitoring(IMMonitoring imMonitoring) {
        this.imMonitoring = imMonitoring;
    }

    /**
     * Initialization part of NodeSource Active Object.
     * register itself to IMMonitoring as events listener
     */
    public void initActivity(Body body) {
        System.out.println("SimpleTestIMMonitoring.initActivity()");
        IMInitialState initState = this.imMonitoring.addIMEventListener((IMEventListener) ProActiveObject.getStubOnThis(),
                IMEvent.KILLED, IMEvent.NODE_ADDED, IMEvent.NODE_BUSY,
                IMEvent.NODE_DOWN, IMEvent.NODE_FREE, IMEvent.NODE_REMOVED,
                IMEvent.NODE_TO_RELEASE, IMEvent.NODESOURCE_CREATED,
                IMEvent.NODESOURCE_REMOVED, IMEvent.SHUTDOWN,
                IMEvent.SHUTTING_DOWN, IMEvent.STARTED);

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
                    IMConstants.NAME_ACTIVE_OBJECT_IMMONITORING;
            }

            IMMonitoring imMonitoring = IMConnection.connectAsMonitor(url);

            SimpleTestIMMonitoring test = (SimpleTestIMMonitoring) ProActiveObject.newActive(SimpleTestIMMonitoring.class.getName(),
                    new Object[] { imMonitoring });
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IMException e) {
            e.printStackTrace();
        }
    }

    private void printInitialState(IMInitialState state) {
        System.out.println("\n\n\t Infrastructure Manager - Initial state : ");
        ArrayList<IMNodeSourceEvent> nsList = state.getNodeSource();

        System.out.println("\n" + nsList.size() + " node sources :");
        for (IMNodeSourceEvent ns : nsList) {
            System.out.println("\ttype : " + ns.getSourceType() + " name : " +
                ns.getSourceName());
        }

        ArrayList<IMNodeEvent> freeNodes = state.getFreeNodes();
        ArrayList<IMNodeEvent> busyNodes = state.getBusyNodes();
        ArrayList<IMNodeEvent> toReleaseNodes = state.getToReleaseNodes();
        ArrayList<IMNodeEvent> downNodes = state.getDownNodes();

        System.out.println(freeNodes.size() + busyNodes.size() +
            toReleaseNodes.size() + downNodes.size() + " nodes :");

        for (IMNodeEvent ne : freeNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (IMNodeEvent ne : busyNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (IMNodeEvent ne : toReleaseNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }

        for (IMNodeEvent ne : downNodes) {
            System.out.println("\t" + ne.getNodeUrl() + ", Node Source : " +
                ne.getNodeSource() + ", Status : " + ne.getState());
        }
    }

    //----------------Events handling ---------------//    
    public void imKilledEvent() {
        System.out.println("SimpleTestIMMonitoring.imKilledEvent()");
    }

    public void imShutDownEvent() {
        System.out.println("SimpleTestIMMonitoring.imShutDownEvent()");
    }

    public void imShuttingDownEvent() {
        System.out.println("SimpleTestIMMonitoring.imShuttingDownEvent()");
    }

    public void imStartedEvent() {
        System.out.println("SimpleTestIMMonitoring.imStartedEvent()");
    }

    public void nodeAddedEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeAddedEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeBusyEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeBusyEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeDownEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeDownEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeFreeEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeFreeEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeToReleaseEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeToReleaseEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeRemovedEvent(IMNodeEvent n) {
        System.out.println("SimpleTestIMMonitoring.nodeRemovedEvent() " +
            n.getNodeUrl() + " status : " + n.getState() + " VMname : " +
            n.getVMName());
    }

    public void nodeSourceAddedEvent(IMNodeSourceEvent ns) {
        System.out.println("SimpleTestIMMonitoring.nodeSourceAddedEvent() " +
            ns.getSourceName());
    }

    public void nodeSourceRemovedEvent(IMNodeSourceEvent ns) {
        System.out.println("SimpleTestIMMonitoring.nodeSourceRemovedEvent() " +
            ns.getSourceName());
    }
}
