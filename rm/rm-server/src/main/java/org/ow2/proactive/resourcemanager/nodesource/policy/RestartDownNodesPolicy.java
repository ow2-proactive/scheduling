/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;


/**
 *
 * A policy that acquires nodes once and of node becomes down tries to restart it.
 *
 */
@ActiveObject
public class RestartDownNodesPolicy extends NodeSourcePolicy {

    private static Logger logger = Logger.getLogger(RestartDownNodesPolicy.class);

    private static final String TIMER_NAME = "RestartDownNodesPolicy node status check";

    @Configurable(description = "ms (30 mins by default)", dynamic = true)
    private int checkNodeStateEach = 30 * 60 * 1000; // 30 mins by default;

    private Timer timer;

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        BooleanWrapper parentConfigured = super.configure(policyParameters);
        setCheckNodeStateEach(policyParameters);
        return parentConfigured;
    }

    @Override
    public void reconfigure(Object... updatedPolicyParameters) {
        super.reconfigure(updatedPolicyParameters);
        this.timer.cancel();
        setCheckNodeStateEach(updatedPolicyParameters);
        scheduleRestartTimer();
    }

    private void setCheckNodeStateEach(Object[] policyParameters) {
        if (policyParameters != null && policyParameters.length > 2) {
            this.checkNodeStateEach = Integer.parseInt(policyParameters[2].toString());
        }
    }

    /**
     * Activates static policy. Register a listener in RMMonitoring
     */
    @Override
    public BooleanWrapper activate() {

        acquireAllNodes();
        scheduleRestartTimer();

        return new BooleanWrapper(true);
    }

    private void scheduleRestartTimer() {

        this.timer = new Timer(TIMER_NAME);

        this.timer.schedule(new TimerTask() {

            @Override
            public void run() {
                checkDownNodesAndReacquireNodesIfNeeded();
            }

        }, this.checkNodeStateEach, this.checkNodeStateEach);
    }

    private void checkDownNodesAndReacquireNodesIfNeeded() {

        long numberOfNodesToDeploy = this.nodeSource.getDownNodes()
                                                    .stream()
                                                    .map(node -> node.getNodeInformation().getURL())
                                                    .peek(nodeUrl -> logger.info("Removing down node " + nodeUrl))
                                                    .map(nodeUrl -> this.nodeSource.getRMCore().removeNode(nodeUrl,
                                                                                                           true))
                                                    .filter(BooleanWrapper::getBooleanValue)
                                                    .peek(x -> logger.info("Down node removed"))
                                                    .count();

        numberOfNodesToDeploy += this.nodeSource.getDeployingAndLostNodes()
                                                .stream()
                                                .filter(RMDeployingNode::isLost)
                                                .map(RMDeployingNode::getNodeURL)
                                                .peek(nodeUrl -> logger.info("Removing lost node " + nodeUrl))
                                                .map(nodeUrl -> this.nodeSource.getRMCore().removeNode(nodeUrl, true))
                                                .filter(BooleanWrapper::getBooleanValue)
                                                .peek(x -> logger.info("Lost node removed"))
                                                .count();

        if (numberOfNodesToDeploy > 0) {
            logger.info("Acquiring " + numberOfNodesToDeploy + " nodes");
            acquireNodes((int) numberOfNodesToDeploy);
        }
    }

    @Override
    public void shutdown(Client initiator) {
        this.timer.cancel();
        super.shutdown(initiator);
    }

    /**
     * Description for the UI
     */
    @Override
    public String getDescription() {
        return "Static nodes acquisition. If node becomes down policy tries to restart it.";
    }

}
