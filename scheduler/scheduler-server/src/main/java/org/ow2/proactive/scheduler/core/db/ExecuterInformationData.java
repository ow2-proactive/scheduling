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

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.utils.NodeSet;


/**
 * @author ActiveEon Team
 * @since 21/09/17
 */
public class ExecuterInformationData implements Serializable {

    private static final Logger logger = Logger.getLogger(ExecuterInformationData.class);

    private long taskId;

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
                } catch (ProActiveRuntimeException e) {
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
        TaskLauncher taskLauncher = new TaskLauncher();
        if (taskLauncherNodeUrl != null) {
            try {
                taskLauncher = PAActiveObject.lookupActive(TaskLauncher.class, taskLauncherNodeUrl);
                logger.info("Retrieve task launcher " + taskLauncherNodeUrl + " successfully for task " + taskId);
            } catch (Exception e) {
                if (loadFullState) {
                    logger.warn("Task launcher " + taskLauncherNodeUrl + " of task " + taskId +
                                " cannot be looked up. Trying to rebind it", e);
                    taskLauncher = getRebindedTaskLauncher();
                }
            }
        }
        return new ExecuterInformation(taskLauncher, nodes, nodeName, hostName);
    }

    private TaskLauncher getRebindedTaskLauncher() {
        try {
            logger.debug("List AOs on " + taskLauncherNodeUrl + " (expect only one): " +
                         Arrays.toString(NodeFactory.getNode(taskLauncherNodeUrl).getActiveObjects()));
            Object[] aos = NodeFactory.getNode(taskLauncherNodeUrl).getActiveObjects();
            return (TaskLauncher) aos[0];
        } catch (Throwable e) {
            logger.error("Failed to rebind TaskLauncher " + taskLauncherNodeUrl + " of task " + taskId, e);
            return new TaskLauncher();
        }
    }

}
