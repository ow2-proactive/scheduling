/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.rm;

import java.util.HashSet;
import java.util.Set;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.tests.performance.jmeter.rm.RMEventsMonitor;


public class NodeSourceEventsMonitor extends RMEventsMonitor {

    private volatile String sourceName;

    private volatile boolean nodeDeploymentFailed;

    private volatile Set<String> deployedNodes;

    private volatile boolean nodeSourceRemoved;

    private volatile StringBuilder nodeSourceEventsLog;

    public NodeSourceEventsMonitor() {
    }

    public NodeSourceEventsMonitor(String sourceName) {
        clearState(sourceName);
    }

    public void clearState(String sourceName) {
        this.sourceName = sourceName;
        this.nodeDeploymentFailed = false;
        this.deployedNodes = new HashSet<String>();
        this.nodeSourceEventsLog = new StringBuilder();
    }

    @Override
    public synchronized void rmEvent(RMEvent event) {
        // System.out.println("Event " + event.getEventType());
    }

    @Override
    public synchronized void nodeSourceEvent(RMNodeSourceEvent event) {
        // System.out.println("Event " + event.getEventType());

        if (!sourceName.equals(event.getSourceName())) {
            return;
        }
        nodeSourceEventsLog.append("Event " + event.getEventType()).append("\n");
        if (event.getEventType() == RMEventType.NODESOURCE_REMOVED) {
            nodeSourceRemoved = true;
            notifyAll();
        }
    }

    @Override
    public synchronized void nodeEvent(RMNodeEvent event) {
        // System.out.println("Event " + event.getEventType() + " " + event.getNodeState());
        if (!sourceName.equals(event.getNodeSource())) {
            return;
        }
        nodeSourceEventsLog.append("Event " + event.getEventType() + " " + event.getNodeState()).append("\n");
        if (!deployedNodes.contains(event.getNodeUrl())) {
            if (event.getEventType().equals(RMEventType.NODE_STATE_CHANGED)) {
                if (event.getNodeState() != NodeState.FREE) {
                    System.out.println("Error, unexpected node state: " + event.getNodeState() + "(host: " +
                        event.getHostName() + ", node info: " + event.getNodeInfo() + ")");
                    nodeDeploymentFailed = true;
                } else {
                    deployedNodes.add(event.getNodeUrl());
                }

                notifyAll();
            }
        }
    }

    public synchronized boolean waitForNodeSourceRemoval(long timeout) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeout;
        while (!nodeSourceRemoved) {
            long waitTime = endTime - System.currentTimeMillis();
            if (waitTime > 0) {
                wait(waitTime);
            } else {
                break;
            }
        }

        if (!nodeSourceRemoved) {
            System.out.println("All node source events:\n" + nodeSourceEventsLog);
        }

        return nodeSourceRemoved;
    }

    public synchronized boolean waitForNodesInitialization(int nodesNumber, long timeout)
            throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeout;
        while (!nodeDeploymentFailed && deployedNodes.size() < nodesNumber) {
            long waitTime = endTime - System.currentTimeMillis();
            if (waitTime > 0) {
                wait(waitTime);
            } else {
                break;
            }
        }
        boolean result = !nodeDeploymentFailed && deployedNodes.size() == nodesNumber;
        if (!result) {
            System.out.println("All node source events:\n" + nodeSourceEventsLog);
        }
        return result;
    }

}
