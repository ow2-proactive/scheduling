package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ow2.proactive.scheduler.common.task.RestartMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;


/**
 * {@link PAMapReduceJobConfiguration} represents the ProActive specific
 * informations the user must additionally specify to let the ProActive
 * MapReduce Hadoop-like API to build the ProActive MapReduce workflow and to
 * execute it using the ProActive Scheduler. If in the ProActiveJobConfiguration
 * instance the value of a property is not explicitly set then a default value
 * is used (when the default value is available). The default values for the
 * ProActive MapReduce workflow are defined in the file
 * "$SCHEDULER_HOME/extensions/mapreduce/proactive.mapreduce.framework.configuration"
 * .
 *
 * Look at the property file "paMapReduceConfigurationProperties" in the package
 * "org.ow2.proactive.scheduler.ext.mapreduce" to know which properties must be
 * defined to build the ProActive MapReduce workflow.
 *
 * @author The ProActive Team
 *
 */
public class PAMapReduceJobConfiguration {

    /**
     * The {@link File} from which the properties must be loaded
     */
    protected File propertyFile = null;

    /**
     * The configuration properties
     */
    protected Properties properties = null;

    /**
     * It indicates if the properties are already loaded from the property file
     */
    protected boolean fileLoaded = false;

    /**
     * The array to keep trace of the multiple classpaths
     */
    protected String[] classpath = null;

    /**
     * Arguments that must be passed to the forked JVM created to execute the
     * tasks that belong to the ProActive MapReduce job
     */
    protected String[] jvmArguments = null;

    /**
     * Create a new instance of {@link PAMapReduceJobConfiguration} with no
     * loaded properties
     */
    public PAMapReduceJobConfiguration() {
        properties = new Properties();
    }

    /**
     * Create a new instance of {@link PAMapReduceJobConfiguration} loading the
     * properties from the specified property file
     *
     * @param propertyFile
     *            the file from which the properties must be loaded from
     */
    public PAMapReduceJobConfiguration(File propertyFile) {
        this.propertyFile = propertyFile;
        init();
    }

    /**
     * Retrieve the value of the specified property as a {@link String}
     *
     * @param propertyName
     *            name of the property whose value must be retrieved
     * @return the value of the property
     */
    public String getPropertyAsString(String propertyName) {
        return properties.getProperty(propertyName);
    }

