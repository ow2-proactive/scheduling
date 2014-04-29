package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;
import org.ow2.proactive.scheduler.ext.mapreduce.util.StreamUtils;
import org.ow2.proactive.scheduler.task.SchedulerVars;


/**
 * MapperPATask is the Java Task the ProActive MapReduce framework executes
 * during the MapReduce ProActive Workflow execution. When implementing the
 * ProActive MapReduce framework with the goal to use the Hadoop MapReduce API,
 * the MapperPATask must executes the code of the Hadoop Mapper class. To do
 * this it must instantiate all the object needed for the execution of the
 * Hadoop Mapper code (e.g.: the org.apache.hadoop.Mapper.Context) object.
 *
 * The input parameters the MapperPATask receives in input are the following:
 * <ul>
 * <li>the information about if the logging is enabled or not. This this
 * information can be retrieved using the property
 * "PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.getKey()"</li>
 * <li>the configuration of the Hadoop MapReduce job. It is, actually, an
 * instance of the {@link PAHadoopJobConfiguration} class. It can be accessed
 * through the property
 * PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.getKey()</li>
 * <li>the information about which data space (INPUT, OUTPUT or LOCAL) this task
 * must use to read its input files. This information can be accessed through
 * the property
 * PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE
 * .getKey()</li>
 * <li>the information about the read mode this MapperPATask must use to read
 * its input data (REMOTE READ, LOCAL READ, PARTIAL LOCAL READ). This
 * information can be retrieved using the property
 * PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.getKey().
 * This information is used in the body of the
 * {@link MapperPATask#execute(TaskResult...)} method to, when the read mode is
 * PARTIAL_LOCAL_READ, transfer on the node the MapperPATask must execute on
 * only the part of the input file identified by the InputSplit the MapperPATask
 * must elaborate and not the whole file</li>
 * <li>the information about which data space (INPUT, OUTPUT or LOCAL) this task
 * must use to write to its output files. This information can be accessed
 * through the property
 * PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE
 * .getKey()</li>
 * </ul>
 *
 * We must notice that the InputSplit this MapperPATask must elaborate is
 * identified using the MapperPATask replication index (in fact that index is
 * different for each MapperPATask replica that has to be executed).
 *
 * We must notice that the MapperPATask does not need to receive in input the
 * name of the file to which the MapReduce operation must be apply, because the
 * name of that file is retrieved from the informations stored in the InputSplit
 * this MapperPATask must elaborate.
 *
 * We must notice that the MapperPATask does not need to receive as parameter
 * the number of ReducerPATask (and so the number of partitions) to execute
 * because that information is retrieved from the Hadoop Configuration this task
 * receives as parameter (the actual class of the Hadoop Configuration is
 * PAHadoopJobConfiguration). In particular, the number of ReducerPATask (or
 * number of partitions) is used inside a the RecordWriter used by the
 * MapperPATask.
 *
 * We must notice we cannot reuse Hadoop classes to write the MapperPATask
 * output data to files because the visibility of that classes is limited to the
 * Hadoop package they belong to, so that they are not visible outside. This
 * means most of the classes used by the MapperPATask to write output data to
 * files are copied and pasted from the Hadoop ones.
 *
 * Lastly, we must notice that when we say that the ProActive MapReduce
 * framework support the Hadoop InputFormat means that the MapperPATask use that
 * InputFormat to read its input. While say that the MapReduce framework support
 * the Hadoop OutputFormat does not mean that the MapperPATask will use that
 * OutputFormat to write output data into files. In fact the MapperPATask ( as
 * the Hadoop MapReduce framework ) use an internal ( this means not
 * customizable by the final user ) OutputFormat to write temporary data into
 * files. This means that for the writing of the temporary data we can choose if
 * we want to use the Hadoop classes or provide equivalent classes in ProActive
 *
 * @author The ProActive Team
 *
 */
public class MapperPATask extends JavaExecutable {

    protected static Logger logger = DefaultLogger.getInstance();
    protected ClassLoader classLoader = null;
    protected Mapper<WritableComparable, WritableComparable, WritableComparable, WritableComparable> hadoopMapper = null;
    protected InputFormat<WritableComparable, WritableComparable> hadoopInputFormat = null;
    protected Mapper.Context hadoopMapperContext = null;
    protected Configuration hadoopJobConfiguration = null;

