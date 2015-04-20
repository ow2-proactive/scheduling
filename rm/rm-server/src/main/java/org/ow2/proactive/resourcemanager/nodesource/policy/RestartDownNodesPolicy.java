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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.rmnode.RMDeployingNode;
import org.apache.log4j.Logger;


/**
 *
 * A policy that acquires nodes once and of node becomes down tries to restart it.
 *
 */
@ActiveObject
public class RestartDownNodesPolicy extends NodeSourcePolicy {

    private static final long serialVersionUID = 62L;

    private static Logger logger = Logger.getLogger(RestartDownNodesPolicy.class);

    private Timer timer = new Timer("RestartDownNodesPolicy node status check");

    @Configurable(description = "ms (30 mins by default)")
    private int checkNodeStateEach = 30 * 60 * 1000; // 30 mins by default;

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     * @throws RMException
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        BooleanWrapper parentConfigured = super.configure(policyParameters);
        if (policyParameters != null && policyParameters.length > 2) {
            checkNodeStateEach = Integer.parseInt(policyParameters[2].toString());
        }
        return parentConfigured;
    }

    /**
     * Activates static policy. Register a listener in RMMonitoring
     */
    @Override
    public BooleanWrapper activate() {
        acquireAllNodes();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int numberOfNodesToDeploy = 0;
                for (Node downNode : nodeSource.getDownNodes()) {
                    String nodeUrl = downNode.getNodeInformation().getURL();

                    logger.info("Removing down node " + nodeUrl);
                    BooleanWrapper removed = nodeSource.getRMCore().removeNode(nodeUrl, true);
                    if (removed.getBooleanValue()) {
                        logger.info("Down node removed " + nodeUrl);
                        numberOfNodesToDeploy++;
                    }
                }

                for (RMDeployingNode lostNode : nodeSource.getDeployingNodes()) {
                    if (!lostNode.isLost()) {
                        continue;
                    }
                    String nodeUrl = lostNode.getNodeURL();

                    logger.info("Removing lost node " + nodeUrl);
                    BooleanWrapper removed = nodeSource.getRMCore().removeNode(nodeUrl, true);
                    if (removed.getBooleanValue()) {
                        logger.info("Lost node removed " + nodeUrl);
                        numberOfNodesToDeploy++;
                    }
                }

                if (numberOfNodesToDeploy > 0) {
                    logger.info("Acquiring " + numberOfNodesToDeploy + " nodes");
                    acquireNodes(numberOfNodesToDeploy);
                }
            }
        }, checkNodeStateEach, checkNodeStateEach);

        return new BooleanWrapper(true);
    }

    @Override
    public void shutdown(Client initiator) {
        timer.cancel();
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
