/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * %$ACTIVEEON_INITIAL_DEV$
 */

package functionaltests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;


public class RMEventMonitor implements RMEventListener {

    private List<RMEventMonitor.RMWaitCondition> waitConditions = new ArrayList<>();

    public void addWaitCondition(RMEventMonitor.RMWaitCondition waitCondition) {
        synchronized (waitConditions) {
            waitConditions.add(waitCondition);
        }
    }

    public boolean waitFor(RMEventMonitor.RMWaitCondition waitCondition, long timeout) {
        synchronized (waitConditions) {
            if (!waitConditions.contains(waitCondition)) {
                throw new IllegalArgumentException("Unknown wait condition.");
            }

            try {
                long endTime = System.currentTimeMillis() + timeout;
                synchronized (waitCondition) {
                    while (!waitCondition.stopWait()) {
                        long newTimeOut = endTime - System.currentTimeMillis();
                        if (newTimeOut > 0) {
                            waitCondition.wait(newTimeOut);
                        } else {
                            break;
                        }
                    }
                }
                return waitCondition.stopWait();
            } catch (Exception e) {
                return false;
            }
        }

    }

    @Override
    public void nodeEvent(RMNodeEvent nodeEvent) {
        synchronized (waitConditions) {
            for (RMEventMonitor.RMWaitCondition waitCondition : waitConditions) {
                waitCondition.nodeEvent(nodeEvent);
            }
        }
    }

    @Override
    public void nodeSourceEvent(RMNodeSourceEvent arg0) {
    }

    @Override
    public void rmEvent(RMEvent arg0) {
    }

    static abstract class RMWaitCondition implements RMEventListener {
        @Override
        public void nodeEvent(RMNodeEvent arg0) {
        }

        @Override
        public void nodeSourceEvent(RMNodeSourceEvent arg0) {
        }

        @Override
        public void rmEvent(RMEvent arg0) {
        }

        abstract boolean stopWait();
    }

    static class RMNodesDeployedWaitCondition extends RMWaitCondition {
        private String nodeSource;
        private int expectedNumOfNodes;

        private Set<String> deployedNodes = new HashSet<>();
        private boolean nodesDeploymentFailed = false;

        public RMNodesDeployedWaitCondition(String nodeSource, int expectedNumOfNodes) {
            this.nodeSource = nodeSource;
            this.expectedNumOfNodes = expectedNumOfNodes;
        }

        @Override
        public synchronized void nodeEvent(RMNodeEvent nodeEvent) {
            if (!nodeSource.equals(nodeEvent.getNodeSource())) {
                return;
            }
            if (!deployedNodes.contains(nodeEvent.getNodeUrl())) {
                if (RMEventType.NODE_STATE_CHANGED.equals(nodeEvent.getEventType())) {
                    if (NodeState.FREE.equals(nodeEvent.getNodeState())) {
                        deployedNodes.add(nodeEvent.getNodeUrl());
                    } else {
                        System.out.println(String.format(
                                "Error, unexpected node state: %s (host:%s, nodeinfo:%s)", nodeEvent
                                        .getNodeState(), nodeEvent.getHostName(), nodeEvent.getNodeInfo()));
                        nodesDeploymentFailed = true;
                    }
                    notifyAll();
                }
            }
        }

        @Override
        boolean stopWait() {
            if (nodesDeploymentFailed) {
                throw new RuntimeException("Nodes deployment failed.");
            }
            return deployedNodes.size() == expectedNumOfNodes;
        }
    }

}
