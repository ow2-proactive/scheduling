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


/**
 * This class represents a Managed Bean to allow the management of the Resource Manager 
 * following the JMX standard for management.
 * It provides some attributes and some statistics indicators.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class RMWrapperAdmin extends RMWrapperAnonym implements RMWrapperAdminMBean {

    private int timePercentageOfNodesInactivity;

    private int timePercentageOfNodesUsage;

    private int totalTimeOfAllAvailableNodes;

    private long previousTimeStamp;

    protected void nodeAdded() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        //update fields
        super.nodeAdded();
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    protected void nodeBusy() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        super.nodeBusy();
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    protected void nodeDown(boolean busy) {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        super.nodeDown(busy);
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    protected void nodeFree() {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        super.nodeFree();
    }

    /**
     * This is a canonical event to calculate the Key Performance Indicator 
     * about the average busy percentage time of a node
     *
     * @param event
     */
    protected void nodeRemovedEvent(boolean busy, boolean free) {
        // Each time that there`s an event, update global percentage based on the number of free and
        // on the number of used nodes in the previous event
        long interval = (System.currentTimeMillis() - this.previousTimeStamp);
        this.timePercentageOfNodesInactivity += this.numberOfFreeNodes * interval;
        this.timePercentageOfNodesUsage += this.numberOfBusyNodes * interval;
        this.totalTimeOfAllAvailableNodes += ((this.numberOfFreeNodes * interval) + (this.numberOfBusyNodes * interval));
        this.previousTimeStamp = System.currentTimeMillis();
        // Update fields
        super.nodeRemovedEvent(busy, free);
    }

    /**
     * It`s the percentage time of inactivity of all the available nodes 
     * 
     * @return the percentage time of nodes inactivity as integer
     */
    public int getTimePercentageOfNodesInactivity() {
        if (this.totalTimeOfAllAvailableNodes == 0) {
            return 0;
        }
        return (int) (((double) this.timePercentageOfNodesInactivity / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    /**
     * It`s the percentage time of usage of all the available nodes
     * 
     * @return the percentage time of nodes usage as integer
     */
    public int getTimePercentageOfNodesUsage() {
        if (this.totalTimeOfAllAvailableNodes == 0) {
            return 0;
        }
        return (int) (((double) this.timePercentageOfNodesUsage / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    // UTILITY METHODS

    /**
     * Getter method for the KPI value timePercentageOfNodesInactivity as double
     * 
     * @return the current percentage time of nodes inactivity as double
     */
    public double getTimePercentageOfNodesInactivityAsDouble() {
        if (this.totalTimeOfAllAvailableNodes == 0) {
            return 0;
        }
        return (((double) this.timePercentageOfNodesInactivity / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }

    /**
     * Getter method for the KPI value timePercentageOfNodesUsage as double
     * 
     * @return the current percentage time of nodes usage as double
     */
    public double getTimePercentageOfNodesUsageAsDouble() {
        if (this.totalTimeOfAllAvailableNodes == 0) {
            return 0;
        }
        return (((double) this.timePercentageOfNodesUsage / (double) this.totalTimeOfAllAvailableNodes) * 100);
    }
}
