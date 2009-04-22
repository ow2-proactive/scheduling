/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package functionaltests.nodesource;

import java.util.ArrayList;
import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.MutableInteger;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;


/**
 * This class implements a RM's monitor. It provides method to wait reception of
 * a certain number of events, and getters for checking received events.
 *
 * @author ProActive team
 *
 */
public class RMCustomizedEventReceiver implements InitActive, RMEventListener {

    private MutableInteger nbEventReceived = new MutableInteger(0);

    // array List to store nodes URLs of events thrown by RMMonitoring
    private ArrayList<String> addedNodes;

    /** list of all nodes become removed */
    private ArrayList<String> removedNodes;

    /** list of all nodes sources created*/
    private ArrayList<String> nodeSourcesCreated;

    /** list of all nodes sources created*/
    private ArrayList<String> nodeSourcesRemoved;

    private Vector<RMEventType> types;

    /**
     * ProActive Empty constructor
     */
    public RMCustomizedEventReceiver() {
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
    public RMCustomizedEventReceiver(RMEventType[] list) {
        addedNodes = new ArrayList<String>();
        removedNodes = new ArrayList<String>();
        nodeSourcesCreated = new ArrayList<String>();
        nodeSourcesRemoved = new ArrayList<String>();

        types = new Vector<RMEventType>();
        for (RMEventType eventType : list) {
            types.add(eventType);
        }
    }

    /**
     * clear all events received and stored by this monitor.
     */
    public void cleanEventLists() {
        addedNodes.clear();
        removedNodes.clear();
        nodeSourcesCreated.clear();
        nodeSourcesRemoved.clear();
    }

    /**
     * ProActive InitActivity method. Register to the RMonitoring object.
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("waitForNEvent");
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
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#rmEvent(org.ow2.proactive.resourcemanager.common.event.RMEvent)
     */
    public void rmEvent(RMEvent event) {
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#nodeSourceEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent)
     */
    public void nodeSourceEvent(RMNodeSourceEvent event) {
        if (types.contains(event.getEventType())) {
            synchronized (this.nbEventReceived) {
                this.nbEventReceived.add(1);
                this.nbEventReceived.notify();
            }
        }

        switch (event.getEventType()) {
            case NODESOURCE_CREATED:
                nodeSourcesCreated.add(event.getSourceName());
                break;
            case NODESOURCE_REMOVED:
                nodeSourcesRemoved.add(event.getSourceName());
                break;
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMEventListener#nodeEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeEvent)
     */
    public void nodeEvent(RMNodeEvent event) {
        if (types.contains(event.getEventType())) {
            synchronized (this.nbEventReceived) {
                this.nbEventReceived.add(1);
                this.nbEventReceived.notify();
            }
        }

        switch (event.getEventType()) {
            case NODE_ADDED:
                addedNodes.add(event.getNodeUrl());
                break;
            case NODE_REMOVED:
                removedNodes.add(event.getNodeUrl());
                break;
        }
    }
}
