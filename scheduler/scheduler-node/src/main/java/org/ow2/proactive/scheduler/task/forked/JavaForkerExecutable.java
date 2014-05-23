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
package org.ow2.proactive.scheduler.task.forked;

import static org.ow2.proactive.scheduler.common.util.VariablesUtil.filterAndUpdate;

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
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.extensions.amqp.federation.AMQPFederationConfig;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.objectweb.proactive.extensions.processbuilder.exception.NotImplementedException;
import org.ow2.proactive.resourcemanager.utils.OneJar;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.exception.TaskAbortedException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.exception.ForkedJVMProcessException;
import org.ow2.proactive.scheduler.exception.ProgressPingerException;
import org.ow2.proactive.scheduler.exception.StartForkedProcessException;
import org.ow2.proactive.scheduler.task.utils.Guard;
import org.ow2.proactive.scheduler.task.utils.InternalForkEnvironment;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncherInitializer;
import org.ow2.proactive.scheduler.task.utils.ForkerUtils;
import org.ow2.proactive.scheduler.task.utils.TaskResultCallback;
import org.ow2.proactive.scheduler.util.process.ThreadReader;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.FileToBytesConverter;
import org.ow2.proactive.utils.NodeSet;


/**
 * This Executable is responsible for executing another executable in a separate JVM. 
 * It receives a reference to a remote taskLauncher object, and delegates execution to this object.
 *
 * @author The ProActive Team
 *
 */
public class JavaForkerExecutable extends JavaExecutable implements ForkerStarterCallback {

    public static final Logger logger = Logger.getLogger(JavaForkerExecutable.class);

    /** Fork environment script binding name */
    public static final String FORKENV_BINDING_NAME = "forkEnvironment";

    /** Forked JVM logs directory property name */
    public static final String FORKED_LOGS_HOME = "pa.logs.home";

    /** Forked JVM logs directory property name */
    public static final String FORKED_PARENT_NODE = "pa.forker.node";

    /** Check start timeout : timeout to check if the process has failed */
    private static final long CHECKSTART_TIMEOUT = 2000;

    /** Total times we wait for the start */
    private static final long START_TIMEOUT = 60 * 1000;

    /** Size of the log buffer on forked side ; logs are buffered on forker side */
    private static final int FORKED_LOG_BUFFER_SIZE = 0;

    /** Forked execution time out checking interval */
    private static final int TIMEOUT = 1000;

    /** Thread for listening out/err of the forked JVM */
    private transient Thread tsout, tserr;

    private String forkedNodeName;
    private ForkedJavaExecutableInitializer execInitializer;
    private File fpolicy = null;
    private File flog4j = null;
    private File fpaconfig = null;

    private Process process = null;
    private Node forkedNode = null;
    private Boolean processStarted = false;

    protected LauncherGuard launcherGuard = new LauncherGuard();

    final private TaskLauncher taskLauncherStub;

    /** Hibernate default constructor */
    public JavaForkerExecutable(TaskLauncher launcherStub) {
        this.taskLauncherStub = launcherStub;
    }

