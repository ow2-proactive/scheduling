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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * This class represents a Managed Bean to allow the management of the Resource Manager 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class RMWrapper implements RMWrapperMBean {
    /** The state of the Resource Manager */
    private String rMState = "STOPPED";

    /** Variables representing the fields of the MBean */
    private int totalNumberOfNodes = 0;

    private int numberOfDownNodes = 0;

    private int numberOfFreeNodes = 0;

    private int numberOfBusyNodes = 0;

    private int timePercentageOfNodesInactivity;

    private int timePercentageOfNodesUsage;

    private int totalTimeOfAllAvailableNodes;

    private long previousTimeStamp;

    /**
     * Method to manage node events of the Resource Manager
     * 
     * @param event
     */
    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_ADDED:
                nodeAdded();
                break;
            case NODE_STATE_CHANGED:
                switch (event.getNodeState()) {
                    case BUSY:
                        nodeBusy();
                        break;
                    case DOWN:
                        nodeDown(event.getNodeState() == NodeState.BUSY);
                        break;
                    case FREE:
                        nodeFree();
                        break;
                }
                break;
            case NODE_REMOVED:
                nodeRemovedEvent(event.getNodeState() == NodeState.BUSY,
                        event.getNodeState() == NodeState.FREE);
                break;
        }
    }

    private void nodeAdded() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        //Update fields
        this.totalNumberOfNodes++;
        //When a node is added, initially, it is free
        this.numberOfFreeNodes++;
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    private void nodeBusy() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        this.numberOfFreeNodes--;
        this.numberOfBusyNodes++;
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    private void nodeDown(boolean busy) {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        if (busy) {
            this.numberOfBusyNodes--;
            this.numberOfDownNodes++;
        } else {
            this.numberOfFreeNodes--;
            this.numberOfDownNodes++;
        }

    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    private void nodeFree() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        this.numberOfBusyNodes--;
        this.numberOfFreeNodes++;
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    private void nodeRemovedEvent(boolean busy, boolean free) {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        this.totalNumberOfNodes--;
        //Check the state of the removed node
        if (busy) {
            this.numberOfBusyNodes--;
        } else if (free) {
            this.numberOfFreeNodes--;
        } else {
            //If the node is not busy, nor free, it is down
            this.numberOfDownNodes--;
        }
    }

    public void rmEvent(RMEvent event) {
        switch (event.getEventType()) {
            case STARTED:
                rMState = "STARTED";
                break;
            case SHUTTING_DOWN:
                rMState = "SHUTTING_DOWN";
                break;
            case SHUTDOWN:
                rMState = "STOPPED";
                break;
        }
    }

    /**
     * Methods to get the attributes of the RMWrapper MBean
     * 
     * @return the current number of down nodes
     */
    public int getNumberOfDownNodes() {
        return this.numberOfDownNodes;
    }

    /** 
     * @return the current number of free nodes
     */
    public int getNumberOfFreeNodes() {
        return this.numberOfFreeNodes;
    }

    /** 
     * @return the current number of busy nodes
     */
    public int getNumberOfBusyNodes() {
        return this.numberOfBusyNodes;
    }

    /** 
     * @return the current number of total nodes available
     */
    public int getTotalNumberOfNodes() {
        return this.totalNumberOfNodes;
    }

    /** 
     * @return the current state of the resource manager
     */
    public String getRMState() {
        return this.rMState;
    }

    /**
     * It`s the percentage time of inactivity of all the available nodes 
     * 
     * @return the percentage time of nodes inactivity as integer
     */
    public int getTimePercentageOfNodesInactivity() {
        return (int) (((double) this.timePercentageOfNodesInactivity / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    /**
     * It`s the percentage time of usage of all the available nodes
     * 
     * @return the percentage time of nodes usage as integer
     */
    public int getTimePercentageOfNodesUsage() {
        return (int) (((double) this.timePercentageOfNodesUsage / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    // UTILITY METHODS

    /**
     * Getter method for the KPI value timePercentageOfNodesInactivity as double
     * 
     * @return the current percentage time of nodes inactivity as double
     */
    public double getTimePercentageOfNodesInactivityAsDouble() {
        return (((double) this.timePercentageOfNodesInactivity / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    /**
     * Getter method for the KPI value timePercentageOfNodesUsage as double
     * 
     * @return the current percentage time of nodes usage as double
     */
    public double getTimePercentageOfNodesUsageAsDouble() {
        return (((double) this.timePercentageOfNodesUsage / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }
}
