/**
 * 
 */
package org.objectweb.proactive.extensions.scheduler.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.extensions.gcmdeployment.core.StartRuntime;
import org.objectweb.proactive.extensions.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extensions.scheduler.common.scripting.Script;
import org.objectweb.proactive.extensions.scheduler.common.task.Log4JTaskLogs;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.common.task.ThreadReader;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.Executable;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.ForkedJavaExecutable;
import org.objectweb.proactive.extensions.scheduler.common.task.executable.JavaExecutable;
import org.objectweb.proactive.extensions.scheduler.core.SchedulerCore;


/**
 * ForkedJavaTaskLauncher is a task launcher which will create a dedicated JVM for task execution.
 * It creates a JVM, creates a ProActive Node on that JVM, and in the end creates a JavaTaskLauncher active object
 * on that node. This JavaTaskLauncher will be responsible for task execution. 
 * 
 * @author The ProActive Team
 */
public class ForkedJavaTaskLauncher extends JavaTaskLauncher {

    /**
     * When creating a ProActive node on a dedicated JVM, assign a default name of VN
     */
    public static final String DEFAULT_VN_NAME = "ForkedTasksVN";

    /**
     * When creating a ProActive node on a dedicated JVM, assign a default JobID
     */
    public static final String DEFAULT_JOB_ID = "ForkedTasksJobID";

    /* Path to directory with Java installed, to this path '/bin/java' will be added. 
     * If the path is null only 'java' command will be called
     */
    private String javaHome = null;

    /* options passed to Java (not an application) (f.e. memory settings or properties) */
    private String javaOptions = null;

    /* for tryAcquire primitive */
    private static final long SEMAPHORE_TIMEOUT = 1;

    /* for tryAcquire primitive */
    private static final int RETRY_ACQUIRE = 5;

    private Process process = null;
    private ProActiveRuntime childRuntime = null;
    private Semaphore semaphore = new Semaphore(0);
    private String forkedNodeName = null;
    private long deploymentID;
    private Thread tsout = null;
    private Thread tserr = null;

    public ForkedJavaTaskLauncher() {
    }

    public ForkedJavaTaskLauncher(TaskId taskId) {
        super(taskId);
    }

    public ForkedJavaTaskLauncher(TaskId taskId, Script<?> pre) {
        super(taskId, pre);
    }

    /**
     * We need to set deployment ID. The new JVM will register itself in the current JVM. 
     * The current JVM will recognize it by this deployment ID. 
     */
    private void init() {
        Random random = new Random((new Date()).getTime());
        // TODO cdelbe cmathieu : use current DepId or a random one ?
        deploymentID = random.nextInt(1000000);

        forkedNodeName = "//localhost/" + this.getClass().getName() + getDeploymentId();
    }

    /**
     * Method responsible for creating a a dedicated JVM, execution of the task on this JVM and collecting result
     */
    public TaskResult doTask(SchedulerCore core, Executable executableTask, TaskResult... results) {
        try {
            init();
            currentExecutable = executableTask;

            /* building command for executing java */
            StringBuffer command = new StringBuffer();
            if (javaHome != null && !"".equals(javaHome)) {
                command.append(javaHome + "/bin/java ");
            } else {
                command.append(" java ");
            }

            if (javaOptions != null && !"".equals(javaOptions)) {
                command.append(" " + javaOptions + " ");
            }

            // option for properties string 
            String classPath = System.getProperty("java.class.path", ".");
            command.append(" -cp " + classPath + " ");
            command.append(" " + StartRuntime.class.getName() + " ");

            String nodeURL = RuntimeFactory.getDefaultRuntime().getURL();
            command.append(" -p " + nodeURL + " ");
            command.append(" -c 1 ");
            command.append(" -d " + getDeploymentId() + " ");

            /* creating registration listener for collecting a registration notice from a created JVM */
            RegistrationListener registrationListener = new RegistrationListener();
            registrationListener.subscribeJMXRuntimeEvent();

            process = Runtime.getRuntime().exec(command.toString());
            BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader serr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            tsout = new Thread(new ThreadReader(sout, System.out, executableTask));
            tserr = new Thread(new ThreadReader(serr, System.err, executableTask));
            tsout.start();
            tserr.start();

            // detecting if process failed or did not register 
            int numberOfTrials = 0;
            for (; numberOfTrials < RETRY_ACQUIRE; numberOfTrials++) {
                boolean permit = semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
                if (permit)
                    break;

                try {
                    process.exitValue();
                    // process terminated abnormally:
                    throw new SchedulerException("Unable to create a separate java process");
                } catch (IllegalThreadStateException e) {
                }
            }
            if (numberOfTrials == RETRY_ACQUIRE)
                throw new SchedulerException("Unable to create a separate java process");

            /* creating a ProActive node on a newly created JVM */
            String nodeUrl = childRuntime.createLocalNode(forkedNodeName, true, null, DEFAULT_VN_NAME,
                    DEFAULT_JOB_ID);

            /* JavaTaskLauncher is will be an active object created on a newly created ProActive node */
            JavaTaskLauncher newLauncher = null;
            if (pre == null) {
                newLauncher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { taskId }, nodeUrl);
            } else {
                newLauncher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { taskId, pre }, nodeUrl);
            }
            if (isWallTime)
                newLauncher.setWallTime(wallTime);

            ForkedJavaExecutable forkedJavaExecutable = new ForkedJavaExecutable(
                (JavaExecutable) executableTask, newLauncher);

            if (isWallTime)
                scheduleTimer(forkedJavaExecutable);

            TaskResult result = (TaskResult) forkedJavaExecutable.execute(results);

            return result;

        } catch (Throwable ex) {
            return new TaskResultImpl(taskId, ex, new Log4JTaskLogs(this.logBuffer.getBuffer()));
        } finally {
            if (isWallTime)
                cancelTimer();
            finalizeTask(core);
        }
    }

    /**
     * Finalizing task does: 
     * - cleaning after creating a separate JVM 
     * - informing the schedulerCore about finished task
     */
    protected void finalizeTask(SchedulerCore core) {
        clean();
        super.finalizeTask(core);
    }

    /**
     * Cleaning method kills all nodes on the dedicated JVM, destroys java process, and closes threads responsible for process output 
     */
    protected void clean() {
        try {
            childRuntime.killAllNodes();
            process.destroy();
            tsout.join();
            tserr.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getDeploymentId() {
        return deploymentID;
    }

    /**
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * @param javaHome the javaHome to set
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    /**
     * @return the javaOptions
     */
    public String getJavaOptions() {
        return javaOptions;
    }

    /**
     * @param javaOptions the javaOptions to set
     */
    public void setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
    }

    /**
     * Registration Listener is responsible for collecting notifications from other JVMs.
     * We need it specifically for collecting registration from a task dedicated JVM.
     * This dedicated JVM is recognized by a specific deployment ID.
     * Once the dedicated JVM registers itself, the semaphore is released and ForkedJavaTaskLauncher can proceed.
     *  
     * @author ProActive
     *
     */
    class RegistrationListener implements NotificationListener {

        public void subscribeJMXRuntimeEvent() {
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
        }

        public void handleNotification(Notification notification, Object handback) {
            String type = notification.getType();

            if (NotificationType.GCMRuntimeRegistered.equals(type)) {
                GCMRuntimeRegistrationNotificationData data = (GCMRuntimeRegistrationNotificationData) notification
                        .getUserData();
                if (data.getDeploymentId() == getDeploymentId()) {
                    childRuntime = data.getChildRuntime();
                    semaphore.release();
                    return;
                }
            }
        }

    }

}
