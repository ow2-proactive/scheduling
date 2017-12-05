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
package org.ow2.proactive.scheduler.task;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskId;


/**
 * This class is used in the task recovery mechanism. If, when a task finishes,
 * the TaskTermination active object cannot be contacted anymore (maybe due to
 * a transient failure of the scheduler), then it will use the
 * TaskLauncherRebinder to reacquire a reference to the
 * TaskTerminateNotification active object.
 *
 * @author ActiveEon Team
 * @since 25/09/17
 */
public class TaskLauncherRebinder {

    private static final Logger logger = Logger.getLogger(TaskLauncherRebinder.class);

    private TaskId taskId;

    private String taskTerminateNotificationHandlerNodeURL;

    public void saveTaskTerminateNotificationURL(TaskId taskId, TaskTerminateNotification terminateNotification) {
        this.taskId = taskId;
        try {
            this.taskTerminateNotificationHandlerNodeURL = PAActiveObject.getActiveObjectNode(terminateNotification)
                                                                         .getNodeInformation()
                                                                         .getURL();
        } catch (NodeException e) {
            logger.warn("The URL of TaskTerminatedNotification handler of task " + taskId.getReadableName() +
                        " cannot be retrieved", e);
        }
    }

    /**
     * Perform a lookup of the TaskTerminateNotification active object of the
     * scheduler, e.g. to check that it is still alive when a task finishes.
     *
     * @param terminateNotification The TaskTerminateNotification that was used at the time the task was launched
     * @return a correct reference to a TaskTerminateNotification, or null if none can be retrieved
     */
    public TaskTerminateNotification makeSureSchedulerIsConnected(TaskTerminateNotification terminateNotification) {
        try {
            PAActiveObject.lookupActive(TaskTerminateNotification.class, PAActiveObject.getUrl(terminateNotification));
            return terminateNotification;
        } catch (Exception e) {
            logger.warn("TaskTerminatedNotification handler of task " + taskId.getReadableName() +
                        " is disconnected from the scheduler, try to rebind it", e);
            return getRebindedTaskTerminateNotificationHandler();
        }
    }

    /**
     * Attempt to reaquire a correct reference to the TaskTerminateNotification
     * active object from a previously saved URL for this object.
     *
     * @return a correct reference to a TaskTerminateNotification, or null if none can be retrieved
     */
    public TaskTerminateNotification getRebindedTaskTerminateNotificationHandler() {
        try {
            logger.debug("List AOs on " + taskTerminateNotificationHandlerNodeURL + " (expect only one): " +
                         Arrays.toString(NodeFactory.getNode(taskTerminateNotificationHandlerNodeURL)
                                                    .getActiveObjects(TaskTerminateNotification.class.getName())));
            Node node = NodeFactory.getNode(taskTerminateNotificationHandlerNodeURL);
            Object[] aos = node.getActiveObjects(TaskTerminateNotification.class.getName());
            logger.info("On node " + node.getNodeInformation().getName() + " number of active objects found is " +
                        aos.length + " and the first one " + aos[0] + " will be used to send back the task result");
            return (TaskTerminateNotification) aos[0];
        } catch (Throwable e) {
            // error when retrieving the termination handler after reconnection
            logger.error("Failed to rebind TaskTerminatedNotification handler of task " + taskId.getReadableName() +
                         " from URL " + taskTerminateNotificationHandlerNodeURL, e);
            return null;
        }
    }

}
