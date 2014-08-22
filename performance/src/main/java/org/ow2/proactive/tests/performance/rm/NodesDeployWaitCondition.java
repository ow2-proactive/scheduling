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

import java.util.LinkedHashSet;
import java.util.Set;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.tests.performance.utils.WaitFailedException;


public class NodesDeployWaitCondition extends RMWaitCondition {

    private final String sourceName;

    private final int nodesNumber;

    private boolean nodeDeploymentFailed;

    private Set<String> deployedNodes = new LinkedHashSet<String>();

    public NodesDeployWaitCondition(String sourceName, int nodesNumber) {
        this.nodesNumber = nodesNumber;
        this.sourceName = sourceName;
    }

    @Override
    public synchronized void nodeEvent(RMNodeEvent event) {
        if (!sourceName.equals(event.getNodeSource())) {
            return;
        }
        System.out.println("Event " + event.getEventType() + " " + event.getNodeState());
        addEventLog("Event " + event.getEventType() + " " + event.getNodeState());
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

    @Override
    public boolean stopWait() throws WaitFailedException {
        if (nodeDeploymentFailed) {
            throw new WaitFailedException("Node deployment failed");
        }
        return deployedNodes.size() == nodesNumber;
    }

}
