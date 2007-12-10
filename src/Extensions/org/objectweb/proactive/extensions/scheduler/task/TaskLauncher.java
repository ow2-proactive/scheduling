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
package org.objectweb.proactive.extensions.scheduler.task;

import java.io.PrintStream;
import java.io.Serializable;
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
import org.objectweb.proactive.extra.logforwarder.BufferedAppender;
import org.objectweb.proactive.extra.logforwarder.EmptyAppender;
import org.objectweb.proactive.extra.logforwarder.LoggingOutputStream;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extensions.scheduler.common.exception.UserException;
import org.objectweb.proactive.extensions.scheduler.common.scripting.Script;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extensions.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extensions.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.core.SchedulerCore;


/**
 * Task Launcher.
 * This is the most simple task launcher.
 * It is able to launch a java task only.
 * You can extend this launcher in order to create a specific launcher.
 * With this default launcher, you can get the node on which the task is running and kill the task.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 10, 2007
 * @since ProActive 3.9
 */
public class TaskLauncher implements InitActive {
    private static final long serialVersionUID = -9159607482957244049L;
    protected TaskId taskId;
    protected Script<?> pre;
    protected String host;
    protected Integer port;

    // handle streams
    protected transient PrintStream redirectedStdout;
    protected transient PrintStream redirectedStderr;
    protected transient BufferedAppender logBuffer;

    /**
     * ProActive empty constructor.
     */
    public TaskLauncher() {
    }

    /**
     * Constructor with task identification.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId represents the task the launcher will execute.
     * @param host the host on which to append the standard output/input.
     * @param port the port number on which to send the standard output/input.
     */
    public TaskLauncher(TaskId taskId, String host, Integer port) {
        this.taskId = taskId;
        this.host = host;
        this.port = port;
    }

    /**
     * Constructor with task identification.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId represents the task the launcher will execute.
     * @param host the host on which to append the standard output/input.
     * @param port the port number on which to send the standard output/input.
     * @param pre the script executed before the task.
     */
    public TaskLauncher(TaskId taskId, String host, Integer port, Script<?> pre) {
        this.taskId = taskId;
        this.host = host;
        this.port = port;
        this.pre = pre;
    }

    /**
    * Initializes the activity of the active object.
    * @param body the body of the active object being initialized
    */
    public void initActivity(Body body) {
        PAActiveObject.setImmediateService("getNodes");
        PAActiveObject.setImmediateService("terminate");
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param executableTask the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    @SuppressWarnings("unchecked")
    public TaskResult doTask(SchedulerCore core, Executable executableTask,
        TaskResult... results) {
        // plug stdout/err into a socketAppender
        this.initLoggers();

        try {
            //launch pre script
            if (pre != null) {
                this.executePreScript(null);
            }

            //init task
            executableTask.init();

            //launch task
            Object userResult = executableTask.execute(results);

            //logBuffer is filled up
            TaskLogs taskLogs = new Log4JTaskLogs(this.logBuffer.getBuffer());
            TaskResult result = new TaskResultImpl(taskId, userResult, taskLogs);

            //return result
            return result;
        } catch (Throwable ex) {
            // exceptions are always handled at scheduler core level
            return new TaskResultImpl(taskId, ex,
                new Log4JTaskLogs(this.logBuffer.getBuffer()));
        } finally {
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
     * Redirect stdout/err in the scheduler task logger.
     */
    @SuppressWarnings("unchecked")
    protected void initLoggers() {
        // error about log should not be logged
        LogLog.setQuietMode(true);

        // create logger
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX +
                this.taskId.getJobId());
        l.setAdditivity(false);

        Appender out = new SocketAppender(this.host, this.port);
        MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId);
        l.removeAllAppenders();
        l.addAppender(EmptyAppender.SINK);
        this.logBuffer = new BufferedAppender(Log4JTaskLogs.JOB_APPENDER_NAME,
                true);
        // TODO : connection to the scheduler should be triggered by the scheduler
        this.logBuffer.addSink(out);
        l.addAppender(this.logBuffer);
        // redirect stdout and err
        this.redirectedStdout = new PrintStream(new LoggingOutputStream(l,
                    Level.INFO), true);
        this.redirectedStderr = new PrintStream(new LoggingOutputStream(l,
                    Level.ERROR), true);
        System.setOut(redirectedStdout);
        System.setErr(redirectedStderr);
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
     * @return the value of the variable GenerationScript.COMMAND_NAME after the script evaluation.
     */
    protected void executePreScript(Node n)
        throws ActiveObjectCreationException, NodeException, UserException {
        ScriptHandler handler = ScriptLoader.createHandler(n);
        ScriptResult<String> res = handler.handle(pre);

        if (res.errorOccured()) {
            System.err.println("Error on pre-script occured : ");
            res.getException().printStackTrace();
            throw new UserException(
                "PreTask script has failed on the current node");
        }

        //return res.getResult();
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
        PAActiveObject.terminateActiveObject(true);
    }
}
