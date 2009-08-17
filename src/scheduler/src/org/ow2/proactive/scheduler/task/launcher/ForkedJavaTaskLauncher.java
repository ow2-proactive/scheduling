/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package org.ow2.proactive.scheduler.task.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkedJavaExecutable;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.NativeExecutable;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scheduler.util.process.ThreadReader;


/**
 * ForkedJavaTaskLauncher is a task launcher which will create a dedicated JVM for task execution.
 * It creates a JVM, creates a ProActive Node on that JVM, and in the end creates a JavaTaskLauncher active object
 * on that node. This JavaTaskLauncher will be responsible for task execution. 
 * 
 * @author The ProActive Team
 */
public class ForkedJavaTaskLauncher extends JavaTaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    /**
     * When creating a ProActive node on a dedicated JVM, assign a default name of VN
     */
    public static final String DEFAULT_VN_NAME = "ForkedTasksVN";

    /**
     * When creating a ProActive node on a dedicated JVM, assign a default JobID
     */
    public static final String DEFAULT_JOB_ID = "ForkedTasksJobID";

    /** 
     * Content of the forked java security policy file.
     */
    private String securityPolicyContent = null;

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

    /* for tryAcquire primitive */
    private static final long SEMAPHORE_TIMEOUT = 2;

    /* for tryAcquire primitive */
    private static final int RETRY_ACQUIRE = 10;

    private Process process = null;
    private ProActiveRuntime childRuntime = null;
    private Semaphore semaphore = new Semaphore(0);
    private String forkedNodeName = null;
    private long deploymentID;
    private Thread tsout = null;
    private Thread tserr = null;
    private ForkedJavaExecutable forkedJavaExecutable = null;
    private File fpolicy = null;

    /**
     * Create a new instance of ForkedJavaTaskLauncher.<br/>
     * Used by ProActive active object creation process.
     *
     */
    public ForkedJavaTaskLauncher() {
    }

    /**
     * Constructor of the forked java task launcher.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize this task launcher.
     */
    public ForkedJavaTaskLauncher(TaskLauncherInitializer initializer) {
        super(initializer);
        this.forkEnvironment = initializer.getForkEnvironment();
        this.securityPolicyContent = initializer.getForkedPolicyContent();
    }

    /**
     * We need to set deployment ID. The new JVM will register itself in the current JVM. 
     * The current JVM will recognize it by this deployment ID. 
     */
    private void init() {
        Random random = new Random((new Date()).getTime());
        deploymentID = random.nextInt(1000000);
        forkedNodeName = this.getClass().getName() + getDeploymentId();
    }

    /**
     * Method responsible for creating a a dedicated JVM, execution of the task on this JVM and collecting result
     * @see org.ow2.proactive.scheduler.task.launcher.JavaTaskLauncher#doTask(org.ow2.proactive.scheduler.common.TaskTerminateNotification, org.ow2.proactive.scheduler.task.ExecutableContainer, org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    public TaskResult doTask(TaskTerminateNotification core, ExecutableContainer executableContainer,
            TaskResult... results) {
        try {
            init();
            //fake native executable used only to instanciate threadReader.
            currentExecutable = new NativeExecutable(null);
            /* building command for executing java */
            logger_dev.info("Preparing new java command");
            StringBuffer command = new StringBuffer();

            setJavaCommand(command);
            setJVMParameters(command);
            setClasspath(command);
            setRuntime(command, RuntimeFactory.getDefaultRuntime().getURL());

            createRegistrationListener();

            logger_dev.info("Create JVM process with command : " + command);
            createJVMProcess(command, currentExecutable);

            waitForRegistration();

            /* JavaTaskLauncher will be an active object created on a newly created ProActive node */
            logger_dev.info("Create remote task launcher");
            JavaTaskLauncher newLauncher = createRemoteTaskLauncher();

            forkedJavaExecutable = new ForkedJavaExecutable((JavaExecutableContainer) executableContainer,
                newLauncher);

            if (isWallTime()) {
                scheduleTimer(forkedJavaExecutable);
            }

            TaskResult result = (TaskResult) forkedJavaExecutable.execute(results);

            return result;

        } catch (Throwable ex) {
            logger_dev.info("", ex);
            return new TaskResultImpl(taskId, ex, this.getLogs());
        } finally {
            if (isWallTime())
                cancelTimer();
            finalizeTask(core);
        }
    }

    private void setJavaCommand(StringBuffer command) {
        String java_home;
        if (forkEnvironment != null && forkEnvironment.getJavaHome() != null &&
            !"".equals(forkEnvironment.getJavaHome())) {
            java_home = forkEnvironment.getJavaHome();
        } else {
            java_home = System.getProperty("java.home");
        }
        command.append(java_home + File.separatorChar + "bin" + File.separatorChar + "java ");
    }

    private void setJVMParameters(StringBuffer command) {
        if (forkEnvironment == null || forkEnvironment.getJVMParameters() == null ||
            !forkEnvironment.getJVMParameters().matches(".*java.security.policy=.*")) {
            try {
                fpolicy = File.createTempFile("forked_jt", null);
                PrintStream out = new PrintStream(fpolicy);
                out.print(securityPolicyContent);
                out.close();
                command.append(" -Djava.security.policy=" + fpolicy.getAbsolutePath() + " ");
            } catch (Exception e) {
                //java policy not set
            }
        }
        if (forkEnvironment != null && forkEnvironment.getJVMParameters() != null &&
            !"".equals(forkEnvironment.getJVMParameters())) {
            command.append(" " + forkEnvironment.getJVMParameters() + " ");
        }
    }

    private void setClasspath(StringBuffer command) {
        String classPath = System.getProperty("java.class.path", ".");
        command.append(" -cp " + classPath + " ");
    }

    private void setRuntime(StringBuffer command, String nodeURL) {
        command.append(" " + StartPARuntime.class.getName() + " ");
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
            if (permit) {
                break;
            }

            try {
                process.exitValue();
                // process terminated abnormally:
                throw new SchedulerException("Unable to create a separate java process");
            } catch (IllegalThreadStateException e) {
            }
        }
        if (numberOfTrials == RETRY_ACQUIRE) {
            throw new SchedulerException("Unable to create a separate java process after " + RETRY_ACQUIRE +
                " tries");
        }

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
        Node forkedNode = childRuntime.createLocalNode(forkedNodeName, true, null, DEFAULT_VN_NAME,
                DEFAULT_JOB_ID);

        /* JavaTaskLauncher will be an active object created on a newly created ProActive node */
        JavaTaskLauncher newLauncher = null;
        logger_dev.info("Create java task launcher");
        TaskLauncherInitializer tli = new TaskLauncherInitializer();
        tli.setTaskId(taskId);
        tli.setPreScript(pre);
        tli.setPostScript(post);
        newLauncher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                new Object[] { tli }, forkedNode);
        return newLauncher;
    }

    /**
     * @see org.ow2.proactive.scheduler.task.launcher.TaskLauncher#terminate()
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
    protected void finalizeTask(TaskTerminateNotification core) {
        clean();
        super.finalizeTask(core);
    }

    /**
     * Cleaning method kills all nodes on the dedicated JVM, destroys java process, and closes threads responsible for process output 
     */
    protected void clean() {
        try {
            logger_dev.info("cleaning task launcher");
            //close fake executable to free its stream
            this.currentExecutable.kill();
            //if tmp file has been set, destroy it.
            if (fpolicy != null) {
                fpolicy.delete();
            }
            // if the process did not register then childRuntime will be null and/or process will be null
            if (childRuntime != null) {
                childRuntime.killAllNodes();
                childRuntime = null;
            }
            if (tsout != null) {
                tsout.join();
                tsout = null;
            }
            if (tserr != null) {
                tserr.join();
                tserr = null;
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
        } catch (Exception e) {
            logger_dev.error("", e);
        }
    }

    private long getDeploymentId() {
        return deploymentID;
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
