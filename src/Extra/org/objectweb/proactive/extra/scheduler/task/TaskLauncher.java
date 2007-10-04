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
package org.objectweb.proactive.extra.scheduler.task;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.logforwarder.EmptyAppender;
import org.objectweb.proactive.extra.logforwarder.LoggingOutputStream;
import org.objectweb.proactive.extra.scheduler.common.exception.UserException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.scripting.Script;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.core.SchedulerCore;


/**
 * Task Launcher.
 * This is the most simple task launcher.
 * It is able to launch a java task only.
 * You can extend this launcher in order to create a specific launcher.
 * With this default launcher, you can get the node on which the task is running and kill the task.
 *
 * @author ProActive Team
 * @version 1.0, Jul 10, 2007
 * @since ProActive 3.2
 */
public class TaskLauncher implements InitActive, Serializable {
    private static final long serialVersionUID = -9159607482957244049L;
    protected TaskId taskId;
    protected JobId jobId;
    protected Script<?> pre;
    protected String host;
    protected Integer port;

    /**
     * ProActive empty constructor.
     */
    public TaskLauncher() {
    }

    /**
     * Constructor with task identification
     *
     * @param taskId represents the task the launcher will execute.
     * @param jobId represents the job where the task is located.
     * @param host the host on which to append the standard output/input.
     * @param port the port number on which to send the standard output/input.
     */
    public TaskLauncher(TaskId taskId, JobId jobId, String host, Integer port) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.host = host;
        this.port = port;
    }

    /**
     * Constructor with task identification
     *
     * @param taskId represents the task the launcher will execute.
     * @param jobId represents the job where the task is located.
     */
    public TaskLauncher(TaskId taskId, JobId jobId, Script<?> pre, String host,
        Integer port) {
        this(taskId, jobId, host, port);
        this.pre = pre;
    }

    /**
    * Initializes the activity of the active object.
    * @param body the body of the active object being initialized
    */
    public void initActivity(Body body) {
        ProActiveObject.setImmediateService("getNodes");
        ProActiveObject.setImmediateService("terminate");
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
    public TaskResult doTask(SchedulerCore core, ExecutableTask executableTask,
        TaskResult... results) {
        //handle loggers
        Appender out = new SocketAppender(host, port);

        // store stdout and err
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;

        // create logger
        Logger l = Logger.getLogger(SchedulerCore.LOGGER_PREFIX + jobId);
        l.removeAllAppenders();
        l.addAppender(EmptyAppender.SINK);
        l.addAppender(out);
        // redirect stdout and err
        System.setOut(new PrintStream(new LoggingOutputStream(l, Level.INFO),
                true));
        System.setErr(new PrintStream(new LoggingOutputStream(l, Level.ERROR),
                true));
        try {
            //launch pre script
            if (pre != null) {
                ScriptHandler handler = ScriptLoader.createHandler(null);
                ScriptResult<Object> res = handler.handle(pre);
                if (res.errorOccured()) {
                    System.err.println("Error on pre-script occured : ");
                    res.getException().printStackTrace();
                    throw new UserException(
                        "PreTask script has failed on the current node");
                }
            }

            //launch task
            TaskResult result = new TaskResultImpl(taskId,
                    executableTask.execute(results));

            //return result
            return result;
        } catch (Exception ex) {
            return new TaskResultImpl(taskId, ex);
        } finally {
            //Unhandle loggers
            LogManager.shutdown();
            System.setOut(stdout);
            System.setErr(stderr);
            //terminate the task
            core.terminate(taskId, jobId);
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
        nodes.add(ProActiveObject.getNode());
        return new NodeSet(new ArrayList<Node>(nodes));
    }

    /**
     * This method will terminate the task that has been launched.
     * In fact it will terminate the launcher.
     */
    public void terminate() {
        ProActiveObject.terminateActiveObject(true);
    }
}