    protected ReadMode readMode = null;
    protected DataSpacesFileObject inputDataSpace = null;
    protected DataSpacesFileObject outputDataSpace = null;

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        super.init(args);

        // initilize the class loader
        classLoader = Thread.currentThread().getContextClassLoader();

        // initialize the logger
        boolean debugLogLevel = Boolean.parseBoolean((String) (args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.getKey())));
        logger.setDebugLogLevel(debugLogLevel);
        boolean profileLogLevel = Boolean.parseBoolean((String) (args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.getKey())));
        logger.setProfileLogLevel(profileLogLevel);

        // org.apache.hadoop.conf.Configuration, it is received as parameter
        hadoopJobConfiguration = (PAHadoopJobConfiguration) args
                .get(PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.getKey());
        if (hadoopJobConfiguration != null) {
            logger.debug("The Hadoop Job Configuration IS NOT null");
        } else {
            logger.debug("The Hadoop Job Configuration IS null");
        }

        // identifies the input data space
        String inputDataSpaceString = (String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_INPUT_DATASPACE.getKey());
        logger.debug("The string that identifies the input space the MapperPATask must use is: " +
            inputDataSpaceString);
        if ((inputDataSpaceString != null) && (!(inputDataSpaceString.equalsIgnoreCase("")))) {
            if (inputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_DATASPACE
                            .getKey())) {
                inputDataSpace = getInputSpace();
                logger.debug("The input dataspace the MapperPATask must use is the INPUT dataspace");
            } else if (inputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE
                            .getKey())) {
                inputDataSpace = getOutputSpace();
                logger.debug("The input dataspace the MapperPATask must use is the OUTPUT dataspace");
            } else {
                inputDataSpace = getLocalSpace();
                logger.debug("The input dataspace the MapperPATask must use is the LOCAL dataspace");
            }
        } else {
            logger.debug("The user has not defined a valid input dataspace the MapperPATask can use");
        }

        // identifies the read mode
        readMode = ReadMode.valueOf((String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_READ_MODE.getKey()));
        logger.debug("The input mode is: " + readMode);

        // identifies the output dataspace
        String outputDataSpaceString = (String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_OUTPUT_DATASPACE.getKey());
        logger.debug("The string that identifies the output space the MapperPATask must use is: " +
            outputDataSpaceString);
        if ((outputDataSpaceString != null) && (!outputDataSpaceString.trim().equalsIgnoreCase(""))) {
            if (outputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE
                            .getKey())) {
                outputDataSpace = getOutputSpace();
                logger.debug("The output dataspace the MapperPATask must use is the OUTPUT dataspace");
            } else {
                outputDataSpace = getLocalSpace();
                logger.debug("The output dataspace the MapperPATask must use is the LOCAL dataspace");
            }
        }

        // instantiate the org.apache.hadoop.mapreduce.Mapper object
        hadoopMapper = (Mapper<WritableComparable, WritableComparable, WritableComparable, WritableComparable>) classLoader
                .loadClass((String) ((PAHadoopJobConfiguration) hadoopJobConfiguration).getMapperClassName())
                .getConstructor().newInstance();

        if (hadoopMapper != null) {
            logger.debug("The class of the Mapper to use inside the MapperPATask is: " +
                hadoopMapper.getClass().getName());
        } else {
            logger.debug("The Mapper to use inside MapperPATask is null.");
        }

        /*
         * We add the data space this MapperPATask must read from to the
         * FakeHadoopConfiguration instance to be able to instantiate the
         * PADataSpacesFileSystem instance
         */
        ((PAHadoopJobConfiguration) hadoopJobConfiguration).setDataSpacesFileObject(inputDataSpace);

        /*
         * instantiate the org.apache.hadoop.mapreduce.InputFormat object We
         * must notice that we retrieve the name of the class the ProActive
         * MapReduce job has to use as the InputFormat from the Hadoop
         * Configuration (that in the case of the ProAtive is, actually, an
         * instance of the PAHadoopConfiguration class).
         */
        hadoopInputFormat = (InputFormat<WritableComparable, WritableComparable>) classLoader.loadClass(
                (String) ((PAHadoopJobConfiguration) hadoopJobConfiguration).getInputFormatClassName())
                .getConstructor().newInstance();

        if (hadoopInputFormat != null) {
            logger.debug("The class of the InputFormat to use inside the MapperPATask is: " +
                hadoopInputFormat.getClass().getName());
        } else {
            logger.debug("The InputFormat to use inside MapperPATask is null.");
        }
    }

    @Override
    /**
     * The variable number of TaskResult in the MapperPATask consists in a java.util.List<org.apache.hadoop.mapreduce.InputSplit>.
     *  The MapperPATask is replicated. Each replica is identified by an identifier that can be
     *  accessed via the property "System.getProperty(SchedulerVars.JAVAENV_TASK_REPLICATION.toString())".
     *  Each replica choose the InputSplit it must elaborate using its own replication index.
     */
    public Serializable execute(TaskResult... taskResults) throws Throwable {
        /*
         * instantiate all the object we need to create an instance of the
         * Hadoop Mapper.Context. This means we have to retrieve/create the
         * following classes or instances that replace them: -
         * org.apache.hadoop.conf.Configuration, that represents the
         * configuration of the Hadoop MapReduce Job -
         * org.apache.hadoop.mapreduce.TaskAttemptID -
         * org.apache.hadoop.mapreduce.RecordReader -
         * org.apache.hadoop.mapreduce.RecordWriter -
         * org.apache.hadoop.mapreduce.OutputCommitter -
         * org.apache.hadoop.mapreduce.InputSplit while the
         * org.hadoop.mapreduce.StatusReporter could be null (see the
         * org.ow2.proactive
         * .scheduler.ext.hadoopMapReduce.FakeHadoopMapperContext class)
         */

        // org.apache.hadoop.mapreduce.TaskAttemptID
        int taskId = 0;
        int jobId = 0;
        String taskIdString = System.getProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME
                .toString());
        if ((taskIdString != null) && (!(taskIdString.equalsIgnoreCase("")))) {
            taskId = Integer.parseInt(taskIdString);
            logger.debug("The taskId of the MapperPATask is: " + taskId);
        } else {
            logger.debug("The taskId string is null or empty");
        }
        String jobIdString = System.getProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString());
        if ((jobIdString != null) && (!(jobIdString.equalsIgnoreCase("")))) {
            jobId = Integer.parseInt(jobIdString);
            logger.debug("The jobId of the MapperPATask is: " + jobId);
        } else {
            logger.debug("The jobId string is null or empty");
        }
        String jtIdentifier = "";
        JobID hadoopJobId = new JobID(jtIdentifier, jobId);
        boolean isMap = false;
        TaskID hadoopTaskId = new TaskID(hadoopJobId, isMap, taskId);
        int taskAttemptId = 0;
        TaskAttemptID hadoopTaskAttemptId = new TaskAttemptID(hadoopTaskId, taskAttemptId);

        /*
         * We store the replication index of the MapperPATask in the Hadoop
         * Configuration because that value is used to build the name of the
         * MapperPATask output file. The name of that file is compliant to the
         * following format: "intermediate_<replicationIndex>". The choice of
         * the replication index let us to use the string "intermediate_$REP"
         * when we must define the file selector for the MapperPATask output
         * files in the PAMapReduceJob (this means when we define which
         * MapperPATask output files must be transferred to the workflow OUTPUT
         * space). We can use the "$REP" because that string will be replaced by
         * the MapperPATask replication index when the path of the MapperPATask
         * output file must be resolved (see TaskLauncher.SchedulerVars). We
         * cannot use the "taskId" (that stores the value of the id of the
         * ProActive task) because we have not a string (equivalent to $REP)
         * that tells to the ProActive runtime to substitute it with the
         * MapperPATask id when the path of the MapperPATask output file must be
         * resolved.
         */
        int replicationIndex = Integer.parseInt(System.getProperty(SchedulerVars.JAVAENV_TASK_REPLICATION
                .toString()));
        hadoopJobConfiguration.set(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey(), "" +
            replicationIndex);

        // org.apache.hadoop.mapreduce.InputSplit
        InputSplit inputSplit = null;
        inputSplit = ((SerializableHadoopInputSplit) ((List<InputSplit>) taskResults[0].value())
                .get(replicationIndex)).getAdaptee();
        logger.debug("The MapperPATask selected InputSplit is TaskResult[" + replicationIndex + "]: " +
            inputSplit.toString());
        logger.debug("The class of the InputSplit the MapperPATask must elaborate is: " +
            inputSplit.getClass().getName());
        if (inputSplit instanceof FileSplit) {
            logger.debug("The name of the file the MapperPATask must elaborate is: " +
                ((FileSplit) inputSplit).getPath().toUri().toString());

            /*
             * TODO the transfer of input data to the local node has to work
             * also with non FileSplit input splits, but for now it works only
             * for FileSplit input splits
             */
            if (readMode.equals(ReadMode.partialLocalRead)) {

                /** @START_PROFILE */
                //logger.profile("Beginning of the [MAPPER_PARTIAL_FILE_TRANSFER_PHASE]");
                long partialLocalReadStartTime = System.currentTimeMillis();
                /** @END_PROFILE */

                /*
                 * We have to transfer only the part of the file referenced by
                 * the input split on the node on which this task is executing.
                 * The name of the transferred part is the same as the name of
                 * the input file to elaborate.
                 */
                DataSpacesFileObject inputFile = getInputFile(((FileSplit) inputSplit).getPath().toUri()
                        .toString());
                if ((inputFile.isReadable())) {
                    DataSpacesFileObject localFile = getLocalFile(((FileSplit) inputSplit).getPath().toUri()
                            .toString());
                    localFile.createFile();

                    logger.debug("The real URI of the local file to which the part of the " +
                        "input data corresponding to the InputSplit the MapperPATask" + "must elaborate is " +
                        localFile.getRealURI());
                    logger.debug("The virtual URI of the local file to which the part of the " +
                        "input data corresponding to the InputSplit the MapperPATask " +
                        "must elaborate is " + localFile.getVirtualURI());

                    InputStream is = inputFile.getContent().getInputStream();

                    /*
                     * The "InputStream.skip(...)" method called on a stream
                     * created from a DataSpacesFileObject living in the
                     * HTTP DataSpace. Hence it is better if we read the
                     * bytes of the InputStream to be skipped using the
                     * "InputStream.read(...)" method.
                     */
                    long bytesToSkip = ((FileSplit) inputSplit).getStart();
                    int bufferSize = 4096;
                    byte[] buffer = new byte[bufferSize];
                    long numberOfIterations = bytesToSkip / bufferSize;
                    long totalBytesRead = 0;
                    for (int i = 0; i < numberOfIterations; i++) {
                        totalBytesRead += is.read(buffer, 0, bufferSize);
                    }
                    totalBytesRead += is.read(buffer, 0, ((int) (bytesToSkip - totalBytesRead)));

                    // at this point we have skipped all the bytes to skip
                    logger.debug("The number of skipped bytes is: " + totalBytesRead);

                    OutputStream os = localFile.getContent().getOutputStream();
                    long numberOfCopiedBytes = StreamUtils.copy(is, os, inputSplit.getLength());
                    /*
                     * We have to set the offset, of the input split respect to
                     * the beginning of the new, "local", input file to zero
                     * because we will read bytes from a file located in the
                     * local data space of this task. The file has exactly the
                     * same size as the input split. The problem is that the
                     * Hadoop FileSplit class has no set methods. This means
                     * that the only way we have to change the values of its
                     * attribute is to create another instance of FileSplit.
                     */
                    inputSplit = new FileSplit(((FileSplit) inputSplit).getPath(), 0, numberOfCopiedBytes,
                        ((FileSplit) inputSplit).getLocations());
                    is.close();
                    os.close();
                    inputFile.close();
                    localFile.close();

                    logger
                            .debug("The number of bytes transferred from the inputDataSpace." +
                                ((FileSplit) inputSplit).getPath().toUri().toString() + " is: " +
                                numberOfCopiedBytes);
                    logger.debug("The size of the input split is: " + inputSplit.getLength());
                    logger.debug("The size of the local file that is created is " +
                        localFile.getContent().getSize());

                    /*
                     * We must notice that we have to change the
                     * DataSpacesFileObject the FileSystem, the Hadoop
                     * InputFormat will use, is built on.
                     */
                    inputDataSpace = getLocalSpace();
                    ((PAHadoopJobConfiguration) hadoopJobConfiguration)
                            .setDataSpacesFileObject(inputDataSpace);

                    logger.debug("The real URI of the local data space wrapped by the file " + "system is " +
                        inputDataSpace.getRealURI());
                    logger.debug("The virtual URI of the local data space wrapped by the " +
                        "file system is " + inputDataSpace.getVirtualURI());
                }

                /** @START_PROFILE */
                long partialLocalReadEndTime = System.currentTimeMillis();
                logger.profile("End of [MAPPER_PARTIAL_FILE_TRANSFER_PHASE]. It takes '" +
                    (partialLocalReadEndTime - partialLocalReadStartTime) + "' milliseconds");
                /** @END_PROFILE */
            }
        }

        // org.apache.hadoop.mapreduce.OutputCommitter
        OutputCommitter outputCommitter = new FakeHadoopOutputCommitter();

        /*
         * We must notice that we have
         * to create an instance of the FakeHadoopTaskAttemptContext because in
         * the RecordReader.initialize(InputSplit, TaskAttemptContext) we need a
         * Configuration instance to instantiate the FileSystem (while the
         * Hadoop TaskAttemptContext return a JobConf instance)
         */
        TaskAttemptContext taskAttemptContext = new FakeHadoopTaskAttemptContext(hadoopJobConfiguration,
            hadoopTaskAttemptId);
        logger
                .debug("The number of Reducer tasks retrieved using the \"FakeHadoopJobContext.getNumReduceTasks()\" method is: " +
                    taskAttemptContext.getNumReduceTasks());

        /*
         * org.apache.hadoop.mapreduce.RecordWriter Creating a RecordWriter that
         * will use the Hadoop-like logic in creating the intermediate files
         * (i.e., the MapperPATask output files)
         */
        FileSystem fileSystem = PAMapReduceFramework.getFileSystem(outputDataSpace);
        RecordWriter recordWriter = new MapperRecordWriter(taskAttemptContext, fileSystem);

        /*
         * org.apache.hadoop.mapreduce.RecordReader
         */
        RecordReader<?, ?> recordReader = hadoopInputFormat
                .createRecordReader(inputSplit, taskAttemptContext);
        recordReader.initialize(inputSplit, taskAttemptContext);
        logger
                .debug("The max length parameter is: " +
                    taskAttemptContext
                            .getConfiguration()
                            .getInt(
                                    PAMapReduceFrameworkProperties
                                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_JOB_LINE_RECORD_READER_MAX_LENGTH
                                                    .getKey()), Integer.MAX_VALUE));

        logger.debug("The class to use as the type of the output key for the mapper is: " +
            taskAttemptContext.getMapOutputKeyClass());
        logger.debug("The class to use as the type of the output value for the mapper is: " +
            taskAttemptContext.getMapOutputValueClass());

        // now we have all the instances of the classes we need to create the
        // Hadoop Mapper.Context instance
        hadoopMapperContext = new FakeHadoopMapperContext(hadoopMapper, hadoopJobConfiguration,
            hadoopTaskAttemptId, recordReader, recordWriter, outputCommitter, inputSplit);

        /** @START_PROFILE */
        //logger.profile("Start of the [MAPPER_HADOOP_PHASE]");
        long hadoopMapperStartTime = System.currentTimeMillis();
        /** @END_PROFILE */

        hadoopMapper.run(hadoopMapperContext);

        /** @START_PROFILE */
        long hadoopMapperEndTime = System.currentTimeMillis();
        logger.profile("End of the [MAPPER_HADOOP_PHASE]. It takes '" +
            (hadoopMapperEndTime - hadoopMapperStartTime) + "' milliseconds");
        /** @END_PROFILE */

        logger.debug("End of the execution of the Hadoop Mapper.");

        recordReader.close();
        recordWriter.close(taskAttemptContext);
        return null;
    }
}