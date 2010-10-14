/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds 
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.util.JMXNotificationManager;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.StartPARuntime;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilderFactory;
import org.objectweb.proactive.extensions.processbuilder.OSUser;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.authentication.SchedulerAuthentication;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.task.launcher.JavaTaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 * 
 * @author The ProActive Team
 *
 */
public class ForkedJavaExecutable extends JavaExecutable {

    enum ExecutableState {
        INITIALIZING, STARTED, LISTEN_DEMANDED, LISTENNING, TERMINATED;
    }

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    /** When creating a ProActive node on a dedicated JVM, assign a default name of VN */
    public static final String DEFAULT_VN_NAME = "ForkedTasksVN";

    /** When creating a ProActive node on a dedicated JVM, assign a default JobID */
    public static final String DEFAULT_JOB_ID = "ForkedTasksJobID";

    /** Forked execution time out checking interval */
    private static final int TIMEOUT = 1000;

    /** For tryAcquire primitive */
    private static final long SEMAPHORE_TIMEOUT = 2;
    /** For tryAcquire primitive */
    private static final int RETRY_ACQUIRE = 10;

    /** State of this executable, used to know when activating logs */
    private ExecutableState execState = null;
    /** Remember appender provider if demanded before the start of the task */
    private AppenderProvider logSink = null;
    /** Java task launcher reference kept to activate logs on it if necessary */
    private JavaTaskLauncher newJavaTaskLauncher;

    /** save task logs when finished */
    private TaskLogs logs;

    private String forkedNodeName;
    private long deploymentID;
    private List<String> command;
    private ForkedJavaExecutableInitializer execInitializer;
    private File fpolicy = null;

    private Process process = null;
    private ProActiveRuntime childRuntime = null;
    private Semaphore semaphore = new Semaphore(0);

    /** Hibernate default constructor */
    public ForkedJavaExecutable() {
    }

    /**
     * Initialize the executable using the given executable Initializer.
     *
     * @param execContainer the executable Initializer used to init the executable itself
     *
     * @throws Exception an exception if something goes wrong during executable initialization.
     */
    // WARNING WHEN REMOVE OR RENAME, called by task launcher by introspection
    private void internalInit(ForkedJavaExecutableInitializer execInitializer) throws Exception {
        execState = ExecutableState.INITIALIZING;
        this.execInitializer = execInitializer;
        init();
        /* building command for executing java */
        logger_dev.info("Preparing new java command");
        createJavaCommand();
        addJVMParameters();
        addClasspath();
        addRuntime();
    }

