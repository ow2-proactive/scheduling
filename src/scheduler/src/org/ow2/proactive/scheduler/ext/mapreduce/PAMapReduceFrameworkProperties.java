package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;


/**
 * {@link PAMapReduceFrameworkProperties} contains all the ProActive MapReduce
 * Framework properties. Those properties can be used to pass arguments to the
 * XxxxxPATask that compose the ProActive MapReduce workflow
 * 
 * @author The ProActive Team
 * 
 *         TODO maybe we have to change all the name of the properties from
 *         WORKFLOW_xxxx into PA_MAPREDUCE_xxxx
 * 
 */
public enum PAMapReduceFrameworkProperties {

    /**
     * This property lists the name of the jars needed by the ProActive
     * MapReduce API
     */
    WORKFLOW_FILE_SYSTEM_LOCALHOST_NAME("org.ow2.proactive.scheduler.ext.mapreduce.localhost.name",
            PropertyType.STRING),

    SCHEDULER_URL("org.ow2.proactive.scheduler.ext.mapreduce.schedulerUrl", PropertyType.STRING),

    SCHEDULER_USERNAME("org.ow2.proactive.scheduler.ext.mapreduce.username", PropertyType.STRING),

    SCHEDULER_PASSWORD("org.ow2.proactive.scheduler.ext.mapreduce.password", PropertyType.STRING),

    WORKFLOW_CANCEL_JOB_ON_ERROR("org.ow2.proactive.scheduler.ext.mapreduce.workflow.cancelJobOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_CLASSPATH("org.ow2.proactive.scheduler.ext.mapreduce.workflow.classpath", PropertyType.STRING),

    WORKFLOW_JVM_ARGUMENTS("org.ow2.proactive.scheduler.ext.mapreduce.workflow.forkedJVM.arguments",
            PropertyType.STRING),

    WORKFLOW_DESCRIPTION("org.ow2.proactive.scheduler.ext.mapreduce.workflow.description",
            PropertyType.STRING),

    WORKFLOW_FILE_NAME_ALL("org.ow2.proactive.scheduler.ext.mapreduce.workflow.fileName.all",
            PropertyType.STRING),
    /**
     * The String to use to build composed file names. E.g., "intermediate" +
     * WORKFLOW_FILE_NAME_LINKING_STRING + "0" can be equal to "intermediate_0"
     * (if the valueof the WORKFLOW_FILE_NAME_LINKING_STRING property is "_").
     */
    WORKFLOW_FILE_NAME_LINKING_STRING(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.fileName.linkingString", PropertyType.STRING),

    WORKFLOW_FILE_SYSTEM_DEFAULT_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.fsDefaultName",
            PropertyType.STRING),

    WORKFLOW_FILE_SYSTEM_DEFAULT_IMPLEMENTATION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.fsDefaultImplementation", PropertyType.STRING),

    WORKFLOW_FILE_SYSTEM_DISABLE_CACHE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.fsDisableCache",
            PropertyType.STRING),

