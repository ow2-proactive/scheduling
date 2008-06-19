/**
 * 
 */
package org.objectweb.proactive.extensions.scheduler.task;

import org.objectweb.proactive.extensions.scheduler.common.scripting.Script;
import org.objectweb.proactive.extensions.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.core.SchedulerCore;


/**
 * JavaTaskLauncher ... 
 *
 * @author The ProActive Team
 * @date 24 avr. 08
 * @version 3.9
 *
 */
public class JavaTaskLauncher extends TaskLauncher {

    /**
     * ProActive Empty Constructor
     */
    public JavaTaskLauncher() {
    }

    /**
     * Constructor of the java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : please do not remove.
     *
     * @param taskId the task identification.
     */
    public JavaTaskLauncher(TaskId taskId) {
        super(taskId);
    }

    /**
     * Constructor of the java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : plz do not remove.
     *
     * @param taskId the task identification.
     * @param pre the script executed before the task.
     */
    public JavaTaskLauncher(TaskId taskId, Script<?> pre) {
        super(taskId, pre);
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
    public TaskResult doTask(SchedulerCore core, Executable executableTask, TaskResult... results) {
        try {
            //launch pre script
            if (pre != null) {
                this.executePreScript(getNodes().get(0));
            }

            currentExecutable = executableTask;
            //init task
            currentExecutable.init();

            if (isWallTime())
                scheduleTimer();

            //launch task            
            Object userResult = executableTask.execute(results);

            //logBuffer is filled up
            TaskLogs taskLogs = new Log4JTaskLogs(this.logBuffer.getBuffer());
            TaskResult result = new TaskResultImpl(taskId, userResult, taskLogs);

            //return result
            return result;
        } catch (Throwable ex) {
            // exceptions are always handled at scheduler core level
            return new TaskResultImpl(taskId, ex, new Log4JTaskLogs(this.logBuffer.getBuffer()));
        } finally {
            if (isWallTime())
                cancelTimer();
            if (core != null)
                // This call should be conditioned by the isKilled ... ?
                this.finalizeTask(core);
            else
                /* if core == null then dont finalize the task. An example when we dont want to finalize task is when using
                 * forked java task, then only finalizing loggers is enough.
                 */
                this.finalizeLoggers();
        }
    }

}
