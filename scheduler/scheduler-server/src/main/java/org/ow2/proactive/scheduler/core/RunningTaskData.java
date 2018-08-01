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
package org.ow2.proactive.scheduler.core;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.utils.NodeSet;


class RunningTaskData {

    private final InternalTask task;

    private final String user;

    private final Credentials credentials;

    private final TaskLauncher launcher;

    private final NodeSet nodes;

    private int pingAttempts = 0;

    private boolean restarting = false;

    private long firstFailedPingAttemptTime = -1;

    RunningTaskData(InternalTask task, String user, Credentials credentials, TaskLauncher launcher) {
        this.task = task;
        // keep track of nodes that executed the task, can change in case of restarts
        if (task.getExecuterInformation() != null) {
            this.nodes = task.getExecuterInformation().getNodes();
        } else {
            this.nodes = new NodeSet();
        }
        this.user = user;
        this.credentials = credentials;
        this.launcher = launcher;
    }

    /**
     * @return Mutable task state, can be changed by multiple threads
     */
    InternalTask getTask() {
        return task;
    }

    TaskLauncher getLauncher() {
        return launcher;
    }

    String getUser() {
        return user;
    }

    Credentials getCredentials() {
        return credentials;
    }

    public synchronized int increaseAndGetPingAttempts() {
        return ++pingAttempts;
    }

    /**
     * Declare a task launcher failure, if not declared previously
     */
    public synchronized void setTaskLauncherFailure() {
        if (this.firstFailedPingAttemptTime < 0) {
            this.firstFailedPingAttemptTime = System.currentTimeMillis();
        }
    }

    /**
     * Resets the task launcher failure time (launcher has been pinged successfully)
     */
    public synchronized void resetTaskLauncherFailure() {
        this.firstFailedPingAttemptTime = -1;
    }

    /**
     * Returns the time since the task launcher is not pingable, 0 if the launcher is pingable
     * @return
     */
    public synchronized long getTaskLauncherFailurePeriod() {
        if (firstFailedPingAttemptTime >= 0) {
            return System.currentTimeMillis() - this.firstFailedPingAttemptTime;
        } else {
            return 0;
        }
    }

    public synchronized int getPingAttempts() {
        return pingAttempts;
    }

    public synchronized boolean isRestarting() {
        return restarting;
    }

    public synchronized void setRestarting() {
        this.restarting = true;
    }

    /**
     * @return Nodes that were used to run this particular instance of the task
     *  (those in {@link InternalTask#getExecuterInformation()} could have changed in case of restarts)
     */
    public NodeSet getNodes() {
        return nodes;
    }

    public Node getNodeExecutor() {
        return nodes.get(0);
    }
}