    WORKFLOW_FILE_SYSTEM_IMPLEMENTATION_DATASPACE_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.fsImplementationDataSpacePropertyName",
            PropertyType.STRING),

    WORKFLOW_FILE_SYSTEM_IMPLEMENTATION_SCHEME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.fsImplementationScheme", PropertyType.STRING),

    WORKFLOW_INPUT_KEY_COUNTER_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputKeyCounterName",
            PropertyType.STRING),

    WORKFLOW_INPUT_KEY_COUNTER_DISPLAY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputKeyCounterDisplayName",
            PropertyType.STRING),

    /**
     * The property used to store the value of the input data space of the
     * ProActive MapReduce job
     */
    WORKFLOW_INPUT_SPACE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputSpace", PropertyType.STRING),

    WORKFLOW_INPUT_VALUE_COUNTER_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputValueCounterName", PropertyType.STRING),

    WORKFLOW_INPUT_VALUE_COUNTER_DISPLAY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputValueCounterDisplayName",
            PropertyType.STRING),

    WORKFLOW_INTERMEDIATE_BUFFER_SIZE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.intermediateBufferSize", PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_REPLICATION_SCRIPT(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.replicationScript.fileName",
            PropertyType.STRING),

    WORKFLOW_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.maxNumberOfExecutions", PropertyType.STRING),

    WORKFLOW_NUMBER_OF_MAPPER_PA_TASK(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.numberOfMapperPATask", PropertyType.INTEGER),

    /**
     * The property used to store the value of the output data space of the
     * ProActive MapReduce job
     */
    WORKFLOW_OUTPUT_SPACE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.outputSpace",
            PropertyType.STRING),

    WORKFLOW_PROJECT_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.projectName",
            PropertyType.STRING),

    REDUCER_PA_TASK_OUTPUT_FILE_TRANSFER_POST_SCRIPT_FILE_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.reducerPATask.outputFileTransferPostScript.fileName",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_REPLICATION_SCRIPT(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.replicationScript.fileName",
            PropertyType.STRING),

    WORKFLOW_RESTART_TASK_ON_ERROR("org.ow2.proactive.scheduler.ext.mapreduce.workflow.restartTaskOnError",
            PropertyType.STRING),

    WORKFLOW_SCRIPT_ENGINE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.scriptEngineName",
            PropertyType.STRING),

    /**
     * The property used to store the identifier of a task of the ProActive
     * workflow. We must notice that the, usually, this property stores an
     * integer that allow us to identifies the different kind of task composing
     * the ProActive MapReduce workflow (SplitterPATask, MapprePATask,
     * MapperJoinPATask, ReducerPATask and ReducerJoinPATask)
     */
    PA_MAPREDUCE_TASK_IDENTIFIER("org.ow2.proactive.scheduler.ext.mapreduce.task.identifier",
            PropertyType.STRING),

    WORKFLOW_TEMPORARY_FILE_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.fileName.temporaryFileName",
            PropertyType.STRING),

    /**
     * The property that identifies the {@link InputAccessMode#none}
     * InputAccessMode for a task of a ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_NONE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputAccessModeNone", PropertyType.STRING),

    /**
     * The property that identifies the
     * {@link InputAccessMode#TransferFromInputSpace} InputAccessMode for a task
     * of a ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_INPUT_SPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputAccessModeTransferFromInputSpace",
            PropertyType.STRING),

    /**
     * The property that identifies the
     * {@link InputAccessMode#TransferFromOutputSpace} InputAccessMode for a
     * task of a ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_INPUT_ACCESS_MODE_TRANSFER_FROM_OUTPUT_SPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputAccessModeTransferFromOutputSpace",
            PropertyType.STRING),

    /**
     * The property that identifies the input space of the ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_INPUT_DATASPACE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.inputDataSpace",
            PropertyType.STRING),

    /**
     * The property that identifies the local space of the ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_LOCAL_DATASPACE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.localDataSpace",
            PropertyType.STRING),

    WORKFLOW_JAVA_TASK_LOGGING_DEBUG("org.ow2.proactive.scheduler.ext.mapreduce.workflow.logging.debug",
            PropertyType.BOOLEAN),

    WORKFLOW_JAVA_TASK_LOGGING_PROFILE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.logging.profile",
            PropertyType.BOOLEAN),

    WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_NONE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.outputAccessModeNone", PropertyType.STRING),

    WORKFLOW_JAVA_TASK_OUTPUT_ACCESS_MODE_TRANSFER_TO_OUTPUT_SPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.outputAccessModeTransferToOutputSpace",
            PropertyType.STRING),

    /**
     * The property that identifies the output space of the ProActive taskflow
     */
    WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE("org.ow2.proactive.scheduler.ext.mapreduce.workflow.outputDataSpace",
            PropertyType.STRING),

    WORKFLOW_JAVA_TASK_RESTART_MODE_ANYWHERE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.restartAnywhere", PropertyType.STRING),

    WORKFLOW_JAVA_TASK_RESTART_MODE_ELSEWHERE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.restartElsewhere", PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_EXECUTABLE_CLASS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.executableClass",
            PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_CANCEL_JOB_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.cancelJobOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_SPLITTER_PA_TASK_DESCRIPTION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.description",
            PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_INPUT_ACCESS_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.inputAccessMode",
            PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.inputDataSpace",
            PropertyType.STRING),

    /**
     * The property that is used to store the value associated to the "readMode"
     * the SplitterPATask must use
     */
    WORKFLOW_SPLITTER_PA_TASK_READ_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.readMode", PropertyType.STRING),

    /**
     * The property used to store the value of the size of the input split a
     * MapperPATask must elaborated
     */
    WORKFLOW_SPLITTER_PA_TASK_INPUT_SPLIT_SIZE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.inputSplitSize",
            PropertyType.INTEGER),

    WORKFLOW_SPLITTER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.maxNumberOfExecutions",
            PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.name",
            PropertyType.STRING),

    WORKFLOW_SPLITTER_PA_TASK_RESTART_TASK_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.splitterPATask.restartTaskOnError",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_EXECUTABLE_CLASS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.executableClass",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_CANCEL_JOB_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.cancelJobOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_MAPPER_PA_TASK_DESCRIPTION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.description",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_INPUT_ACCESS_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.inputAccessMode",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.inputDataSpace",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.maxNumberOfExecutions",
            PropertyType.INTEGER),

    WORKFLOW_MAPPER_PA_TASK_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.name",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_OUTPUT_ACCESS_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.outputAccessMode",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.outputDataSpace",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_READ_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.readMode", PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_RESTART_TASK_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.restartTaskOnError",
            PropertyType.STRING),

    WORKFLOW_MAPPER_PA_TASK_WRITE_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperPATask.writeMode", PropertyType.STRING),

    WORKFLOW_MAPPER_JOIN_PA_TASK_EXECUTABLE_CLASS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.executableClass",
            PropertyType.STRING),

    WORKFLOW_MAPPER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.cancelJobOnError",
            PropertyType.STRING),

    WORKFLOW_MAPPER_JOIN_PA_TASK_DESCRIPTION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.description",
            PropertyType.STRING),

    WORKFLOW_MAPPER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.maxNumberOfExecutions",
            PropertyType.INTEGER),

    WORKFLOW_MAPPER_JOIN_PA_TASK_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.name", PropertyType.STRING),

    WORKFLOW_MAPPER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.mapperJoinPATask.restartTaskOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_REDUCER_PA_TASK_EXECUTABLE_CLASS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.executableClass",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_CANCEL_JOB_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.cancelJobOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_REDUCER_PA_TASK_DESCRIPTION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.description",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_INPUT_ACCESS_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.inputAccessMode",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.inputDataSpace",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.maxNumberOfExecutions",
            PropertyType.INTEGER),

    WORKFLOW_REDUCER_PA_TASK_NAME("org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.name",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_OUTPUT_ACCESS_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.outputAccessMode",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.outputDataSpace",
            PropertyType.STRING),

    /**
     * Define the property to use to store the prefix to use to build the name
     * of the ReducerPATask output files. If "prefix" is the string stored by
     * this property then the name of the output file of the ReducerPATask will
     * be compliant to the following format: "prefix&lt;reducerId&gt;"
     */
    REDUCER_PA_TASK_OUTPUT_FILE_NAME_PREFIX(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.outputFileNamePrefix",
            PropertyType.STRING),

    /**
     * Define the property to use to store the "readMode" the ReducerPATask must
     * use
     */
    WORKFLOW_REDUCER_PA_TASK_READ_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.readMode", PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_RESTART_TASK_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.restartTaskOnError",
            PropertyType.STRING),

    WORKFLOW_REDUCER_PA_TASK_WRITE_MODE(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerPATask.writeMode", PropertyType.STRING),

    WORKFLOW_REDUCER_JOIN_PA_TASK_EXECUTABLE_CLASS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.executableClass",
            PropertyType.STRING),

    WORKFLOW_REDUCER_JOIN_PA_TASK_CANCEL_JOB_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.cancelJobOnError",
            PropertyType.BOOLEAN),

    WORKFLOW_REDUCER_JOIN_PA_TASK_DESCRIPTION(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.description",
            PropertyType.STRING),

    WORKFLOW_REDUCER_JOIN_PA_TASK_MAX_NUMBER_OF_EXECUTIONS(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.maxNumberOfExecutions",
            PropertyType.STRING),

    WORKFLOW_REDUCER_JOIN_PA_TASK_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.name", PropertyType.STRING),

    WORKFLOW_REDUCER_JOIN_PA_TASK_RESTART_TASK_ON_ERROR(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.reducerJoinPATask.restartTaskOnError",
            PropertyType.INTEGER),

    /**
     * This property stores the name of the property used by Hadoop to retrieve
     * the name of the class used as the Combiner
     */
    HADOOP_COMBINER_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.combinerClass.propertyName",
            PropertyType.STRING),

    HADOOP_JOB_CONFIGURATION("pa.scheduler.mapreduce.ext.hadoopJobConfiguration", PropertyType.STRING),

    HADOOP_JOB_LINE_RECORD_READER_MAX_LENGTH("org.apache.hadoop.job.lineRecordReaderMaxLength",
            PropertyType.STRING),

    HADOOP_JOB_MAP_OUTPUT_KEY_CLASS("org.apache.hadoop.job.mapOutput.keyClass", PropertyType.STRING),

    HADOOP_JOB_MAP_OUTPUT_VALUE_CLASS("org.apache.hadoop.job.mapOutput.valueClass", PropertyType.STRING),

    /**
     * This property stores the name of the Hadoop property used to retrieve the
     * name of the property used to retrieve the number of reducers that must be
     * executed from the Hadoop {@link Configuration}
     */
    HADOOP_NUMBER_OF_REDUCER_TASKS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.numberOfReducerTasks.propertyName",
            PropertyType.STRING),

    HADOOP_JOB_OUTPUT_VALUE_GROUPING_COMPARATOR("org.apache.hadoop.job.outputValueGroupingComparator",
            PropertyType.STRING),

    HADOOP_JOB_OUTPUT_KEY_COMPARATOR_CLASS("org.apache.hadoop.job.outputKeyComparatorClass",
            PropertyType.STRING),

    HADOOP_JOB_PRIORITY("org.apache.hadoop.job.priority", PropertyType.STRING),

    HADOOP_JOB_TEXT_OUTPUT_FORMAT_DEFAULT_KEY_VALUE_SEPARATOR(
            "org.apache.hadoop.job.textOutputFormat.defaultKeyValueSeparator", PropertyType.STRING),

    HADOOP_JOB_TEXT_OUTPUT_FORMAT_KEY_VALUE_SEPARATOR(
            "org.apache.hadoop.job.textOutputFormat.keyValueSeparator", PropertyType.STRING),

    HADOOP_FS_DEFAULT_NAME_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.hadoopFsDefaultNamePropertyName",
            PropertyType.STRING),

    HADOOP_FS_DISABLE_CACHE_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.hadoopFsDisableCachePropertyName",
            PropertyType.STRING),

    HADOOP_FS_IMPLEMENTATION_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.hadoopFsImplementationPropertyName",
            PropertyType.STRING),

    HADOOP_FS_LOCAL_BLOCK_SIZE("org.apache.hadoop.fs.local.block.size", PropertyType.STRING),

    HADOOP_FS_LOCAL_BLOCK_SIZE_DEFAULT_VALUE(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.fs.local.block.size.defaultValue",
            PropertyType.STRING),

    /**
     * This property stores the name of the Hadoop property used to retrieve the
     * name of the property used in the method {@link Job#getInputFormatClass()}
     * to retrieve the class to use as the {@link InputFormat}
     */
    HADOOP_INPUT_FORMAT_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.inputFormat.propertyName", PropertyType.STRING),

    /**
     * This property stores the name of the Hadoop property used to retrieve the
     * name of the property used in the method {@link Job#getMapperClass()} to
     * retrieve the class to use as the {@link Mapper}
     */
    HADOOP_MAPPER_CLASS_PROPERTY_NAME("org.ow2.proactive.scheduler.ext.mapreduce.hadoop.mapper.propertyName",
            PropertyType.STRING),

    HADOOP_MAX_SPLIT_SIZE_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.max.split.size.propertyName",
            PropertyType.STRING),

    HADOOP_MIN_SPLIT_SIZE_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.min.split.size.propertyName",
            PropertyType.STRING),

    HADOOP_OUTPUT_KEY_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.outputKeyClass.propertyName",
            PropertyType.STRING),

    HADOOP_OUTPUT_VALUE_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.outputValueClass.propertyName",
            PropertyType.STRING),

    HADOOP_INPUT_DIRECTORY_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.hadoopInputDirectoryPropertyName",
            PropertyType.STRING),

    HADOOP_IO_FILE_BUFFER_SIZE("org.apache.hadoop.io.file.buffer.size", PropertyType.STRING),

    HADOOP_IO_FILE_BUFFER_SIZE_DEFAULT_VALUE(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.file.buffer.size.defaultValue",
            PropertyType.STRING),

    HADOOP_IO_SORT_FACTOR_DEFAULT_VALUE(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.factor.defaultValue",
            PropertyType.STRING),

    HADOOP_IO_SORT_FACTOR_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.factorPropertyName",
            PropertyType.STRING),

    HADOOP_IO_SORT_MAP_SORT_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.mapSortClassPropertyName",
            PropertyType.STRING),

    HADOOP_IO_SORT_MB_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.mbPropertyName", PropertyType.STRING),

    HADOOP_IO_SORT_RECORD_PERCENT_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.recordPercentPropertyName",
            PropertyType.STRING),

    HADOOP_IO_SORT_SPILL_PERCENT_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.io.sort.spillPercentPropertyName",
            PropertyType.STRING),

    HADOOP_MAPPER_OUTPUT_KEY_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.mapper.outputKeyClassPropertyName",
            PropertyType.STRING),

    HADOOP_MAPPER_OUTPUT_VALUE_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.mapper.outputValueClassPropertyName",
            PropertyType.STRING),

    HADOOP_MINIMUM_NUMBER_OF_SPILL_FOR_COMBINE_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.minimumNumberOfSpillForCombinePropertyName",
            PropertyType.STRING),

    HADOOP_NUMBER_OF_REDUCERS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.numberOfReducersPropertyName",
            PropertyType.STRING),

    HADOOP_OUTPUT_DIRECTORY_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.workflow.hadoopOutputDirectoryPropertyName",
            PropertyType.STRING),

    /**
     * This property stores the name of the Hadoop property used to retrieve the
     * name of the property used in the method
     * {@link Job#getOutputFormatClass()} to retrieve the class to use as the
     * {@link OutputFormat}
     */
    HADOOP_OUTPUT_FORMAT_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.outputFormat.propertyName", PropertyType.STRING),

    HADOOP_OUTPUT_KEY_COMPARATOR_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.output.keyComparatorClassPropertyName",
            PropertyType.STRING),

    /**
     * This property stores the name of the Hadoop property used to retrieve the
     * name of the property used in the method {@link Job#getReducerClass()} to
     * retrieve the class to use as the {@link Reducer}
     */
    HADOOP_REDUCER_CLASS_PROPERTY_NAME(
            "org.ow2.proactive.scheduler.ext.mapreduce.hadoop.reducer.propertyName", PropertyType.STRING);

    /** key of the specific instance */
    protected String key;
    /** value of the specific instance */
    protected PropertyType propertyType;

    /** the properties of the ProActive MapReduce framework */
    private static Properties properties = null;

    private static boolean fileLoaded = false;

    /**
     * Create a new instance of {@link PAMapReduceFrameworkProperties}
     * 
     * @param key
     *            the key of the instance
     * @param propertyType
     *            the real Java type of this instance
     */
    PAMapReduceFrameworkProperties(String key, PropertyType propertyType) {
        this.key = key;
        this.propertyType = propertyType;
    }

    /**
     * Retrieve the properties from the default configuration file
     * 
     * @return the properties
     */
    public static Properties getProperties() {
        if (!fileLoaded) {
            loadProperties();
            return properties;
        }
        return properties;
    }

    /**
     * Load the properties from the default configuration file. We must notice
     * we are forced to use the {@link ForkEnvironment} to be able to retrieve
     * the resource as a stream. Otherwise the properties file will be not found
     */
    private static void loadProperties() {
        if (!fileLoaded) {
            String propertiesFileName = null;
            try {
                propertiesFileName = PAMapReduceFramework.PA_MAPREDUCE_FRAMEWORK_PROPERTIES_FILE_NAME;
                properties = new Properties();
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        propertiesFileName));
                fileLoaded = true;
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            } catch (FileNotFoundException e) {
                /*
                 * thrown by
                 * "InputStream is = new FileInputStream(propertyFile);"
                 * instruction
                 */
                e.printStackTrace();
            } catch (IOException e) {
                // thrown by "properties.load(is);" instruction
                e.printStackTrace();
            }
        }
    }

    /**
     * Supported types for {@link PAMapReduceFrameworkProperties}
     */
    public enum PropertyType {
        STRING, BOOLEAN, INTEGER;
    }

    /**
     * Return the key of the specific instance
     * 
     * @return the key of the specific instance
     */
    public String getKey() {
        return key;
    }

    /**
     * Return the type of the specific instance
     * 
     * @return the type of the specific instance
     */
    public PropertyType getType() {
        return propertyType;
    }

    /**
     * Retrieve the value of the specified property as a {@link String}
     * 
     * @param propertyName
     *            name of the property whose value must be retrieved
     * @return the {@link String} that represents the value of the property
     */
    public static String getPropertyAsString(String propertyName) {
        return getProperties().getProperty(propertyName);
    }

    /**
     * Retrieve the value of the specified property as a boolean
     * 
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @return the boolean that represents the value of the property, false if
     *         no property corresponding to the defined name was found
     */
    public static boolean getPropertyAsBoolean(String propertyName) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return false;
        }
        return Boolean.parseBoolean(propertyValue);
    }

    /**
     * Retrieve the value of the specified property as an int
     * 
     * @param propertyName
     *            the name of the property whose value must be retrieved
     * @return the int that represents the value of the property, "-1" if no
     *         property corresponding to the defined name was found
     */
    public static int getPropertyAsInteger(String propertyName) {
        String propertyValue = getPropertyAsString(propertyName);
        if ((propertyValue == null) || (propertyValue.equalsIgnoreCase(""))) {
            return (new Integer(-1));
        }
        return Integer.parseInt(propertyValue);
    }
}