    /**
     * Retrieve the value of the specified property as a {@link String}
     *
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @param defaultValue
     *            the dafault value to return if no property with the specified
     *            name exists
     * @return the value of the property
     */
    public String getPropertyAsString(String propertyName, String defaultValue) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return defaultValue;
        }
        return propertyValue;
    }

    /**
     * Retrieve the value of the specified property as a boolean
     *
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @return the value of the property or false if no property corresponding
     *         to the defined name was found
     */
    public boolean getPropertyAsBoolean(String propertyName) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return false;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    /**
     * Retrieve the value of the specified property as a boolean
     *
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @param defaultValue
     *            the default value to return where no property corresponding to
     *            the defined name was found
     * @return the value of the property or the specified default value if no
     *         property corresponding to the defined name was found
     */
    public boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return defaultValue;
        }
        return getPropertyAsBoolean(propertyName);
    }

    /**
     * Retrieve the value of the specified property as an int
     *
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @return the value of the property or "-1" if no property corresponding to
     *         the defined name was found
     */
    public int getPropertyAsInteger(String propertyName) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return (new Integer(-1));
        }
        return Integer.parseInt(propertyValue);
    }

    /**
     * Retrieve the value of the specified property as an int
     *
     * @param propertyName
     *            propertyName the name of the property whose value must be
     *            retrieved
     * @param defaultValue
     *            the value to return if no property corresponding to the
     *            defined name was found
     * @return the value of the property or the default value if no property
     *         corresponding to the defined name was found
     */
    public int getPropertyAsInteger(String propertyName, int defaultValue) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return defaultValue;
        }
        return Integer.parseInt(propertyValue);
    }

    /**
     * Retrieve the value of the specified property as a long
     *
     * @param propertyName
     *            propertyName the name of the property whose value must be
     *            retrieved
     * @param defaultValue
     *            the value to return if no property corresponding to the
     *            defined name was found
     * @return the value of the property or the default value if no property
     *         corresponding to the defined name was found
     */
    public long getPropertyAsLong(String propertyName, long defaultValue) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return defaultValue;
        }
        return Long.parseLong(propertyValue);
    }

    /**
     * Initialize the {@link PAMapReduceJobConfiguration#properties} attribute
     * loading the property values from the file referenced by the
     * {@link PAMapReduceJobConfiguration#propertyFile} attribute
     */
    protected void init() {
        properties = new Properties();
        try {
            InputStream is = new FileInputStream(propertyFile);
            fileLoaded = propertyFile.exists();
            properties.load(is);
        } catch (FileNotFoundException e) {
            // thrown by "InputStream is = new FileInputStream(propertyFile);"
            e.printStackTrace();
        } catch (IOException e) {
            // thrown by "properties.load(is);"
            e.printStackTrace();
        }

        /*
         * The classpath represents the path that points to the directory that
         * stores the user defined Hadoop classes (the Mapper, the Reducer,
         * InputFormat etc...). It can be null (in that case the user defined
         * Hadoop classes stay somewhere else, e.g. in the Hadoop jar that
         * contains the Hadoop examples)
         */
        String classpathString = properties
                .getProperty(PAMapReduceFrameworkProperties.WORKFLOW_CLASSPATH.key);
        if (classpathString != null) {
            classpath = classpathString.replaceAll("\\s+", " ").replaceAll("[,\\s]+", ",").split(",", 0);
        }

        /*
         * Retrieve the JVM arguments (that will be used when the forked JVM is
         * created ). We suppose we always have some default jvm arguments and
         * but if the user defines his jvm arguments we take into account only
         * the jvm arguments defined by the user, discarding the default ones
         */
        String jvmArgumentString = properties.getProperty(
                PAMapReduceFrameworkProperties.WORKFLOW_JVM_ARGUMENTS.key, PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JVM_ARGUMENTS.key));
        if (jvmArgumentString != null) {
            jvmArguments = jvmArgumentString.replaceAll("\\s+", " ").replaceAll("[,\\s]+", ",").split(",", 0);
        }

        /*
         * we must explicitly call the two methods setReadMode() and
         * setWriteMode to have the properties that depend on the "readMode" and
         * "writeMode" properties set when we need them. Since those two methods
         * take the task identifier as parameter, we must invoke those methods
         * on each task it concern. For example the method setReadMode() must be
         * invoked on the SplitterPATask, on the MapperPATask and on the
         * ReducerPATask. While the setWriteMode() method must be invoked on the
         * MapperPATask and on the ReducerPATask.
         */
        setReadMode(PAMapReduceFramework.SPLITTER_PA_TASK, ReadMode.valueOf(getPropertyAsString(
                PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_READ_MODE.key, PAMapReduceFramework
                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_READ_MODE.key))));
        setReadMode(PAMapReduceFramework.MAPPER_PA_TASK, ReadMode.valueOf(getPropertyAsString(
                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key, PAMapReduceFramework
                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key))));
        setReadMode(PAMapReduceFramework.REDUCER_PA_TASK, ReadMode.valueOf(getPropertyAsString(
                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key, PAMapReduceFramework
                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key))));
        setWriteMode(PAMapReduceFramework.MAPPER_PA_TASK, WriteMode.valueOf(getPropertyAsString(
                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key, PAMapReduceFramework
                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key))));
        setWriteMode(PAMapReduceFramework.REDUCER_PA_TASK, WriteMode.valueOf(getPropertyAsString(
                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key, PAMapReduceFramework
                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key))));
    }

    /**
     * Set the 'cancelJobOnError' attribute for the ProActive MapReduce workflow
     *
     * @param jobCancelJobOnError
     *            the value to assign to the 'cancelJobOnError' attribute of the
     *            ProActive MapReduce workflow
     */
    public void setJobCancelJobOnError(boolean jobCancelJobOnError) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_CANCEL_JOB_ON_ERROR.key, "" +
            jobCancelJobOnError);
    }

    /**
     * Retrieve the 'cancelJobOnError' attribute of the ProActive MapReduce
     * workflow
     *
     * @return the value of the 'cancelJobOnError' attribute of the ProActive
     *         MapReduce workflow
     */
    public boolean getJobCancelOnError() {
        return getPropertyAsBoolean(PAMapReduceFrameworkProperties.WORKFLOW_CANCEL_JOB_ON_ERROR.key);
    }

    /**
     * Set the 'restartTaskOnError' attribute of the ProActive MapReduce
     * workflow
     *
     * @param jobRestartMode
     *            the {@link RestartMode} of the ProActive MapReduce workflow
     */
    public void setRestartTaskOnError(RestartMode jobRestartMode) {
        if (jobRestartMode.equals(RestartMode.ANYWHERE)) {
            properties.setProperty(
                    PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ANYWHERE.key);
        } else {
            properties.setProperty(
                    PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ELSEWHERE.key);
        }
    }

    /**
     * Retrieve the value of the 'restartTaskOnError' attribute of the ProActive
     * MapReduce workflow
     *
     * @return the {@link RestartMode} that represents the value of the
     *         'restartTaskOnError' attribute of the ProActive MapReduce
     *         workflow
     */
    public RestartMode getRestartTaskOnError() {
        String jobRestartModeString = properties
                .getProperty(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key);
        if (jobRestartModeString != null) {
            if (jobRestartModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ANYWHERE.key)) {
                return RestartMode.ANYWHERE;
            } else {
                return RestartMode.ELSEWHERE;
            }
        }
        return null;
    }

    /**
     * Set the 'maxNumberOfExecutions' attribute of the ProActive MapReduce
     * workflow
     *
     * @param jobMaxNumberOfExecutions
     *            the value to assign to the 'maxNumberOfExecutions' attribute
     *            of the ProACtive MapReduce workflow
     */
    public void setMaxNumberOfExecutions(int jobMaxNumberOfExecutions) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_MAX_NUMBER_OF_EXECUTIONS.key, "" +
            jobMaxNumberOfExecutions);
    }

    /**
     * Retrieve the value of the 'maxNumberOfExecution' attribute of the
     * ProACtive MapReduce workflow
     *
     * @return the value of the 'maxNumberOfExecutions' attribute of the
     *         ProACtive MapReduce workflow
     */
    public int getMaxNumberOfExecutions() {
        return Integer.parseInt(PAMapReduceFramework
                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAX_NUMBER_OF_EXECUTIONS.key));
    }

    /**
     * Set the 'projectName' attribute of the ProActive MapReduce workflow
     *
     * @param projectName
     *            the value to assign to the 'projectName' attribute of the
     *            ProACtive MapReduce workflow
     */
    public void setProjectName(String projectName) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_PROJECT_NAME.key, projectName);
    }

    /**
     * Retrieve the value of the 'projectName' attribute of the ProActive
     * MapReduce workflow
     *
     * @return the value of the 'projectName' attribute of the ProActive
     *         MapReduce workflow
     */
    public String getProjectName() {
        return properties.getProperty(PAMapReduceFrameworkProperties.WORKFLOW_PROJECT_NAME.key);
    }

    /**
     * Set the 'description' attribute of the ProActive MapReduce workflow
     *
     * @param jobDescription
     *            the value to assign to the 'description' attribute of the
     *            ProActive MapReduce workflow
     */
    public void setDescription(String jobDescription) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_DESCRIPTION.key, jobDescription);
    }

    /**
     * Retrieve the 'description' attribute of the ProActive MapReduce workflow
     *
     * @return the {@link String} that represents the 'description' attribute of
     *         the ProActive MapReduce workflow
     */
    public String getDescription() {
        return properties.getProperty(PAMapReduceFrameworkProperties.WORKFLOW_DESCRIPTION.key);
    }

    /**
     * Set the INPUT space ProActive MapReduce workflow
     *
     * @param inputSpaceString
     *            the {@link String} that represents the input space of the
     *            ProActive MapReduce workflow
     */
    public void setInputSpace(String inputSpaceString) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_SPACE.key, inputSpaceString);
    }

    /**
     * Retrieve INPUT space of the ProActive MapReduce workflow
     *
     * @return the {@link String} that represents the INPUT space of the
     *         ProActive MapReduce workflow
     */
    public String getInputSpace() {
        return properties.getProperty(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_SPACE.key);
    }

    /**
     * Set the OUTPUT space ProActive MapReduce workflow
     *
     * @param outputSpaceString
     *            the {@link String} that represents the OUTPUT space of the
     *            ProActive MapReduce workflow
     */
    public void setOutputSpace(String outputSpaceString) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_OUTPUT_SPACE.key, outputSpaceString);
    }

    /**
     * Retrieve OUTPUT space of the ProActive MapReduce workflow
     *
     * @return the {@link String} that represents the OUTPUT space of the
     *         ProActive MapReduce workflow
     */
    public String getOutputSpace() {
        return properties.getProperty(PAMapReduceFrameworkProperties.WORKFLOW_OUTPUT_SPACE.key);
    }

    /**
     * Set the classpath of the ProActive Job
     *
     * @param classpath
     *            the classpath to set
     */
    public void setClasspath(String[] classpath) {
        this.classpath = classpath;
    }

    /**
     * Retrieve the classpath of the ProACtive job
     *
     * @return the classpath of the ProACtive job
     */
    public String[] getClasspath() {
        return classpath;
    }

    /**
     * Set the 'name' attribute of the task identified by the specified taskId.
     * The possible values for the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the id of the task whose name must be set
     * @param taskName
     *            the value to assign to the 'name' attribute of the task
     *            identified by the the specified taskId
     */
    public void setTaskName(int taskId, String taskName) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_NAME.key,
                        taskName);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_NAME.key,
                        taskName);
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_NAME.key,
                        taskName);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_NAME.key,
                        taskName);
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_NAME.key,
                        taskName);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the 'name' attribute of the task identified by the specified
     * taskId. The possible values for the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose name must be retrieved
     * @return the name of the task
     */
    public String getTaskName(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_NAME.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_NAME.key));
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_NAME.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_NAME.key));
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_NAME.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_NAME.key));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_NAME.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_NAME.key));
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_NAME.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_NAME.key));
            }
            default:
                return null;
        }
    }

    /**
     * Set the 'cancelJobOnError' attribute of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'cancelJobOnError' must be
     *            set
     * @param cancelJobOnError
     *            the value to assign to the 'cancelJobOnError' attribute of the
     *            task
     */
    public void setCancelJobOnError(int taskId, boolean cancelJobOnError) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_CANCEL_JOB_ON_ERROR.key, "" +
                            cancelJobOnError);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_CANCEL_JOB_ON_ERROR.key, "" +
                            cancelJobOnError);
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        "" + cancelJobOnError);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_CANCEL_JOB_ON_ERROR.key, "" +
                            cancelJobOnError);
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        "" + cancelJobOnError);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Get the 'cancelJobOnError' attribute of the task identified by the
     * specified taskId
     *
     * @param taskId
     *            the identifier of the task whose 'cancelJobOnError' attribute
     *            must be retrieved
     * @return the value of the 'cancelJobOnError' attribute
     */
    public boolean getCancelJobOnError(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return getPropertyAsBoolean(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        Boolean
                                .parseBoolean(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_CANCEL_JOB_ON_ERROR.key)));
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsBoolean(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        Boolean
                                .parseBoolean(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_CANCEL_JOB_ON_ERROR.key)));
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                return getPropertyAsBoolean(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        Boolean
                                .parseBoolean(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key)));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsBoolean(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        Boolean
                                .parseBoolean(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_CANCEL_JOB_ON_ERROR.key)));
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                return getPropertyAsBoolean(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key,
                        Boolean
                                .parseBoolean(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR.key)));
            }
            default:
                return false;
        }
    }

    /**
     * Set the 'restartTaskOnError' attribute of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'restartTaskOnError'
     *            attribute must be set
     * @param restartMode
     *            the value to assign to the 'restartTaskOnError' attribute
     */
    public void setRestartMode(int taskId, RestartMode restartMode) {
        String restartModeString = null;
        if (restartMode.equals(RestartMode.ANYWHERE)) {
            restartModeString = PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ANYWHERE.key;
        } else {
            restartModeString = PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ELSEWHERE.key;
        }

        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        restartModeString);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        restartModeString);
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key,
                                restartModeString);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        restartModeString);
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key,
                                restartModeString);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the 'restartTaskOnError' attribute of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'restartTaskOnError' must be
     *            retrieved
     * @return the {@link RestartMode} that represents the value of the
     *         'restartTaskOnError' attribute of the task, or the default value
     *         if the 'restartTaskOnError' attribute was not previously
     *         explicitly set
     */
    public RestartMode getRestartMode(int taskId) {
        String restartModeString = null;

        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                restartModeString = getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR.key));
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                restartModeString = getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_RESTART_TASK_ON_ERROR.key));
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                restartModeString = getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key));
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                restartModeString = getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_RESTART_TASK_ON_ERROR.key));
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                restartModeString = getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR.key));
                break;
            }
            default:
                return null;
        }

        if (restartModeString != null) {
            if (restartModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_RESTART_MODE_ANYWHERE.key)) {
                return RestartMode.ANYWHERE;
            } else {
                return RestartMode.ELSEWHERE;
            }
        }
        return null;
    }

    /**
     * Set the 'maxNumberOfExecutions' attribute of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the id of the task whose 'maxNumberOfExecutions' attribute
     *            must be set
     * @param maxNumberOfExecutions
     *            the value to assign to the 'maxNumberOfExecutions' attribute
     *            of the task
     */
    public void setMaxNumberOfExecutions(int taskId, int maxNumberOfExecutions) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                                "" + maxNumberOfExecutions);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        "" + maxNumberOfExecutions);
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                                "" + maxNumberOfExecutions);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        "" + maxNumberOfExecutions);
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                                "" + maxNumberOfExecutions);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the 'maxNumberOfExecutions' attribute of the task identified by
     * the specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'maxNumberOfExecutions'
     *            attribute must be retrieved
     * @return the value of the 'maxNumberOfExecutions' attribute or the default
     *         value if the 'maxNumberOfExecutions' attribute was not previously
     *         explicitly set
     */
    public int getMaxNumberOfExecutions(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return getPropertyAsInteger(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        Integer
                                .parseInt(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key)));
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsInteger(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        Integer
                                .parseInt(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key)));
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                return getPropertyAsInteger(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        Integer
                                .parseInt(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key)));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsInteger(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        Integer
                                .parseInt(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key)));
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                return getPropertyAsInteger(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key,
                        Integer
                                .parseInt(PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS.key)));
            }
            default:
                return -1;
        }
    }

    /**
     * Set the 'description' attribute of the task identified by the specified
     * taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'description' attribute must
     *            be set
     * @param description
     *            the value to assign to the 'description' attribute of the task
     */
    public void setDescription(int taskId, String description) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                properties
                        .setProperty(
                                PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_DESCRIPTION.key,
                                description);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_DESCRIPTION.key, description);
                break;
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_DESCRIPTION.key,
                        description);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_DESCRIPTION.key, description);
                break;
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_DESCRIPTION.key,
                        description);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the 'description' attribute of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose 'description' attribute must
     *            be retrieved
     * @return the value of the 'description' attribute of the task
     */
    public String getDescription(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_DESCRIPTION.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_DESCRIPTION.key));
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_DESCRIPTION.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_DESCRIPTION.key));
            }
            case PAMapReduceFramework.MAPPER_JOIN_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_DESCRIPTION.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_JOIN_PA_TASK_DESCRIPTION.key));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_DESCRIPTION.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_DESCRIPTION.key));
            }
            case PAMapReduceFramework.REDUCER_JOIN_PA_TASK: {
                return getPropertyAsString(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_DESCRIPTION.key,
                        PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_JOIN_PA_TASK_DESCRIPTION.key));
            }
            default:
                return null;
        }
    }

    /**
     * Retrieve the {@link InputAccessMode} of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the input access mode for the tasks
     * identified by the {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * two tasks does not need to read input data during their own execution.
     *
     * @param taskId
     *            the identifier of the task whose InputAccessMode must be
     *            retrieved
     * @return the {@link InputAccessMode} the task must use to access its input
     *         data
     */
    public InputAccessMode getInputAccessMode(int taskId) {
        /*
         * In the code that follows, we must notice that the execution of the
         * method PAMapReduceJobConfiguration.setReadMode(...) invoked by the
         * method PAMapReduceJobConfiguration.init(), that is the first method
         * invoked after the PAMapReduceJobConfiguration object is instantiated,
         * have already set the input access mode of the tasks. This means we
         * can be sure to get a non null InputAccessMode (at least for the tasks
         * that must read input data).
         */
        String inputAccessModeString = null;
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                inputAccessModeString = getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_ACCESS_MODE.key);
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                inputAccessModeString = getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_ACCESS_MODE.key);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                inputAccessModeString = getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_ACCESS_MODE.key);
                break;
            }
        }

        if (inputAccessModeString != null) {
            if (inputAccessModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE.key)) {
                return InputAccessMode.none;
            } else if (inputAccessModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_INPUT_SPACE.key)) {
                return InputAccessMode.TransferFromInputSpace;
            } else {
                return InputAccessMode.TransferFromOutputSpace;
            }
        }
        return null;
    }

    /**
     * Retrieve the space the task identified by the specified taskId must use
     * as input space The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the input space of the tasks identified
     * by the {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * two tasks does not need to read input data during their execution.
     *
     * @param taskId
     *            the identifier of the task whose input space must be retrieved
     * @return the {@link String} that represents the space the specified task
     *         must use as its own input space
     */
    public String getInputSpace(int taskId) {
        /*
         * In the code that follows, we must notice that the execution of the
         * method PAMapReduceJobConfiguration.setReadMode(...) invoked by the
         * method PAMapReduceJobConfiguration.init(), that is the first method
         * invoked after the PAMapReduceJobConfiguration object is instantiated,
         * have already set the input space of the tasks. This means we can be
         * sure to get a non null string as the representation of the space the
         * specified task uses as its own input space.
         */
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE.key);
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.key);
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE.key);
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Retrieve the space the task identified by the specified taskId must use
     * as output space. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the output space for the tasks
     * identified by the {@link PAMapReduceFramework#SPLITTER_PA_TASK},
     * {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * three tasks does not need to write output data during their execution.
     *
     *
     * @param taskId
     *            the identifier of the task whose output space must be
     *            retrieved
     * @return the {@link String} that represents the space the specified task
     *         must use as output space
     */
    public String getOutputSpace(int taskId) {
        /*
         * In the code that follows, we must notice that the execution of the
         * method PAMapReduceJobConfiguration.setWriteMode(...) invoked by the
         * method PAMapReduceJobConfiguration.init(), that is the first method
         * invoked after the PAMapReduceJobConfiguration object is instantiated,
         * have already set the output space of the tasks. This means we can be
         * sure to get a non null string as the representation of the space the
         * specified task uses as its own output space.
         */
        switch (taskId) {
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE.key);
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE.key);
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Set the size of the input split the MapperPATask must elaborate
     *
     * @param inputSplitSize
     *            the size of the input split
     */
    public void setInputSplitSize(long inputSplitSize) {
        properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_SPLIT_SIZE.key,
                "" + inputSplitSize);
    }

    /**
     * Retrieve the size of the input split the MapperPATask must elaborate
     *
     * @return the size of the input split or, if the user has not defined
     *         explicitly it, the Hadoop default maximum value for the size of
     *         the input split
     */
    public long getInputSplitSize() {
        return getPropertyAsLong(
                PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_SPLIT_SIZE.key,
                Long
                        .parseLong(PAMapReduceFramework
                                .getDefault(PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME.key))));
    }

    /**
     * Set the mode the task identified by the specified taskId must use to read
     * its input data. The possible values for the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that setting the read mode for the tasks identified by the
     * {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} has no effect since
     * those two tasks does not need to read input data during their own
     * execution. We must also notice that:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK} can use only
     * {@link ReadMode#remoteRead} or {@link ReadMode#fullLocalRead}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK} can use
     * {@link ReadMode#remoteRead} or {@link ReadMode#fullLocalRead} or
     * {@link ReadMode#partialLocalRead}</li>
     * <li>
     * {@link PAMapReduceFramework#REDUCER_PA_TASK} can use
     * {@link ReadMode#remoteRead} or {@link ReadMode#fullLocalRead}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task for which the read mode must be set
     * @param readMode
     *            the read mode of the task
     */
    public void setReadMode(int taskId, ReadMode readMode) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                /*
                 * the SplitterPATask can read its input data from the ProActive
                 * MapReduce workflow INPUT space or from its own LOCAL space. The
                 * choice between which space the data must be read from depends on
                 * the value of the 'readMode' the user defines for the
                 * SplitterPATask. The possible values are 'remoteRead' or
                 * 'fullLocalRead'. The former indicates that the SplitterPATask
                 * reads the input data directly from the ProActive MapReduce
                 * workflow INPUT space while the latter means that the input data
                 * are first transferred to the node the SplitterPATask executes on
                 * and then they are read (by the SplitterPATask to create the input
                 * splits). It's easy to understand that the 'readMode' defines,
                 * beyond the space the data must be read from, also the input
                 * access mode to use for those data. Hence 'remoteRead' defines
                 * InputAccessMode.none as the input access mode for the
                 * SplitterPATask input data while 'fullLocalRead' defines
                 * InputAccessMode.TransferFromInputSpace as the input access mode
                 * for the SplitterPATask input data. Lastly we must notice that in
                 * the case of the SplitterPATask we do not need to set a property
                 * equivalent to the
                 * "PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key"
                 * . In fact in the case of the SplitterPATask we do not need to
                 * perform a test on the read mode, when the task is executing, to
                 * decide if we have to transfer input data in the SplitterPATask
                 * LOCAL space (see the comment on the MapperPATask case to get more
                 * details).
                 */
                if (readMode.equals(ReadMode.remoteRead)) {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_DATASPACE.key);
                } else {
                    properties
                            .setProperty(
                                    PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_ACCESS_MODE.key,
                                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_INPUT_SPACE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                }
                break;
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                /*
                 * the MapperPATask can read its input data from the ProActive
                 * MapReduce workflow INPUT space or from its own LOCAL space. The
                 * choice between the two possible spaces depends on the value of
                 * the 'readMode'. The possible values of the 'readMode' are
                 * 'remoteRead' or 'fullLocalRead' or 'partialLocalRead'. The first
                 * indicates that the input data are read directly from the
                 * ProActive MapReduce workflow INPUT space (this means also that
                 * the InputAccessMode to use for the MapperPATask input data is
                 * InputAccessMode.none). The 'fullLocalRead' indicates that, first,
                 * all the input data of the ProActive MapReduce workflow are
                 * transferred in the MapperPATask LOCAL space and then the map
                 * function is applied on those data (this means also that the
                 * InputAccessMode for the MapperPATask input data is
                 * InputAccessMode.TransferFromInputSpace). Finally, the
                 * 'partialLocalRead' indicates that only the input data the
                 * MapperPATask must elaborate are transferred on the node the
                 * MapperPATask executes on (this means the InputAccessMode to use
                 * for the ProActive MapReduce workflow input data is
                 * InputAccessMode.none but the MapperPATask manually copy its input
                 * data from the ProActive MapReduce workflow INPUT space to its own
                 * LOCAL space. To do that we must check the value of the
                 * 'readMode', the user defined, inside the MapperPATask. That is
                 * the reason why we must pass to the MapperPATask the 'readMode' as
                 * the value associated to the property
                 * 'PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key'
                 * ).
                 */
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key,
                        readMode.getKey());

                if (readMode.equals(ReadMode.remoteRead)) {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_DATASPACE.key);
                } else if (readMode.equals(ReadMode.fullLocalRead)) {
                    properties
                            .setProperty(
                                    PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_ACCESS_MODE.key,
                                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_INPUT_SPACE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                } else {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                }
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                /*
                 * the ReducerPATask can read its input data from its LOCAL space or
                 * from the ProActive MapReduce workflow OUTPUT space. The input
                 * data refer to the data before they are merged (and so sorted). In
                 * fact the result of the merge is always stored in the
                 * ReducerPATask LOCAL space, so that the ReducerPATask will read
                 * the values to which it must apply the reduce function from its
                 * LOCAL space. When the user defines the read mode of the
                 * ReducerPATask it defines from which space the data to merge must
                 * be read. After the merge is done the data are stored in the
                 * ReducerPATask LOCAL space or in the ReducerPATask main memory.
                 * The choice between the LOCAL space and the OUTPUT space depends
                 * on the value of the 'readMode' the user defines. The possible
                 * values are 'remoteRead' or 'fullLocalRead'. The former indicates
                 * that the data to merge are read from the ProActive MapReduce
                 * workflow OUTPUT space while the latter means that the data to
                 * merge are first transferred on the node the ReducerPATask
                 * executes on and then those data are read from the ReducerPATask
                 * LOCAL space when the reduce function must be applied on them.
                 */
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key,
                        readMode.getKey());

                if (readMode.equals(ReadMode.remoteRead)) {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE.key);
                } else {
                    properties
                            .setProperty(
                                    PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_ACCESS_MODE.key,
                                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_OUTPUT_SPACE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the read mode of the task identified by the specified taskId.
     * The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the read mode for the tasks identified
     * by the {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} or
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * two tasks does not need to read input data during their own execution.
     *
     * @param taskId
     *            the identifier of the task whose read mode must be retrieved
     * @return the read mode of the task or null if no read mode was set
     */
    public ReadMode getReadMode(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.SPLITTER_PA_TASK: {
                return ReadMode
                        .valueOf(getPropertyAsString(
                                PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_READ_MODE.key,
                                PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_READ_MODE.key)));
            }
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return ReadMode
                        .valueOf(getPropertyAsString(
                                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key,
                                PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.key)));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return ReadMode
                        .valueOf(getPropertyAsString(
                                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key,
                                PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.key)));
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Set the write mode of the task identified by the specified taskId. The
     * possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that setting the write mode for the tasks identified by
     * the {@link PAMapReduceFramework#SPLITTER_PA_TASK},
     * {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} has no effect since
     * those three tasks does not need to write output data during their
     * execution. The possible values for the writeMode are:
     * <ul>
     * <li>{@link WriteMode#remoteWrite}</li>
     * <li>{@link WriteMode#localWrite}</li>
     * </ul>
     *
     * @param taskId
     *            the identifier of the task whose write mode must be set
     * @param writeMode
     *            the write mode to set for the specified task
     */
    public void setWriteMode(int taskId, WriteMode writeMode) {
        switch (taskId) {
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                /*
                 * the MapperPATask can write its output in its LOCAL space or into
                 * the ProActive MapReduce workflow OUTPUT space. The choice between
                 * those two spaces depends on the 'writeMode'. The possible values
                 * of the 'writeMode' for the MapperPATask are 'remoteWrite' and
                 * 'localWrite'. The former indicates that the MapperPATask writes
                 * its output data directly into the ProActive MapReduce workflow
                 * OUTPUT space (this means that the OutputAccessMode the
                 * MapperPATask must use to access its output data is
                 * OutputAccessMode.none). The latter indicates that the
                 * MapperPATask writes its output data in its LOCAL space and
                 * successively the ProActive DataSpaces mechanism transfers those
                 * data towards the ProActive MapReduce workflow OUTPUT space (this
                 * means that the OutputAccessMode the MapperPATask must use to
                 * access its output data is
                 * OutputAccessMode.TransferToOutputSpace). Finally, we must notice
                 * that we set the property
                 * 'PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key'
                 * to keep trace of which write mode is used by the MapperPATask but
                 * we do not access that property during the MapperPATask execution.
                 */
                properties.setProperty(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key,
                        writeMode.getKey());

                if (writeMode.equals(WriteMode.remoteWrite)) {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE.key);
                } else {
                    properties
                            .setProperty(
                                    PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_ACCESS_MODE.key,
                                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_TRANSFER_TO_OUTPUT_SPACE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                }
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                /*
                 * the ReducerPATask can write its output in its LOCAL space or into
                 * the ProActive MapReduce workflow OUTPUT space. The choice between
                 * those two spaces depends on the 'writeMode'. The possible values
                 * of the 'writeMode' for the ReducerPATask are 'remoteWrite' and
                 * 'localWrite'. The former indicates that the ReducerPATask writes
                 * its output data directly into the ProActive MapReduce workflow
                 * OUTPUT space (this means that the OutputAccessMode the
                 * ReducerPATask must use to access its output data is
                 * OutputAccessMode.none). The latter indicates that the
                 * ReducerPATask writes its output data in its LOCAL space and
                 * successively the ProActive DataSpaces mechanism transfers those
                 * data towards the ProActive MapReduce workflow OUTPUT space (this
                 * means that the OutputAccessMode the ReducerPATask must use to
                 * access its output data is
                 * OutputAccessMode.TransferToOutputSpace). Finally, we must notice
                 * that we set the property
                 * 'PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key'
                 * to keep trace of which write mode is used by the ReducerPATask
                 * but we do not access that property during the ReducerPATask
                 * execution.
                 */
                properties.setProperty(
                        PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key, writeMode
                                .getKey());

                if (writeMode.equals(WriteMode.remoteWrite)) {
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_ACCESS_MODE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_NONE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE.key);
                } else {
                    properties
                            .setProperty(
                                    PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_ACCESS_MODE.key,
                                    PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_TRANSFER_TO_OUTPUT_SPACE.key);
                    properties.setProperty(
                            PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE.key,
                            PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.key);
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Retrieve the write mode of the task identified by the specified taskId.
     * The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the write mode for the tasks identified
     * by the {@link PAMapReduceFramework#SPLITTER_PA_TASK},
     * {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * three tasks does not need to write output data during their execution.
     *
     * @param taskId
     *            the identifier of the task whose write mode must be retrieved
     * @return the write mode of the task or null if no write mode was set
     */
    public WriteMode getWriteMode(int taskId) {
        switch (taskId) {
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                return WriteMode
                        .valueOf(getPropertyAsString(
                                PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key,
                                PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_WRITE_MODE.key)));
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                return WriteMode
                        .valueOf(getPropertyAsString(
                                PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key,
                                PAMapReduceFramework
                                        .getDefault(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_WRITE_MODE.key)));
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Retrieve the {@link OutputAccessMode} of the task identified by the
     * specified taskId. The possible values of the taskId are:
     * <ul>
     * <li>{@link PAMapReduceFramework#SPLITTER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_PA_TASK}</li>
     * <li>{@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK}</li>
     * </ul>
     * We must notice that trying to get the output access mode for the tasks
     * identified by the {@link PAMapReduceFramework#SPLITTER_PA_TASK},
     * {@link PAMapReduceFramework#MAPPER_JOIN_PA_TASK} and
     * {@link PAMapReduceFramework#REDUCER_JOIN_PA_TASK} return null since those
     * three tasks does not need to write output data during their own
     * execution.
     *
     * @param taskId
     *            the identifier of the task whose OutputAccessMode must be
     *            retrieved
     * @return the {@link OutputAccessMode} the task must use to access its
     *         output data
     */
    public OutputAccessMode getOutputAccessMode(int taskId) {
        /*
         * In the code that follows, we must notice that the execution of the
         * method PAMapReduceJobConfiguration.setWriteMode(...) invoked by the
         * method PAMapReduceJobConfiguration.init(), that is the first method
         * invoked after the PAMapReduceJobConfiguration object is instantiated,
         * have already set the output access mode the tasks must use to access
         * its output data. This means we can be sure to get a not null
         * OutputAccessMode (at least for the tasks that produce output data)
         */
        String outputAccessModeString = null;
        switch (taskId) {
            case PAMapReduceFramework.MAPPER_PA_TASK: {
                outputAccessModeString = getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_ACCESS_MODE.key);
                break;
            }
            case PAMapReduceFramework.REDUCER_PA_TASK: {
                outputAccessModeString = getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_ACCESS_MODE.key);
                break;
            }
        }

        if (outputAccessModeString != null) {
            if (outputAccessModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_NONE.key)) {
                return OutputAccessMode.none;
            } else if (outputAccessModeString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_TRANSFER_TO_OUTPUT_SPACE.key)) {
                return OutputAccessMode.TransferToOutputSpace;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Set the prefix the reducer must use to build the name of the output file.
     * The name of the file built by the reducer will be compliant to the
     * following format: prefix_&lt;reducerId&gt;
     *
     * @param prefix
     *            the prefix to set
     */
    public void setReducerOutputFileNamePrefix(String prefix) {
        properties.setProperty(PAMapReduceFrameworkProperties.REDUCER_PA_TASK_OUTPUT_FILE_NAME_PREFIX.key,
                prefix);
    }

    /**
     * Retrieve the prefix the reducer must use to build the name of the output
     * file.
     *
     * @return the prefix
     */
    public String getReducerOutputFileNamePrefix() {
        return properties
                .getProperty(PAMapReduceFrameworkProperties.REDUCER_PA_TASK_OUTPUT_FILE_NAME_PREFIX.key);
    }

    /**
     * Set the JVM arguments. This method act as the
     * {@link PAMapReduceJobConfiguration#addJVMArguments(String[])}
     *
     * @param jvmArguments
     *            the JVM arguments to set
     */
    public void setJVMArguments(String[] arguments) {
        jvmArguments = arguments;
    }

    /**
     * Add the specified JVM argument to the list of existing JVM arguments
     *
     * @param jvmArgument
     *            the JVM argument to add
     */
    public void addJVMArgument(String argument) {
        addJVMArguments(new String[] { argument });
    }

    /**
     * Add the list of specified JVM arguments to the list of existing JVM
     * arguments
     *
     * @param jvmArguments
     *            the list of JVM arguments to add
     */
    public void addJVMArguments(String[] arguments) {
        int newLength = jvmArguments.length + arguments.length;
        String[] tmpJvmArguments = new String[newLength];
        System.arraycopy(jvmArguments, 0, tmpJvmArguments, 0, jvmArguments.length);
        for (int i = jvmArguments.length, j = 0; i < newLength; i++, j++) {
            tmpJvmArguments[i] = arguments[j];
        }
        jvmArguments = tmpJvmArguments;
    }

    /**
     * Retrieve the JVM arguments
     *
     * @return the String[] of JVM arguments
     */
    public String[] getJVMArguments() {
        return jvmArguments;
    }
}
