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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.SweetCountDownLatch;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastSelector;
import org.ow2.proactive.db.types.BigString;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.task.ExecutableInitializer;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.executable.Executable;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.appenders.AsyncAppenderWithStorage;
import org.ow2.proactive.scheduler.common.util.logforwarder.util.LoggingOutputStream;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.KillTask;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;
import org.ow2.proactive.scripting.PropertyUtils;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptHandler;
import org.ow2.proactive.scripting.ScriptLoader;
import org.ow2.proactive.scripting.ScriptResult;


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
public abstract class TaskLauncher implements InitActive {

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.LAUNCHER);
    public static final Logger logger_dev_dataspace = ProActiveLogger
            .getLogger(SchedulerDevLoggers.DATASPACE);
    public static final String EXECUTION_SUCCEED_BINDING_NAME = "success";

    protected DataSpacesFileObject SCRATCH = null;
    protected DataSpacesFileObject INPUT = null;
    protected DataSpacesFileObject OUTPUT = null;
    protected String namingServiceUrl = null;
    protected List<InputSelector> inputFiles;
    protected List<OutputSelector> outputFiles;

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
        JAVAENV_TASK_NAME_VARNAME("pas.task.name");

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

    // handle streams
    protected transient PrintStream redirectedStdout;
    protected transient PrintStream redirectedStderr;
    // default appender for log storage
    protected transient AsyncAppenderWithStorage logAppender;

    // not null if an executable is currently executed
    protected Executable currentExecutable;
    // true if the executable has been stopped before its normal termination
    protected boolean hasBeenKilled;
    // true if finalizeLoggers has been called
    private final AtomicBoolean loggersFinalized = new AtomicBoolean(false);
    // true if loggers are currently activated
    private final AtomicBoolean loggersActivated = new AtomicBoolean(false);
    // 0 if the launcher (loggers and env) have been initialized
    private final SweetCountDownLatch launcherInitialized = new SweetCountDownLatch(1);

    /** Maximum execution time of the task (in milliseconds), the variable is only valid if isWallTime is true */
    protected long wallTime = 0;

    /** The timer that will terminate the launcher if the task doesn't finish before the walltime */
    protected KillTask killTaskTimer = null;

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
        if (initializer.getWalltime() > 0) {
            this.wallTime = initializer.getWalltime();
        }
        //keep input/output files descriptor in memory for further copy
        this.inputFiles = initializer.getTaskInputFiles();
        this.outputFiles = initializer.getTaskOutputFiles();
        this.namingServiceUrl = initializer.getNamingServiceUrl();
    }

    /**
     * Initializes the activity of the active object.
     * @param body the body of the active object being initialized
     */
    public void initActivity(Body body) {
        // TODO cdelbe : race condition remains here 
        // see PROACTIVE-652
        // see SCHEDULING-263
        PAActiveObject.setImmediateService("activateLogs");
        PAActiveObject.setImmediateService("terminate");
        PAActiveObject.setImmediateService("getLogs");
        logger_dev.debug("Immediate services : activateLogs, terminate, getLogs");

        // plug stdout/err into a socketAppender
        this.initLoggers();
        // set scheduler defined env variables
        this.initEnv();
        // set the launcher as initialized
        this.launcherInitialized.countDown();
        logger_dev.debug("TaskLauncher initialized");
    }

    /**
     * Common final behavior for any type of task launcher.
     * @param core reference to the scheduler.
     */
    protected void finalizeTask(TaskTerminateNotification core) {
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

        //terminate the task
        // if currentExecutable has been killed, no call back
        if (!hasBeenKilled) {
            core.terminate(taskId);
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
     * Execute the user task as an active object.
     *
     * @param core The scheduler core to be notify
     * @param execContainer contains the user defined executable to execute
     * @param results the possible results from parent tasks.(if task flow)
     * @return a task result representing the result of this task execution.
     */
    public abstract TaskResult doTask(TaskTerminateNotification core, ExecutableContainer execContainer,
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
        try {
            MDC.getContext().put(Log4JTaskLogs.MDC_HOST,
                    PAActiveObject.getNode().getNodeInformation().getVMInformation().getHostName());
        } catch (NodeException e) {
            MDC.getContext().put(Log4JTaskLogs.MDC_HOST, "Unknown host");
        }
        l.removeAllAppenders();
        // create an async appender for multiplexing (storage plus redirect through socketAppender)
        // int logMaxSize = PASchedulerProperties.LOGS_MAX_SIZE.getValueAsInt();
        this.logAppender = new AsyncAppenderWithStorage();//logMaxSize);
        l.addAppender(this.logAppender);
        // redirect stdout and err
        this.redirectedStdout = new PrintStream(new LoggingOutputStream(l, Level.INFO), true);
        this.redirectedStderr = new PrintStream(new LoggingOutputStream(l, Level.ERROR), true);
        System.setOut(redirectedStdout);
        System.setErr(redirectedStderr);
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

    /**
     * Activate the logs on this host and port.
     * @param logSink the provider for the appender to write in.
     */
    @SuppressWarnings("unchecked")
    public void activateLogs(AppenderProvider logSink) {
        this.launcherInitialized.await();
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
            Appender a = null;
            try {
                a = logSink.getAppender();
            } catch (LogForwardingException e) {
                logger_dev.error("Cannot create log appender.", e);
                return;
            }
            // fill appender
            if (!this.loggersFinalized.get()) {
                this.logAppender.addAppender(a);
            } else {
                logger_dev.info("Logs for task " + this.taskId + " are closed. Flushing buffer...");
                // Everything is closed: reopen and close...
                for (LoggingEvent e : this.logAppender.getStorage()) {
                    a.doAppend(e);
                }
                a.close();
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
            this.logAppender.close();
            System.setOut(System.out);
            System.setErr(System.err);
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
    public TaskLogs getLogs() {
        this.flushStreams();
        TaskLogs logs = new Log4JTaskLogs(this.logAppender.getStorage());
        return logs;
    }

    /**
     * Execute the preScript on the node n, or on the default node if n is null.
     * 
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePreScript(Node n) throws ActiveObjectCreationException, NodeException,
            UserException {
        logger_dev.info("Executing pre-script");
        ScriptHandler handler = ScriptLoader.createHandler(n);
        ScriptResult<String> res = handler.handle(pre);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on pre-script occured : ", res.getException());
            throw new UserException("Pre-script has failed on the current node", res.getException());
        }
        // flush prescript output
        this.flushStreams();
    }

    /**
     * Execute the postScript on the node n, or on the default node if n is null.
     * 
     * @param Node the node on which to execute the post script
     * @param executionSucceed a boolean describing the state of the task execution.(true if execution succeed, false if not)
     * @throws ActiveObjectCreationException if the script handler cannot be created
     * @throws NodeException if the script handler cannot be created
     * @throws UserException if an error occurred during the execution of the script
     */
    @SuppressWarnings("unchecked")
    protected void executePostScript(Node n, boolean executionSucceed) throws ActiveObjectCreationException,
            NodeException, UserException {
        logger_dev.info("Executing post-script");
        ScriptHandler handler = ScriptLoader.createHandler(n);
        handler.addBinding(EXECUTION_SUCCEED_BINDING_NAME, executionSucceed);
        ScriptResult<String> res = handler.handle(post);

        if (res.errorOccured()) {
            res.getException().printStackTrace();
            logger_dev.error("Error on post-script occured : ", res.getException());
            throw new UserException("Post-script has failed on the current node", res.getException());
        }
        // flush postscript output
        this.flushStreams();
    }

    /**
     * This method will terminate the task that has been launched.
     * In fact it will terminate the launcher.
     */
    public void terminate(boolean normalTermination) {
        if (!normalTermination) {
            if (this.currentExecutable != null) {
                this.currentExecutable.kill();
                this.hasBeenKilled = true;
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
        PAActiveObject.terminateActiveObject(true);
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
            } catch (Throwable t) {
                logger_dev_dataspace.warn(
                        "There was a problem while initializing dataSpaces, they won't be activated", t);
                //print for user task
                System.err
                        .println("There was a problem while initializing dataSpaces, they won't be activated : " +
                            t.getMessage());
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
                //dwooooo !
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
                return;
            }

            ArrayList<DataSpacesFileObject> results = new ArrayList<DataSpacesFileObject>();
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
                            FastSelector.findFiles(INPUT, fast, true, results);
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.info("", fse);
                            toBeThrown = new FileSystemException(
                                "Could not contact INPUT space. Check that INPUT space is still reachable !");
                        } catch (NullPointerException npe) {
                            //do nothing
                        }
                        break;
                    case TransferFromOutputSpace:
                        //search in OUTPUT
                        try {
                            FastSelector.findFiles(OUTPUT, fast, true, results);
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.info("", fse);
                            toBeThrown = new FileSystemException(
                                "Could not contact OUTPUT space. Check that OUTPUT space is still reachable !");
                        } catch (NullPointerException npe) {
                            //do nothing
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
            String inuri = (INPUT == null) ? "" : INPUT.getVirtualURI();

            Set<String> relPathes = new HashSet<String>();

            //debug ---
            if (!logger_dev_dataspace.isDebugEnabled()) {
                if (results == null || results.size() == 0) {
                    logger_dev_dataspace
                            .debug("No file found to copy from INPUT or OUTPUT space to LOCAL space");
                } else {
                    logger_dev_dataspace
                            .debug("Files that will be copied from INPUT or OUTPUT space to LOCAL space :");
                }
            }
            //debug ---

            for (DataSpacesFileObject dsfo : results) {
                try {
                    String relativePath;
                    if (dsfo.isWritable()) {
                        relativePath = dsfo.getVirtualURI().replaceFirst(outuri + "/?", "");
                    } else {
                        relativePath = dsfo.getVirtualURI().replaceFirst(inuri + "/?", "");
                    }
                    logger_dev_dataspace.debug("* " + relativePath);
                    if (!relPathes.contains(relativePath)) {
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

    protected void copyScratchDataToOutput() throws FileSystemException {
        if (isDataspaceAware()) {
            if (outputFiles == null) {
                logger_dev_dataspace.debug("Output selector is empty, no file to copy");
                return;
            }
            //check first the OUTPUT and then the INPUT, take care if not set
            if (OUTPUT == null) {
                logger_dev_dataspace.debug("Job OUTPUT space is not defined, cannot copy file.");
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
                            FastSelector.findFiles(SCRATCH, fast, true, results);
                            //debug ---
                            if (!logger_dev_dataspace.isDebugEnabled()) {
                                if (results == null || results.size() == 0) {
                                    logger_dev_dataspace
                                            .debug("No file found to copy from LOCAL space to OUTPUT space");
                                } else {
                                    logger_dev_dataspace
                                            .debug("Files that will be copied from LOCAL space to OUTPUT space :");
                                }
                            }
                            //debug ---
                            String buri = SCRATCH.getVirtualURI();
                            for (DataSpacesFileObject dsfo : results) {
                                try {
                                    String relativePath = dsfo.getVirtualURI().replaceFirst(buri + "/?", "");
                                    logger_dev_dataspace.debug("* " + relativePath);
                                    OUTPUT
                                            .resolveFile(relativePath)
                                            .copyFrom(
                                                    dsfo,
                                                    org.objectweb.proactive.extensions.dataspaces.api.FileSelector.SELECT_SELF);
                                } catch (FileSystemException fse) {
                                    logger_dev_dataspace.warn("", fse);
                                    toBeThrown = fse;
                                }
                            }
                        } catch (FileSystemException fse) {
                            logger_dev_dataspace.warn("", fse);
                            toBeThrown = fse;
                        }
                        break;
                    case none:
                        break;
                }
            }

            if (toBeThrown != null) {
                throw toBeThrown;
            }
        }
    }

    private boolean isDataspaceAware() {
        return true;
    }

}
