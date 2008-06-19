package org.objectweb.proactive.extensions.scheduler.task;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 * 
 * @author The ProActive Team
 *
 */
public class ForkedJavaExecutable extends JavaExecutable {

    private JavaExecutable executable = null;
    private TaskLauncher taskLauncher = null;

    private final static int TIMEOUT = 1000;

    /**
     * Constructor 
     * @param ex executable object that should run user java task
     * @param tl remote object residing in a dedicated JVM 
     */
    public ForkedJavaExecutable(JavaExecutable ex, TaskLauncher tl) {
        this.executable = ex;
        this.taskLauncher = tl;
    }

    /**
     * Task execution, in fact this method delegates execution to a remote taskLauncher object
     */
    @Override
    public Object execute(TaskResult... results) throws Throwable {
        TaskResult result = taskLauncher.doTask(null /* no need here to pass schedulerCore object */,
                executable, results);
        while (!isKilled()) {
            try {
                /* the below method throws an exception if timeout expires */
                PAFuture.waitFor(result, TIMEOUT);
                break;
            } catch (ProActiveTimeoutException e) {
            }
        }
        if (isKilled()) {
            FutureMonitoring.removeFuture(((FutureProxy) ((StubObject) result).getProxy()));
            throw new SchedulerException("Walltime exceeded");
        }
        return result;
    }

    /**
     * The kill method should result in killing the executable, and cleaning after launching the separate JVM
     */
    @Override
    public void kill() {
        taskLauncher.terminate();
        super.kill();
    }

    /**
     * @return the executable which will be executed in a separate JVM
     */
    public JavaExecutable getExecutable() {
        return executable;
    }

    /**
     * @return the taskLauncher will is responsible for execution of executable
     */
    public TaskLauncher getTaskLauncher() {
        return taskLauncher;
    }

}
