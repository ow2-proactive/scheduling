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

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.ExecuterInformation;
import org.ow2.proactive.utils.NodeSet;


/**
 * @author ActiveEon Team
 * @since 21/09/17
 */
public class ExecuterInformationData implements Serializable {

    private static final Logger logger = Logger.getLogger(ExecuterInformationData.class);

    private String taskLauncherNodeUrl;

    private NodeSet nodes;

    private String nodeName;

    private String hostName;

    public ExecuterInformationData(ExecuterInformation executerInformation) {
        taskLauncherNodeUrl = PAActiveObject.getUrl(executerInformation.getLauncher());
        nodes = executerInformation.getNodes();
        nodeName = executerInformation.getNodeName();
        hostName = executerInformation.getHostName();
    }

    public ExecuterInformation toExecuterInformation(boolean loadFullState) {
        TaskLauncher taskLauncher = new TaskLauncher();
        if (loadFullState) {
            try {
                taskLauncher = PAActiveObject.lookupActive(TaskLauncher.class, taskLauncherNodeUrl);
                logger.info("Retrieve task launcher " + taskLauncherNodeUrl + " successfully");
            } catch (Exception e) {
                logger.warn("Task launcher " + taskLauncherNodeUrl + " cannot be looked up", e);
            }
        }
        return new ExecuterInformation(taskLauncher, nodes, nodeName, hostName);
    }

}
