/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package nodestate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;


/**
 * This class implements a RM's monitor. It provides method to wait reception of 
 * a certain number of events, and getters for checking received events.
 * 
 * @author ProActive team
 *
 */
public class RMEventReceiver implements InitActive, RunActive, RMEventListener {

    private MutableInteger nbEventReceived = new MutableInteger(0);

    RMMonitoring monitor;

    // array List to store nodes URLs of events thrown by RMMonitoring
    /** list of freed nodes URLs */
    private ArrayList<String> freeNodes;

    /** list of busied nodes URLs */
    private ArrayList<String> busyNodes;

    /** list of all fallen nodes URLs */
    private ArrayList<String> downNodes;

    /** list of all fallen nodes URLs */
    private ArrayList<String> addedNodes;

    /** list of all nodes become 'to be released' */
    private ArrayList<String> toBeReleasedNodes;

    /** list of all nodes become removed */
    private ArrayList<String> removedNodes;

    /** list of all nodes sources created*/
    private ArrayList<String> nodeSourcesCreated;

    /** list of all nodes sources created*/
    private ArrayList<String> nodeSourcesRemoved;

    private Vector<String> methodCalls;

    private RMEventType[] eventsList;

    /**
     * ProActive Empty constructor
     */
    public RMEventReceiver() {
    }

    /**
     * Constructor. Initialize all lists that store events received,
     * and set the RMonitoring AO of the RM to monitor. (RMonitoring 
     * is the RM's AO that send events). It sets the event list that have to be thrown
     * by the RM.
     * 
     * @param m RMMOnitoring active object that throws RMEvents
     * @param list List of RMEvents that must be received.
     */
    public RMEventReceiver(RMMonitoring m, RMEventType[] list) {
        monitor = m;
        eventsList = list;
        addedNodes = new ArrayList<String>();
        freeNodes = new ArrayList<String>();
        busyNodes = new ArrayList<String>();
        downNodes = new ArrayList<String>();
        toBeReleasedNodes = new ArrayList<String>();
        removedNodes = new ArrayList<String>();
        nodeSourcesCreated = new ArrayList<String>();
        nodeSourcesRemoved = new ArrayList<String>();

        methodCalls = new Vector<String>();
        for (Method method : RMEventListener.class.getMethods()) {
            methodCalls.add(method.getName());
        }
    }

    /**
     * clear all events received and stored by this monitor.  
     */
    public void cleanEventLists() {
        addedNodes.clear();
        freeNodes.clear();
        busyNodes.clear();
        downNodes.clear();
        toBeReleasedNodes.clear();
        removedNodes.clear();
        nodeSourcesCreated.clear();
        nodeSourcesRemoved.clear();
    }

    /**
     * ProActive InitActivity method. Register to the RMonitoring object. 
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        // TODO Auto-generated method stub

        RMInitialState initState = monitor.addRMEventListener((RMEventListener) PAActiveObject
                .getStubOnThis(), eventsList);
        PAActiveObject.setImmediateService("waitForNEvent");
    }

    /** ProActive runActivity method. Register to the RMonitoring object.
     * Check that requests (method call) received by this AO, correspond
     * to event reception, if yes increment the meter of event received.
     * @see org.objectweb.proactive.RunActive#runActivity(org.objectweb.proactive.Body)
     */
    public void runActivity(Body body) {
        Service s = new Service(body);
        while (body.isActive()) {
            Request r = s.blockingRemoveOldest();
            s.serve(r);
            if (methodCalls.contains(r.getMethodName())) {
                System.out.println(" EventReceived : " + r.getMethodName());
                synchronized (this.nbEventReceived) {
                    this.nbEventReceived.add(1);
                    this.nbEventReceived.notify();
                }
            }
        }
    }

    /** Method that provide a way to wait reception of certain number of events.
     * @param nbEvents number of events to wait
     * @throws InterruptedException if the waiting fails.
     */
    public void waitForNEvent(int nbEvents) throws InterruptedException {
        synchronized (this.nbEventReceived) {
            while ((this.nbEventReceived.getValue() < nbEvents)) {
                this.nbEventReceived.wait();
            }
            this.nbEventReceived.add(-nbEvents);
        }
    }

    /** Clean and get the list of node added events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodesAddedEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.addedNodes.clone();
        addedNodes.clear();
        return toReturn;
    }

    /** Clean and get the list of node busy events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodesBusyEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) busyNodes.clone();
        busyNodes.clear();
        return toReturn;
    }

    /** Clean and get the list of node Free events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodesFreeEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.freeNodes.clone();
        this.freeNodes.clear();
        return toReturn;
    }

    /** Clean and get the list of node to release events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodesToReleaseEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.toBeReleasedNodes.clone();
        this.toBeReleasedNodes.clear();
        return toReturn;

    }

    /** Clean and get the list of node down events received
    * @return and ArrayList containing these events
    */
    public ArrayList<String> cleanNgetNodesdownEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.downNodes.clone();
        this.downNodes.clear();
        return toReturn;

    }

    /** Clean and get the list of node removed events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodesremovedEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.removedNodes.clone();
        this.removedNodes.clear();
        return toReturn;

    }

    /** Clean and get the list of node source created events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodeSourcesCreatedEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.nodeSourcesCreated.clone();
        this.nodeSourcesCreated.clear();
        return toReturn;

    }

    /** Clean and get the list of node source removed events received
     * @return and ArrayList containing these events
     */
    public ArrayList<String> cleanNgetNodeSourcesRemovedEvents() {
        ArrayList<String> toReturn = (ArrayList<String>) this.nodeSourcesRemoved.clone();
        this.nodeSourcesRemoved.clear();
        return toReturn;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#nodeEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_ADDED:
                this.addedNodes.add(event.getNodeUrl());
                break;
            case NODE_REMOVED:
                removedNodes.add(event.getNodeUrl());
                break;
            case NODE_STATE_CHANGED:
                switch (event.getNodeState()) {
                    case BUSY:
                        busyNodes.add(event.getNodeUrl());
                        break;
                    case DOWN:
                        downNodes.add(event.getNodeUrl());
                        break;
                    case TO_BE_RELEASED:
                        toBeReleasedNodes.add(event.getNodeUrl());
                        break;
                    case FREE:
                        freeNodes.add(event.getNodeUrl());
                        break;
                }
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#nodeSourceEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceEvent(RMNodeSourceEvent event) {
        switch (event.getEventType()) {
            case NODESOURCE_CREATED:
                nodeSourcesCreated.add(event.getSourceName());
                break;
            case NODESOURCE_REMOVED:
                nodeSourcesRemoved.add(event.getSourceName());
                break;
            case NODESOURCE_NODES_ACQUISTION_INFO_ADDED:
                break;
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#rmEvent(org.ow2.proactive.resourcemanager.common.event.RMEvent)
     */
    public void rmEvent(RMEvent event) {
        switch (event.getEventType()) {
            case STARTED:
            case SHUTTING_DOWN:
            case SHUTDOWN:
        }
    }
}
