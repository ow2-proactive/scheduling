/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
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
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.exception.WalltimeExceededException;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.exception.StartForkedProcessException;
import org.ow2.proactive.scheduler.task.launcher.InternalForkEnvironment;
import org.ow2.proactive.scheduler.task.launcher.JavaTaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.launcher.utils.ForkerUtils;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scheduler.util.process.ThreadReader;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 * 
 * @author The ProActive Team
 *
 */
public class ForkedJavaExecutable extends JavaExecutable {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);

    /** When creating a ProActive node on a dedicated JVM, assign a default name of VN */
    public static final String DEFAULT_VN_NAME = "ForkedTasksVN";

    /** Fork environment script binding name */
    public static final String FORKENV_BINDING_NAME = "forkEnvironment";

    /** Size of the log buffer on forked side ; logs are buffered on forker side */
    private static final int FORKED_LOG_BUFFER_SIZE = 0;

    /** Forked execution time out checking interval */
    private static final int TIMEOUT = 1000;

    /** For tryAcquire primitive */
    private static final long SEMAPHORE_TIMEOUT = 2;
    /** For tryAcquire primitive */
    private static final int RETRY_ACQUIRE = 10;

    /** Java task launcher reference kept to activate logs on it if necessary */
    private JavaTaskLauncher newJavaTaskLauncher;

    /** Thread for listening out/err of the forked JVM */
    private transient Thread tsout, tserr;

    private String forkedNodeName;
    private long deploymentID;
    private ForkedJavaExecutableInitializer execInitializer;
    private File fpolicy = null;
    private File flog4j = null;
    private File fpaconfig = null;

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
        this.execInitializer = execInitializer;
        init();
    }

    /**
     * Task execution, in fact this method delegates execution to a remote taskLauncher object
     * @see org.ow2.proactive.scheduler.common.task.executable.Executable#execute(org.ow2.proactive.scheduler.common.task.TaskResult[])
     */
    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        try {

            // building command for executing java and start process
            OSProcessBuilder ospb = createProcessAndPrepareCommand();
            createRegistrationListener();
            process = startProcess(ospb);
            this.initStreamReaders();
            waitForRegistration(ospb);

            //create task launcher on new JVM node
            logger_dev.debug("Create remote task launcher");
            newJavaTaskLauncher = createForkedTaskLauncher();

            // redirect tasks logs to local stdout/err
            newJavaTaskLauncher.activateLogs(new StdAppenderProvider());

            execInitializer.getJavaExecutableContainer().setNodes(execInitializer.getNodes());
            //do task must not pass schedulerCore object,
            //the deployed java task must not notify the core from termination
            //the forked java task launcher will do that in place
            logger_dev.debug("Starting java task");
            newJavaTaskLauncher.configureNode();
            TaskResult result = newJavaTaskLauncher.doTask(null,
                    execInitializer.getJavaExecutableContainer(), results);

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

            try {
                //if no exception, JVM has terminated and task result is not available
                //so return exit code that must be handle correctly by forkedJavaTaskLauncher
                return process.exitValue();
            } catch (IllegalThreadStateException e) {
                //process not terminated
            }

            if (!isKilled()) {
                newJavaTaskLauncher.closeNodeConfiguration();
            } else {
                logger_dev.debug("Task has been killed");
                FutureMonitoring.removeFuture(((FutureProxy) ((StubObject) result).getProxy()));
                throw new WalltimeExceededException("Walltime exceeded");
            }
            return result;
        } finally {
            clean();
        }
    }

    /**
     * Start listening thread on process out/err
     */
    private void initStreamReaders() {
        // redirect streams to local stdout/err
        BufferedReader sout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader serr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        tsout = new Thread(new ThreadReader(sout, System.out, this));
        tserr = new Thread(new ThreadReader(serr, System.err, this));
        tsout.start();
        tserr.start();
    }

    /**
     * Wait for listening threads completion.
     */
    private void terminateStreamReaders() {
        try {
            // wait for log flush
            if (tsout != null) {
                tsout.join();
            }
            if (tserr != null) {
                tserr.join();
            }
        } catch (InterruptedException e) {
        } finally {
            tsout = null;
            tserr = null;
        }
    }

    /**
     * <ul>
     * <li>Create new process builder</li>
     * <li>Update fork env with system env</li>
     * <li>Execute environment script if needed</li>
     * <li>Create command and add it to process builder</li>
     * <li>Set working dir</li>
     * <li>Set system environment</li>
     * <li>And return the created OS process builder</li>
     * </ul>
     *
     * @return the created OS process builder
     * @throws Exception if a problem occurs while creating the process
     */
    private OSProcessBuilder createProcessAndPrepareCommand() throws Exception {
        logger_dev.debug("Preparing new java process");
        //create process builder
        OSProcessBuilder ospb = createProcess();
        //update fork env with forked system env
        createInternalForkEnvironment(ospb);
        //execute environment script
        executeEnvScript(ospb);
        //create command and set it to process builder
        List<String> command = createJavaCommand();
        addJVMArguments(command);
        addClasspath(command);
        addRuntime(command);
        //set command
        setCommand(ospb, command);
        //set working dir
        setWorkingDir(ospb);
        //set system environment
        setSystemEnvironment(ospb);
        if (logger_dev.isDebugEnabled()) {
            logger_dev.debug("JVM process and command created with command : " + command);
        }
        return ospb;
    }

    /**
     * This method takes the system environment from the given OS Process Builder
     * and update the user fork environment.<br/>
     * When this method returns, the fork environment is updated with the forked system environment
     *
     * @param ospb the process builder on which to get the system environment
     */
    private void createInternalForkEnvironment(OSProcessBuilder ospb) {
        ForkEnvironment fe = this.execInitializer.getForkEnvironment();
        InternalForkEnvironment ife;
        try {
            //ospb.environment() can throw an exception if env cannot be created :
            //will be reported to user via exception in user task
            Map<String, String> env = ospb.environment();
            ife = new InternalForkEnvironment(fe, env);
        } catch (NotImplementedException e) {
            //if user has not set any system properties, create the internal fork environment
            //without any sys env and in read only mode on system env
            //else, the state is not consistent -> throw an exception
            if (fe == null || fe.getSystemEnvironment().size() == 0) {
                ife = new InternalForkEnvironment(fe, null, true);
            } else {
                throw new IllegalStateException(
                    "System property was set and fork process environment could not be obtained", e);
            }
        }
        //change reference of forkEnv to the internal one
        this.execInitializer.setForkEnvironment(ife);
    }

    /**
     * Execute the envScript on the node n, or on the default node if n is null.<br/>
     * The script will be executed only if fork environment has been set and a script is set.<br/>
     * When this method returns, the fork environment is updated with new value set in the script
     *
     * @param ospb the process builder on which to get the system environment
     * @throws Exception if the script handler cannot be created of
     * 			if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    private void executeEnvScript(OSProcessBuilder ospb) throws Exception {
        ForkEnvironment fe = this.execInitializer.getForkEnvironment();
        if (fe != null && fe.getEnvScript() != null) {
            logger_dev.info("Executing env-script");
            ScriptHandler handler = ScriptLoader.createLocalHandler();
            handler.addBinding(FORKENV_BINDING_NAME, fe);
            ScriptResult<String> res = handler.handle(fe.getEnvScript());
            //result
            if (res.errorOccured()) {
                res.getException().printStackTrace();
                logger_dev.error("Error on env-script occured : ", res.getException());
                throw new UserException("Env-script has failed on the current node", res.getException());
            }
        }
    }

    /**
     * Return the progress value of the executable that runs in the forked JVM.
     */
    public int getProgress() {
        return this.newJavaTaskLauncher.getProgress();
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
     *
     * @return a new command as a list of string that can be completed
     */
    private List<String> createJavaCommand() {
        ForkEnvironment forkEnvironment = this.execInitializer.getForkEnvironment();
        String java_home;
        if (forkEnvironment != null && forkEnvironment.getJavaHome() != null &&
            !"".equals(forkEnvironment.getJavaHome())) {
            java_home = forkEnvironment.getJavaHome();
        } else {
            java_home = System.getProperty("java.home");
        }
        List<String> command = new ArrayList<String>();
        command.add(java_home + File.separatorChar + "bin" + File.separatorChar + "java");
        return command;
    }

    /**
     * Add JVM arguments to the given command
     *
     * @param command the command to be completed
     */
    private void addJVMArguments(List<String> command) {
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        //set mandatory security policy
        if (forkEnvironment == null || !contains("java.security.policy", forkEnvironment.getJVMArguments())) {
            try {
                fpolicy = File.createTempFile("forked_jts", null);
                PrintStream out = new PrintStream(fpolicy);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getPolicyContent());
                out.close();
                command.add("-Djava.security.policy=" + fpolicy.getAbsolutePath());
            } catch (Exception e) {
                //java policy not set
                logger_dev.debug("", e);
            }
        }
        //set mandatory log4j file
        if (forkEnvironment == null || !contains("log4j.configuration", forkEnvironment.getJVMArguments())) {
            try {
                flog4j = File.createTempFile("forked_jtl", null);
                PrintStream out = new PrintStream(flog4j);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getLog4JContent());
                out.close();
                command.add("-Dlog4j.configuration=file:" + flog4j.getAbsolutePath());
            } catch (Exception e) {
                //log4j not set
                logger_dev.debug("", e);
            }
        }
        //set default PAConfiguration
        if (forkEnvironment == null ||
            !contains("proactive.configuration", forkEnvironment.getJVMArguments())) {
            try {
                fpaconfig = File.createTempFile("forked_jtp", null);
                PrintStream out = new PrintStream(fpaconfig);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getPaConfigContent());
                out.close();
                command.add("-Dproactive.configuration=file:" + fpaconfig.getAbsolutePath());
            } catch (Exception e) {
                //PAConfig not set
                logger_dev.debug("", e);
            }
        }
        // set log size to minmum value as log are handled on forker side
        if (forkEnvironment == null ||
            !contains(TaskLauncher.MAX_LOG_SIZE_PROPERTY, forkEnvironment.getJVMArguments())) {
            command.add("-D" + TaskLauncher.MAX_LOG_SIZE_PROPERTY + "=" + FORKED_LOG_BUFFER_SIZE);
        }

        if (forkEnvironment != null && forkEnvironment.getJVMArguments().size() > 0) {
            for (String s : forkEnvironment.getJVMArguments()) {
                command.add(s);
            }
        }
    }

    /**
     * Return true if the given array is null or contains the given string, false otherwise.
     *
     * @param pattern the string to search
     * @param array the String array in which to search. If this argument is null, it returns false;
     * @throws IllegalArgumentException if pattern is null
     * @return true if the given array contains the given string, false otherwise.
     */
    private boolean contains(String pattern, List<String> array) {
        if (pattern == null) {
            throw new IllegalArgumentException("Null pattern is not allowed");
        }
        if (array == null) {
            return false;
        }
        for (String s : array) {
            if (s != null && s.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add classpath to the given command
     *
     * @param command the command to be completed
     */
    private void addClasspath(List<String> command) {
        StringBuilder classPath = new StringBuilder("." + File.pathSeparatorChar);
        classPath.append(System.getProperty("java.class.path", ""));
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        if (forkEnvironment != null) {
            for (String s : forkEnvironment.getAdditionalClasspath()) {
                classPath.append(File.pathSeparatorChar + s);
            }
        }
        command.add("-cp");
        command.add(classPath.toString());
    }

    /**
     * Add runtime class name to be launched to the given command
     *
     * @param command the command to be completed
     */
    private void addRuntime(List<String> command) throws ProActiveException {
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
     *
     * @param ospb the process builder that will execute the command
     */
    private void waitForRegistration(OSProcessBuilder ospb) throws SchedulerException, InterruptedException {
        int numberOfTrials = 0;
        for (; numberOfTrials < RETRY_ACQUIRE; numberOfTrials++) {
            boolean permit = semaphore.tryAcquire(SEMAPHORE_TIMEOUT, TimeUnit.SECONDS);
            if (permit) {
                break;
            }

            try {
                int ec = process.exitValue();
                // process terminated abnormally:
                throw new StartForkedProcessException(
                    "Unable to create a separate java process. Exit code : " + ec, ospb.command());
            } catch (IllegalThreadStateException e) {
                logger_dev.debug("Process not terminated, continue Forked VM launching (try number " +
                    numberOfTrials + ")");
            }
        }
        if (numberOfTrials == RETRY_ACQUIRE) {
            throw new StartForkedProcessException("Unable to create a separate java process after " +
                RETRY_ACQUIRE + " tries", ospb.command());
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
     * Create a new process builder with user credentials if needed
     *
     * @returns an OS Process Builder
     * @throws IOException
     */
    private OSProcessBuilder createProcess() throws Exception {
        //check if it must be run under user and if so, apply the proper method
        OSProcessBuilder ospb = null;
        if (isRunAsUser()) {
            ospb = ForkerUtils.getOSProcessBuilderFactory().getBuilder(
                    ForkerUtils.checkConfigAndGetUser(this.execInitializer.getDecrypter()));
        } else {
            ospb = ForkerUtils.getOSProcessBuilderFactory().getBuilder();
        }
        return ospb;
    }

    /**
     * Set the given command to the given OS Process Builder
     *
     * @param ospb the process builder on which to set the command
     * @param command the command to be set
     */
    private void setCommand(OSProcessBuilder ospb, List<String> command) {
        ospb.command(command.toArray(new String[command.size()]));
    }

    /**
     * Set the working directory of the given OS process builder
     *
     * @param ospb the process builder on which to set the working directory
     */
    private void setWorkingDir(OSProcessBuilder ospb) {
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        if (forkEnvironment != null && forkEnvironment.getWorkingDir() != null) {
            ospb.directory(new File(forkEnvironment.getWorkingDir()));
        }
    }

    /**
     * Set the system environment of the given OS process builder
     *
     * @param ospb the process builder on which to set the system environment
     */
    private void setSystemEnvironment(OSProcessBuilder ospb) {
        try {
            Map<String, String> env = ospb.environment();
            ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
            if (forkEnvironment != null) {
                Map<String, String> fenv = forkEnvironment.getSystemEnvironment();
                if (fenv != null) {
                    for (Entry<String, String> e : fenv.entrySet()) {
                        env.put(e.getKey(), e.getValue());
                    }
                }
            }
        } catch (NotImplementedException e) {
            //normal behavior if user has not set any sys property in fork env
            logger_dev.info("OS ProcessBuilder environment could not be retreived : " + e.getMessage());
        }
    }

    /**
     * Start the process built on the given OS Process Builder.<br/>
     * Also creating a child JVM, intercepting stdout and stderr
     *
     * @param ospb the process builder on which to start a process
     * @return the started process
     * @throws Exception if a problem occurs when starting the process
     */
    private Process startProcess(OSProcessBuilder ospb) throws Exception {
        return ospb.start();
    }

    /**
     * Return true if this task is to be ran under a user account id or not.
     *
     * @return true if this task is to be ran under a user account id, false otherwise.
     */
    private boolean isRunAsUser() {
        return this.execInitializer.getDecrypter() != null;
    }

    /**
     * Create a java task launcher on the local new JVM
     *
     * @return the created java task launcher
     * @throws Exception
     */
    private JavaTaskLauncher createForkedTaskLauncher() throws Exception {
        /* creating a ProActive node on a newly created JVM */
        Node forkedNode = childRuntime.createLocalNode(forkedNodeName, true, null, DEFAULT_VN_NAME);

        /* JavaTaskLauncher will be an active object created on a newly created ProActive node */
        logger_dev.info("Create java task launcher");
        TaskLauncherInitializer tli = execInitializer.getJavaTaskLauncherInitializer();
        JavaTaskLauncher newLauncher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class
                .getName(), new Object[] { tli }, forkedNode);
        return newLauncher;
    }

    /**
     * Cleaning method kills all nodes on the dedicated JVM, destroys java process, and closes threads responsible for process output
     */
    private void clean() {
        try {
            logger_dev.info("Cleaning forked java executable");
            //if tmp file have been set, destroy it.
            if (fpolicy != null) {
                fpolicy.delete();
            }
            if (flog4j != null) {
                flog4j.delete();
            }
            if (fpaconfig != null) {
                fpaconfig.delete();
            }
            // if the process did not register then childRuntime will be null and/or process will be null
            if (childRuntime != null) {
                try {
                    childRuntime.killAllNodes();
                    childRuntime.killRT(false);
                    childRuntime = null;
                } catch (Exception e) {
                }
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
            terminateStreamReaders();
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

    /**
     * Simple AppenderProvider that provides an appender that redirect all logs
     * on REAL stdout/stderr, i.e. OUT and ERR defined by the system.
     * @see TaskLauncher.SYSTEM_OUT

     */
    public static class StdAppenderProvider implements AppenderProvider {

        /**
		 * 
		 */
		private static final long serialVersionUID = 30L;

		/**
         * Returns an appender that redirect all logs on stdout/stderr depending on the level.
         * @return  an appender that redirect all logs on stdout/stderr depending on the level.
         */
        public Appender getAppender() throws LogForwardingException {
            return new AppenderSkeleton() {

                @Override
                public boolean requiresLayout() {
                    return false;
                }

                @Override
                public void close() {
                    this.closed = true;
                }

                @Override
                protected void append(LoggingEvent event) {
                    if (event.getLevel().equals(Log4JTaskLogs.STDOUT_LEVEL)) {
                        TaskLauncher.SYSTEM_OUT.println(event.getMessage());
                    } else if (event.getLevel().equals(Log4JTaskLogs.STDERR_LEVEL)) {
                        TaskLauncher.SYSTEM_ERR.println(event.getMessage());
                    } else {
                        TaskLauncher.SYSTEM_ERR.println("[INCORRECT STREAM] " + event.getMessage());
                    }

                }
            };
        }

    }

}
