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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import org.ow2.proactive.resourcemanager.common.scripting.Script;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.scheduler.Tools;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.core.SchedulerCore;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.process.ThreadReader;


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

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

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
    private ForkedJavaExecutable forkedJavaExecutable = null;

    /**
     * Create a new instance of ForkedJavaTaskLauncher.<br/>
     * Used by ProActive active object creation process.
     *
     */
    public ForkedJavaTaskLauncher() {
    }

    /**
     * Create a new instance of ForkedJavaTaskLauncher.
     *
     * @param taskId the task id of the linked executable.
     */
    public ForkedJavaTaskLauncher(TaskId taskId) {
        super(taskId);
    }

    /**
     * Create a new instance of ForkedJavaTaskLauncher.
     *
     * @param taskId the task id of the linked executable.
     * @param pre the prescript that have to be executed on the node.
     */
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
     * @see org.ow2.proactive.scheduler.task.JavaTaskLauncher#doTask(org.ow2.proactive.scheduler.core.SchedulerCore, org.ow2.proactive.scheduler.task.ExecutableContainer, org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    public TaskResult doTask(SchedulerCore core, ExecutableContainer executableContainer,
            TaskResult... results) {
        try {
            init();
            currentExecutable = new NativeExecutable("");//executableContainer; //??

            /* building command for executing java */
            StringBuffer command = new StringBuffer();

            setJavaCommand(command);
            setJVMParameters(command);
            setClasspath(command);
            setRuntime(command, RuntimeFactory.getDefaultRuntime().getURL());

            createRegistrationListener();

            createJVMProcess(command, currentExecutable);

            waitForRegistration();

            /* JavaTaskLauncher is will be an active object created on a newly created ProActive node */
            JavaTaskLauncher newLauncher = createRemoteTaskLauncher();

            forkedJavaExecutable = new ForkedJavaExecutable((JavaExecutableContainer) executableContainer,
                newLauncher);

            if (isWallTime())
                scheduleTimer(forkedJavaExecutable);

            TaskResult result = (TaskResult) forkedJavaExecutable.execute(results);

            return result;

        } catch (Throwable ex) {
            return new TaskResultImpl(taskId, ex, new Log4JTaskLogs(this.logBuffer.getBuffer()));
        } finally {
            if (isWallTime())
                cancelTimer();
            finalizeTask(core);
        }
    }

    private void setJavaCommand(StringBuffer command) {
        if (forkEnvironment != null && forkEnvironment.getJavaHome() != null &&
            !"".equals(forkEnvironment.getJavaHome())) {
            command.append(forkEnvironment.getJavaHome() + File.separatorChar + "bin" + File.separatorChar +
                "java ");
        } else {
            command.append(" java ");
        }
    }

    private void setJVMParameters(StringBuffer command) {
        if (forkEnvironment != null && forkEnvironment.getJVMParameters() != null &&
            !"".equals(forkEnvironment.getJVMParameters())) {
            command.append(" " + forkEnvironment.getJVMParameters() + " ");
        }
        command.append(" -Djava.security.policy=" +
            Tools.getPathFromSchedulerHome(PASchedulerProperties.FORKEDJAVA_SECURITY_POLICY
                    .getValueAsString()) + " ");
    }

    private void setClasspath(StringBuffer command) {
        String classPath = System.getProperty("java.class.path", ".");
        command.append(" -cp " + classPath + " ");
    }

    private void setRuntime(StringBuffer command, String nodeURL) {
        command.append(" " + StartRuntime.class.getName() + " ");
        command.append(" -p " + nodeURL + " ");
        command.append(" -c 1 ");
        command.append(" -d " + getDeploymentId() + " ");
    }

    // wait until the child runtime registers itself at the current JVM
    // in case it fails to register (because of any reason), we don't start the task at all exiting with an exception
    private void waitForRegistration() throws SchedulerException, InterruptedException {
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
            throw new SchedulerException("Unable to create a separate java process after " + RETRY_ACQUIRE +
                " tries");

    }

    private void createRegistrationListener() {
        /* creating registration listener for collecting a registration notice from a created JVM */
        RegistrationListener registrationListener = new RegistrationListener();
        registrationListener.subscribeJMXRuntimeEvent();
    }

    // creating a child JVM, intercepting stdout and stderr
    private void createJVMProcess(StringBuffer command, Executable executableTask) throws IOException {
        process = Runtime.getRuntime().exec(command.toString());
        BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader serr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        tsout = new Thread(new ThreadReader(sout, System.out, executableTask));
        tserr = new Thread(new ThreadReader(serr, System.err, executableTask));
        tsout.start();
        tserr.start();
    }

    private JavaTaskLauncher createRemoteTaskLauncher() throws Exception {
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
        return newLauncher;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.TaskLauncher#terminate()
     */
    public void terminate() {
        if (forkedJavaExecutable != null)
            forkedJavaExecutable.kill();
        clean();
        super.terminate();
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
            // if the process did not register then childRuntime will be null and/or process will be null
            if (childRuntime != null) {
                childRuntime.killAllNodes();
                childRuntime = null;
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            if (tsout != null) {
                tsout.join();
                tsout = null;
            }
            if (tserr != null) {
                tserr.join();
                tserr = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getDeploymentId() {
        return deploymentID;
    }

    /**
     * @return the forkEnvironment
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    /**
     * @param forkEnvironment the forkEnvironment to set
     */
    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
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

        /**  */
        public void subscribeJMXRuntimeEvent() {
            ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
            JMXNotificationManager.getInstance().subscribe(part.getMBean().getObjectName(), this);
        }

        /**
         * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
         */
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
