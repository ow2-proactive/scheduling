package org.objectweb.proactive.extensions.scheduler.task;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.extensions.scheduler.common.exception.ExecutableCreationException;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask;
import org.objectweb.proactive.extensions.scheduler.util.classloading.TaskClassServer;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 * 
 * @author The ProActive Team
 *
 */
public class ForkedJavaExecutable extends JavaExecutable implements ExecutableContainer {

    private JavaExecutableContainer executableContainer = null;
    private TaskLauncher taskLauncher = null;

    private final static int TIMEOUT = 1000;

    /**
     * Constructor 
     * @param container contains the executable object that should run user java task
     * @param tl remote object residing in a dedicated JVM 
     */
    public ForkedJavaExecutable(JavaExecutableContainer container, TaskLauncher tl) {
        this.executableContainer = container;
        this.taskLauncher = tl;
    }

    /**
     * Task execution, in fact this method delegates execution to a remote taskLauncher object
     */
    public Object execute(TaskResult... results) throws Throwable {
        TaskResult result = taskLauncher.doTask(null /* no need here to pass schedulerCore object */,
                executableContainer, results);
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
    public void kill() {
        taskLauncher.terminate();
        super.kill();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scheduler.task.ExecutableContainer#getExecutable()
     */
    public Executable getExecutable() throws ExecutableCreationException {
        return this.executableContainer.getExecutable();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extensions.scheduler.task.ExecutableContainer#init()
     */
    public void init(InternalJob job, InternalTask task) {
        // nothing to do

    }
}
