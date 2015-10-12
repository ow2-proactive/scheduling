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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.HashMap;
import java.util.Map.Entry;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


/**
 * 
 * NodeSource Policy for Amazon EC2
 * <p>
 * Acquires resources according to the current load of the Scheduler as
 * specified by {@link SchedulerLoadingPolicy}
 * <p>
 * Releases resources only on the last 10 minutes of the last paid hour to
 * minimize costs
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 * 
 */
@ActiveObject
public class EC2Policy extends SchedulerLoadingPolicy {

    {
        // 40 mn
        nodeDeploymentTimeout = 40 * 60 * 1000;
    }

    /**
     * Paid instance duration in Milliseconds: time after which the instance has
     * to be paid again. For AWS EC2, one hour
     */
    private final static int releaseDelay = 60 * 60 * 1000; // 1 hour

    /**
     * Nodes can be released if t = [releaseDelay - threshold, releasDelay]
     */
    private final static int threshold = 10 * 60 * 1000; // 10 minutes

    /**
     * associates a Node URL with a acquisition time the time (as return by
     * System.currentTimeMillis()) is actually when it was registered in the RM,
     * not the VM startup in AWS accounting, which probably occurred ~2mn sooner
     */
    private HashMap<String, Long> nodes = new HashMap<>();

    /**
     * EC2Policy AO empty non-arg constructor
     */
    public EC2Policy() {

    }

    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);
        return new BooleanWrapper(true);
    }

    @Override
    protected void removeNode() {
        String bestFree = null;
        String bestBusy = null;
        String bestDown = null;

        long t = System.currentTimeMillis();

        /*
         * A Node can be removed only if (minutes_since_acquisisiont % 60 < 10),
         * ie. we are in the last 10 minutes of the last paid hour Down nodes
         * are removed in priority, then free nodes, then busy nodes
         */

        for (Entry<String, Long> node : nodes.entrySet()) {
            long rt = releaseDelay - ((t - node.getValue()) % releaseDelay);
            NodeState state = null;
            try {
                state = nodeSource.getRMCore().getNodeState(node.getKey());
            } catch (Throwable exc) {
                // pending / configuring
                continue;
            }

            switch (state) {
                case BUSY:
                case CONFIGURING:
                case DEPLOYING:
                    if (rt < threshold) {
                        bestBusy = node.getKey();
                    }
                    break;
                case LOST:
                case DOWN:
                    if (rt < threshold) {
                        bestDown = node.getKey();
                    }
                    break;
                case FREE:
                    if (rt < threshold) {
                        bestFree = node.getKey();
                    }
                    break;
            }
        }

        if (bestDown != null) {
            removeNode(bestDown, false);
            this.nodes.remove(bestDown);
        } else if (bestFree != null) {
            removeNode(bestFree, false);
            this.nodes.remove(bestFree);
        } else if (bestBusy != null) {
            removeNode(bestBusy, false);
            this.nodes.remove(bestBusy);
        } else {
            // no node can be removed, cancel request
            timeStamp = 0;
        }

    }

    /*
     * Store the time at which EC2 instances register to the RM The actual
     * starting time in EC2 accounting might have been a couple minutes sooner
     * 
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive.scheduler.resourcemanager.nodesource.policy.
     * SchedulerLoadingPolicy
     * #nodeEvent(org.ow2.proactive.resourcemanager.common.event.RMNodeEvent)
     */
    @Override
    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_ADDED:
                this.nodes.put(event.getNodeUrl(), System.currentTimeMillis());
                break;
            case NODE_REMOVED:
                this.nodes.remove(event.getNodeUrl());
        }

        super.nodeEvent(event);
    }
}
