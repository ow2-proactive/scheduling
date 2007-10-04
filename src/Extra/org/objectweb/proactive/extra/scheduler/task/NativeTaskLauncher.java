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

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.objectweb.proactive.extra.logforwarder.EmptyAppender;
import org.objectweb.proactive.extra.logforwarder.LoggingOutputStream;
import org.objectweb.proactive.extra.scheduler.common.exception.UserException;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.core.SchedulerCore;


/**
 * Native Task Launcher.
 * This launcher is the class that will launch a nativ class.
 *
 * @author ProActive Team
 * @version 1.0, Jul 10, 2007
 * @since ProActive 3.2
 */
public class NativeTaskLauncher extends TaskLauncher {

    /** Serial version UID */
    private static final long serialVersionUID = 8574369410634220047L;
    private Process process;

    /**
     * ProActive Empy Constructor
     */
    public NativeTaskLauncher() {
    }

    /**
     * Constructor of the native task launcher.
     *
     * @param taskId the task identification.
     * @param jobId the job identification.
     * @param host the host on which the task is launched.
     * @param port the port on which the task is launched.
     */
    public NativeTaskLauncher(TaskId taskId, JobId jobId, String host,
        Integer port) {
        super(taskId, jobId, host, port);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param executableTask the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    @Override
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
            //get process
            process = ((ExecutableNativeTask) executableTask).getProcess();
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
     * Kill all launched nodes/tasks and terminate the launcher.
     *
     * @see org.objectweb.proactive.extra.scheduler.task.TaskLauncher#terminate()
     */
    @Override
    public void terminate() {
        process.destroy();
        super.terminate();
    }
}
