/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.JobResultImpl;
import org.ow2.proactive.scheduler.resourcemanager.ResourceManagerProxy;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.threading.CallableWithTimeoutAction;


/**
 * TimedDoTaskAction is used to start the task execution in parallel.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class TimedDoTaskAction implements CallableWithTimeoutAction<Void> {

    private AtomicBoolean timeoutCalled = new AtomicBoolean(false);
    private SchedulerCore schedulerCore;
    private InternalJob job;
    private InternalTask task;
    private TaskLauncher launcher;
    private Node node;
    private SchedulerCore coreStub;
    private ResourceManagerProxy resourceManager;
    private TaskResult[] parameters;

    /**
     * Create a new instance of TimedDoTaskAction
     *
     * @param job the internal job
     * @param task the internal task
     * @param launcher the launcher of the task
     * @param node the main node on which the task will be started
     * @param coreStub the stub on SchedulerCore
     * @param parameters the parameters to be given to the task
     */
    public TimedDoTaskAction(SchedulerCore core, InternalJob job, InternalTask task, TaskLauncher launcher,
            Node node, SchedulerCore coreStub, ResourceManagerProxy resourceManager, TaskResult[] parameters) {
        this.schedulerCore = core;
        this.job = job;
        this.task = task;
        this.launcher = launcher;
        this.node = node;
        this.coreStub = coreStub;
        this.resourceManager = resourceManager;
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     */
    public Void call() throws Exception {
        try {
            //if a task has been launched
            if (launcher != null) {

                TaskResult tr = launcher.doTask(coreStub, task.getExecutableContainer(), parameters);

                if (timeoutCalled.get()) {
                    launcher.terminate();
                    freenodes();
                } else {
                    ((JobResultImpl) job.getJobResult()).storeFuturResult(task.getName(), tr);
                    //mark the task and job (if needed) as started and send events
                    schedulerCore.finalizeStarting(job, task, node);
                }
            }
        } catch (Exception e) {
            freenodes();
        }
        //results is not needed
        return null;
    }

    private void freenodes() {
        try {
            //launcher node
            resourceManager.freeNode(node);
            //multi nodes task
            resourceManager.freeNodes(task.getExecutableContainer().getNodes());
        } catch (Throwable ni) {
            //miam miam
        }
    }

    public void timeoutAction() {
        timeoutCalled.set(true);
    }

}
