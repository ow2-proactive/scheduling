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
package org.ow2.proactive.scheduler.task.launcher;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SocketAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.SweetCountDownLatch;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.appenders.AsyncAppenderWithStorage;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.LoggingOutputStream;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.KillTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.NodeSet;


/**
 * Abstract Task Launcher.
 * This is the most simple task launcher.
 * It is able to launch a java task only.
 * You can extend this launcher in order to create a specific launcher.
 * With this default launcher, you can get the node on which the task is running and kill the task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public abstract class TaskLauncher implements InitActive {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    /**
     * Scheduler related java properties. Thoses properties are automatically 
     * translated into system property when the task is native (see NativeTaskLauncher) :
     * SYSENV_NAME = upcase(JAVAENV_NAME).replace('.','_')
     */
    public enum SchedulerVars {
        /**  */
        JAVAENV_JOB_ID_VARNAME("pas.job.id"),
        /**  */
        JAVAENV_JOB_NAME_VARNAME("pas.job.name"),
        /**  */
        JAVAENV_TASK_ID_VARNAME("pas.task.id"),
        /**  */
        JAVAENV_TASK_NAME_VARNAME("pas.task.name");

        String varName;

        SchedulerVars(String vn) {
            varName = vn;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        public String toString() {
            return varName;
        }
    }

    protected TaskId taskId;
    protected Script<?> pre;
    protected Script<?> post;

    // handle streams
    protected transient PrintStream redirectedStdout;
    protected transient PrintStream redirectedStderr;
    // default appender for log storage
    protected transient AsyncAppenderWithStorage logAppender;

    // not null if an executable is currently executed
    protected Executable currentExecutable;
    // true if the executable has been stopped before its normal termination
    protected boolean hasBeenKilled;
    // true if finalizeLoggers has been called
    private final AtomicBoolean loggersFinalized = new AtomicBoolean(false);
    // true if loggers are currently activated
    private final AtomicBoolean loggersActivated = new AtomicBoolean(false);
    // 0 if the launcher (loggers and env) have been initialized
    private final SweetCountDownLatch launcherInitialized = new SweetCountDownLatch(1);

    /** Maximum execution time of the task (in milliseconds), the variable is only valid if isWallTime is true */
    protected long wallTime = 0;

    /** The timer that will terminate the launcher if the task doesn't finish before the walltime */
    protected KillTask killTaskTimer = null;

    /**
     * ProActive empty constructor.
     */
    public TaskLauncher() {
    }

    /**
     * Constructor with task identification.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize every task launcher.
     */
    public TaskLauncher(TaskLauncherInitializer initializer) {
        this.taskId = initializer.getTaskId();
        this.pre = initializer.getPreScript();
        this.post = initializer.getPostScript();
        if (initializer.getWalltime() > 0) {
            this.wallTime = initializer.getWalltime();
        }
    }

    /**
     * Initializes the activity of the active object.
     * @param body the body of the active object being initialized
     */
    public void initActivity(Body body) {
        // TODO cdelbe : race condition remains here 
        // see PROACTIVE-652
        // see SCHEDULING-263
        PAActiveObject.setImmediateService("activateLogs");
        PAActiveObject.setImmediateService("getNodes");
        PAActiveObject.setImmediateService("terminate");
        logger_dev.debug("Immediate services : getNodes, activateLogs, terminate");

        // plug stdout/err into a socketAppender
        this.initLoggers();
        // set scheduler defined env variables
        this.initEnv();
        // set the launcher as initialized
        this.launcherInitialized.countDown();
        logger_dev.debug("TaskLauncher initialized");
    }

    /**
     * Common final behavior for any type of task launcher.
     * @param core reference to the scheduler.
     */
    protected void finalizeTask(TaskTerminateNotification core) {
        // unset env
        this.unsetEnv();
        // reset stdout/err
        try {
            this.finalizeLoggers();
        } catch (RuntimeException e) {
            // exception should not be thrown to the scheduler core
            // the result has been computed and must be returned !
            logger_dev.warn("Loggers are not shutdown !", e);
        }

        //terminate the task
        // if currentExecutable has been killed, no call back
        if (!hasBeenKilled) {
            core.terminate(taskId);
        }
        this.currentExecutable = null;
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param execContainer contains the user defined executable to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    public abstract TaskResult doTask(TaskTerminateNotification core, ExecutableContainer execContainer,
            TaskResult... results);

    /**
     * Redirect stdout/err in the buffered appender.
     */
    @SuppressWarnings("unchecked")
    protected void initLoggers() {
        logger_dev.info("Init loggers");
        // error about log should not be logged
        LogLog.setQuietMode(true);
        // create logger
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + this.taskId.getJobId());
        l.setAdditivity(false);
        MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
        try {
            MDC.getContext().put(Log4JTaskLogs.MDC_HOST,
                    PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
        } catch (NodeException e) {
            MDC.getContext().put(Log4JTaskLogs.MDC_HOST, "Unknown host");
        }
        l.removeAllAppenders();
        // create an async appender for multiplexing (storage plus redirect through socketAppender)
        // int logMaxSize = PASchedulerProperties.LOGS_MAX_SIZE.getValueAsInt();
        this.logAppender = new AsyncAppenderWithStorage();//logMaxSize);
        l.addAppender(this.logAppender);
        // redirect stdout and err
        this.redirectedStdout = new PrintStream(new LoggingOutputStream(l, Level.INFO), true);
        this.redirectedStderr = new PrintStream(new LoggingOutputStream(l, Level.ERROR), true);
        System.setOut(redirectedStdout);
        System.setErr(redirectedStderr);
    }

    /**
     * Set scheduler related variables for the current task. 
     */
    protected void initEnv() {
        System.setProperty("" + SchedulerVars.JAVAENV_JOB_ID_VARNAME, "" + this.taskId.getJobId());
        System.setProperty("" + SchedulerVars.JAVAENV_JOB_NAME_VARNAME, "" +
            this.taskId.getJobId().getReadableName());
        System.setProperty("" + SchedulerVars.JAVAENV_TASK_ID_VARNAME, "" + this.taskId);
        System.setProperty("" + SchedulerVars.JAVAENV_TASK_NAME_VARNAME, "" + this.taskId.getReadableName());
    }

    /**
     * Reset scheduler related variables value.
     */
    // TODO cdelbe : reset to an empty string ? Useful ?
    protected void unsetEnv() {
        System.setProperty("" + SchedulerVars.JAVAENV_JOB_ID_VARNAME, "");
        System.setProperty("" + SchedulerVars.JAVAENV_JOB_NAME_VARNAME, "");
        System.setProperty("" + SchedulerVars.JAVAENV_TASK_ID_VARNAME, "");
        System.setProperty("" + SchedulerVars.JAVAENV_TASK_NAME_VARNAME, "");
    }

    /**
     * Activate the logs on this host and port.
     * @param logSink the provider for the appender to write in.
     */
    @SuppressWarnings("unchecked")
    public void activateLogs(AppenderProvider logSink) {
        this.launcherInitialized.await();
        synchronized (this.loggersFinalized) {
            logger_dev.info("Activating logs for task " + this.taskId);
            if (this.loggersActivated.get()) {
                logger_dev.info("Logs for task " + this.taskId + " are already activated");
                return;
            }
            this.loggersActivated.set(true);
            // should reset taskId because calling thread is not active thread (immediate service)
            MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
            try {
                MDC.getContext().put(Log4JTaskLogs.MDC_HOST,
                        PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
            } catch (NodeException e) {
                MDC.getContext().put(Log4JTaskLogs.MDC_HOST, "Unknown host");
            }
            // create appender
            Appender a = null;
            try {
                a = logSink.getAppender();
            } catch (LogForwardingException e) {
                logger_dev.error("Cannot create log appender.", e);
                return;
            }
            // fill appender
            if (!this.loggersFinalized.get()) {
                this.logAppender.addAppender(a);
            } else {
                logger_dev.info("Logs for task " + this.taskId + " are closed. Flushing buffer...");
                // Everything is closed: reopen and close...
                for (LoggingEvent e : this.logAppender.getStorage()) {
                    a.doAppend(e);
                }
                a.close();
                this.loggersActivated.set(false);
                return;
            }
            logger_dev.info("Activated logs for task " + this.taskId);
        }
    }

    /**
     * Close scheduler task logger and reset stdout/err
     */
    protected void finalizeLoggers() {
        synchronized (this.loggersFinalized) {
            logger_dev.info("Terminating loggers for task " + this.taskId + "...");
            this.loggersFinalized.set(true);
            this.loggersActivated.set(false);
            //Unhandle loggers
            this.flushStreams();
            this.logAppender.close();
            System.setOut(System.out);
            System.setErr(System.err);
            logger_dev.info("Terminated loggers for task " + this.taskId);
        }
    }

    /**
     * Flush out and err streams.
     */
    protected void flushStreams() {
        this.redirectedStdout.flush();
        this.redirectedStderr.flush();
    }

    /**
     * Return a TaskLogs object that contains the logs produced by the executed tasks
     * @return a TaskLogs object that contains the logs produced by the executed tasks
     */
    protected TaskLogs getLogs() {
        this.flushStreams();
        TaskLogs logs = new Log4JTaskLogs(this.logAppender.getStorage());
        return logs;
    }

    /**
     * Execute the preScript on the node n, or on the default node if n is null.
     * 
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePreScript(Node n) throws ActiveObjectCreationException, NodeException,
            UserException {
        logger_dev.info("Executing pre-script");
        ScriptHandler handler = ScriptLoader.createHandler(n);
        ScriptResult<String> res = handler.handle(pre);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on pre-script occured : ", res.getException());
            throw new UserException("Pre-script has failed on the current node", res.getException());
        }
        // flush prescript output
        this.flushStreams();
    }

    /**
     * Execute the postScript on the node n, or on the default node if n is null.
     * 
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePostScript(Node n) throws ActiveObjectCreationException, NodeException,
            UserException {
        logger_dev.info("Executing post-script");
        ScriptHandler handler = ScriptLoader.createHandler(n);
        ScriptResult<String> res = handler.handle(post);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on post-script occured : ", res.getException());
            throw new UserException("Post-script has failed on the current node", res.getException());
        }
        // flush postscript output
        this.flushStreams();
    }

    /**
     * To get the node(s) on which this active object has been launched.
     *
     * @return the node(s) of this active object.
     * @throws NodeException
     */
    public NodeSet getNodes() throws NodeException {
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.add(PAActiveObject.getNode());

        return new NodeSet(new ArrayList<Node>(nodes));
    }

    /**
     * This method will terminate the task that has been launched.
     * In fact it will terminate the launcher.
     */
    public void terminate() {

        if (this.currentExecutable != null) {
            this.currentExecutable.kill();
            this.hasBeenKilled = true;
            this.currentExecutable = null;
        }

        // unset env
        this.unsetEnv();
        // reset stdout/err    
        try {
            this.finalizeLoggers();
        } catch (RuntimeException e) {
            // exception should not be thrown to the scheduler core
            // the result has been computed and must be returned !
            logger_dev.warn("Loggers are not shut down !", e);
        }
        PAActiveObject.terminateActiveObject(true);
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltime then the timer will be canceled
     */
    protected void scheduleTimer() {
        logger_dev.info("Execute timer because task '" + taskId + "' is walltimed");
        scheduleTimer(currentExecutable);
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltime then the timer will be canceled
     */
    protected void scheduleTimer(Executable executable) {
        if (isWallTime()) {
            killTaskTimer = new KillTask(executable, wallTime);
            killTaskTimer.schedule();
        }
    }

    /**
     * Canceling timer for killing the task, if we cancel the timer this means that the task finished before the walltime
     */
    protected void cancelTimer() {
        if (isWallTime() && killTaskTimer != null) {
            killTaskTimer.cancel();
        }
    }

    /**
     * @return the isWallTime
     */
    public boolean isWallTime() {
        return wallTime > 0;
    }

}
