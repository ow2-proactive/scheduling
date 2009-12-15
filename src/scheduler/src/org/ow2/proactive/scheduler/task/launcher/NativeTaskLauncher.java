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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutable;
import org.ow2.proactive.scheduler.task.NativeExecutableInitializer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This launcher is the class that will launch a native process.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class NativeTaskLauncher extends TaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    private static final String DATASPACE_TAG = "\\$LOCALSPACE";

    /**
     * ProActive Empty Constructor
     */
    public NativeTaskLauncher() {
    }

    /**
     * Constructor of the native task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public NativeTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        super.initActivity(body);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param executableContainer contains the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    @Override
    public TaskResult doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        long duration = -1;
        long sample = 0;
        try {

            //copy datas from OUTPUT or INPUT to local scratch
            copyInputDataToScratch();

            //get Executable before schedule timer
            currentExecutable = executableContainer.getExecutable();
            //start walltime if needed
            if (isWallTime()) {
                scheduleTimer();
            }

            sample = System.currentTimeMillis();
            //execute pre-script
            if (pre != null) {
                this.executePreScript(PAActiveObject.getNode());
            }
            duration = System.currentTimeMillis() - sample;

            //init task
            ExecutableInitializer execInit = executableContainer.createExecutableInitializer();
            replaceWorkingDirDSTags(execInit);
            callInternalInit(NativeExecutable.class, NativeExecutableInitializer.class, execInit);

            //replace dataspace tags in command (if needed) by local scratch directory
            replaceCommandDSTags();

            Throwable exception = null;
            Serializable userResult = null;
            sample = System.currentTimeMillis();
            try {
                //launch task
                logger_dev.debug("Starting execution of task '" + taskId + "'");
                userResult = currentExecutable.execute(results);
            } catch (Throwable t) {
                exception = t;
            }
            duration += System.currentTimeMillis() - sample;

            //copy output file
            copyScratchDataToOutput();

            sample = System.currentTimeMillis();
            //launch post script
            if (post != null) {
                int retCode = Integer.parseInt(userResult.toString());
                this.executePostScript(PAActiveObject.getNode(), retCode == 0 && exception == null);
            }
            duration += System.currentTimeMillis() - sample;

            //throw exception if needed
            if (exception != null) {
                throw exception;
            }

            //logBuffer is filled up
            TaskResult result = new TaskResultImpl(taskId, userResult, this.getLogs(), duration);

            //return result
            return result;
        } catch (Throwable ex) {
            logger_dev.info("", ex);
            // exceptions are always handled at scheduler core level
            return new TaskResultImpl(taskId, ex, this.getLogs(), duration);
        } finally {
            terminateDataSpace();
            if (isWallTime()) {
                cancelTimer();
            }
            this.finalizeTask(core);
        }
    }

    private void replaceWorkingDirDSTags(ExecutableInitializer execInit) {
        String wd = ((NativeExecutableInitializer) execInit).getWorkingDir();
        if (wd != null) {
            wd = wd.replaceAll(DATASPACE_TAG, SCRATCH.getRealURI().replace("file://", ""));
        }
        ((NativeExecutableInitializer) execInit).setWorkingDir(wd);
    }

    private void replaceCommandDSTags() throws Exception {
        String[] args = ((NativeExecutable) currentExecutable).getCommand();
        //I cannot use DataSpace to get the local scratch path
        if (SCRATCH != null) {
            String fullScratchPath = SCRATCH.getRealURI().replace("file://", "");
            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].replaceAll(DATASPACE_TAG, fullScratchPath);
            }
        }
    }

}
