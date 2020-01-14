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
package org.ow2.proactive.scheduler.core.db;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObjectFactory;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.utils.NodeSet;

import com.google.common.util.concurrent.Uninterruptibles;


/**
 * @author ActiveEon Team
 * @since 21/09/17
 */
public class ExecuterInformationData implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ExecuterInformationData.class);

    private static transient volatile boolean pamrReconnectionTimeoutSpent = false;

    private long taskId;

    // This naming is misleading as the field contains the task launcher and not the node url
    // changing the name will introduce a serialization incompatible change
    private String taskLauncherNodeUrl;

    private NodeSet nodes;

    private String nodeName;

    private String hostName;

    public ExecuterInformationData(long taskId, ExecuterInformation executerInformation) {
        this.taskId = taskId;
        if (executerInformation != null) {
            if (executerInformation.getLauncher() != null) {
                try {
                    taskLauncherNodeUrl = PAActiveObject.getUrl(executerInformation.getLauncher());
                } catch (Exception e) {
                    logger.warn("TaskLauncher node URL could not be retrieved for task " + taskId);
                }
            }
            nodes = executerInformation.getNodes();
            nodeName = executerInformation.getNodeName();
            hostName = executerInformation.getHostName();
        }
    }

    /**
     * Rebuild an executer information from the data. A stub to the task
     * launcher is attempted to be retrieved.
     * @param loadFullState whether it is important to have a task launcher
     *                      stub in the end (it is important if the task is
     *                      running)
     */
    public ExecuterInformation toExecuterInformation(boolean loadFullState) {
        TaskLauncher taskLauncher = null;
        if (taskLauncherNodeUrl != null) {
            try {
                taskLauncher = PAActiveObject.lookupActive(TaskLauncher.class, taskLauncherNodeUrl);
                logger.info("Retrieve task launcher " + taskLauncherNodeUrl + " successfully for task " + taskId);
            } catch (Exception e) {
                if (loadFullState) {
                    logger.warn("Task launcher " + taskLauncherNodeUrl + " of task " + taskId +
                                " cannot be looked up, try to rebind it");
                    taskLauncher = getReboundTaskLauncherIfStillExist();
                }
            }
        }
        return new ExecuterInformation(taskLauncher, nodes, nodeName, hostName);
    }

    private TaskLauncher getReboundTaskLauncherIfStillExist() {
        TaskLauncher answer = null;
        boolean nodeConnected = false;
        boolean isPAMR = taskLauncherNodeUrl.startsWith(PAMRRemoteObjectFactory.PROTOCOL_ID + "://");
        // When using the pamr protocol, nodes (pamr agents) will reconnect by re-registering with the router using the same agent id.
        // Each reconnection attempt is using a maximum interval of Agent.MAXIMUM_RETRY_DELAY_MS milliseconds, the router will not know of these agents until the reconnection occurs.
        long initialTime = System.currentTimeMillis();
        String nodeUrl = nodes.get(0).getNodeInformation().getURL();
        boolean taskLauncherFound = false;
        do {
            try {
                logger.debug("List AOs on " + nodeUrl + " (expect only one): ");
                Object[] aos = NodeFactory.getNode(nodeUrl).getActiveObjects();
                nodeConnected = true;
                logger.debug(Arrays.toString(aos));
                taskLauncherFound = aos.length > 0;
                if (!taskLauncherFound) {
                    // node was found but task launcher is absent, exit the loop
                    break;
                }
                answer = (TaskLauncher) aos[0];
            } catch (NodeException t) {
                if (isPAMR) {
                    logger.debug("Failed to access node " + nodeUrl);
                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                }
            } catch (Throwable t) {
                logger.warn("Error while retrieving task launcher", t);
            }
            // Once the maximum pamr reconnection interval has been reached, no agent id re-attribution should occur,
            // which means the nodes are definitely not reachable
            pamrReconnectionTimeoutSpent = pamrReconnectionTimeoutSpent ||
                                           (System.currentTimeMillis() - initialTime > Agent.MAXIMUM_RETRY_DELAY_MS);
        } while (!nodeConnected && isPAMR && !pamrReconnectionTimeoutSpent);

        if (!nodeConnected || answer == null) {
            logger.warn("Failed to rebind TaskLauncher of task " + taskId + ". TaskLauncher with node URL: " + nodeUrl +
                        " could not be looked up. Running task cannot be recovered, it will be restarted if possible.");
            answer = new TaskLauncher();
        }
        return answer;
    }
}