    /**
     * Initialize the executable using the given executable Initializer.
     *
     * @param execInitializer the executable Initializer used to init the executable itself
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

            launcherGuard.setNode(PAActiveObject.getNode());

            // building command for executing java and start process
            OSProcessBuilder ospb = createProcessAndPrepareCommand();
            process = startProcess(ospb);
            this.initStreamReaders();
            waitForRegistration(ospb);

            //create task launcher on new JVM node
            logger.debug("Create remote task launcher");

            launcherGuard.initialize(createForkedTaskLauncher());

            // redirect tasks logs to local stdout/err
            launcherGuard.use().activateLogs(new StdAppenderProvider());

            execInitializer.getJavaExecutableContainer().setNodes(new NodeSet(execInitializer.getNodes()));
            //do task must not pass schedulerCore object,
            //the deployed java task must not notify the core from termination
            //the forked java task launcher will do that in place
            logger.debug("Starting java task");
            launcherGuard.configureNode();
            launcherGuard.doTaskAndGetResult(results);

            //waiting for task result futur
            //as it is forked, wait until futur has arrive or someone kill the task (core OR tasktimer)
            logger.debug("Java task started, waiting for result or kill...");
            while (!launcherGuard.wasKilled() && !launcherGuard.resultAvailable()) {
                try {
                    launcherGuard.waitForResult(TIMEOUT);
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

            if (launcherGuard.wasKilled()) {
                throw new TaskAbortedException("Task killed or walltime exceeded");
            }
            return launcherGuard.getResult();
        } finally {
            launcherGuard.clean(TaskLauncher.CLEAN_TIMEOUT);
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
    private OSProcessBuilder createProcessAndPrepareCommand() throws Throwable {
        logger.debug("Preparing new java process");
        //create process builder
        OSProcessBuilder ospb = createProcess();
        //update fork env with forked system env
        createInternalForkEnvironment(ospb);
        //execute environment script
        launcherGuard.executeEnvScript(ospb);
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
        if (logger.isDebugEnabled()) {
            logger.debug("JVM process and command created with command : " + command);
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
            logger.info("Executing env-script");
            Script<String> envScript = (Script<String>) fe.getEnvScript();
            // resolve any variables in the environment setup script
            filterAndUpdate(envScript, getVariables());

            ScriptHandler handler = ScriptLoader.createLocalHandler();
            handler.addBinding(FORKENV_BINDING_NAME, fe);

            // add dataspaces binding
            handler.addBinding(TaskLauncher.DS_SCRATCH_BINDING_NAME, this.execInitializer.getLocal());
            handler.addBinding(TaskLauncher.DS_INPUT_BINDING_NAME, this.execInitializer.getInput());
            handler.addBinding(TaskLauncher.DS_OUTPUT_BINDING_NAME, this.execInitializer.getOutput());
            handler.addBinding(TaskLauncher.DS_GLOBAL_BINDING_NAME, this.execInitializer.getGlobal());

            ScriptResult<String> res = handler.handle(envScript);
            //result
            if (res.errorOccured()) {
                res.getException().printStackTrace();
                logger.error("Error on env-script occured : ", res.getException());
                throw new UserException("Env-script has failed on the current node", res.getException());
            }
        }
    }

    /**
     * Return the progress value of the executable that runs in the forked JVM.
     */
    public int getProgress() {
        return launcherGuard.getProgress();
    }

