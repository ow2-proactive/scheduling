package org.ow2.proactive.resourcemanager.utils;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * This class represents the statistics of the Resource Manager. Different kind of 
 * values are collected by interpreting incoming events from the Resource Manager.
 * <p>
 * An instance of this class must not be accessed and modified concurrently.
 * The concurrency is handled by the {@link AtomicRMStatisticsHolder} class. 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public final class RMStatistics {
    /** The state of the Resource Manager */
    private RMEventType rmStatus;
    /** The current available nodes count */
    private int availableNodesCount;
    /** The count of nodes in {@link NodeState#FREE} state */
    private int freeNodesCount;
    /** The count of nodes in {@link NodeState#BUSY} state */
    private int busyNodesCount;
    /** The count of nodes in {@link NodeState#TO_BE_RELEASED} state */
    private int toBeReleasedNodesCount;
    /** The count of nodes in {@link NodeState#DOWN} state */
    private int downNodesCount;
    /** The maximum free nodes count */
    private int maxFreeNodes;
    /** The maximum busy nodes count */
    private int maxBusyNodes;
    /** The maximum to be released nodes count */
    private int maxToBeReleasedNodes;
    /** The maximum down nodes count */
    private int maxDownNodes;
    /** The cumulative activity time */
    private long cumulativeActivityTime;
    /** The cumulative inactivity time */
    private long cumulativeInactivityTime;
    /** A time stamp used for cumulative times computation */
    private long previousTimeStamp;

    /**
     * Creates a new instance of the this class.
     */
    public RMStatistics() {
        // Set the initial status as shutdown
        this.rmStatus = RMEventType.SHUTDOWN;

        // Initialize all nodes related counts
        this.availableNodesCount = 0;
        this.freeNodesCount = 0;
        this.busyNodesCount = 0;
        this.toBeReleasedNodesCount = 0;
        this.downNodesCount = 0;
        this.maxFreeNodes = 0;
        this.maxBusyNodes = 0;
        this.maxToBeReleasedNodes = 0;
        this.maxDownNodes = 0;

        // Initialize all cumulative times
        this.cumulativeInactivityTime = 0;
        this.cumulativeActivityTime = 0;

        // Initialize the time stamp
        this.previousTimeStamp = System.nanoTime();
    }

    /**
     * Copy constructor.
     */
    public RMStatistics(final RMStatistics rmStatistics) {
        this.updateFrom(rmStatistics);
    }

    /**
     * Updates all fields of this class from an existing instance.
     * @param rmStatistics existing instance of this class
     * @return the current instance of this class
     */
    public RMStatistics updateFrom(final RMStatistics rmStatistics) {
        this.rmStatus = rmStatistics.rmStatus;
        this.availableNodesCount = rmStatistics.availableNodesCount;
        this.freeNodesCount = rmStatistics.freeNodesCount;
        this.busyNodesCount = rmStatistics.busyNodesCount;
        this.toBeReleasedNodesCount = rmStatistics.toBeReleasedNodesCount;
        this.downNodesCount = rmStatistics.downNodesCount;
        this.maxFreeNodes = rmStatistics.maxFreeNodes;
        this.maxBusyNodes = rmStatistics.maxBusyNodes;
        this.maxToBeReleasedNodes = rmStatistics.toBeReleasedNodesCount;
        this.maxDownNodes = rmStatistics.maxDownNodes;
        this.cumulativeInactivityTime = rmStatistics.cumulativeInactivityTime;
        this.cumulativeActivityTime = rmStatistics.cumulativeActivityTime;
        this.previousTimeStamp = rmStatistics.previousTimeStamp;
        return this;
    }

    /**
     * Handle incoming node events of the Resource Manager
     * @param event incoming event
     */
    public void nodeEvent(final RMNodeEvent event) {
        // Update cumulative times based on activity and inactivity during the last time interval
        final long currentTimeStamp = System.nanoTime();
        final long timeInterval = currentTimeStamp - this.previousTimeStamp;
        this.cumulativeActivityTime += this.busyNodesCount * timeInterval;
        this.cumulativeInactivityTime += this.freeNodesCount * timeInterval;
        // Update the previous time stamp to the current
        this.previousTimeStamp = currentTimeStamp;

        // Depending on the event type update nodes count
        switch (event.getEventType()) {
            case NODE_ADDED:
                // Increment the available nodes count
                this.availableNodesCount++;
                // When a node is added, it is free so increment the free nodes count
                this.incrementFreeNodesCount();
                break;
            case NODE_REMOVED:
                // Get the state of the node before it was removed
                final NodeState nodeState = event.getNodeState();
                switch (nodeState) {
                    case FREE:
                        this.decrementFreeNodesCount();
                        break;
                    case BUSY:
                        this.decrementBusyNodesCount();
                        break;
                    case TO_BE_RELEASED:
                        this.decrementToBeReleasedNodesCount();
                        break;
                    case DOWN:
                        this.decrementDownNodesCount();
                        break;
                    default:
                        // Unknown NodeState
                }
                this.decrementAvailableNodesCount();
            case NODE_STATE_CHANGED:
                // Depending on the previous state decrement counters
                final NodeState previousNodeState = event.getPreviousNodeState();
                if (previousNodeState != null) {
                    switch (previousNodeState) {
                        case FREE:
                            this.decrementFreeNodesCount();
                            break;
                        case BUSY:
                            this.decrementBusyNodesCount();
                            break;
                        case TO_BE_RELEASED:
                            this.decrementToBeReleasedNodesCount();
                            break;
                        case DOWN:
                            this.decrementDownNodesCount();
                            break;
                        default:
                            // Unknown NodeState
                    }
                }
                final NodeState currentNodeState = event.getNodeState(); // can't be null
                // Depending on the current state increment counters
                switch (currentNodeState) {
                    case FREE:
                        this.incrementFreeNodesCount();
                        break;
                    case BUSY:
                        this.incrementBusyNodesCount();
                        break;
                    case TO_BE_RELEASED:
                        this.incrementToBeReleasedNodesCount();
                        break;
                    case DOWN:
                        this.incrementDownNodesCount();
                        break;
                    default:
                        // Unknown NodeState
                }
            default:
                // Unknown RMEventType
        }
    }

    /**
     * Handle incoming Resource Manager events
     * @param event The Resource Manager event
     */
    public void rmEvent(RMEvent event) {
        this.rmStatus = event.getEventType();
    }

    ////////////////////////////
    // INTERNAL METHODS
    ////////////////////////////

    private void incrementFreeNodesCount() {
        // Increment and update free nodes max value		
        if (++this.freeNodesCount > this.maxFreeNodes) {
            this.maxFreeNodes = this.freeNodesCount;
        }
    }

    private void incrementBusyNodesCount() {
        // Increment and update busy nodes max value		
        if (++this.busyNodesCount > this.maxBusyNodes) {
            this.maxBusyNodes = this.busyNodesCount;
        }
    }

    private void incrementToBeReleasedNodesCount() {
        // Increment and update toBeReleased nodes max value		
        if (++this.toBeReleasedNodesCount > this.maxToBeReleasedNodes) {
            this.maxToBeReleasedNodes = this.toBeReleasedNodesCount;
        }
    }

    private void incrementDownNodesCount() {
        // Increment and update down nodes max value
        if (++this.downNodesCount > this.maxDownNodes) {
            this.maxDownNodes = this.downNodesCount;
        }
    }

    private void decrementAvailableNodesCount() {
        // Decrement available nodes count (keep always >= 0)
        if (this.availableNodesCount > 0) {
            this.availableNodesCount--;
        }
    }

    private void decrementFreeNodesCount() {
        // Decrement free nodes count (keep always >= 0)
        if (this.freeNodesCount > 0) {
            this.freeNodesCount--;
        }
    }

    private void decrementBusyNodesCount() {
        // Decrement busy nodes count (keep always >= 0)
        if (this.busyNodesCount > 0) {
            this.busyNodesCount--;
        }
    }

    private void decrementToBeReleasedNodesCount() {
        // Decrement toBeReleased nodes count (keep always >= 0)
        if (this.toBeReleasedNodesCount > 0) {
            this.toBeReleasedNodesCount--;
        }
    }

    private void decrementDownNodesCount() {
        // Decrement down nodes count (keep always >= 0)
        if (this.downNodesCount > 0) {
            this.downNodesCount--;
        }
    }

    ///////////////////////////
    // ACCESSORS
    ///////////////////////////

    /**
     * Returns the current status of the resource manager. 
     * @return the current status of the resource manager
     */
    public String getRMStatus() {
        return this.rmStatus.toString();
    }

    /** 
     * Returns the current number of available nodes.
     * @return the current number of available nodes
     */
    public int getAvailableNodesCount() {
        return this.availableNodesCount;
    }

    /** 
     * Returns the current number of nodes in {@link NodeState#FREE} state.
     * @return the current number of free nodes
     */
    public int getFreeNodesCount() {
        return this.freeNodesCount;
    }

    /**
     * Returns the current number of nodes in {@link NodeState#BUSY} state.
     * @return the current number of busy nodes
     */
    public int getBusyNodesCount() {
        return this.busyNodesCount;
    }

    /**
     * Returns the current number of nodes in {@link NodeState#TO_BE_RELEASED} state.
     * @return the current number of busy nodes
     */
    public int getToBeReleasedNodesCount() {
        return this.toBeReleasedNodesCount;
    }

    /**
     * Returns the current number of nodes in {@link NodeState#DOWN} state.
     * @return the current number of down nodes
     */
    public int getDownNodesCount() {
        return this.downNodesCount;
    }

    /**
     * Returns the maximum number of free nodes.
     * @return the maximum number of free nodes
     */
    public int getMaxFreeNodes() {
        return this.maxFreeNodes;
    }

    /**
     * Returns the maximum number of busy nodes.
     * @return the maximum number of busy nodes
     */
    public int getMaxBusyNodes() {
        return this.maxBusyNodes;
    }

    /**
     * Returns the maximum number of toBeReleased nodes.
     * @return the maximum number of toBeReleased nodes
     */
    public int getMaxToBeReleasedNodes() {
        return this.maxBusyNodes;
    }

    /**
     * Returns the maximum number of down nodes.
     * @return the maximum number of down nodes
     */
    public int getMaxDownNodes() {
        return this.maxDownNodes;
    }

    /**
     * Returns the nodes activity time percentage.
     * @return the nodes activity time percentage
     */
    public double getActivityTimePercentage() {
        final double total = this.cumulativeActivityTime + this.cumulativeInactivityTime;
        if (total == 0) {
            return 0;
        }
        return (((double) this.cumulativeActivityTime / total) * 100);
    }

    /**
     * Returns the nodes inactivity time percentage.
     * @return the nodes inactivity time percentage
     */
    public double getInactivityTimePercentage() {
        final double total = this.cumulativeActivityTime + this.cumulativeInactivityTime;
        if (total == 0) {
            return 0;
        }
        return (((double) this.cumulativeInactivityTime / total) * 100);
    }
}
