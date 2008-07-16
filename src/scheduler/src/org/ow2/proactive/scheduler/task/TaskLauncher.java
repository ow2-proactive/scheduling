/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.task;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.resourcemanager.common.scripting.Script;
import org.ow2.proactive.resourcemanager.common.scripting.ScriptHandler;
import org.ow2.proactive.resourcemanager.common.scripting.ScriptLoader;
import org.ow2.proactive.resourcemanager.common.scripting.ScriptResult;
import org.ow2.proactive.resourcemanager.frontend.NodeSet;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.core.SchedulerCore;
import org.ow2.proactive.scheduler.util.logforwarder.BufferedAppender;
import org.ow2.proactive.scheduler.util.logforwarder.LoggingOutputStream;


/**
 * Abstract Task Launcher.
 * This is the most simple task launcher.
 * It is able to launch a java task only.
 * You can extend this launcher in order to create a specific launcher.
 * With this default launcher, you can get the node on which the task is running and kill the task.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public abstract class TaskLauncher implements InitActive {

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

    // handle streams
    protected transient PrintStream redirectedStdout;
    protected transient PrintStream redirectedStderr;
    protected transient BufferedAppender logBuffer;

    // not null if an executable is currently executed
    protected Executable currentExecutable;

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
     * @param taskId represents the task the launcher will execute.
     */
    public TaskLauncher(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * Constructor with task identification.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param taskId represents the task the launcher will execute.
     * @param pre the script executed before the task.
     */
    public TaskLauncher(TaskId taskId, Script<?> pre) {
        this.taskId = taskId;
        this.pre = pre;
    }

    /**
     * Initializes the activity of the active object.
     * @param body the body of the active object being initialized
     */
    public void initActivity(Body body) {
        // plug stdout/err into a socketAppender
        this.initLoggers();
        // set scheduler defined env variables
        this.initEnv();
        PAActiveObject.setImmediateService("getNodes");
        PAActiveObject.setImmediateService("activateLogs");
        PAActiveObject.setImmediateService("terminate");
    }

    /**
     * Common final behavior for any type of task launcher.
     * @param core reference to the scheduler.
     */
    protected void finalizeTask(SchedulerCore core) {
        // unset env
        this.unsetEnv();
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
        // if currentExecutable is null it has been killed, so no call back
        if (this.currentExecutable != null) {
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
    @SuppressWarnings("unchecked")
    public abstract TaskResult doTask(SchedulerCore core, ExecutableContainer execContainer,
            TaskResult... results);

    /**
     * Redirect stdout/err in the buffered appender.
     */
    @SuppressWarnings("unchecked")
    protected void initLoggers() {
        // error about log should not be logged
        LogLog.setQuietMode(true);
        // create logger
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + this.taskId.getJobId());
        l.setAdditivity(false);
        MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
        l.removeAllAppenders();
        this.logBuffer = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME, true);
        l.addAppender(this.logBuffer);
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
     *
     * @param host the host on which to activate the log.
     * @param port the host on which to activate the log.
     */
    public void activateLogs(String host, int port) {
        // should reset taskId because calling thread is not active thread (immediate service)
        MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
        Appender out = new SocketAppender(host, port);
        // already logged events are flushed into out sink
        this.logBuffer.addSink(out);
    }

    /**
     * Close scheduler task logger and reset stdout/err
     */
    protected void finalizeLoggers() {
        //Unhandle loggers
        this.redirectedStdout.flush();
        this.redirectedStderr.flush();
        this.logBuffer.close();
        //FIXME : want to close only the task logger !
        //LogManager.shutdown();
        System.setOut(System.out);
        System.setErr(System.err);
    }

    /**
     * Execute the preScript on the node n, or on the default node if n is null
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    protected void executePreScript(Node n) throws ActiveObjectCreationException, NodeException,
            UserException {
        ScriptHandler handler = ScriptLoader.createHandler(n);
        ScriptResult<String> res = handler.handle(pre);

        if (res.errorOccured()) {
            System.err.println("Error on pre-script occured : ");
            res.getException().printStackTrace();
            throw new UserException("PreTask script has failed on the current node");
        }
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
    // TODO cdelbe, gsigety : RACE CONDITION BEETWEEN terminate and doTask (call to finalizeTask ) !!!
    public void terminate() {

        if (this.currentExecutable != null) {
            this.currentExecutable.kill();
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
            // TODO : logger.warn
            System.err.println("WARNING : Loggers are not shut down !");
        }
        PAActiveObject.terminateActiveObject(true);
    }

    /**
     * Set the walltime of this taskLauncher
     * 
     * @param wallTime the maximum execution time of the task
     */
    public void setWallTime(long wallTime) {
        if (wallTime > 0) {
            this.wallTime = wallTime;
        }
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltine then the timer will be cancelled 
     */
    protected void scheduleTimer() {
        scheduleTimer(currentExecutable);
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltine then the timer will be cancelled
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
