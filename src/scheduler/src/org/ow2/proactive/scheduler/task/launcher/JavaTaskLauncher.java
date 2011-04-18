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
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.JavaExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * JavaTaskLauncher is the class that will start a Java Process.
 *
 * @author The ProActive Team
 */
public class JavaTaskLauncher extends TaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);
    protected static final String NODE_DATASPACE_SCRATCHDIR = "node.dataspace.scratchdir";

    /**
     * ProActive Empty Constructor
     */
    public JavaTaskLauncher() {
    }

    /**
     * Constructor of the java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public JavaTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify or null if the finalizeTask method is not to be called
     * @param executableContainer contains the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    @Override
    public TaskResult doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        long duration = -1;
        long sample = 0;
        // Executable result (res or ex)
        Throwable exception = null;
        Serializable userResult = null;
        // TaskResult produced by doTask
        TaskResultImpl res = null;
        try {
            //init dataspace
            initDataSpaces();
            replaceDSIterationTag();

            sample = System.currentTimeMillis();
            //copy datas from OUTPUT or INPUT to local scratch
            copyInputDataToScratch();
            sample = System.currentTimeMillis() - sample;
            logger_dev.info("Time spent copying INPUT datas to SCRATCH : " + sample + " ms");

            if (!hasBeenKilled) {
                // set exported vars
                this.setPropagatedProperties(results);

                // create the executable (will set the context class loader to the taskclassserver)
                currentExecutable = executableContainer.getExecutable();
            }

            //launch pre script
            if (!hasBeenKilled && pre != null) {
                sample = System.currentTimeMillis();
                this.executePreScript();
                duration += System.currentTimeMillis() - sample;
            }

            if (!hasBeenKilled) {
                //init task
                ExecutableInitializer initializer = executableContainer.createExecutableInitializer();
                replaceIterationTags(initializer);
                // if an exception occurs in init method, unwrapp the InvocationTargetException
                // the result of the execution is the user level exception
                try {
                    callInternalInit(JavaExecutable.class, JavaExecutableInitializer.class, initializer);
                } catch (InvocationTargetException e) {
                    throw e.getCause() != null ? e.getCause() : e;
                }
                sample = System.currentTimeMillis();
                try {
                    //launch task
                    userResult = currentExecutable.execute(results);
                } catch (Throwable t) {
                    exception = t;
                }
                duration += System.currentTimeMillis() - sample;
            }

            //for the next two steps, task could be killed anywhere
            if (!hasBeenKilled) {
                sample = System.currentTimeMillis();
                //copy output file
                copyScratchDataToOutput();
                sample = System.currentTimeMillis() - sample;
                logger_dev.info("Time spent copying SCRATCH datas to OUTPUT : " + sample + " ms");
            }

            if (!hasBeenKilled && post != null) {
                sample = System.currentTimeMillis();
                //launch post script
                this.executePostScript(exception == null);
                duration += System.currentTimeMillis() - sample;
            }
        } catch (Throwable ex) {
            logger_dev.debug("Exception occured while running task " + this.taskId + ": ", ex);
            exception = ex;
            userResult = null;
        } finally {
            if (!hasBeenKilled) {
                // set the result
                if (exception != null) {
                    res = new TaskResultImpl(taskId, exception, null, duration, null);
                } else {
                    res = new TaskResultImpl(taskId, userResult, null, duration, null);
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
                    res = new TaskResultImpl(taskId, e, null, duration, null);
                    // action is set to default as the script was not evaluated
                    res.setAction(FlowAction.getDefaultAction(this.flow));
                }
                res.setPropagatedProperties(retreivePropagatedProperties());
                res.setLogs(this.getLogs());
            }

            // finalize doTask
            terminateDataSpace();
            // This call is conditioned by the isKilled ...
            // An example when we don't want to finalize task is when using
            // forked java task, then only finalizing loggers is enough.
            this.finalizeTask(core);
        }
        return res;
    }

    /**
     * Replaces iteration and replication index syntactic macros
     * in various string locations across the task descriptor
     *
     * @param init the executable initializer containing the Java arguments
     */
    protected void replaceIterationTags(ExecutableInitializer init) {
        JavaExecutableInitializer jinit = (JavaExecutableInitializer) init;
        try {
            Map<String, Serializable> args = jinit.getArguments(Thread.currentThread()
                    .getContextClassLoader());
            for (Entry<String, Serializable> arg : args.entrySet()) {
                if (arg.getValue() instanceof String) {
                    String str = ((String) arg.getValue()).replace(ITERATION_INDEX_TAG, "" +
                        this.iterationIndex);
                    str = str.replace(REPLICATION_INDEX_TAG, "" + this.replicationIndex);
                    jinit.setArgument(arg.getKey(), str);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Configure node to use dataspace !
     * MUST ONLY BE USED BY FORKED EXECUTABLE
     * 
     * @return true if configuration went right
     */
    public boolean configureNode() {
        try {
            // configure node for Data Spaces
            String scratchDir;
            if (System.getProperty(NODE_DATASPACE_SCRATCHDIR) == null) {
                //if scratch dir java property is not set, set to default
                scratchDir = System.getProperty("java.io.tmpdir");
            } else {
                //else use the property
                scratchDir = System.getProperty(NODE_DATASPACE_SCRATCHDIR);
            }
            final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(null,
                scratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
        } catch (Throwable t) {
            logger_dev.error("Cannot configure dataSpace", t);
            return false;
        }
        return true;
    }

    /**
     * Close node dataspace configuration !
     * MUST ONLY BE USED BY FORKED EXECUTABLE
     * 
     * @return true if configuration went right
     */
    @ImmediateService
    public boolean closeNodeConfiguration() {
        try {
            DataSpacesNodes.closeNodeConfig(PAActiveObject
                    .getActiveObjectNode(PAActiveObject.getStubOnThis()));
        } catch (Throwable t) {
            logger_dev.error("Cannot close properly DataSpaces.", t);
            return false;
        }
        return true;
    }
}