    /**
     * Task execution, in fact this method delegates execution to a remote taskLauncher object
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        try {
            createRegistrationListener();

            logger_dev.info("Create JVM process with command : " + command);
            createJVMProcess();

            waitForRegistration();

            logger_dev.debug("Create remote task launcher");
            newJavaTaskLauncher = createForkedTaskLauncher();

            execInitializer.getJavaExecutableContainer().setNodes(execInitializer.getNodes());
            //do task must not pass schedulerCore object,
            //the deployed java task must not notify the core from termination
            //the forked java task launcher will do that in place
            logger_dev.debug("Starting java task");
            newJavaTaskLauncher.configureNode();
            TaskResult result = newJavaTaskLauncher.doTask(null,
                    execInitializer.getJavaExecutableContainer(), results);

            if (execState == ExecutableState.LISTEN_DEMANDED) {
                newJavaTaskLauncher.activateLogs(logSink);
                execState = ExecutableState.LISTENNING;
            } else {
                execState = ExecutableState.STARTED;
            }

            //waiting for task result futur
            //as it is forked, wait until futur has arrive or someone kill the task (core OR tasktimer)
            logger_dev.debug("Java task started, waiting for result or kill...");
            while (!isKilled()) {
                try {
                    /* the below method throws an exception if timeout expires */
                    PAFuture.waitFor(result, TIMEOUT);
                    break;
                } catch (ProActiveTimeoutException e) {
                }
            }

            execState = ExecutableState.TERMINATED;

            if (!isKilled()) {
                logs = newJavaTaskLauncher.getLogs();
                newJavaTaskLauncher.closeNodeConfiguration();
            } else {
                logger_dev.debug("Task has been killed");
                FutureMonitoring.removeFuture(((FutureProxy) ((StubObject) result).getProxy()));
                throw new InternalSchedulerException("Walltime exceeded");
            }
            return result;
        } finally {
            clean();
        }
    }

    /**
     * Activate logs on the forked 'java task launcher'
     *
     * @param logSink appender provider
     */
    public void activateLogs(AppenderProvider logSink) {
        this.logSink = logSink;
        if (execState == ExecutableState.STARTED) {
            newJavaTaskLauncher.activateLogs(this.logSink);
            execState = ExecutableState.LISTENNING;
        } else if (execState != ExecutableState.TERMINATED && execState != ExecutableState.LISTENNING) {
            execState = ExecutableState.LISTEN_DEMANDED;
        }
    }

    /**
     * Get the logs generated by this forked executable
     *
     * @return The taskLogs representing the
     */
    public TaskLogs getLogs() {
        return logs;
    }

    /**
     * We need to set deployment ID. The new JVM will register itself in the current JVM.
     * The current JVM will recognize it by this deployment ID.
     */
    private void init() {
        Random random = new Random((new Date()).getTime());
        deploymentID = random.nextInt(1000000);
        forkedNodeName = this.getClass().getName() + deploymentID;
    }

    /**
     * Prepare java command
     */
    private void createJavaCommand() {
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        String java_home;
        if (forkEnvironment != null && forkEnvironment.getJavaHome() != null &&
            !"".equals(forkEnvironment.getJavaHome())) {
            java_home = forkEnvironment.getJavaHome();
        } else {
            java_home = System.getProperty("java.home");
        }
        command = new ArrayList<String>();
        command.add(java_home + File.separatorChar + "bin" + File.separatorChar + "java");
    }

    /**
     * Add jvm parameters
     */
    private void addJVMParameters() {
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        if (forkEnvironment == null || forkEnvironment.getJVMParameters() == null ||
            !forkEnvironment.getJVMParameters().matches(".*java.security.policy=.*")) {
            try {
                fpolicy = File.createTempFile("forked_jt", null);
                PrintStream out = new PrintStream(fpolicy);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getPolicyContent());
                out.close();
                command.add("-Djava.security.policy=" + fpolicy.getAbsolutePath());
            } catch (Exception e) {
                //java policy not set
                logger_dev.debug("", e);
            }
        }
        if (forkEnvironment != null && forkEnvironment.getJVMParameters() != null &&
            !"".equals(forkEnvironment.getJVMParameters())) {
            command.add(forkEnvironment.getJVMParameters());
        }
    }

    /**
     * Add classpath
     */
    private void addClasspath() {
        String classPath = System.getProperty("java.class.path", ".");
        command.add("-cp");
        command.add(classPath);
    }

    /**
     * Add runtime class name to be launched
     *
     * @param nodeURL URL of the node on which to deploy
     */
    private void addRuntime() throws ProActiveException {
        command.add(StartPARuntime.class.getName());
        command.add("-p");
        command.add(RuntimeFactory.getDefaultRuntime().getURL());
        command.add("-c");
        command.add("1");
        command.add("-d");
        command.add("" + deploymentID);
    }

    /**
     * wait until the child runtime registers itself at the current JVM
     * in case it fails to register (because of any reason), we don't start the task at all exiting with an exception
     */
    private void waitForRegistration() throws SchedulerException, InterruptedException {
        int numberOfTrials = 0;
        for (; numberOfTrials < RETRY_ACQUIRE; numberOfTrials++) {
            boolean permit = semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                break;
            }

            try {
                int ec = process.exitValue();
                // process terminated abnormally:
                throw new InternalSchedulerException(
                    "Unable to create a separate java process. Exit code : " + ec);
            } catch (IllegalThreadStateException e) {
                logger_dev.debug("Process not terminated, continue Forked VM launching (try number " +
                    numberOfTrials + ")");
            }
        }
        if (numberOfTrials == RETRY_ACQUIRE) {
            throw new InternalSchedulerException("Unable to create a separate java process after " +
                RETRY_ACQUIRE + " tries");
        }

    }

    /**
     * Creating registration listener for collecting a registration notice from a created JVM
     */
    private void createRegistrationListener() {
        /* creating registration listener for collecting a registration notice from a created JVM */
        RegistrationListener registrationListener = new RegistrationListener();
        registrationListener.subscribeJMXRuntimeEvent();
    }

    /**
     * creating a child JVM, intercepting stdout and stderr
     * Also ask for credentials with new generated keypair
     * @throws IOException
     */
    private void createJVMProcess() throws Exception {
        //build process
        OSProcessBuilder ospb = OSProcessBuilderFactory.getBuilder();
        ospb.command(command.toArray(new String[command.size()]));
        //check if it must be run under user
        boolean runAsUser = this.execInitializer.getJavaTaskLauncherInitializer().isRunAsUser();
        if (runAsUser) {
            //generate new keypair
            KeyPairGenerator keyGen = null;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                //should never happen as RSA exists
            }
            keyGen.initialize(1024, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            //connect to the authentication interface and ask for new cred
            SchedulerAuthentication schedAuth = null;
            Credentials cred = schedAuth.getUserCrendentials(this.execInitializer
                    .getJavaTaskLauncherInitializer().getOwner(), keyPair.getPublic());

            //decrypt
            String[] credentials = null;
            try {
                credentials = cred.decrypt(keyPair.getPrivate());
                ospb.user(new OSUser(credentials[0], credentials[1]));
            } catch (KeyException e) {
                throw new LoginException("Could not decrypt credentials: " + e);
            }
        }

        //and start process
        process = ospb.start();
    }

    /**
     * Create a java task launcher on the loval new JVM
     *
     * @return the created java task launcher
     * @throws Exception
     */
    private JavaTaskLauncher createForkedTaskLauncher() throws Exception {
        /* creating a ProActive node on a newly created JVM */
        Node forkedNode = childRuntime.createLocalNode(forkedNodeName, true, null, DEFAULT_VN_NAME,
                DEFAULT_JOB_ID);

        /* JavaTaskLauncher will be an active object created on a newly created ProActive node */
        logger_dev.info("Create java task launcher");
        TaskLauncherInitializer tli = execInitializer.getJavaTaskLauncherInitializer();
        JavaTaskLauncher newLauncher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class
                .getName(), new Object[] { tli }, forkedNode);
        return newLauncher;
    }

    /**
     * The kill method should result in killing the executable, and cleaning after launching the separate JVM
     *
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#kill()
     */
    @Override
    public void kill() {
        //this method is called by the scheduler core or by the TimerTask of the walltime.
        //No need to terminate current taskLauncher for both cases because :
        //If the schedulerCore call it, the task is killed and the taskLauncher terminated.
        //If the TimerTask call it, so the taskLauncher is already terminated by throwing an exception.
        try {
            logs = (TaskLogs) PAFuture.getFutureValue(newJavaTaskLauncher.getLogs());
        } catch (Throwable t) {
        }
        clean();
        super.kill();
    }

    /**
     * Cleaning method kills all nodes on the dedicated JVM, destroys java process, and closes threads responsible for process output
     */
    private void clean() {
        try {
            logger_dev.info("Cleaning forked java executable");
            //if tmp file has been set, destroy it.
            if (fpolicy != null) {
                fpolicy.delete();
            }
            // if the process did not register then childRuntime will be null and/or process will be null
            if (childRuntime != null) {
                try {
                    childRuntime.killAllNodes();
                    childRuntime = null;
                } catch (Exception e) {
                }
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
        } catch (Exception e) {
            logger_dev.error("", e);
        }
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
                if (data.getDeploymentId() == deploymentID) {
                    childRuntime = data.getChildRuntime();
                    semaphore.release();
                    return;
                }
            }
        }

    }

}
