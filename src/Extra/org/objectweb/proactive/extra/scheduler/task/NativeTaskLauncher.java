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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.scheduler.common.exception.UserException;
import org.objectweb.proactive.extra.scheduler.common.scripting.GenerationScript;
import org.objectweb.proactive.extra.scheduler.common.scripting.Script;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptHandler;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptLoader;
import org.objectweb.proactive.extra.scheduler.common.scripting.ScriptResult;
import org.objectweb.proactive.extra.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskLogs;
import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extra.scheduler.core.SchedulerCore;


/**
 * This launcher is the class that will launch a native class.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 10, 2007
 * @since ProActive 3.9
 */
public class NativeTaskLauncher extends TaskLauncher {

    /** Serial version UID */
    private static final long serialVersionUID = 8574369410634220047L;
    private Process process;

    /**
     * ProActive Empty Constructor
     */
    public NativeTaskLauncher() {
    }

    /**
     * Constructor of the native task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId the task identification.
     * @param jobId the job identification.
     * @param host the host on which the task is launched.
     */
    public NativeTaskLauncher(TaskId taskId, String host, Integer port) {
        super(taskId, host, port);
    }

    /**
     * Constructor of the native task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId the task identification.
     * @param host the host on which the task is launched.
     * @param port the port on which the task is launched.
     * @param pre the script executed before the task.
     */
    public NativeTaskLauncher(TaskId taskId, String host, Integer port,
        Script<?> pre) {
        super(taskId, host, port, pre);
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param executable the task to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    @Override
    @SuppressWarnings("unchecked")
    public TaskResult doTask(SchedulerCore core, Executable executable,
        TaskResult... results) {
        this.initLoggers();

        try {
            NativeExecutable toBeLaunched = (NativeExecutable) executable;

            //launch generation script
            if (toBeLaunched.getGenerationScript() != null) {
                String preScriptDefinedCommand = this.executeGenerationScript(toBeLaunched.getGenerationScript());

                // if preScriptDefinedCommand is not null, a new command 
                // has been defined by the generation script
                if ((preScriptDefinedCommand != null) &&
                        (!GenerationScript.DEFAULT_COMMAND_VALUE.equals(
                            preScriptDefinedCommand))) {
                    // the command is set
                    toBeLaunched.setCommand(preScriptDefinedCommand);
                }
            }

            //get process
            process = toBeLaunched.getProcess();

            //launch task
            Object userResult = toBeLaunched.execute(results);

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
     * Execute the generationScript on the default node
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     * @return the value of the variable GenerationScript.COMMAND_NAME after the script evaluation.
     */
    protected String executeGenerationScript(GenerationScript script)
        throws ActiveObjectCreationException, NodeException, UserException {
        ScriptHandler handler = ScriptLoader.createHandler(null);
        ScriptResult<String> res = handler.handle(script);

        if (res.errorOccured()) {
            System.err.println("Error on pre-script occured : ");
            res.getException().printStackTrace();
            throw new UserException(
                "PreTask script has failed on the current node");
        }

        return res.getResult();
    }

    /**
     * Kill all launched nodes/tasks and terminate the launcher.
     *
     * @see org.objectweb.proactive.extra.scheduler.task.TaskLauncher#terminate()
     */
    @Override
    public void terminate() {
        if (process != null) {
            process.destroy();
        }
        super.terminate();
    }
}
