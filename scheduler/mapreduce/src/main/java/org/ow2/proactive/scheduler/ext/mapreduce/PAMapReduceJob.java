package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.ext.mapreduce.exception.PAJobConfigurationException;
import org.ow2.proactive.scheduler.ext.mapreduce.fs.PADataSpacesFileSystem;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher.SchedulerVars;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;


/**
 * {@link PAMapReduceJob} creates the ProActive MapReduce Workflow to submit to
 * the ProActive Scheduler. To do this it translates the Hadoop Job (created
 * using the new Hadoop MapReduce API) into a ProActive MapReduce Workflow,
 * using some additional information the user must provide specifying them in
 * the PAMapReduceJobConfiguration object.
 *
 * We have to notice that some code in this class is copied and pasted from the
 * Hadoop classes: - see the method hasWindowsDrive - see the method
 * changeHadoopPath - see the method changeHadoopPathList
 *
 * In the ProActive MapReduce framework we choose to not support the Hadoop
 * {@link JobConf} class because it belongs to the old Hadoop API and it is deprecated.
 *
 * Concerning the logs of the ProActive MapReduce job we must notice that they are
 * enabled/disabled via the method "Task.setPreciousLogs(boolean preciousLogs)".
 * If "preciousLogs" is true, the logs produced by the task are stored in a
 * "TaskLogs-[jobid]-[taskname].log" file in localspace, and transferred to
 * outputspace at the end of the execution.
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public class PAMapReduceJob {

    protected static final Logger logger = DefaultLogger.getInstance();

    protected TaskFlowJob mapReduceWorkflow = null;
    protected Job hadoopJob = null;
    protected PAMapReduceJobConfiguration paMapReduceJobConfiguration = null;

    /**
     * Store the id of this job when it is submitted to the ProActive Scheduler
     */
    protected JobId jobId = null;

    public PAMapReduceJob(Job job, PAMapReduceJobConfiguration pamrjc) throws PAJobConfigurationException {
        this.hadoopJob = job;
        this.paMapReduceJobConfiguration = pamrjc;
        initLogger(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
        checkConfiguration(paMapReduceJobConfiguration, initRequiredConfigurationProperties());
        init();
    }

    /**
     * Initialize the logger to use during the configuration of the ProActive
     * MapReduce job
     *
     * @param debugLevelString
     *            the string representation of the boolean value that indicates
     *            if the debug log level must be enabled or not
     */
    protected void initLogger(String debugLevelString) {
        logger.setDebugLogLevel(Boolean.parseBoolean(debugLevelString));
    }

    /**
     * Translate the Hadoop MapReduce Job in the ProActive one
     *
     * @throws JobCreationException
     * @throws PAJobConfigurationException
     */
    protected void init() throws PAJobConfigurationException {
        if (!isInitialized()) {
            // execute only if the Hadoop Job and the
            // PAMapReduceJobConfiguration are not null
            if ((hadoopJob != null) && (paMapReduceJobConfiguration != null)) {

                /*
                 * Define the fork environment the tasks must tasks must use.
                 *
                 * Notice that by default the max size of the
                 * jvm heap depends on various factor such as the available
                 * memory on the host, the architecture of the host (32 bit, 64
                 * bit, ...) etc... In the case of the Eon cluster, the default
                 * heap size seams to be 1 GB and anyways it is sure it is more
                 * than 512MB
                 */

                ForkEnvironment forkEnvironment = new ForkEnvironment();

                /*
                 * A workaround for SCHEDULING-1307: some classes from the
                 * scheduler core are required by MapReduce on the node side, so
                 * use envScript to add ProActive_Scheduler-core.jar to the
                 * classpath of the forkEnvironment
                 */
                String envScript = "home = org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();\n"
                    + "forkEnvironment.addAdditionalClasspath(home + \"/dist/lib/*\");";
                logger.debug("Setting envScript");
                try {
                    forkEnvironment.setEnvScript(new SimpleScript(envScript, "javascript"));
                } catch (InvalidScriptException e) {
                    logger.warning("Failed to set envScript");
                    e.printStackTrace();
                }

                /*
                 * Specify the parameter for the forked environment
                 */
                String[] jvmArgumentArray = paMapReduceJobConfiguration.getJVMArguments();
                if (jvmArgumentArray != null) {
                    for (int i = 0; i < jvmArgumentArray.length; i++) {
                        forkEnvironment.addJVMArgument(jvmArgumentArray[i]);
                        logger.debug("Setting JVM argument '" + jvmArgumentArray[i] + "'");
                    }
                }

                String reducerInputIndexFileSelector = null;
                String reducerInputFileSelector = null;
                String outputFileName = null;

                Configuration hadoopJobConfiguration = hadoopJob.getConfiguration();

                /*
                 * Since in the ProActive MapReduce framework actual class of
                 * the configuration instance is a PAHadoopJobConfiguration the
                 * first stuff we do is to create a PAHadoopJobConfiguration
                 * instance from the Hadoop Configuration one. Then all the
                 * getters and setters must be invoked on that instance and not
                 * on the Hadoop one.
                 */
                PAHadoopJobConfiguration paHadoopJobConfiguration = getPAHadoopJobConfiguration(hadoopJobConfiguration);

                /*
                 * We must set some properties to be able to use the file system
                 * implementation based on the ProActive DataSpaces. This will
                 * overwrite the already existing properties in the Hadoop
                 * Configuration instance. In particular we must add: - a
                 * property whose name is "fs.<fsUriScheme>.impl" and whose
                 * value is the name of the class that implements the file
                 * system through the ProActive DataSpaces; - a property whose
                 * name is "fs.default.name" and whose value is the name of the
                 * file system implemented through the ProActive DataSpaces - a
                 * property whose name is "fs.<fsUriScheme>.impl.disable.cache"
                 * and whose value is a boolean that if "true" means the cache
                 * for the file system whose scheme is "fsUriScheme" is disabled
                 * (In the case of the file system implemented through the
                 * ProActive DataSpaces we leave the cache disabled)
                 */
                paHadoopJobConfiguration
                        .set(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_IMPLEMENTATION_PROPERTY_NAME.key),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DEFAULT_IMPLEMENTATION.key));
                logger
                        .debug("The Hadoop Abstract File System implementation is '" +
                            paHadoopJobConfiguration
                                    .get(PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_IMPLEMENTATION_PROPERTY_NAME.key)) +
                            "'");

                paHadoopJobConfiguration
                        .set(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DEFAULT_NAME_PROPERTY_NAME.key),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DEFAULT_NAME.key));
                logger
                        .debug("The Hadoop Abstract File System implementation default name is '" +
                            paHadoopJobConfiguration
                                    .get(PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DEFAULT_NAME_PROPERTY_NAME.key)) +
                            "'");

                paHadoopJobConfiguration
                        .set(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DISABLE_CACHE_PROPERTY_NAME.key),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DISABLE_CACHE.key));
                logger
                        .debug("The Hadoop Abstract File System implementation enabled cache is '" +
                            paHadoopJobConfiguration
                                    .get(PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DISABLE_CACHE_PROPERTY_NAME.key)) +
                            "'");

                String[] inputPathStringList = paHadoopJobConfiguration
                        .get(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME.key))
                        .split(StringUtils.COMMA_STR);
                logger
                        .debug("The input files of the Hadoop MapREduce job are '" +
                            paHadoopJobConfiguration
                                    .get(PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME.key)) +
                            "'");

                /*
                 * We start the build of the ProActive MapReduce job
                 */
                TaskFlowJob tmpMapReduceWorkflow = new TaskFlowJob();
                tmpMapReduceWorkflow.setCancelJobOnError(paMapReduceJobConfiguration.getJobCancelOnError());
                logger
                        .debug("The value of the cancelJobOnError attribute of the ProActive MapReduce job is '" +
                            paMapReduceJobConfiguration.getJobCancelOnError() + "'");

                tmpMapReduceWorkflow.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartTaskOnError());
                logger.debug("The value of the restartTaskOnError of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getRestartTaskOnError() + "'");

                tmpMapReduceWorkflow.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions());
                logger.debug("The value of the maxNumberOfExecutions of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getMaxNumberOfExecutions() + "'");

                // tmpMapReduceWorkflow.setLogFile(paMapReduceJobConfiguration
                // .getLogFilePath());
                // logger.debug("The value of the logFilePath of the ProActive MapReduce job is '"
                // + paMapReduceJobConfiguration.getLogFilePath() + "'");

                tmpMapReduceWorkflow.setName(hadoopJob.getJobName());
                logger.debug("The value of the name of the ProActive MapReduce job is '" +
                    hadoopJob.getJobName() + "'");

                String hadoopJobPriorityString = paHadoopJobConfiguration.get(PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_JOB_PRIORITY.key));
                tmpMapReduceWorkflow.setPriority(getPriority(hadoopJobPriorityString));
                logger.debug("The value of the priority of the ProActive MapReduce job is '" +
                    getPriority(hadoopJobPriorityString) + "'");

                tmpMapReduceWorkflow.setProjectName(paMapReduceJobConfiguration.getProjectName());
                logger.debug("The value of the projectName of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getProjectName() + "'");

                tmpMapReduceWorkflow.setDescription(paMapReduceJobConfiguration.getDescription());
                logger.debug("The value of the description of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getDescription() + "'");

                tmpMapReduceWorkflow.setInputSpace(paMapReduceJobConfiguration.getInputSpace());
                logger.debug("The value of the input space of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getInputSpace() + "'");

                tmpMapReduceWorkflow.setOutputSpace(paMapReduceJobConfiguration.getOutputSpace());
                logger.debug("The value of the output space of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getOutputSpace() + "'");

                tmpMapReduceWorkflow.setGlobalSpace(paMapReduceJobConfiguration.getGlobalSpace());
                logger.debug("The value of the global space of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getGlobalSpace() + "'");

                tmpMapReduceWorkflow.setUserSpace(paMapReduceJobConfiguration.getUserSpace());
                logger.debug("The value of the user space of the ProActive MapReduce job is '" +
                    paMapReduceJobConfiguration.getUserSpace() + "'");

                // Set the classpath of the job
                String[] classpath = paMapReduceJobConfiguration.getClasspath();
                if (classpath != null) {
                    JobEnvironment je = new JobEnvironment();
                    try {
                        je.setJobClasspath(classpath);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    tmpMapReduceWorkflow.setEnvironment(je);
                }

                // start of create the splitter task
                JavaTask splitterPATask = new JavaTask();
                splitterPATask.setName(paMapReduceJobConfiguration
                        .getTaskName(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger.debug("The value of the name of the ProActive MapReduce SplitterPATask is '" +
                    paMapReduceJobConfiguration.getTaskName(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask.setCancelJobOnError(paMapReduceJobConfiguration
                        .getCancelJobOnError(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger
                        .debug("The value of the cancelJobOnError of the ProActive MapReduce SplitterPATask is '" +
                            paMapReduceJobConfiguration
                                    .getCancelJobOnError(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartMode(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger.debug("The value of the restartMode of the ProActive MapReduce SplitterPATask is '" +
                    paMapReduceJobConfiguration.getRestartMode(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger
                        .debug("The value of the maxNumberOfExecutions of the ProActive MapReduce SplitterPATask is '" +
                            paMapReduceJobConfiguration.getMaxNumberOfExecutions() + "'");

                splitterPATask.setDescription(paMapReduceJobConfiguration
                        .getDescription(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger.debug("The value of the description of the ProActive MapReduce SplitterPATask is '" +
                    paMapReduceJobConfiguration.getDescription(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask
                        .setPreciousLogs(PAMapReduceFrameworkProperties
                                .getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
                logger
                        .debug("The value of the precious logs of the ProActive MapReduce SplitterPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) +
                            "'");

                /*
                 * we must add the input files to the SplitterPATask only when
                 * the ReadMode of the SplitterPATask is equal to fullLocalRead
                 */
                if (paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.SPLITTER_PA_TASK).equals(
                        ReadMode.fullLocalRead)) {
                    for (int i = 0; i < inputPathStringList.length; i++) {
                        splitterPATask.addInputFiles(inputPathStringList[i], paMapReduceJobConfiguration
                                .getInputAccessMode(PAMapReduceFramework.SPLITTER_PA_TASK));
                    }
                }
                logger.debug("The value of the readMode of the ProActive MapReduce SplitterPATask is '" +
                    paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");
                logger
                        .debug("The value of the inputAccessMode of the ProActive MapReduce SplitterPATask is '" +
                            paMapReduceJobConfiguration
                                    .getInputAccessMode(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask
                        .setExecutableClassName(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_EXECUTABLE_CLASS.key));
                logger
                        .debug("The value of the executablClassName of the ProActive MapReduce SplitterPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_EXECUTABLE_CLASS.key) +
                            "'");

                splitterPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE.key,
                        paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.SPLITTER_PA_TASK));
                logger.debug("The value of the input space of the ProActive MapReduce SplitterPATask is '" +
                    paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.SPLITTER_PA_TASK) + "'");

                splitterPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));

                splitterPATask.addArgument(PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.key,
                        paHadoopJobConfiguration);

                InputStream replicateMapperPATaskInputStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(PAMapReduceFramework.REPLICATE_MAPPER_PA_TASK_SCRIPT_NAME);
                Script replicateMapperScript = null;
                try {
                    replicateMapperScript = new SimpleScript(
                        readScriptFile(replicateMapperPATaskInputStream),
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SCRIPT_ENGINE.key),
                        new String[] {
                                SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) });
                } catch (InvalidScriptException ise) {
                    ise.printStackTrace();
                }
                FlowScript replicateMapperFlowScript = null;
                try {
                    replicateMapperFlowScript = FlowScript.createReplicateFlowScript(replicateMapperScript);
                } catch (InvalidScriptException ise) {
                    ise.printStackTrace();
                }
                splitterPATask.setFlowScript(replicateMapperFlowScript);
                splitterPATask.setForkEnvironment(forkEnvironment);

                try {
                    tmpMapReduceWorkflow.addTask(splitterPATask);
                } catch (UserException ue) {
                    ue.printStackTrace();
                }
                // end of create splitter task

                // start of create mapper task
                JavaTask mapperPATask = new JavaTask();
                mapperPATask.setName(paMapReduceJobConfiguration
                        .getTaskName(PAMapReduceFramework.MAPPER_PA_TASK));
                logger.debug("The value of the name of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getTaskName(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask.setCancelJobOnError(paMapReduceJobConfiguration
                        .getCancelJobOnError(PAMapReduceFramework.MAPPER_PA_TASK));
                logger
                        .debug("The value of the cancelJobOnError of the ProActive MapReduce MapperPATask is '" +
                            paMapReduceJobConfiguration
                                    .getCancelJobOnError(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartMode(PAMapReduceFramework.MAPPER_PA_TASK));
                logger.debug("The value of the restartMode of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getRestartMode(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions(PAMapReduceFramework.MAPPER_PA_TASK));
                logger
                        .debug("The value of the maxNumberOfExecutions of the ProActive MapReduce MapperPATask is '" +
                            paMapReduceJobConfiguration.getMaxNumberOfExecutions() + "'");

                mapperPATask.setDescription(paMapReduceJobConfiguration
                        .getDescription(PAMapReduceFramework.MAPPER_PA_TASK));
                logger.debug("The value of the description of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getDescription(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask
                        .setPreciousLogs(PAMapReduceFrameworkProperties
                                .getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
                logger
                        .debug("The value of the precious logs of the ProActive MapReduce MapperPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) +
                            "'");

                mapperPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key,
                        paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.MAPPER_PA_TASK).key);
                logger.debug("The value of the readMode of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.key,
                        paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.MAPPER_PA_TASK));
                logger.debug("The value of the input space of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                /*
                 * we must add the input files to the MapperPATask only when the
                 * ReadMode of the MapperPATask is equal to fullLocalRead
                 */
                if (paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.MAPPER_PA_TASK).equals(
                        ReadMode.fullLocalRead)) {
                    for (int i = 0; i < inputPathStringList.length; i++) {
                        mapperPATask.addInputFiles(inputPathStringList[i], paMapReduceJobConfiguration
                                .getInputAccessMode(PAMapReduceFramework.MAPPER_PA_TASK));
                    }
                }

                mapperPATask
                        .setExecutableClassName(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_EXECUTABLE_CLASS.key));
                logger
                        .debug("The value of the executableClassName of the ProActive MapReduce MapperPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_EXECUTABLE_CLASS.key) +
                            "'");

                mapperPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE.key,
                        paMapReduceJobConfiguration.getOutputSpace(PAMapReduceFramework.MAPPER_PA_TASK));
                logger.debug("The value of the output space of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getOutputSpace(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                /*
                 * the string to use to select the output file of a mapper will
                 * be the following: intermediate_$REP.out TODO check that when
                 * the writeMode is
                 * "PAMapReduceFramework.WRITE_MODE_REMOTE_WRITE" (this means
                 * that the output access mode is "none") then we do not need to
                 * specify the output files of the MapperPATask TODO do the same
                 * for the ReducerPATask TODO when the readMode is equal to
                 * PAMapReduceFramework.READ_MODE_REMOTE_READ or
                 * PAMapReduceFramework.READ_MODE_PARTIAL_LOCAL_READ (so that
                 * the input access mode is "none") we do not need to add input
                 * files to the task (SplitterPATask, MapperPATask and
                 * ReducerPATask)
                 */
                if (!(paMapReduceJobConfiguration.getWriteMode(PAMapReduceFramework.MAPPER_PA_TASK)
                        .equals(WriteMode.remoteWrite))) {
                    outputFileName = PAMapReduceFramework
                            .getMapperIntermediateFileSelector(PAMapReduceFramework.REPLICATION_INDEX_TAG);
                    logger
                            .debug("The value of the intermediateFileSelector of the ProActive MapReduce MapperPATask is '" +
                                outputFileName + "'");

                    mapperPATask.addOutputFiles(outputFileName, paMapReduceJobConfiguration
                            .getOutputAccessMode(PAMapReduceFramework.MAPPER_PA_TASK));
                    logger
                            .debug("The value of the outputAccessMode of the ProActive MapReduce MapperPATask is '" +
                                paMapReduceJobConfiguration
                                        .getOutputAccessMode(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                    /*
                     * the string to use to select the output index file of a
                     * mapper will be the following: intermediate_$REP.index
                     */
                    outputFileName = PAMapReduceFramework
                            .getMapperIntermediateIndexFileSelector(PAMapReduceFramework.REPLICATION_INDEX_TAG);
                    logger
                            .debug("The value of the intermediateIndexFileSelector of the ProActive MapReduce MapperPATask is '" +
                                outputFileName + "'");

                    mapperPATask.addOutputFiles(outputFileName, paMapReduceJobConfiguration
                            .getOutputAccessMode(PAMapReduceFramework.MAPPER_PA_TASK));
                }

                logger.debug("The value of the writeMode of the ProActive MapReduce MapperPATask is '" +
                    paMapReduceJobConfiguration.getWriteMode(PAMapReduceFramework.MAPPER_PA_TASK) + "'");

                mapperPATask.addArgument(PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.key,
                        paHadoopJobConfiguration);

                mapperPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));

                mapperPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.key));

                mapperPATask.addDependence(splitterPATask);

                mapperPATask.setForkEnvironment(forkEnvironment);

                try {
                    tmpMapReduceWorkflow.addTask(mapperPATask);
                } catch (UserException ue) {
                    ue.printStackTrace();
                }
                // end of create mapper task

                // start of create the mapper join task
                JavaTask mapperJoinPATask = new JavaTask();
                mapperJoinPATask.setName(paMapReduceJobConfiguration
                        .getTaskName(PAMapReduceFramework.MAPPER_JOIN_PA_TASK));
                logger.debug("The value of the name of the ProActive MapReduce MapperJoinPATask is '" +
                    paMapReduceJobConfiguration.getTaskName(PAMapReduceFramework.MAPPER_JOIN_PA_TASK) + "'");

                mapperJoinPATask.setDescription(paMapReduceJobConfiguration
                        .getDescription(PAMapReduceFramework.MAPPER_JOIN_PA_TASK));
                logger.debug("The value of the description of the ProActive MapReduce MapperJoinPATask is '" +
                    paMapReduceJobConfiguration.getDescription(PAMapReduceFramework.MAPPER_JOIN_PA_TASK) +
                    "'");

                mapperJoinPATask
                        .setPreciousLogs(PAMapReduceFrameworkProperties
                                .getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
                logger
                        .debug("The value of the precious logs of the ProActive MapReduce MapperJoinPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) +
                            "'");

                mapperJoinPATask.setCancelJobOnError(paMapReduceJobConfiguration
                        .getCancelJobOnError(PAMapReduceFramework.MAPPER_JOIN_PA_TASK));
                logger
                        .debug("The value of the cancelJobOnError of the ProActive MapReduce MapperJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getCancelJobOnError(PAMapReduceFramework.MAPPER_JOIN_PA_TASK) + "'");

                mapperJoinPATask.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartMode(PAMapReduceFramework.MAPPER_JOIN_PA_TASK));
                logger.debug("The value of the restartMode of the ProActive MapReduce MapperJoinPATask is '" +
                    paMapReduceJobConfiguration.getRestartMode(PAMapReduceFramework.MAPPER_JOIN_PA_TASK) +
                    "'");

                mapperJoinPATask.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions(PAMapReduceFramework.MAPPER_JOIN_PA_TASK));
                logger
                        .debug("The value of the maxNumberOfExecutions of the ProActive MapReduce MapperJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getMaxNumberOfExecutions(PAMapReduceFramework.MAPPER_JOIN_PA_TASK) + "'");

                mapperJoinPATask
                        .setExecutableClassName(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_EXECUTABLE_CLASS.key));
                logger
                        .debug("The value of the executableClassName of the ProActive MapReduce MapperJoinPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_EXECUTABLE_CLASS.key) +
                            "'");

                mapperJoinPATask.addDependence(mapperPATask);

                mapperJoinPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));

                mapperJoinPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_DIRECTORY_PROPERTY_NAME.key),
                                paHadoopJobConfiguration
                                        .get(PAMapReduceFrameworkProperties
                                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_DIRECTORY_PROPERTY_NAME.key)));

                InputStream replicateReducerPATaskInputStream = Thread.currentThread()
                        .getContextClassLoader().getResourceAsStream(
                                PAMapReduceFramework.REPLICATE_REDUCER_PA_TASK_SCRIPT_NAME);
                Script<?> replicateReducerScript = null;
                try {
                    replicateReducerScript = new SimpleScript(
                        this.readScriptFile(replicateReducerPATaskInputStream),
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SCRIPT_ENGINE.key),
                        new String[] {
                                SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString(),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key),
                                "" +
                                    paHadoopJobConfiguration
                                            .getInt(
                                                    PAMapReduceFrameworkProperties
                                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_NUMBER_OF_REDUCER_TASKS_PROPERTY_NAME.key),
                                                    1) });

                } catch (InvalidScriptException ise) {
                    ise.printStackTrace();
                }
                FlowScript replicateReducerFlowScript = null;
                try {
                    replicateReducerFlowScript = FlowScript.createReplicateFlowScript(replicateReducerScript);
                } catch (InvalidScriptException ise) {
                    ise.printStackTrace();
                }
                mapperJoinPATask.setFlowScript(replicateReducerFlowScript);
                mapperJoinPATask.setForkEnvironment(forkEnvironment);

                try {
                    tmpMapReduceWorkflow.addTask(mapperJoinPATask);
                } catch (UserException ue) {
                    ue.printStackTrace();
                }
                // end of create the mapper join task

                // start of: create the reducer task
                JavaTask reducerPATask = new JavaTask();
                reducerPATask.setName(paMapReduceJobConfiguration
                        .getTaskName(PAMapReduceFramework.REDUCER_PA_TASK));
                logger.debug("The value of the name of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getTaskName(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.setCancelJobOnError(paMapReduceJobConfiguration
                        .getCancelJobOnError(PAMapReduceFramework.REDUCER_PA_TASK));
                logger
                        .debug("The value of the cancelJobOnError of the ProActive MapReduce ReducerPATask is '" +
                            paMapReduceJobConfiguration
                                    .getCancelJobOnError(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartMode(PAMapReduceFramework.REDUCER_PA_TASK));
                logger.debug("The value of the restartMode of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getRestartMode(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions(PAMapReduceFramework.REDUCER_PA_TASK));
                logger
                        .debug("The value of the maxNumberOfExecutions of the ProActive MapReduce ReducerPATask is '" +
                            paMapReduceJobConfiguration
                                    .getMaxNumberOfExecutions(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.setDescription(paMapReduceJobConfiguration
                        .getDescription(PAMapReduceFramework.REDUCER_PA_TASK));
                logger.debug("The value of the description of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getDescription(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask
                        .setPreciousLogs(PAMapReduceFrameworkProperties
                                .getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
                logger
                        .debug("The value of the precious logs of the ProActive MapReduce ReducerPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) +
                            "'");

                reducerPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key,
                        paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.REDUCER_PA_TASK).key);
                logger.debug("The value of the readMode of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE.key,
                        paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.REDUCER_PA_TASK));
                logger.debug("The value of the input space of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getInputSpace(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                /*
                 * We must test if the "readMode" of the ReducerPATask is
                 * 'fullLocalRead' then we must transfer on the node the
                 * ReducerPATask will execute on the MapperPATask output "index"
                 * files and the MapperPATask output "actual data" files. If the
                 * "readMode" is "remoteRead" we must transfer on the node the
                 * ReducerPATask will execute on only the index files. This
                 * means the index files are ALWAYS transferred on the node the
                 * ReducerPATask will execute on (so we use
                 * "InputAccessMode.TransferFromOutputSpace" directly without
                 * retrieving the input access mode information from the
                 * configuration).
                 */
                reducerInputIndexFileSelector = PAMapReduceFramework
                        .getReducerIntermediateIndexFileSelector();
                logger
                        .debug("The value of the intermediateIndexFileSelector of the ProActive MapReduce ReducerPATask is '" +
                            reducerInputIndexFileSelector + "'");

                reducerPATask.addInputFiles(reducerInputIndexFileSelector,
                        InputAccessMode.TransferFromOutputSpace);

                if (paMapReduceJobConfiguration.getReadMode(PAMapReduceFramework.REDUCER_PA_TASK).equals(
                        ReadMode.fullLocalRead)) {
                    /*
                     * the string to use to select the output files of a mapper
                     * is the following: intermediate_*.out
                     */
                    reducerInputFileSelector = PAMapReduceFramework.getReducerIntermediateFileSelector();
                    logger
                            .debug("The value of the intermediateFileSelector of the ProActive MapReduce ReducerPATask is '" +
                                reducerInputFileSelector + "'");

                    reducerPATask.addInputFiles(reducerInputFileSelector, paMapReduceJobConfiguration
                            .getInputAccessMode(PAMapReduceFramework.REDUCER_PA_TASK));
                }

                reducerPATask
                        .setExecutableClassName(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_EXECUTABLE_CLASS.key));

                reducerPATask.addArgument(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE.key,
                        paMapReduceJobConfiguration.getOutputSpace(PAMapReduceFramework.REDUCER_PA_TASK));
                logger.debug("The value of the output space of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getOutputSpace(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                /*
                 * re-using the Hadoop OutputFormat we must notice the output
                 * will be written into a directory like the following one:
                 * "$OUTPUT_DIRECTORY/_temporary/_attempt_<jtIdentifier>_<jobId>_r_<taskId>_<taskAttemptId>/part-r-<taskId>"
                 * for which a possible selection string will be:
                 * "$OUTPUT_DIRECTORY/_temporary/_attempt_* /part-r-*"
                 */
                String outputFilesSelectionString = paHadoopJobConfiguration
                        .get(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_OUTPUT_DIRECTORY_PROPERTY_NAME.key)) +
                    "/" + PAMapReduceFramework.getTemporaryOutputDirectoryRegex();
                logger
                        .debug("The value of the outputFileSelectionString of the ProActive MapReduce ReducerPATask is '" +
                            outputFilesSelectionString + "'");

                /*
                 * we must add the output files to the ReducerPATask only when
                 * the WriteMode of the ReducerPATask is equal to localWrite
                 */
                if (paMapReduceJobConfiguration.getWriteMode(PAMapReduceFramework.REDUCER_PA_TASK).equals(
                        WriteMode.localWrite)) {
                    reducerPATask.addOutputFiles(outputFilesSelectionString, paMapReduceJobConfiguration
                            .getOutputAccessMode(PAMapReduceFramework.REDUCER_PA_TASK));
                }

                logger
                        .debug("The value of the outputAccessMode of the ProActive MapReduce ReducerPATask is '" +
                            paMapReduceJobConfiguration
                                    .getOutputAccessMode(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                logger.debug("The value of the writeMode of the ProActive MapReduce ReducerPATask is '" +
                    paMapReduceJobConfiguration.getWriteMode(PAMapReduceFramework.REDUCER_PA_TASK) + "'");

                reducerPATask.addArgument(PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.key,
                        paHadoopJobConfiguration);
                reducerPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));

                reducerPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.key));

                reducerPATask.addDependence(mapperJoinPATask);
                reducerPATask.setForkEnvironment(forkEnvironment);

                try {
                    tmpMapReduceWorkflow.addTask(reducerPATask);
                } catch (UserException ue) {
                    ue.printStackTrace();
                }
                // end of create the reduce task

                // start of create the reducer join task
                JavaTask reducerJoinPATask = new JavaTask();
                reducerJoinPATask.setName(paMapReduceJobConfiguration
                        .getTaskName(PAMapReduceFramework.REDUCER_JOIN_PA_TASK));
                logger.debug("The value of the name of the ProActive MapReduce ReducerJoinPATask is '" +
                    paMapReduceJobConfiguration.getTaskName(PAMapReduceFramework.REDUCER_JOIN_PA_TASK) + "'");

                reducerJoinPATask.setDescription(paMapReduceJobConfiguration
                        .getDescription(PAMapReduceFramework.REDUCER_JOIN_PA_TASK));
                logger
                        .debug("The value of the description of the ProActive MapReduce ReducerJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getDescription(PAMapReduceFramework.REDUCER_JOIN_PA_TASK) + "'");

                reducerJoinPATask
                        .setPreciousLogs(PAMapReduceFrameworkProperties
                                .getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));
                logger
                        .debug("The value of the precious logs of the ProActive MapReduce ReducerJoinPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key) +
                            "'");

                reducerJoinPATask.setCancelJobOnError(paMapReduceJobConfiguration
                        .getCancelJobOnError(PAMapReduceFramework.REDUCER_JOIN_PA_TASK));
                logger
                        .debug("The value of the cancelJobOnError of the ProActive MapReduce ReducerJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getCancelJobOnError(PAMapReduceFramework.REDUCER_JOIN_PA_TASK) + "'");

                reducerJoinPATask.setRestartTaskOnError(paMapReduceJobConfiguration
                        .getRestartMode(PAMapReduceFramework.REDUCER_JOIN_PA_TASK));
                logger
                        .debug("The value of the restartTaskOnError of the ProActive MapReduce ReducerJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getRestartMode(PAMapReduceFramework.REDUCER_JOIN_PA_TASK) + "'");

                reducerJoinPATask.setMaxNumberOfExecution(paMapReduceJobConfiguration
                        .getMaxNumberOfExecutions(PAMapReduceFramework.REDUCER_JOIN_PA_TASK));
                logger
                        .debug("The value of the maxNumberOfExecutions of the ProActive MapReduce ReducerJoinPATask is '" +
                            paMapReduceJobConfiguration
                                    .getMaxNumberOfExecutions(PAMapReduceFramework.REDUCER_JOIN_PA_TASK) +
                            "'");

                reducerJoinPATask
                        .setExecutableClassName(PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_EXECUTABLE_CLASS.key));
                logger
                        .debug("The value of the executableClassName of the ProActive MapReduce ReducerJoinPATask is '" +
                            PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_EXECUTABLE_CLASS.key) +
                            "'");

                reducerJoinPATask
                        .addArgument(
                                PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key,
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key));

                InputStream fileTransferPostScriptInputStream = Thread.currentThread()
                        .getContextClassLoader().getResourceAsStream(
                                PAMapReduceFramework.OUTPUT_FILE_TRANSFER_POST_SCRIPT_NAME);
                Script<?> fileTransferScript = null;
                try {
                    /*
                     * we must notice that when we reuse the Hadoop OutputFormat
                     * classes the name of the output file in which the
                     * ReducerPATask will put its output data is implicitly
                     * defined (the output folder directory will be something
                     * like "_temporary/_attempt..."). This means if we want to
                     * put the ReducerPATask output files in the directory the
                     * user specified and we want to give the output files the
                     * name the user desires we must execute a post script to
                     * move files (renaming them at the same time). The name of
                     * the ReducerPATak output file will be compliant to the
                     * following format: "<userDefinedPrefix><reducerId>". By
                     * default, if the user does not specify any prefix for the
                     * ReducerPATask output file names then the String retrieved
                     * by the method
                     * PAMapReduceFramework.getReducerOutputFileNamePrefix()
                     * will be used (that string until now is "reducer_").
                     * Look at the ReducerPATask class to get more information
                     * about the name of output file of the reducer task.
                     */
                    String reducerOutputFileNamePrefix = null;
                    reducerOutputFileNamePrefix = paMapReduceJobConfiguration
                            .getReducerOutputFileNamePrefix();
                    if (reducerOutputFileNamePrefix == null) {
                        reducerOutputFileNamePrefix = PAMapReduceFramework.getReducerOutputFileNamePrefix();
                    }

                    fileTransferScript = new SimpleScript(
                        this.readScriptFile(fileTransferPostScriptInputStream),
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SCRIPT_ENGINE.key),
                        new String[] {
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.key),
                                outputFilesSelectionString, reducerOutputFileNamePrefix });

                } catch (InvalidScriptException ise) {
                    ise.printStackTrace();
                }

                reducerJoinPATask.setPostScript(fileTransferScript);

                reducerJoinPATask.addDependence(reducerPATask);
                reducerJoinPATask.setForkEnvironment(forkEnvironment);

                try {
                    tmpMapReduceWorkflow.addTask(reducerJoinPATask);
                } catch (UserException ue) {
                    ue.printStackTrace();
                }
                // end of create the reducer join task

                mapReduceWorkflow = tmpMapReduceWorkflow;

                /*
                 * TODO verify if there is a getUser() and getCredentials()
                 * method we could use to as the username and password (in
                 * general as the credentials) to establsh a connection to the
                 * scheduler
                 */

                /*
                 * TODO verify if we can use an hadoop method as
                 * hadoopJobConfiguration.getMaxMapAttempts(), that give us the
                 * maximum number of attempts that will be made to run a map
                 * task
                 */

                /*
                 * TODO verify if we can use an hadoop method as
                 * hadoopJobConfiguration.getMaxMapAttempts(), that give us the
                 * maximum number of attempts that will be made to run a reduce
                 * task
                 */

            }
        }
    }

    /**
     * Run the Hadoop MapReduce Job
     *
     * @return true if the Hadoop MapReduce Job is submitted correctly to the
     *         ProActive Scheduler
     */
    public boolean run() {
        if (isInitialized()) {
            return (submitJob(mapReduceWorkflow));
        }
        return false;
    }

    /**
     * Return the {@link TaskFlowJob} built by this PAMapReduceJob. The result
     * may be used to submit the MapReduce job to the ProActive Scheduler if the
     * desired mode of submission is not covered by the {@link #run} method.
     *
     * @return the TaskFlowJob built by this PAMapReduceJob
     */
    public TaskFlowJob getMapReduceWorkflow() {
        return mapReduceWorkflow;
    }

    /**
     * Check if the ProActive MapReduce Workflow is already initialized
     *
     * @return true if the ProActive MapReduce Workflow is already initialized,
     *         false otherwise
     */
    protected boolean isInitialized() {
        if (mapReduceWorkflow != null) {
            return true;
        }
        return false;
    }

    /**
     * Submit the TaskFlowJob representation of the Hadoop Job to the ProActive
     * Scheduler
     *
     * @param mapReduceWorkflow
     *            : the ProActive TaksFlowJob representation of the Hadoop Job
     * @return boolean true if the job is submitted successfully, false
     *         otherwise
     */
    protected boolean submitJob(TaskFlowJob mapReduceWorkflow) {
        SchedulerAuthenticationInterface sai = null;
        try {
            sai = SchedulerConnection.join(paMapReduceJobConfiguration
                    .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_URL.key));
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Scheduler scheduler = null;
        try {
            scheduler = sai.login(Credentials.getCredentials());
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AlreadyConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyException e) {
            try {
                // (2) alternative authentication method
                PublicKey pubKey = null;
                try {
                    pubKey = sai.getPublicKey();
                } catch (LoginException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (pubKey == null) {
                    pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                }
                try {
                    String username = paMapReduceJobConfiguration
                            .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_USERNAME.key);
                    String password = paMapReduceJobConfiguration
                            .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_PASSWORD.key);
                    scheduler = sai.login(Credentials.createCredentials(new CredData(username, password),
                            pubKey));
                } catch (LoginException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (AlreadyConnectedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (KeyException ke2) {
                // cannot find public key !
            }
        }

        if (scheduler != null) {
            try {
                jobId = scheduler.submit(mapReduceWorkflow);
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PermissionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SubmissionClosedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JobCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            scheduler.disconnect();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (jobId != null) {
            return true;
        }

        return false;
    }

    /**
     * Read the content of the file containing the script
     *
     * @param inputStream
     *            the InputStream to use to read from the file containing the
     *            script
     * @return String the content of the script file
     */
    protected String readScriptFile(InputStream inputStream) {
        if (inputStream != null) {
            final char[] buffer = new char[0x10000];
            StringBuilder out = new StringBuilder();
            Reader in;
            try {
                in = new InputStreamReader(inputStream, "UTF-8");
                int read;
                do {
                    read = in.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        out.append(buffer, 0, read);
                    }
                } while (read >= 0);
            } catch (UnsupportedEncodingException e) {
                // thrown by in.read(buffer, 0, buffer.length);
                e.printStackTrace();
            } catch (IOException e) {
                // thrown by in.read(buffer, 0, buffer.length);
                e.printStackTrace();
            }
            return out.toString();
        }
        return null;
    }

    /**
     * Build a {@link Serializable} {@link Configuration} object so that we can
     * pass it to the ProActive tasks as an argument
     *
     * @param configuration
     *            the Hadoop {@link Configuration} object from which we have to
     *            create the Serializable configuration object
     * @return {@link PAHadoopJobConfiguration} the {@link Serializable}
     *         configuration object
     * @throws PAJobConfigurationException
     */
    protected PAHadoopJobConfiguration getPAHadoopJobConfiguration(Configuration configuration)
            throws PAJobConfigurationException {
        if (configuration != null) {
            changeHadoopInputPathList(configuration);

            PAHadoopJobConfiguration pahjc = new PAHadoopJobConfiguration(configuration);

            /*
             * To force Hadoop to create InputSplit instances whose dimension
             * will be the one the user specified we must set the properties
             * "mapred.min.split.size" and "mapred.max.split.size" to the value
             * of the split size the user defined (in the configuration file or
             * invoking the method
             * PAMapReduceJobConfiguration.setInputSplitSize()). We must notice
             * that in the following code we set the Hadoop properties that
             * represent the minimum and maximum split size equal to the size of
             * the input split the user defined ONLY if the user has not already
             * defined the "mapred.min.split.size" and "mapred.max.split.size"
             * Hadoop property for the Hadoop job. To do that we check if the
             * value of the minimum and maximum of the Hadoop properties
             * "mapred.min.split.size" and "mapred.max.split.size" are equal to
             * the Hadoop defined default values and if that is the case we do
             * not change the values of the properties "mapred.min.split.size"
             * and "mapred.max.split.size". We must also notice that, if the
             * size the user defined for the input split is greater than the
             * size of input file, we must want that the Hadoop FileInputFormat
             * set the size of the input split equal to the size of the input
             * file. To grant that we must not alter the default value of the
             * "mapred.min.split.size" property. The problem is that when we
             * build the ProActive MapReduce job we do not know the
             * DataSpacesFileObject that refers to the input file. This means we
             * cannot check if the size the user defined for the input split is
             * greater than the size of the input file. Hence, to get an input
             * split whose size is equal to the size of the input file we must
             * not alter the default values of "mapred.min.split.size" and
             * "mapred.max.split.size" properties. This means if the user does
             * not specify the size of the input split the
             * "mapred.min.split.size" and "mapred.max.split.size" properties
             * maintain their default value and the Hadoop FileInputFormat
             * creates input splits whose size is equal to the size of the input
             * file. This means the input split size is not a REQUIRED property
             * and that we must check if the user has defined or not the input
             * split size to see when we must alter the default value of the
             * "mapred.min.split.size" and "mapred.max.split.size" properties.
             * In conclusion, we must notice that to obtain one input split
             * whose size is equal to the size of the input file the user must
             * define a size for the input split that is greater than the size
             * of the input file. In that case the FileInputFormat ends to build
             * the input split when the EOF is encountered in the input file. At
             * that point the size of the input split is equal to the size of
             * the input file. But the simple way to obtain an input split whose
             * size is equal to the size of the input file is to tell the user
             * not to define the property that represents the value of the input
             * split size since in some cases the user cannot know the size of
             * the input file in advance (i.e., he cannot define a size for the
             * input split greater than the size of the input file).
             */

            if (paMapReduceJobConfiguration.getInputSplitSize() != Long
                    .parseLong(PAMapReduceFramework
                            .getDefault(PAMapReduceFrameworkProperties
                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                            .getKey())))) {
                /*
                 * if we are here it means the user defined the size of the
                 * input split since the
                 * PAMapReduceJobcConfiguration.getInputSplitSize() method did
                 * not return the default value (that is equal to the Hadoop
                 * maximum value for the size of the input split)
                 */

                logger
                        .debug("The value to use to set the minimum size for the input split is '" +
                            pahjc
                                    .getLong(
                                            PAMapReduceFrameworkProperties
                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                            .getKey()),
                                            Long
                                                    .parseLong(PAMapReduceFramework
                                                            .getDefault(PAMapReduceFrameworkProperties
                                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                                            .getKey())))) + "'");
                logger
                        .debug("The default value of the minimum size of the input split is '" +
                            Long
                                    .parseLong(PAMapReduceFramework
                                            .getDefault(PAMapReduceFrameworkProperties
                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                            .getKey()))) + "'");

                if (pahjc
                        .getLong(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                .getKey()),
                                Long
                                        .parseLong(PAMapReduceFramework
                                                .getDefault(PAMapReduceFrameworkProperties
                                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                                .getKey())))) == Long
                        .parseLong(PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                .getKey())))) {
                    pahjc
                            .set(
                                    PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                    .getKey()), "" +
                                        paMapReduceJobConfiguration.getInputSplitSize());

                    logger
                            .debug("The minimum size of the input split in the ProActive MapReduce job is '" +
                                pahjc
                                        .get(PAMapReduceFrameworkProperties
                                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME
                                                        .getKey())) + "'");
                }

                logger
                        .debug("The value to use to set the maximum size for the input split is '" +
                            pahjc
                                    .getLong(
                                            PAMapReduceFrameworkProperties
                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                            .getKey()),
                                            Long
                                                    .parseLong(PAMapReduceFramework
                                                            .getDefault(PAMapReduceFrameworkProperties
                                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                                            .getKey())))) + "'");
                logger
                        .debug("The default value of the maximum size of the input split is '" +
                            Long
                                    .parseLong(PAMapReduceFramework
                                            .getDefault(PAMapReduceFrameworkProperties
                                                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                            .getKey()))) + "'");

                if (pahjc
                        .getLong(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                .getKey()),
                                Long
                                        .parseLong(PAMapReduceFramework
                                                .getDefault(PAMapReduceFrameworkProperties
                                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                                .getKey())))) == Long
                        .parseLong(PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                .getKey())))) {
                    pahjc
                            .set(
                                    PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                    .getKey()), "" +
                                        paMapReduceJobConfiguration.getInputSplitSize());

                    logger
                            .debug("The maximum size of the input split in the ProActive MapReduce job is '" +
                                pahjc
                                        .get(PAMapReduceFrameworkProperties
                                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME
                                                        .getKey())) + "'");
                }
            } else {
                logger.debug("The user did not define the size of the input split");
            }

            return pahjc;
        }
        return null;
    }

    /**
     * Modify each {@link Path} in the list of input path of the Hadoop job
     * using the method {@link PAMapReduceJob#changeHadoopPath(String)}.
     * 
     * This method contains some code copied from
     * 
     * {@link FileInputFormat#getInputPaths(org.apache.hadoop.mapreduce.JobContext)}
     * and {@link FileInputFormat#addInputPaths(Job, String)}.
     * 
     * @param configuration
     *            the configuration to use to retrieve the list of the job input
     *            {@link Path}s
     */
    protected void changeHadoopInputPathList(Configuration configuration) {
        String inputDirPropertyName = PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME.key);
        String inputPathStringList = configuration.get(inputDirPropertyName);
        if ((inputPathStringList != null) && (!inputPathStringList.trim().equalsIgnoreCase(""))) {
            String newInputPathStringList = "";
            String[] list = StringUtils.split(inputPathStringList);
            for (int i = 0; i < list.length; i++) {
                if (i == 0) {
                    newInputPathStringList += changeHadoopPath(StringUtils.escapeString(list[i]),
                            configuration);
                } else {
                    newInputPathStringList += StringUtils.COMMA_STR +
                        changeHadoopPath(StringUtils.escapeString(list[i]), configuration);
                }
            }
            configuration.set(inputDirPropertyName, newInputPathStringList);
        }
    }

    /**
     * Undo the effect of {@link FileInputFormat#addInputPath()}.
     * 
     * {@link FileInputFormat#addInputPath()} makes relative {@link Path}s
     * absolute by resolving them relative to the current working directory (see
     * {@link Path.makeQalified()}. We need the paths to remain relative, so we
     * reverse the effect of {@link FileInputFormat#addInputPath()} by stripping
     * the working directory from the path.
     * 
     * Note: we need the paths to remain relative because:
     * 
     * - it makes no sense to resolve them using workdir (they are relative to
     * the input dataspace)
     * 
     * - this way the correct filesystem implementation is chosen by Hadoop
     * (relative paths contain no schema, thus the default is used, the
     * default is determined by
     * {@link PAMapReduceFrameworkProperties#WORKFLOW_FILE_SYSTEM_DEFAULT_NAME},
     * which points to "pads://", which corresponds to
     * {@link PADataSpacesFileSystem}, which is what we need).
     * 
     * @param pathString
     *            the String representation of the Hadoop {@link Path}
     * 
     * @param configuration
     *            Hadoop configuration object
     */
    protected String changeHadoopPath(String pathString, Configuration configuration) {
        String workDir;

        try {
            workDir = FileSystem.get(configuration).getWorkingDirectory().toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot determine working directory!", e);
        }

        if (!pathString.startsWith(workDir)) {
            throw new RuntimeException("Path does not start with working directory: " + pathString);
        }
        // we rely on the fact that workDir has no trailing slash
        // thus + 1
        return pathString.substring(workDir.length() + 1);
    }

    /**
     * Check if the String representation Hadoop {@link Path} refers to a window
     * system or not TODO delete the hard coding
     *
     * @param pathString
     *            the String representation of the Hadoop path
     * @param slashed
     *            boolean that indicates if the first character of the string
     *            representation of the path is a "/" or not
     * @return true if the string representation of the Hadoop {@link Path} has
     *         a windows drive letter false otherwise
     *
     *         We have to notice that this code is copied and pasted from the
     *         code of the same method of the Hadoop {@link Path} class
     */
    protected boolean hasWindowsDrive(String pathString, boolean slashed) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        if (!windows)
            return false;
        int start = slashed ? 1 : 0;
        return pathString.length() >= start + 2 &&
            (slashed ? pathString.charAt(0) == '/' : true) &&
            pathString.charAt(start + 1) == ':' &&
            ((pathString.charAt(start) >= 'A' && pathString.charAt(start) <= 'Z') || (pathString
                    .charAt(start) >= 'a' && pathString.charAt(start) <= 'z'));
    }

    /**
     * Translate the string representation of the priority of the Hadoop Job
     * into the equivalent priority of the ProActive Job
     *
     * @param hadoopJobPriorityString
     *            the string representation of the HadoopJob
     * @return {@link JobPriority} the priority of the ProActive Job
     */
    protected JobPriority getPriority(String hadoopJobPriorityString) {
        if ((hadoopJobPriorityString == null) || (hadoopJobPriorityString.trim().equalsIgnoreCase(""))) {
            return JobPriority.NORMAL;
        } else {
            if (org.apache.hadoop.mapred.JobPriority.valueOf(hadoopJobPriorityString).equals(
                    org.apache.hadoop.mapred.JobPriority.VERY_HIGH)) {
                return JobPriority.HIGHEST;
            } else if (org.apache.hadoop.mapred.JobPriority.valueOf(hadoopJobPriorityString).equals(
                    org.apache.hadoop.mapred.JobPriority.HIGH)) {
                return JobPriority.HIGH;
            } else if (org.apache.hadoop.mapred.JobPriority.valueOf(hadoopJobPriorityString).equals(
                    org.apache.hadoop.mapred.JobPriority.NORMAL)) {
                return JobPriority.NORMAL;
            } else if (org.apache.hadoop.mapred.JobPriority.valueOf(hadoopJobPriorityString).equals(
                    org.apache.hadoop.mapred.JobPriority.LOW)) {
                return JobPriority.LOW;
            } else if (org.apache.hadoop.mapred.JobPriority.valueOf(hadoopJobPriorityString).equals(
                    org.apache.hadoop.mapred.JobPriority.VERY_LOW)) {
                return JobPriority.LOWEST;
            } else {
                return JobPriority.IDLE;
            }
        }
    }

    /**
     * Check if the given configuration is a valid configuration
     *
     * @param pamrjc
     *            the configuration to check
     * @param requiredConfigurationPropertyList
     *            the list of the properties that is required that they are set
     * @return true if the configuration is valid, false otherwise
     * @throws PAJobConfigurationException
     */
    protected boolean checkConfiguration(PAMapReduceJobConfiguration pamrjc,
            List<String> requiredConfigurationPropertyList) throws PAJobConfigurationException {
        for (String currentProperty : requiredConfigurationPropertyList) {
            if (pamrjc.getPropertyAsString(currentProperty) == null) {
                throw new PAJobConfigurationException("Property '" + currentProperty +
                    "' is required but it is not set!");
            }
        }

        return true;
    }

    /**
     * Initialize the list that represents the required properties that must be
     * set to be able to build the ProActive MapReduce taskflow
     */
    protected List<String> initRequiredConfigurationProperties() {
        List<String> requiredConfigurationPropertyList = new ArrayList<String>();

        /*
         * The property that stores the value of the input space the ProActive
         * MapReduce job must use is needed because otherwise we do not know
         * where input files are stored
         */
        requiredConfigurationPropertyList.add(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_SPACE.getKey());

        /*
         * The property that stores the value of the output space the ProActive
         * MapReduce job must use is needed because otherwise we do not know
         * where output files must be stored
         */
        requiredConfigurationPropertyList.add(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_SPACE.getKey());

        /*
         * The property that stores the "readMode" of the MapperPATask is needed
         * because if it is set we can be sure the input space and
         * InputAccessMode of the MapperPATask input files are set
         */
        requiredConfigurationPropertyList
                .add(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.getKey());

        /*
         * The property that stores the "readMode" of the ReducerPATask is
         * needed because if it is set we can be sure the input space and
         * InputAccessMode of the ReducerPATask input files are set
         */
        requiredConfigurationPropertyList
                .add(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.getKey());

        /*
         * The property that stores the "writeMode" of the MapperPATask is
         * needed because if it is set we can be sure the output space and
         * OutputAccessMode of the MapperPATask output files are set
         */
        requiredConfigurationPropertyList
                .add(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.getKey());

        /*
         * The property that stores the "writeMode" of the ReducerPATask is
         * needed because if it is set we can be sure the output space and
         * OutputAccessMode of the ReducerPATask output files are set
         */
        requiredConfigurationPropertyList
                .add(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.getKey());

        return requiredConfigurationPropertyList;
    }

    /**
     * Retrieve the id of this job when it has been submitted to the ProActive
     * Scheduler
     *
     * @return the JobId of the job
     */
    public JobId getJobId() {
        return jobId;
    }
}
