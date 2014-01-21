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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ScriptExecutableInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


/**
 * ScriptTaskLauncher will run a script executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@ActiveObject
public class ScriptTaskLauncher extends TaskLauncher {

    public static final Logger logger = Logger.getLogger(ScriptTaskLauncher.class);

    /**
     * ProActive Empty Constructor
     */
    public ScriptTaskLauncher() {
    }

    /**
     * Constructor of the script task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public ScriptTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify or null if the finalizeTask method is not to be called
     * @param executableContainer contains the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     */
    @Override
    public void doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        doTaskAndGetResult(core, executableContainer, results);
    }

    public TaskResult doTaskAndGetResult(TaskTerminateNotification core,
            ExecutableContainer executableContainer, TaskResult... results) {
        long duration = -1;
        long sample = 0;
        long intervalms = 0;
        // Executable result (res or ex)
        Throwable exception = null;
        Serializable userResult = null;
        // TaskResult produced by doTask
        TaskResultImpl res;
        try {
            //init dataspace
            executableGuard.initDataSpaces();
            replaceTagsInDataspaces();

            updatePropagatedVariables(results);

            // create the executable (will set the context class loader to the taskclassserver)
            executableGuard.initialize(executableContainer.getExecutable());

            sample = System.nanoTime();
            //copy datas from OUTPUT or INPUT to local scratch
            executableGuard.copyInputDataToScratch();
            intervalms = Math.round(Math.ceil((System.nanoTime() - sample) / 1000));
            logger.info("Time spent copying INPUT datas to SCRATCH : " + intervalms + " ms");

            // set exported vars
            this.setPropagatedProperties(results);

            //launch pre script
            if (pre != null) {
                sample = System.nanoTime();
                executableGuard.executePreScript();
                duration += System.nanoTime() - sample;
            }

            //init task
            ScriptExecutableInitializer initializer = (ScriptExecutableInitializer) executableContainer
                    .createExecutableInitializer();

            sample = System.nanoTime();
            try {
                userResult = executableGuard.execute(results);
                this.flushStreams();

            } catch (Throwable t) {
                exception = t;
            }
            duration += System.nanoTime() - sample;

            //for the next two steps, task could be killed anywhere

            if (post != null) {
                sample = System.nanoTime();
                //launch post script
                executableGuard.executePostScript(exception == null);
                duration += System.nanoTime() - sample;
            }

            sample = System.nanoTime();
            //copy output file
            executableGuard.copyScratchDataToOutput();
            intervalms = Math.round(Math.ceil((System.nanoTime() - sample) / 1000));
            logger.info("Time spent copying SCRATCH datas to OUTPUT : " + intervalms + " ms");
            logger.info("Task terminated without error");
        } catch (Throwable ex) {
            logger.info("Exception occured while running task " + this.taskId + ": ", ex);
            exception = ex;
            userResult = null;
        } finally {
            if (!executableGuard.wasKilled()) {
                // set the result
                if (exception != null) {
                    res = new TaskResultImpl(taskId, exception, null, duration / 1000000, null);
                } else {
                    res = new TaskResultImpl(taskId, userResult, null, duration / 1000000, null);
                }
                try {
                    // logs have to be retrieved after flowscript exec if any
                    if (flow != null) {
                        // *WARNING* : flow action is set in res UNLESS an exception is thrown !
                        // see FlowAction.getDefaultAction()
                        this.executeFlowScript(res);
                    }
                } catch (Throwable e) {
                    // task result is now the exception thrown by flowscript
                    // flowaction is set to default
                    res = new TaskResultImpl(taskId, e, null, duration / 1000000, null);
                    // action is set to default as the script was not evaluated
                    res.setAction(FlowAction.getDefaultAction(this.flow));
                }
                res.setPropagatedProperties(retreivePropagatedProperties());
                attachPropagatedVariables(res);
            } else {
                res = new TaskResultImpl(taskId, new RuntimeException("Task " + taskId + " has been killed"), null, duration / 1000000, null);
            }
            res.setLogs(this.getLogs());

            // This call is conditioned by the isKilled ...
            // An example when we don't want to finalize task is when using
            // forked java task, then only finalizing loggers is enough.
            this.finalizeTask(core, res);
        }
        return res;
    }

}
