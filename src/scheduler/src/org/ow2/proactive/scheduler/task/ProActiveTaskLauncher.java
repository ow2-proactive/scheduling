/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.common.scripting.Script;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.ProActiveExecutable;
import org.ow2.proactive.scheduler.core.SchedulerCore;


/**
 * ProActive task Launcher will be able to launch an application task
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class ProActiveTaskLauncher extends TaskLauncher {

    /** execution nodes list */
    private NodeSet nodesList;

    /**
     * ProActive empty constructor.
     */
    public ProActiveTaskLauncher() {
    }

    /**
     * Create a new instance of ProActiveTaskLauncher
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId the identification of the task to launch.
     */
    public ProActiveTaskLauncher(TaskId taskId) {
        super(taskId);
    }

    /**
     * Create a new instance of ProActiveTaskLauncher
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId the identification of the task to launch.
     * @param pre the script executed before the task is launched.
     * @param post the script executed after the task is launched.
     */
    public ProActiveTaskLauncher(TaskId taskId, Script<?> pre, Script<?> post) {
        super(taskId, pre, post);
    }

    /**
     * @see org.ow2.proactive.scheduler.task.TaskLauncher#initActivity(org.objectweb.proactive.Body)
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
        PAActiveObject.setImmediateService("getNodes");
    }

    /**
     * This method should have NEVER been called in an ProActive task launcher.
     */
    @Override
    public TaskResult doTask(SchedulerCore core, ExecutableContainer executableContainer,
            TaskResult... results) {
        throw new RuntimeException("This method should have NEVER been called in this context !!");
    }

    /**
     * Execute the user ProActive task as an active object.
     * This will provide the user the number of asked nodes.
     *
     * @param core The scheduler core to be notify
     * @param executableContainer contains the ProActive task to execute.
     * @param nodes the nodes that the user needs to run his ProActive task.
     * @return a task result representing the result of this task execution.
     */
    @SuppressWarnings("unchecked")
    public TaskResult doTask(SchedulerCore core, JavaExecutableContainer executableContainer, NodeSet nodes) {
        nodesList = nodes;

        try {
            nodesList.add(super.getNodes().get(0));
        } catch (NodeException e) {
        }

        try {
            currentExecutable = executableContainer.getExecutable();

            //launch pre script
            if (pre != null) {
                for (Node node : nodes) {
                    this.executePreScript(node);
                }
            }

            //init task
            currentExecutable.init();

            if (isWallTime())
                scheduleTimer();

            //launch task
            Serializable userResult = ((ProActiveExecutable) currentExecutable).execute(nodes);

            //launch post script
            if (post != null) {
                for (Node node : nodes) {
                    this.executePostScript(node);
                }
            }

            //logBuffer is filled up
            TaskLogs taskLogs = new Log4JTaskLogs(this.logAppender.getStorage());

            //return result
            return new TaskResultImpl(taskId, userResult, taskLogs);
        } catch (Throwable ex) {
            // exceptions are always handled at scheduler core level
            return new TaskResultImpl(taskId, ex, new Log4JTaskLogs(this.logAppender.getStorage()));
        } finally {
            if (isWallTime())
                cancelTimer();
            // reset stdout/err
            try {
                this.finalizeLoggers();
            } catch (RuntimeException e) {
                // exception should not be thrown to the scheduler core
                // the result has been computed and must be returned !
                // TODO : logger.warn
                System.err.println("WARNING : Loggers are not shut down !");
            }

            //terminate the task
            core.terminate(taskId);
        }
    }

    /**
     * Return the nodes list.
     *
     * @return the nodesList.
     */
    @Override
    public NodeSet getNodes() {
        return nodesList;
    }
}
