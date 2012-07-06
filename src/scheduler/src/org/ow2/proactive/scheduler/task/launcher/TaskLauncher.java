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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastSelector;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.appenders.AsyncAppenderWithStorage;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.LoggingOutputStream;
import org.ow2.proactive.scheduler.exception.IllegalProgressException;
import org.ow2.proactive.scheduler.exception.ProgressPingerException;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.KillTask;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.ow2.proactive.scripting.PropertyUtils;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.Formatter;


/**
 * Abstract Task Launcher.
 * This is the most simple task launcher.
 * It is able to launch a java task only.
 * You can extend this launcher in order to create a specific launcher.
 * With this default launcher, you can get the node on which the task is running and kill the task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public abstract class TaskLauncher {

    public static final Logger logger_dev = ProActiveLogger.getLogger(TaskLauncher.class);
    public static final Logger logger_dev_dataspace = ProActiveLogger.getLogger(TaskLauncher.class);

    //Scratch dir property : we cannot take the key property from DataSpaceNodeConfigurationAgent class in RM.
    //we should not depend from RM package in this class.
    public static final String NODE_DATASPACE_SCRATCHDIR = "node.dataspace.scratchdir";

    // Standard out/err are stored to be restored after execution
    public static final PrintStream SYSTEM_OUT = System.out;
    public static final PrintStream SYSTEM_ERR = System.err;

    public static final String EXECUTION_SUCCEED_BINDING_NAME = "success";
    public static final String DS_SCRATCH_BINDING_NAME = "localspace";
    public static final String DS_INPUT_BINDING_NAME = "input";
    public static final String DS_OUTPUT_BINDING_NAME = "output";
    public static final String DS_GLOBAL_BINDING_NAME = "globalspace";

    private static final int KEY_SIZE = 1024;

    // to define the max line number of a task logs
    // DO NOT USE SCHEDULER PROPERTY FOR THAT (those properties are not available on node side)
    @Deprecated
    public static final String OLD_MAX_LOG_SIZE_PROPERTY = "proactive.scheduler.logs.maxsize";
    public static final String MAX_LOG_SIZE_PROPERTY = "pas.launcher.logs.maxsize";

    // default log size, counted in number of log events
    // 125 based on JL's statistics :)
    public static final int DEFAULT_LOG_MAX_SIZE = 125;

    // the prefix for log file produced in localspace
    public static final String LOG_FILE_PREFIX = "TaskLogs";

    protected DataSpacesFileObject SCRATCH = null;
    protected DataSpacesFileObject INPUT = null;
    protected DataSpacesFileObject OUTPUT = null;
    protected DataSpacesFileObject GLOBAL = null;

    protected String namingServiceUrl = null;
    protected List<InputSelector> inputFiles;
    protected List<OutputSelector> outputFiles;

    // buffered string to store datapspaces error/warn messages
    private StringBuffer dataspacesStatus;

    protected OneShotDecrypter decrypter = null;

    /**
     * Scheduler related java properties. Thoses properties are automatically
     * translated into system property when the task is native (see NativeTaskLauncher) :
     * SYSENV_NAME = upcase(JAVAENV_NAME).replace('.','_')
     */
    public enum SchedulerVars {
        /**  */
        JAVAENV_JOB_ID_VARNAME("pas.job.id"),
        /**  */
        JAVAENV_JOB_NAME_VARNAME("pas.job.name"),
        /**  */
        JAVAENV_TASK_ID_VARNAME("pas.task.id"),
        /**  */
        JAVAENV_TASK_NAME_VARNAME("pas.task.name"),
        /**  */
        JAVAENV_TASK_ITERATION("pas.task.iteration"),
        /**  */
        JAVAENV_TASK_REPLICATION("pas.task.replication");

        String varName;

        SchedulerVars(String vn) {
            varName = vn;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return varName;
        }
    }

    protected TaskId taskId;
    protected Script<?> pre;
    protected Script<?> post;
    protected FlowScript flow;

    /** replication index: task was replicated in parallel */
    protected int replicationIndex = 0;
    /** iteration index: task was replicated sequentially */
    protected int iterationIndex = 0;

    // handle streams
    protected transient PrintStream redirectedStdout;
    protected transient PrintStream redirectedStderr;

    // default appender for log storage
    protected transient AsyncAppenderWithStorage logAppender;
    // if true, store logs in a file in LOCALSPACE
    protected boolean storeLogs;

    // not null if an executable is currently executed
    protected Executable currentExecutable;
    // true if the executable has been stopped before its normal termination
    protected volatile boolean hasBeenKilled;
    // true if finalizeLoggers has been called
    private final AtomicBoolean loggersFinalized = new AtomicBoolean(false);
    // true if loggers are currently activated
    private final AtomicBoolean loggersActivated = new AtomicBoolean(false);

    /** Maximum execution time of the task (in milliseconds), the variable is only valid if isWallTime is true */
    protected long wallTime = 0;

    /** The timer that will terminate the launcher if the task doesn't finish before the walltime */
    protected KillTask killTaskTimer = null;

    /** Will be replaced in file paths by the task's iteration index */
    protected static final String ITERATION_INDEX_TAG = "$IT";

    /** Will be replaced in file paths by the task's replication index */
    protected static final String REPLICATION_INDEX_TAG = "$REP";

    /** Will be replaced in file paths by the job id */
    protected static final String JOBID_INDEX_TAG = "$JID";

    /**
     * ProActive empty constructor.
     */
    public TaskLauncher() {
    }

    /**
     * Constructor with task identification.
     * CONSTRUCTOR USED BY THE SCHEDULER CORE : do not remove.
     *
     * @param initializer represents the class that contains information to initialize every task launcher.
     */
    public TaskLauncher(TaskLauncherInitializer initializer) {
        this.taskId = initializer.getTaskId();
        this.pre = initializer.getPreScript();
        this.post = initializer.getPostScript();
        this.flow = initializer.getControlFlowScript();
        if (initializer.getWalltime() > 0) {
            this.wallTime = initializer.getWalltime();
        }
        //keep input/output files descriptor in memory for further copy
        this.inputFiles = initializer.getTaskInputFiles();
        this.outputFiles = initializer.getTaskOutputFiles();
        this.namingServiceUrl = initializer.getNamingServiceUrl();
        this.replicationIndex = initializer.getReplicationIndex();
        this.iterationIndex = initializer.getIterationIndex();
        this.storeLogs = initializer.isPreciousLogs();
        this.dataspacesStatus = new StringBuffer();
        this.init();
    }

    /**
     * Initialization
     */
    private void init() {
        // plug stdout/err into a socketAppender
        this.initLoggers();
        // set scheduler defined env variables
        this.initEnv();
        logger_dev.debug("TaskLauncher initialized");
    }

    /**
     * Common final behavior for any type of task launcher.
     * @param core reference to the scheduler.
     */
    protected void finalizeTask(TaskTerminateNotification core, TaskResult taskResult) {
        /*
         * if task was killed then unsetEnv and finalizeLoggers were already called, don't call it
         * again, otherwise it can affect others tasks (SCHEDULING-1526)
         */
        if (!hasBeenKilled) {
            // unset env
            this.unsetEnv();
            // reset stdout/err
            try {
                this.finalizeLoggers();
            } catch (RuntimeException e) {
                // exception should not be thrown to the scheduler core
                // the result has been computed and must be returned !
                logger_dev.warn("Loggers are not shutdown !", e);
            }
        }

        //terminate the task
        // if currentExecutable has been killed, no call back
        if (!hasBeenKilled && core != null) {
            core.terminate(taskId, taskResult);
        }
        this.currentExecutable = null;
    }

    /**
     * Call the internal init private method on the current executable using the given argument.<br>
     * This method first get the method "internalInit" (let's call it 'm') of the given <b>targetedClass</b>.<br>
     * <b>parameterType</b> is the type of the argument to find the internal init method.<br>
     * Then the targeted method of 'm' is switched to accessible, finally 'm' is invoked on the current executable
     * with the given <b>argument</b>.
     *
     * @param targetedClass the class on which to look for the private 'internal init' method
     * @param parameterType the type of the parameter describing the definition of the method to look for.
     * @param argument the argument passed to the invocation of the found method on the current executable.
     * @throws Exception reported if something wrong occurs.
     */
    protected void callInternalInit(Class<?> targetedClass, Class<?> parameterType,
            ExecutableInitializer argument) throws Exception {
        Method m = targetedClass.getDeclaredMethod("internalInit", parameterType);
        m.setAccessible(true);
        m.invoke(currentExecutable, argument);
    }

    /**
     * Generate a couple of key and return the public one
     *
     * @return the generated public key
     * @throws NoSuchAlgorithmException if RSA is unknown
     */
    public PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = null;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();
        //connect to the authentication interface and ask for new cred
        decrypter = new OneShotDecrypter(keyPair.getPrivate());
        return keyPair.getPublic();
    }

    /**
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param execContainer contains the user defined executable to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    public abstract void doTask(TaskTerminateNotification core, ExecutableContainer execContainer,
            TaskResult... results);

    /**
     * Redirect stdout/err in the buffered appender.
     */
    @SuppressWarnings("unchecked")
    protected void initLoggers() {
        logger_dev.debug("Init loggers");
        // error about log should not be logged
        LogLog.setQuietMode(true);
        // create logger
        Logger l = Logger.getLogger(Log4JTaskLogs.JOB_LOGGER_PREFIX + this.taskId.getJobId());
        l.setAdditivity(false);
        MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
        MDC.getContext().put(Log4JTaskLogs.MDC_HOST, getHostname());
        l.removeAllAppenders();
        // create an async appender for multiplexing (storage plus redirect through socketAppender)
        String logMaxSizeProp = System.getProperty(TaskLauncher.MAX_LOG_SIZE_PROPERTY);
        if (logMaxSizeProp == null) {
            logMaxSizeProp = System.getProperty(TaskLauncher.OLD_MAX_LOG_SIZE_PROPERTY);
        }
        if (logMaxSizeProp == null || "".equals(logMaxSizeProp.trim())) {
            this.logAppender = new AsyncAppenderWithStorage(TaskLauncher.DEFAULT_LOG_MAX_SIZE);
        } else {
            try {
                int logMaxSize = Integer.parseInt(logMaxSizeProp);
                this.logAppender = new AsyncAppenderWithStorage(logMaxSize);
                logger_dev.info("Logs are limited to " + logMaxSize + " lines for task " + this.taskId);
            } catch (NumberFormatException e) {
                logger_dev.warn(MAX_LOG_SIZE_PROPERTY +
                    " property is not correctly defined. Logs size is bounded to default value " +
                    TaskLauncher.DEFAULT_LOG_MAX_SIZE + " for task " + this.taskId, e);
                this.logAppender = new AsyncAppenderWithStorage(TaskLauncher.DEFAULT_LOG_MAX_SIZE);
            }
        }
        l.addAppender(this.logAppender);
        // redirect stdout and err
        this.redirectedStdout = new PrintStream(new LoggingOutputStream(l, Log4JTaskLogs.STDOUT_LEVEL), true);
        this.redirectedStderr = new PrintStream(new LoggingOutputStream(l, Log4JTaskLogs.STDERR_LEVEL), true);
        System.setOut(redirectedStdout);
        System.setErr(redirectedStderr);
    }

    /**
     * Create log file in $LOCALSPACE.
     * @throws IOException if the file cannot be created.
     */
    private void initLocalLogsFile() throws IOException {
        DataSpacesFileObject outlog = SCRATCH.resolveFile(TaskLauncher.LOG_FILE_PREFIX + "-" +
            this.taskId.getJobId() + "-" + this.taskId.getReadableName() + ".log");
        outlog.createFile();
        // fileAppender constructor needs a path and not a URI.
        FileAppender fap = new FileAppender(Log4JTaskLogs.getTaskLogLayout(), outlog.getRealURI().substring(
                "file://".length()), false);
        this.logAppender.addAppender(fap);
    }

    /**
     * Set scheduler related variables for the current task. 
     */
    protected void initEnv() {
        // set task vars
        System.setProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString(), this.taskId.getJobId().value());
        System.setProperty(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString(), this.taskId.getJobId()
                .getReadableName());
        System.setProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(), this.taskId.value());
        System.setProperty(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString(), this.taskId.getReadableName());
        System.setProperty(SchedulerVars.JAVAENV_TASK_ITERATION.toString(), "" + this.iterationIndex);
        System.setProperty(SchedulerVars.JAVAENV_TASK_REPLICATION.toString(), "" + this.replicationIndex);

        // previously exported and propagated vars must be deleted
        System.clearProperty(PropertyUtils.EXPORTED_PROPERTIES_VAR_NAME);
        System.clearProperty(PropertyUtils.PROPAGATED_PROPERTIES_VAR_NAME);
    }

    /**
     * Reset scheduler related variables value.
     */
    protected void unsetEnv() {
        System.clearProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString());
        System.clearProperty(SchedulerVars.JAVAENV_JOB_NAME_VARNAME.toString());
        System.clearProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString());
        System.clearProperty(SchedulerVars.JAVAENV_TASK_NAME_VARNAME.toString());
        System.clearProperty(SchedulerVars.JAVAENV_TASK_ITERATION.toString());
        System.clearProperty(SchedulerVars.JAVAENV_TASK_REPLICATION.toString());
    }

    /**
     * Set as Java Property all the properties that comes with incoming results, i.e.
     * properties that have been propagated in parent tasks.
     * @see org.ow2.proactive.scripting.PropertyUtils
     */
    protected void setPropagatedProperties(TaskResult[] incomingResults) {
        for (int i = 0; i < incomingResults.length; i++) {
            Map<String, String> properties = incomingResults[i].getPropagatedProperties();
            if (properties != null) {
                logger_dev.info("Incoming properties for task " + this.taskId + " are " + properties);
                for (String key : properties.keySet()) {
                    logger_dev.debug("Value of Incoming property " + key + " is " + properties.get(key));
                    System.setProperty(key, properties.get(key));
                }
            } else {
                logger_dev.info("No Incoming properties for task " + this.taskId);
            }
        }
    }

    /**
     * Extract name and value of all the properties that have been propagated during the execution
     * of this task launcher (on scripts and executable).
     * @see org.ow2.proactive.scripting.PropertyUtils
     * @return a map that contains [name->value] of all propagated properties.
     */
    protected Map<String, BigString> retreivePropagatedProperties() {
        // get all names of propagated vars
        String allVars = System.getProperty(PropertyUtils.PROPAGATED_PROPERTIES_VAR_NAME);
        if (allVars != null) {
            logger_dev.debug("Propagated properties for task " + this.taskId + " are : " + allVars);
            StringTokenizer parser = new StringTokenizer(allVars, PropertyUtils.VARS_VAR_SEPARATOR);
            Map<String, BigString> exportedVars = new Hashtable<String, BigString>();
            while (parser.hasMoreTokens()) {
                String key = parser.nextToken();
                String value = System.getProperty(key);
                if (value != null) {
                    logger_dev.debug("Value of Propagated property " + key + " is " + value);
                    exportedVars.put(key, new BigString(value));
                    System.clearProperty(key);
                } else {
                    logger_dev.warn("Propagated property " + key + " is not set !");
                }
            }
            System.clearProperty(PropertyUtils.PROPAGATED_PROPERTIES_VAR_NAME);
            return exportedVars;
        } else {
            logger_dev.debug("No Propagated properties for task " + this.taskId);
            return null;
        }
    }

    @ImmediateService
    public void getStoredLogs(AppenderProvider logSink) {
        Appender appender;
        try {
            appender = logSink.getAppender();
        } catch (LogForwardingException e) {
            logger_dev.error("Cannot create log appender.", e);
            return;
        }
        this.logAppender.appendStoredEvents(appender);
    }

    /**
     * Activate the logs on this host and port.
     * @param logSink the provider for the appender to write in.
     */
    @SuppressWarnings("unchecked")
    @ImmediateService
    public void activateLogs(AppenderProvider logSink) {
        synchronized (this.loggersFinalized) {
            logger_dev.info("Activating logs for task " + this.taskId);
            if (this.loggersActivated.get()) {
                logger_dev.info("Logs for task " + this.taskId + " are already activated");
                return;
            }
            this.loggersActivated.set(true);
            // should reset taskId because calling thread is not active thread (immediate service)
            MDC.getContext().put(Log4JTaskLogs.MDC_TASK_ID, this.taskId.getReadableName());
            try {
                MDC.getContext().put(Log4JTaskLogs.MDC_HOST,
                        PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
            } catch (NodeException e) {
                MDC.getContext().put(Log4JTaskLogs.MDC_HOST, "Unknown host");
            }
            // create appender
            Appender appender = null;
            try {
                appender = logSink.getAppender();
            } catch (LogForwardingException e) {
                logger_dev.error("Cannot create log appender.", e);
                return;
            }
            // fill appender
            if (!this.loggersFinalized.get()) {
                this.logAppender.addAppender(appender);
            } else {
                logger_dev.info("Logs for task " + this.taskId + " are closed. Flushing buffer...");
                // Everything is closed: reopen and close...
                for (LoggingEvent e : this.logAppender.getStorage()) {
                    appender.doAppend(e);
                }
                appender.close();
                this.loggersActivated.set(false);
                return;
            }
            logger_dev.info("Activated logs for task " + this.taskId);
        }
    }

    /**
     * Close scheduler task logger and reset stdout/err
     */
    protected void finalizeLoggers() {
        synchronized (this.loggersFinalized) {
            logger_dev.debug("Terminating loggers for task " + this.taskId + "...");
            this.loggersFinalized.set(true);
            this.loggersActivated.set(false);
            //Unhandle loggers
            this.flushStreams();
            if (this.logAppender != null) {
                this.logAppender.close();
            }
            System.setOut(TaskLauncher.SYSTEM_OUT);
            System.setErr(TaskLauncher.SYSTEM_ERR);
            logger_dev.debug("Terminated loggers for task " + this.taskId);
        }
    }

    /**
     * Flush out and err streams.
     */
    protected void flushStreams() {
        if (this.redirectedStdout != null) {
            this.redirectedStdout.flush();
        }
        if (this.redirectedStderr != null) {
            this.redirectedStderr.flush();
        }
    }

    /**
     * Return a TaskLogs object that contains the logs produced by the executed tasks
     * 
     * @return a TaskLogs object that contains the logs produced by the executed tasks
     */
    @ImmediateService
    public TaskLogs getLogs() {
        this.flushStreams();
        TaskLogs logs = new Log4JTaskLogs(this.logAppender.getStorage(), this.taskId.getJobId().value());
        return logs;
    }

    /**
     * Return the latest progress value set by the launched executable.<br/>
     * If the value returned by user is not in [0:100], the closest bound (0 or 100) is returned.
     *
     * @return the latest progress value set by the launched executable.
     * @throws IllegalProgressException if the userExecutable.getProgress() method throws an exception
     */
    @ImmediateService
    public int getProgress() throws ProgressPingerException {
        if (this.currentExecutable == null) {
            //not yet started
            return 0;
        } else {
            try {
                int progress = currentExecutable.getProgress();
                if (progress < 0) {
                    logger_dev.warn("Returned progress (" + progress + ") is negative, return 0 instead.");
                    return 0;
                } else if (progress > 100) {
                    logger_dev.warn("Returned progress (" + progress +
                        ") is greater than 100, return 100 instead.");
                    return 100;
                } else {
                    return progress;
                }
            } catch (Throwable t) {
                //protect call to user getProgress() if overridden
                throw new IllegalProgressException("executable.getProgress() method has thrown an exception",
                    t);
            }
        }
    }

    /**
     * Execute the preScript on the local node.
     * 
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePreScript() throws ActiveObjectCreationException, NodeException, UserException {
        replaceTagsInScript(pre);
        logger_dev.info("Executing pre-script");
        ScriptHandler handler = ScriptLoader.createLocalHandler();
        this.addDataspaceBinding(handler);
        ScriptResult<String> res = handler.handle((Script<String>) pre);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on pre-script occured : ", res.getException());
            throw new UserException("Pre-script has failed on the current node", res.getException());
        }
        // flush prescript output
        this.flushStreams();
    }

    /**
     * Execute the postScript on the local node.
     * 
     * @param executionSucceed a boolean describing the state of the task execution.(true if execution succeed, false if not)
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePostScript(boolean executionSucceed) throws ActiveObjectCreationException,
            NodeException, UserException {
        replaceTagsInScript(post);
        logger_dev.info("Executing post-script");
        ScriptHandler handler = ScriptLoader.createLocalHandler();
        this.addDataspaceBinding(handler);
        handler.addBinding(EXECUTION_SUCCEED_BINDING_NAME, executionSucceed);
        ScriptResult<String> res = handler.handle((Script<String>) post);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on post-script occured : ", res.getException());
            throw new UserException("Post-script has failed on the current node", res.getException());
        }
        // flush postscript output
        this.flushStreams();
    }

    /**
     * Execute the control flow script on the local node and set the flow action in res.
     * 
     * @param res TaskResult of this launcher's task, input of the script.
     * @throws Throwable if an exception occurred in the flow script. 
     *      TaskResult#setAction(FlowAction) will NOT be called on res 
     */
    protected void executeFlowScript(TaskResult res) throws Throwable {
        replaceTagsInScript(flow);
        logger_dev.info("Executing flow-script");
        ScriptHandler handler = ScriptLoader.createLocalHandler();
        this.addDataspaceBinding(handler);
        handler.addBinding(FlowScript.resultVariable, res.value());
        ScriptResult<FlowAction> sRes = handler.handle(flow);
        this.flushStreams();

        if (sRes.errorOccured()) {
            Throwable ee = sRes.getException();
            if (ee != null) {
                // stacktraced on user logs
                ee.printStackTrace();
                logger_dev.error("Error on flow-script occured : ", ee);
                throw new UserException("Flow-script has failed on the current node", ee);
            }
        } else {
            FlowAction action = sRes.getResult();
            ((TaskResultImpl) res).setAction(action);
        }
    }

    /**
     * Adds in the given ScriptHandler bindings for this Launcher's Dataspace handlers
     * 
     * @param script the ScriptHandler in which bindings will be added
     */
    private void addDataspaceBinding(ScriptHandler script) {
        script.addBinding(DS_SCRATCH_BINDING_NAME, this.SCRATCH);
        script.addBinding(DS_INPUT_BINDING_NAME, this.INPUT);
        script.addBinding(DS_OUTPUT_BINDING_NAME, this.OUTPUT);
        script.addBinding(DS_GLOBAL_BINDING_NAME, this.GLOBAL);
    }

    /**
     * This method will terminate the task that has been launched.
     * In fact it will terminate the launcher.
     */
    @ImmediateService
    public void terminate(boolean normalTermination) {
        if (!normalTermination) {
            this.hasBeenKilled = true;
            if (this.currentExecutable != null) {
                this.currentExecutable.kill();
                this.currentExecutable = null;
            }

            // unset env
            this.unsetEnv();
            // reset stdout/err    
            try {
                this.finalizeLoggers();
            } catch (RuntimeException e) {
                // exception should not be thrown to the scheduler core
                // the result has been computed and must be returned !
                logger_dev.warn("Loggers are not shutdown !", e);
            }
        }
        PAActiveObject.terminateActiveObject(normalTermination);
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltime then the timer will be canceled
     */
    protected void scheduleTimer() {
        scheduleTimer(currentExecutable);
    }

    /**
     * If user specified walltime for the particular task, the timer will be scheduled to kill the task
     * if it does not finish before the walltime. If it does finish before the walltime then the timer will be canceled
     */
    protected void scheduleTimer(Executable executable) {
        if (isWallTime()) {
            logger_dev.info("Execute timer because task '" + taskId + "' is walltimed");
            killTaskTimer = new KillTask(executable, wallTime);
            killTaskTimer.schedule();
        }
    }

    /**
     * Canceling timer for killing the task, if we cancel the timer this means that the task finished before the walltime
     */
    protected void cancelTimer() {
        if (isWallTime() && killTaskTimer != null) {
            killTaskTimer.cancel();
        }
    }

    /**
     * @return the isWallTime
     */
    public boolean isWallTime() {
        return wallTime > 0;
    }

    protected void initDataSpaces() {
        if (isDataspaceAware()) {
            try {
                // configure node for application
                long id = taskId.getJobId().hashCode();
                DataSpacesNodes.configureApplication(PAActiveObject.getActiveObjectNode(PAActiveObject
                        .getStubOnThis()), id, namingServiceUrl);
                //prepare scratch, input, output
                SCRATCH = PADataSpaces.resolveScratchForAO();
                INPUT = PADataSpaces.resolveDefaultInput();
                OUTPUT = PADataSpaces.resolveDefaultOutput();
                try {
                    GLOBAL = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
                } catch (Throwable t) {
                    logger_dev_dataspace.warn("GLOBALSPACE is disabled");
                    logger_dev_dataspace.debug("", t);
                }
                // create a log file in local space if the node is configured
                if (this.storeLogs) {
                    logger_dev.info("logfile is enabled for task " + taskId);
                    initLocalLogsFile();
                }

            } catch (Throwable t) {
                logger_dev_dataspace.warn(
                        "There was a problem while initializing dataSpaces, they are not activated", t);
                this.logDataspacesStatus(
                        "There was a problem while initializing dataSpaces, they are not activated",
                        DataspacesStatusLevel.ERROR);
                this.logDataspacesStatus(Formatter.stackTraceToString(t), DataspacesStatusLevel.ERROR);
            }
        }
    }

    protected void terminateDataSpace() {
        if (isDataspaceAware()) {
            try {
                //in dataspace debug mode, scratch directory are not cleaned after task execution
                if (!logger_dev_dataspace.isDebugEnabled()) {
                    DataSpacesNodes.tryCloseNodeApplicationConfig(PAActiveObject
                            .getActiveObjectNode(PAActiveObject.getStubOnThis()));
                }
            } catch (Exception e) {
                logger_dev_dataspace
                        .warn("There was a problem while terminating dataSpaces. Dataspaces on this node might not work anymore.");
                // cannot add this message to dataspaces status as it is called in finally block
            }
        }
    }

    protected void copyInputDataToScratch() throws FileSystemException {
        if (isDataspaceAware()) {
            if (inputFiles == null) {
                logger_dev_dataspace.debug("Input selector is empty, no file to copy");
                return;
            }
            //check first the OUTPUT and then the INPUT, take care if not set
            if (INPUT == null && OUTPUT == null) {
                logger_dev_dataspace.debug("Job INPUT/OUTPUT spaces are not defined, cannot copy file.");
                this.logDataspacesStatus("Job inputspace and outputspace are not defined or not properly"
                    + " configured while intput files are specified : ", DataspacesStatusLevel.ERROR);
                for (InputSelector is : inputFiles) {
                    this.logDataspacesStatus("--> " + is, DataspacesStatusLevel.ERROR);
                }
                return;
            }

            ArrayList<DataSpacesFileObject> inResults = new ArrayList<DataSpacesFileObject>();
            ArrayList<DataSpacesFileObject> outResults = new ArrayList<DataSpacesFileObject>();
            ArrayList<DataSpacesFileObject> globResults = new ArrayList<DataSpacesFileObject>();

            FileSystemException toBeThrown = null;
            for (InputSelector is : inputFiles) {
                //fill fast file selector
                FastFileSelector fast = new FastFileSelector();
                fast.setIncludes(is.getInputFiles().getIncludes());
                fast.setExcludes(is.getInputFiles().getExcludes());
                fast.setCaseSensitive(is.getInputFiles().isCaseSensitive());
                switch (is.getMode()) {
                    case TransferFromInputSpace:
                        //search in INPUT
                        try {
                            int s = inResults.size();
                            FastSelector.findFiles(INPUT, fast, true, inResults);
                            if (s == inResults.size()) {
                                this.logDataspacesStatus(
                                        "No file is transferred from inputspace for selector " + is,
                                        DataspacesStatusLevel.WARNING);
                            }
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.info("", fse);
                            toBeThrown = new FileSystemException(
                                "Could not contact INPUT space. Check that INPUT space is still reachable !");
                            this.logDataspacesStatus("An error occured while resolving selector " + is,
                                    DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                    DataspacesStatusLevel.ERROR);
                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case TransferFromOutputSpace:
                        //search in OUTPUT
                        try {
                            int s = outResults.size();
                            FastSelector.findFiles(OUTPUT, fast, true, outResults);
                            if (s == outResults.size()) {
                                this.logDataspacesStatus(
                                        "No file is transferred from outputspace for selector " + is,
                                        DataspacesStatusLevel.WARNING);
                            }
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.info("", fse);
                            toBeThrown = new FileSystemException(
                                "Could not contact OUTPUT space. Check that OUTPUT space is still reachable !");
                            this.logDataspacesStatus("An error occured while resolving selector " + is,
                                    DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                    DataspacesStatusLevel.ERROR);
                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case TransferFromGlobalSpace:
                        try {
                            int s = globResults.size();
                            FastSelector.findFiles(GLOBAL, fast, true, globResults);
                            if (s == globResults.size()) {
                                this.logDataspacesStatus(
                                        "No file is transferred from globalspace for selector " + is,
                                        DataspacesStatusLevel.WARNING);
                            }
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.info("", fse);
                            toBeThrown = new FileSystemException(
                                "Could not contact GLOBAL space. Check that GLOBAL space is still reachable !");
                            this.logDataspacesStatus("An error occured while resolving selector " + is,
                                    DataspacesStatusLevel.ERROR);
                            this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                    DataspacesStatusLevel.ERROR);
                        } catch (NullPointerException npe) {
                            // nothing to do
                        }
                        break;
                    case none:
                        //do nothing
                        break;
                }
            }

            if (toBeThrown != null) {
                throw toBeThrown;
            }

            String outuri = (OUTPUT == null) ? "" : OUTPUT.getVirtualURI();
            String globuri = (GLOBAL == null) ? "" : GLOBAL.getVirtualURI();
            String inuri = (INPUT == null) ? "" : INPUT.getVirtualURI();

            Set<String> relPathes = new HashSet<String>();

            ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
            results.addAll(inResults);
            results.addAll(outResults);
            results.addAll(globResults);

            for (DataSpacesFileObject dsfo : results) {
                try {
                    String relativePath;
                    if (inResults.contains(dsfo)) {
                        relativePath = dsfo.getVirtualURI().replaceFirst(inuri + "/?", "");
                    } else if (outResults.contains(dsfo)) {
                        relativePath = dsfo.getVirtualURI().replaceFirst(outuri + "/?", "");
                    } else { // if (globResults.contains(dsfo)) {
                        relativePath = dsfo.getVirtualURI().replaceFirst(globuri + "/?", "");
                    }
                    logger_dev_dataspace.debug("* " + relativePath);
                    if (!relPathes.contains(relativePath)) {
                        logger_dev.info("------------ resolving " + relativePath);
                        SCRATCH.resolveFile(relativePath).copyFrom(dsfo,
                                org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
                    }
                    relPathes.add(relativePath);
                } catch (FileSystemException fse) {
                    logger_dev.info("", fse);
                    toBeThrown = fse;
                }
            }
            if (toBeThrown != null) {
                throw toBeThrown;
            }
        }
    }

    protected void copyScratchDataToOutput(List<OutputSelector> outputFiles) throws FileSystemException {
        if (isDataspaceAware()) {
            try {
                if (outputFiles == null) {
                    logger_dev_dataspace.debug("Output selector is empty, no file to copy");
                    return;
                }

                //check first the OUTPUT and then the INPUT, take care if not set
                if (OUTPUT == null) {
                    logger_dev_dataspace.debug("Job OUTPUT space is not defined, cannot copy file.");
                    this.logDataspacesStatus("Job outputspace is not defined or not properly"
                        + " configured while output files are specified : ", DataspacesStatusLevel.ERROR);
                    for (OutputSelector os : outputFiles) {
                        this.logDataspacesStatus("--> " + os, DataspacesStatusLevel.ERROR);
                    }
                    return;
                }

                ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
                FileSystemException toBeThrown = null;

                for (OutputSelector os : outputFiles) {
                    //fill fast file selector
                    FastFileSelector fast = new FastFileSelector();
                    fast.setIncludes(os.getOutputFiles().getIncludes());
                    fast.setExcludes(os.getOutputFiles().getExcludes());
                    fast.setCaseSensitive(os.getOutputFiles().isCaseSensitive());
                    switch (os.getMode()) {
                        case TransferToOutputSpace:
                            try {
                                int s = results.size();
                                handleOutput(OUTPUT, fast, results);
                                if (results.size() == s) {
                                    this.logDataspacesStatus(
                                            "No file is transferred to outputspace for selector " + os,
                                            DataspacesStatusLevel.WARNING);
                                }
                            } catch (FileSystemException fse) {
                                logger_dev_dataspace.warn("", fse);
                                toBeThrown = fse;
                                this.logDataspacesStatus(
                                        "Error while transferring to outputspace for selector " + os,
                                        DataspacesStatusLevel.ERROR);
                                this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                        DataspacesStatusLevel.ERROR);
                            }
                            break;
                        case TransferToGlobalSpace:
                            try {
                                int s = results.size();
                                handleOutput(GLOBAL, fast, results);
                                if (results.size() == s) {
                                    this.logDataspacesStatus(
                                            "No file is transferred to globalspace for selector " + os,
                                            DataspacesStatusLevel.WARNING);
                                }
                            } catch (FileSystemException fse) {
                                logger_dev_dataspace.warn("", fse);
                                toBeThrown = fse;
                                this.logDataspacesStatus(
                                        "Error while transferring to outputspace for selector " + os,
                                        DataspacesStatusLevel.ERROR);
                                this.logDataspacesStatus(Formatter.stackTraceToString(fse),
                                        DataspacesStatusLevel.ERROR);
                            }
                            break;
                        case none:
                            break;
                    }
                    results.clear();
                }

                if (toBeThrown != null) {
                    throw toBeThrown;
                }

            } finally {
                // display dataspaces error and warns if any
                this.displayDataspacesStatus();
            }
        }
    }

    protected void copyScratchDataToOutput() throws FileSystemException {
        if (isDataspaceAware()) {
            if (this.storeLogs) {
                if (this.outputFiles == null) {
                    this.outputFiles = new ArrayList<OutputSelector>();
                }
                this.outputFiles.addAll(getTaskOutputSelectors());
            }
            copyScratchDataToOutput(outputFiles);
        }
    }

    protected List<OutputSelector> getTaskOutputSelectors() {
        List<OutputSelector> result = new ArrayList<OutputSelector>(1);
        OutputSelector logFiles = new OutputSelector(new FileSelector(TaskLauncher.LOG_FILE_PREFIX + "*"),
            OutputAccessMode.TransferToOutputSpace);
        result.add(logFiles);
        return result;
    }

    private void handleOutput(DataSpacesFileObject out, FastFileSelector fast,
            ArrayList<DataSpacesFileObject> results) throws FileSystemException {
        FastSelector.findFiles(SCRATCH, fast, true, results);
        if (!logger_dev_dataspace.isDebugEnabled()) {
            if (results == null || results.size() == 0) {
                logger_dev_dataspace.debug("No file found to copy from LOCAL space to OUTPUT space");
            } else {
                logger_dev_dataspace.debug("Files that will be copied from LOCAL space to OUTPUT space :");
            }
        }
        String buri = SCRATCH.getVirtualURI();
        for (DataSpacesFileObject dsfo : results) {
            try {
                String relativePath = dsfo.getVirtualURI().replaceFirst(buri + "/?", "");
                logger_dev_dataspace.debug("* " + relativePath);
                out.resolveFile(relativePath).copyFrom(dsfo,
                        org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
            } catch (FileSystemException fse) {
                logger_dev_dataspace.warn("", fse);
                throw fse;
            }
        }
    }

    private boolean isDataspaceAware() {
        return true;
    }

    /**
     * Replace iteration and replication helper tags in the dataspace's input and output descriptions
     */
    protected void replaceTagsInDataspaces() {
        if (isDataspaceAware()) {
            if (inputFiles != null) {
                for (InputSelector is : inputFiles) {
                    String[] inc = is.getInputFiles().getIncludes();
                    String[] exc = is.getInputFiles().getExcludes();

                    if (inc != null) {
                        for (int i = 0; i < inc.length; i++) {
                            inc[i] = replaceAllTags(inc[i]);
                        }
                    }
                    if (exc != null) {
                        for (int i = 0; i < exc.length; i++) {
                            exc[i] = replaceAllTags(exc[i]);
                        }
                    }

                    is.getInputFiles().setIncludes(inc);
                    is.getInputFiles().setExcludes(exc);
                }
            }
            if (outputFiles != null) {
                for (OutputSelector os : outputFiles) {
                    String[] inc = os.getOutputFiles().getIncludes();
                    String[] exc = os.getOutputFiles().getExcludes();

                    if (inc != null) {
                        for (int i = 0; i < inc.length; i++) {
                            inc[i] = replaceAllTags(inc[i]);
                        }
                    }
                    if (exc != null) {
                        for (int i = 0; i < exc.length; i++) {
                            exc[i] = replaceAllTags(exc[i]);
                        }
                    }

                    os.getOutputFiles().setIncludes(inc);
                    os.getOutputFiles().setExcludes(exc);
                }
            }
        }
    }

    /**
     * Replace iteration and replication helper tags in the scripts' contents and parameters
     * 
     * @param script the script where tags should be replaced
     */
    protected void replaceTagsInScript(Script<?> script) {
        if (script == null) {
            return;
        }
        String code = script.getScript();
        String[] args = script.getParameters();

        code = replaceAllTags(code);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                args[i] = replaceAllTags(args[i]);
            }
        }

        script.setScript(code);
    }

    /**
     * Replace all tags ($IT, $REP and $JID) in s by the current value.
     * @return the string with all tags replaced.
     */
    private String replaceAllTags(String s) {
        s = s.replace(ITERATION_INDEX_TAG, "" + this.iterationIndex);
        s = s.replace(REPLICATION_INDEX_TAG, "" + this.replicationIndex);
        s = s.replace(JOBID_INDEX_TAG, this.taskId.getJobId().value());
        return s;
    }

    /**
     * OneShotDecrypter is used to ensure one shot usage of private key for a decryption.
     *
     * @author The ProActive Team
     * @since ProActive Scheduling 2.2
     */
    public static final class OneShotDecrypter {
        private PrivateKey key = null;
        private Credentials credentials = null;

        /**
         * Create a new instance of OneShotDecrypter
         *
         * @param key the private key that will be used for decryption
         */
        OneShotDecrypter(PrivateKey key) {
            if (key == null) {
                throw new IllegalArgumentException("Given key cannot be null");
            }
            this.key = key;
        }

        /**
         * Set the credentials to be decrypted.
         * This method is not mandatory. It allows to store the credentials temporarily.
         * A call to {@link #decrypt(Credentials)} or {@link #decrypt()} will clear the key and credentials.
         *
         * @param cred the credentials to be decrypted.
         */
        public void setCredentials(Credentials cred) {
            this.credentials = cred;
        }

        /**
         * Decrypt the given credential with this object private key.
         *
         * @param cred the credentials to be decrypted
         * @return the decrypted credData
         * @throws IllegalAccessException if the key is null or have already been used to decrypt a credential
         * @throws KeyException decryption failure, malformed data
         */
        public CredData decrypt(final Credentials cred) throws IllegalAccessException, KeyException {
            if (this.key == null) {
                throw new IllegalAccessException("Cannot decrypt credentials !");
            }
            //decrypt
            CredData data = cred.decrypt(this.key);
            //reset key
            this.key = null;
            return data;
        }

        /**
         * Decrypt the stored credential with this object private key.
         *
         * @return the decrypted credData
         * @throws IllegalAccessException if the key is null or have already been used to decrypt a credential
         * 			or if no credentials has been provided before this call.
         * @throws KeyException decryption failure, malformed data
         */
        public CredData decrypt() throws IllegalAccessException, KeyException {
            if (this.credentials == null) {
                throw new IllegalAccessException("Cannot decrypt credentials !");
            }
            return decrypt(this.credentials);
        }
    }

    /**
     * Add a message to the dataspaces status buffer. This buffer is displayed at the end 
     * of the task if non empty.
     * @param message 
     * @param level
     */
    protected void logDataspacesStatus(String message, DataspacesStatusLevel level) {
        final String eol = System.getProperty("line.separator");
        final boolean hasEol = message.endsWith(eol);
        if (level == DataspacesStatusLevel.ERROR) {
            this.dataspacesStatus.append("[DATASPACES-ERROR] " + message + (hasEol ? "" : eol));
        } else if (level == DataspacesStatusLevel.WARNING) {
            this.dataspacesStatus.append("[DATASPACES-WARNING] " + message + (hasEol ? "" : eol));
        } else if (level == DataspacesStatusLevel.INFO) {
            this.dataspacesStatus.append("[DATASPACES-INFO] " + message + (hasEol ? "" : eol));
        }
    }

    /**
     * Log level for dataspaces messages.
     */
    protected enum DataspacesStatusLevel {
        ERROR, WARNING, INFO;
    }

    /**
     * Display the content of the dataspaces status buffer on stderr if non empty.
     */
    protected void displayDataspacesStatus() {
        if (this.dataspacesStatus.length() != 0) {
            System.err.println("");
            System.err.println(this.dataspacesStatus);
            this.dataspacesStatus = new StringBuffer();
        }
    }

    /**
     * Use ProActive API to get the hostname of this JVM.
     * Avoid using ActiveObject API which can be not started at initialization time.<br/>
     * <br/>
     * This method don't need the activeObject to exist to be called.
     * 
     * @return the hostname of the local JVM
     */
    private String getHostname() {
        //return PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName();
        return ProActiveInet.getInstance().getInetAddress().getHostName();
        // return URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress());
    }

}