    /**
     * We need to set deployment ID. The new JVM will register itself in the current JVM.
     * The current JVM will recognize it by this deployment ID.
     */
    private void init() throws Exception {
        Random random = new Random((new Date()).getTime());
        long deploymentID = random.nextInt(1000000);
        forkedNodeName = "f" + deploymentID;
        Map<String, Serializable> propagatedVariables = SerializationUtil
                .deserializeVariableMap(this.execInitializer.getPropagatedVariables());
        setVariables(propagatedVariables);
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

    private String getLogsHome() {
        String logHome = System.getProperty(PASchedulerProperties.SCHEDULER_HOME.getKey());
        if (logHome == null) {
            logHome = System.getProperty(CentralPAPropertyRepository.PA_HOME.getName());
        }
        try {
            if (logHome == null) {
                ProActiveRuntimeImpl runtime = ProActiveRuntimeImpl.getProActiveRuntime();
                logHome = runtime.getProActiveHome();
            }
        } catch (ProActiveException pae) {
            logHome = System.getProperty("java.io.tmpdir");
        }
        return logHome + File.separator + "logs";
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
                fpolicy = createTempFile("forked_jts");
                PrintStream out = new PrintStream(fpolicy);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getPolicyContent());
                out.close();
                command.add("-Djava.security.policy=" + fpolicy.getAbsolutePath());
            } catch (Exception e) {
                //java policy not set
                logger.debug("", e);
            }
        }
        //set logHome than can be used in log4j file
        String logHome = getLogsHome();
        File tmp = new File(logHome);
        if (!tmp.exists()) {
            tmp.mkdir();
        }

        String nodeName;
        try {
            nodeName = PAActiveObject.getNode().getNodeInformation().getName();
        } catch (ProActiveException pae) {
            nodeName = "DefaultNode";
        }
        command.add("-D" + FORKED_LOGS_HOME + "=" + logHome);

        command.add("-D" + FORKED_PARENT_NODE + "=" + nodeName);

        //set mandatory log4j file
        if (forkEnvironment == null || !contains("log4j.configuration", forkEnvironment.getJVMArguments())) {
            try {
                flog4j = createTempFile("forked_jtl");
                PrintStream out = new PrintStream(flog4j);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getLog4JContent());
                out.close();
                command.add("-Dlog4j.configuration=file:" + flog4j.getAbsolutePath());
            } catch (Exception e) {
                //log4j not set
                logger.debug("", e);
            }
        }
        //set default PAConfiguration
        if (forkEnvironment == null ||
            !contains("proactive.configuration", forkEnvironment.getJVMArguments())) {
            try {
                fpaconfig = createTempFile("forked_jtp");
                PrintStream out = new PrintStream(fpaconfig);
                out.print(execInitializer.getJavaTaskLauncherInitializer().getPaConfigContent());
                out.close();
                command.add("-Dproactive.configuration=file:" + fpaconfig.getAbsolutePath());
            } catch (Exception e) {
                //PAConfig not set
                logger.debug("", e);
            }
        }
        // set log size to minimum value as log are handled on forker side
        if (forkEnvironment == null ||
            !contains(TaskLauncher.MAX_LOG_SIZE_PROPERTY, forkEnvironment.getJVMArguments())) {
            command.add("-D" + TaskLauncher.MAX_LOG_SIZE_PROPERTY + "=" + FORKED_LOG_BUFFER_SIZE);
        }
        //set scratchdir
        String sd = System.getProperty(TaskLauncher.NODE_DATASPACE_SCRATCHDIR);
        if (sd != null && !"".equals(sd)) {
            command.add("-D" + TaskLauncher.NODE_DATASPACE_SCRATCHDIR + "=" + sd);
        }

        if (forkEnvironment != null && forkEnvironment.getJVMArguments().size() > 0) {
            for (String s : forkEnvironment.getJVMArguments()) {
                command.add(s);
            }
        }

        propagateProtocolProperties(command);
    }

    private void propagateProtocolProperties(List<String> command) {
        //TODO SCHEDULING-1302 : WORK AROUND
        //automatically propagate PAMR props to the forked JVM if defined on the forker.
        PAProperty[] properitiesToPropagate = { PAMRConfig.PA_NET_ROUTER_ADDRESS,
                PAMRConfig.PA_NET_ROUTER_PORT, AMQPConfig.PA_AMQP_BROKER_ADDRESS,
                AMQPConfig.PA_AMQP_BROKER_PORT, AMQPConfig.PA_AMQP_BROKER_USER,
                AMQPConfig.PA_AMQP_BROKER_PASSWORD, AMQPConfig.PA_AMQP_BROKER_VHOST,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_ADDRESS,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PORT,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_USER,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PASSWORD,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_VHOST,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_MAPPING_FILE };

        for (PAProperty property : properitiesToPropagate) {
            if (property.isSet()) {
                command.add(property.getCmdLine() + property.getValueAsString());
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

        for (String classpathElement : OneJar.getClasspath()) {
            classPath.append(File.pathSeparatorChar).append(classpathElement);
        }
        ForkEnvironment forkEnvironment = execInitializer.getForkEnvironment();
        if (forkEnvironment != null) {
            for (String s : forkEnvironment.getAdditionalClasspath()) {
                classPath.append(File.pathSeparatorChar).append(s);
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
        command.add(ForkerStarter.class.getName());
        String url = PAActiveObject.registerByName(taskLauncherStub, forkedNodeName, "pnp");
        command.add(url);
        command.add(forkedNodeName);
    }

    /**
     * wait until the child runtime callback at the current JVM
     * in case it fails to register (because of any reason), we don't start the task at all exiting with an exception
     *
     * @param ospb the process builder that will execute the command
     */
    private synchronized void waitForRegistration(OSProcessBuilder ospb) throws SchedulerException,
            InterruptedException {
        int numberOfTrials = 0;

        long waitEndTime = System.currentTimeMillis() + START_TIMEOUT;

        while (System.currentTimeMillis() < waitEndTime) {
            this.wait(CHECKSTART_TIMEOUT);

            if (processStarted) {
                //process is started : OK unlock return to unlock this method
                return;
            }

            try {
                int ec = process.exitValue();
                throw startProcessException(String.format(
                        "Unable to create a separate java process. Exit code: %d.", ec), ospb);
            } catch (IllegalThreadStateException e) {
                //thrown by process.exitValue() if process is not finished
                logger.debug("Process not terminated, continue launching Forked VM (try number " +
                    numberOfTrials + ")");
            }
        }
        throw startProcessException(String.format("Separate java process didn't start after %dms.",
                START_TIMEOUT), ospb);
    }

    private StartForkedProcessException startProcessException(String message, OSProcessBuilder ospb) {
        String processInfo = getProcessCommandInformation(ospb.command(), fpolicy, flog4j, fpaconfig);
        throw new StartForkedProcessException(message + "\nProcess information:\n" + processInfo);
    }

    String getProcessCommandInformation(List<String> command, File... tempFiles) {
        if (command.size() < 1) {
            throw new IllegalArgumentException();
        }

        StringBuilder result = new StringBuilder();
        result.append("Command:\n");
        result.append(command.get(0));
        for (int i = 1; i < command.size(); i++) {
            result.append(' ').append(command.get(i));
        }
        if (tempFiles.length > 0) {
            result.append("\nTemporary files:\n");
            for (File file : tempFiles) {
                if (file != null) {
                    String content;
                    try {
                        content = new String(FileToBytesConverter.convertFileToByteArray(file));
                    } catch (IOException e) {
                        content = "Failed to get file content: " + e.getMessage();
                    }
                    result.append("Temporary file '" + file.getName() + "' ").append(
                            "(" + file.getAbsolutePath() + "). ");
                    result.append("Content of the temporary file:\n").append(content).append('\n');
                }
            }
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void callback(Node n) {
        forkedNode = n;
        processStarted = true;
        this.notify();
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
            logger.info("OS ProcessBuilder environment could not be retreived : " + e.getMessage());
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
    private JavaTaskLauncherForked createForkedTaskLauncher() throws Exception {
        /* JavaTaskLauncher will be an active object created on a newly created ProActive node */
        logger.info("Create java task launcher");
        TaskLauncherInitializer tli = execInitializer.getJavaTaskLauncherInitializer();
        // for the forked java task precious log is is handled by the JavaTaskLauncherForker
        tli.setPreciousLogs(false);
        JavaTaskLauncherForked newLauncher = (JavaTaskLauncherForked) PAActiveObject.newActive(
                JavaTaskLauncherForked.class.getName(), new Object[] { tli }, forkedNode);
        return newLauncher;
    }

    /**
     * Create temp file in java.io.tmpdir if SCRATCHDIR is not set, 
     * otherwise, create temp file in SCRATCHDIR
     *
     * @param prefix the prefix for the temp file
     * @return the newly created file
     * @throws IOException if the temp file could not be created,
     * 					   if SCRATCH DIR does not exist or is not a directory
     */
    private File createTempFile(String prefix) throws IOException {
        String sd = System.getProperty(TaskLauncher.NODE_DATASPACE_SCRATCHDIR);
        if (sd == null || "".equals(sd)) {
            //create file in java.io.tmpdir
            return File.createTempFile(prefix, null);
        } else {
            File f = new File(System.getProperty(TaskLauncher.NODE_DATASPACE_SCRATCHDIR));
            //check if scratch dir exists and is a directory
            if (!f.exists()) {
                throw new IOException(f.getAbsolutePath() + " SCRATCH DIR not found");
            } else if (!f.isDirectory()) {
                throw new IOException(f.getAbsolutePath() + " SCRATCH DIR is not a directory");
            } else {
                return File.createTempFile(prefix, null, f);
            }
        }
    }

    /**
     * Close forked JVM dataspace before killing
     * See SCHEDULING-1080
     * @since Scheduling 3.0.1
     */
    @Override
    public void kill() {

        // send a kill message to the forked jvm
        launcherGuard.kill(false);

        launcherGuard.clean(TaskLauncher.CLEAN_TIMEOUT);

        // set the task status to killed
        super.kill();
    }

    /**
     * Simple AppenderProvider that provides an appender that redirect all logs
     * on REAL stdout/stderr, i.e. OUT and ERR defined by the system.
     * @see TaskLauncher

     */
    public static class StdAppenderProvider implements AppenderProvider {

        /**
         * Returns an appender that redirect all logs on stdout/stderr depending on the level.
         * @return an appender that redirect all logs on stdout/stderr depending on the level.
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

    public class LauncherGuard extends Guard<JavaTaskLauncherForked> {

        TaskResult result;
        ForkedJVMProcessException resultException = null;

        boolean resultAvailable = false;
        Object syncResultAccess = new Object();

        TaskResultCallback taskResultHandlerStub = null;

        int lastProgress = 0;
        int pingAttempt = 0;

        String forkedJVMDataspace;

        public void doTaskAndGetResult(TaskResult... results) throws ActiveObjectCreationException,
                NodeException {
            TaskResultCallback taskResultHandler = new TaskResultCallback(this);
            TaskResultCallback taskResultHandlerStub = PAActiveObject.turnActive(taskResultHandler);

            launcherGuard.use().doTaskAndGetResult(taskResultHandlerStub,
                    execInitializer.getJavaExecutableContainer(), results);
        }

        public void setResult(TaskResult taskResult) {
            synchronized (syncResultAccess) {
                logger.debug("Setting the result of task");
                result = taskResult;
                resultAvailable = true;
                syncResultAccess.notifyAll();
            }
        }

        public TaskResult getResult() {
            synchronized (syncResultAccess) {
                return result;
            }
        }

        public void waitForResult(int timeout) throws InterruptedException {
            synchronized (syncResultAccess) {
                syncResultAccess.wait(timeout);
                if (resultException != null) {
                    throw resultException;
                }
            }
        }

        public boolean resultAvailable() {
            synchronized (syncResultAccess) {
                return resultAvailable;
            }
        }

        public synchronized void configureNode() {
            forkedJVMDataspace = launcherGuard.use().configureNode();
        }

        private void executeEnvScript(final OSProcessBuilder ospb) throws Throwable {
            submitACallable(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        JavaForkerExecutable.this.executeEnvScript(ospb);
                        return true;
                    } catch (Throwable throwable) {
                        throw new ToUnwrapException(throwable);
                    }
                }
            }, false);
            waitCallable();
        }

        @Override
        public void internalKill() {
            if (targetInitialized) {
                try {
                    target.killForkedJavaTaskLauncher();
                } catch (Throwable e) {
                    logger.debug("Exception when killing Forked Java Task Launcher, this may be normal.", e);
                }
            }
        }

        @Override
        protected void internalClean() {
            try {
                logger.info("Cleaning forked java executable");
                if (!killMessageReceived && targetInitialized) {
                    // killing remote processes
                    try {
                        target.cleanForkedJavaTaskLauncher();
                    } catch (Throwable e) {
                        logger.warn("Exception when Cleaning Forked Java Task Launcher.", e);
                    }
                }

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
                if (process != null) {
                    process.destroy();
                }
                terminateStreamReaders();
            } catch (Exception e) {
                logger.error("", e);
            }
        }

        public synchronized int getProgress() {
            if (this.state == GuardState.NOT_INITIALIZED) {
                return 0;
            } else if (this.state == GuardState.KILLED || this.state == GuardState.CLEANED) {
                return lastProgress;
            } else {
                try {
                    lastProgress = this.target.getProgress();
                    pingAttempt = 0;
                    return lastProgress;
                } catch (ProgressPingerException e) {
                    // in that case it is an exception produced by the getProgress method
                    throw e;
                } catch (Throwable e) {
                    pingAttempt++;

                    ForkedJVMProcessException exception = new ForkedJVMProcessException(
                        "Forked JVM seems to be dead", e);

                    if (pingAttempt >= execInitializer.getJavaTaskLauncherInitializer().getPingAttempts()) {
                        // Forked JVM seems be to be dead - unblocking task launcher
                        synchronized (syncResultAccess) {
                            resultException = exception;
                            syncResultAccess.notifyAll();
                        }
                    }

                    // in that case it is another kind of exception most likely related to communication
                    throw exception;
                }
            }
        }

    }
}
