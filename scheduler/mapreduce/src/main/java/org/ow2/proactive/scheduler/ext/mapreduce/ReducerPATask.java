package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskID;
import org.objectweb.proactive.extensions.dataspaces.api.Capability;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;
import org.ow2.proactive.scheduler.task.SchedulerVars;


/**
 * ReducerPATask is the Java Task the ProActive MapReduce framework executes
 * during the "reducer" phase of the MapReduce ProActive Workflow execution.
 * When implementing the ProActive MapReduce framework with the goal to use the
 * Hadoop MapReduce API, the MapperPATask must executes the code of the Hadoop
 * Reducer class. To do that it must instantiate all the object needed for the
 * execution of the Hadoop Reducer code (e.g.: the
 * org.apache.hadoop.Reducer.Context) object.
 *
 * The input parameters the ReducerPATask receives in input are the following:
 * <ul>
 * <li>the information about if the logging is enabled or not. This this
 * information can be retrieved using the property
 * "PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.getKey()"</li>
 * <li>the configuration of the Hadoop MapReduce job. It is, actually, an
 * instance of the {@link PAHadoopJobConfiguration} class. It can be accessed
 * through the property
 * PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.getKey()</li>
 * <li>the information about the "readMode": if "fullLocalRead" or "remoteRead"</li>
 * <li>the information about which data space (INPUT, OUTPUT or LOCAL) this task
 * must use to read its input files. This information can be accessed through
 * the property
 * PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE
 * .getKey()</li>
 * <li>te information about which data space (OUTPUT or LOCAL) this task must
 * use to read its input files. This information can be accessed through the
 * property
 * PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE
 * .getKey()</li>
 * </ul>
 *
 * We must notice that all the Hadoop classes needed to execute this
 * ReducerPATask are retrieved from the Hadoop Configuration instance this
 * ReducerPATask receives.
 *
 * We must notice that we test if the input data space is randomly accessible or
 * not. Then if it is randomly accessible we transfer only the input data (of
 * the output file of the different MapperPATask) the ReducerPATask need on the
 * node it will be executed on, otherwise we transfer the whole MapperPATaks
 * output files but then the ReducerPATask will elaborate only the data it must
 * elaborate. TODO this behavior is a little bit different from the one of the
 * MapperPATask in which is the user that defines the way the task must read its
 * input data. Hence may be also on the ReducerPATask side we can let the user
 * to define the way the ReducerPATask read its input data.
 *
 * @author The ProActive Team
 *
 */

public class ReducerPATask extends org.ow2.proactive.scheduler.common.task.executable.JavaExecutable {

    protected static Logger logger = DefaultLogger.getInstance();
    protected ClassLoader classLoader = null;
    protected Reducer<WritableComparable, WritableComparable, WritableComparable, WritableComparable> hadoopReducer = null;
    protected OutputFormat<WritableComparable, WritableComparable> hadoopOutputFormat = null;
    protected Reducer.Context hadoopReducerContext = null;
    protected Configuration hadoopJobConfiguration = null;

    protected ReadMode readMode = null;
    protected DataSpacesFileObject inputDataSpace = null;
    protected DataSpacesFileObject outputDataSpace = null;

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        super.init(args);

        // initialize the class loader
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

        // identify the "readMode"
        readMode = ReadMode.valueOf((String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_READ_MODE.getKey()));
        logger.debug("The input mode is: " + readMode);

        // identify the dataspace this ReducerPATask must use to read files from
        String inputDataSpaceString = (String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_INPUT_DATASPACE.getKey());
        logger.debug("The string that identifies the input space the ReducerPATask must use is: " +
            inputDataSpaceString);
        if ((inputDataSpaceString != null) && (!(inputDataSpaceString.trim().equalsIgnoreCase("")))) {
            if (inputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_DATASPACE
                            .getKey())) {
                inputDataSpace = getInputSpace();
                logger.debug("The input dataspace is the ReducerPATask must use is the INPUT dataspace");
            } else if (inputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE
                            .getKey())) {
                inputDataSpace = getOutputSpace();
                logger.debug("The input dataspace the ReducerPATask must use is the OUTPUT dataspace");
            } else {
                inputDataSpace = getLocalSpace();
                logger.debug("The input dataspace the ReducerPATask must use is the LOCAL dataspace");
            }
        } else {
            logger.debug("The user has not defined a valid input dataspace the ReducerPATask can use");
        }

        // identifies the output dataspace, see the HadoopMapReduceApplication
        // for more informations
        String outputDataSpaceString = (String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_OUTPUT_DATASPACE.getKey());
        logger.debug("The string that identifies the output dataspace the ReducerPATask must use is: " +
            outputDataSpaceString);
        if ((outputDataSpaceString != null) && (!outputDataSpaceString.equalsIgnoreCase(""))) {
            if (outputDataSpaceString
                    .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_OUTPUT_DATASPACE
                            .getKey())) {
                outputDataSpace = getOutputSpace();
                logger.debug("The output dataspace the ReducerPATask must use is the OUTPUT dataspace");
            } else {
                outputDataSpace = getLocalSpace();
                logger.debug("The output dataspace the ReducerPATask must use is the LOCAL dataspace");
            }
        }

        /*
         * We add the data space this ReducerPATask must read from to the
         * FakeHadoopConfiguration instance to be able to instantiate the
         * PADataSpacesFileSystem instance
         */
        ((PAHadoopJobConfiguration) hadoopJobConfiguration).setDataSpacesFileObject(outputDataSpace);

        // instantiate the org.apache.hadoop.mapreduce.Reducer object
        hadoopReducer = (Reducer<WritableComparable, WritableComparable, WritableComparable, WritableComparable>) classLoader
                .loadClass((String) ((PAHadoopJobConfiguration) hadoopJobConfiguration).getReducerClassName())
                .getConstructor().newInstance();
        if (hadoopReducer != null) {
            logger.debug("The class of the Redcuer to use inside the ReducerPATask is: " +
                hadoopReducer.getClass().getName());
        } else {
            logger.debug("The Reducer to use inside ReducerPATask is null.");
        }

        // intantiate the org.apache.hadoop.mapreduce.OutputFormat object
        hadoopOutputFormat = (OutputFormat<WritableComparable, WritableComparable>) classLoader.loadClass(
                (String) ((PAHadoopJobConfiguration) hadoopJobConfiguration).getOutputFormatClassName())
                .getConstructor().newInstance();
        if (hadoopOutputFormat != null) {
            logger.debug("The class of the OutputFormat to use inside the ReducerPATask is: " +
                hadoopOutputFormat.getClass().getName());
        } else {
            logger.debug("The OutputFormat to use inside ReducerPATask is null.");
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        /*
         * we must instantiate the "org.apache.hadoop.mapreduce.Reducer.Context"
         * to be able to run an Hadoop Reducer. To do so we need the following
         * instances: org.apache.hadoop.mapreduce.Reducer -
         * org.apache.hadoop.conf.Configuration -
         * org.apache.hadoop.mapreduce.TaskAttemptID -
         * org.apache.hadoop.mapred.RawKeyValueIterator -
         * org.apache.hadoop.mapreduce.Counter: inputKeyCounter -
         * org.apache.hadoop.mapreduce.Counter: inputValueCounter -
         * org.apache.hadoop.mapreduce.RecordWriter: output -
         * org.apache.hadoop.mapreduce.OutputCommitter: committer -
         * org.apache.hadoop.mapreduce.StatusReporter: reporter -
         * org.apache.hadoop.io.RawComparator: comparator -
         * java.lang.Class<INKEY>: keyClass - java.lang.Class<INVALUE>:
         * valueClass
         */

        /*
         * org.apache.hadoop.mapreduce.TaskAttemptID The informations we store
         * in the TaskAttempdID ( and in the other objects we need to build it:
         * TaskID and JobID) defines the directory and the name of the file in
         * which this ReducerPATask will write its output. The name of the
         * directory will be something like:
         * $PA_DATASPACE/$USER_DEFINED_OUTPUT_DIRECTORY
         * /_temporary/_attempt_<jtIdentifier
         * >_<jobId>_r_<taskId>_<taskAttemptId> where the "r" indicates that the
         * task that produced the output is a "reducer". There is only one
         * problem: - for the moment the "jtIdentifier" string that in Hadoop is
         * used to identify the JobTraker, in our case is empty. Moreover in
         * ProActive we have not a TaskAttemptId, so we set the value of the
         * TaskAttemptId to '0'.
         * We could use the "jtIdentifier" to identify the ProActive Scheduler
         * that submitted (a string representation of "rmi://segfault.inria.fr:55855/"
         * without the characters that cannot be used in the name of directories and
         * files).
         * The name of the output file stored in that directory will be something
         * like "part-r-<taskId>".
         * To understand how the name of the output directory and output file are
         * build look at "org.apache.hadoop.mapreduce.lib.output.FileOutputFormat"
         *
         * We must notice we cannot define the name we want for the directory
         * and the file this ReducerPATask will write its output in the case we
         * re-use the Hadoop OutputFormat and OutputCommitter.
         */
        int taskId = 0;
        int jobId = 0;
        String taskIdString = System.getProperty(SchedulerVars.JAVAENV_TASK_ID_VARNAME
                .toString());
        if ((taskIdString != null) && (!(taskIdString.equalsIgnoreCase("")))) {
            taskId = Integer.parseInt(taskIdString);
            logger.debug("The taskId of the ReducerPATask is: " + taskId);
        } else {
            logger.debug("The taskId string is null or empty");
        }
        String jobIdString = System.getProperty(SchedulerVars.JAVAENV_JOB_ID_VARNAME.toString());
        if ((jobIdString != null) && (!(jobIdString.equalsIgnoreCase("")))) {
            jobId = Integer.parseInt(jobIdString);
            logger.debug("The jobId of the ReducerPATask is: " + jobId);
        } else {
            logger.debug("The jobId string is null or empty");
        }
        String jtIdentifier = "";
        JobID hadoopJobId = new JobID(jtIdentifier, jobId);
        boolean isMap = false;
        TaskID hadoopTaskId = new TaskID(hadoopJobId, isMap, taskId);
        int taskAttemptId = 0;
        TaskAttemptID hadoopTaskAttemptId = new TaskAttemptID(hadoopTaskId, taskAttemptId);

        // org,apacge .hadoop.mapreduce.Counter (inputKeyCounter and
        // inputValueCounter)
        Counter fakeHadoopInputKeyCounter = new FakeHadoopCounter(
            PAMapReduceFrameworkProperties
                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_KEY_COUNTER_NAME
                            .getKey()),
            PAMapReduceFrameworkProperties
                    .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_KEY_COUNTER_DISPLAY_NAME
                            .getKey()));
        Counter fakeHadoopInputValueCounter = new FakeHadoopCounter(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_VALUE_COUNTER_NAME
                        .getKey()), PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_INPUT_VALUE_COUNTER_DISPLAY_NAME
                        .getKey()));

        // org.apache.hadoop.mapreduce.RecordWriter
        TaskAttemptContext fakeTaskAttemptContext = new FakeHadoopTaskAttemptContext(hadoopJobConfiguration,
            hadoopTaskAttemptId);
        RecordWriter<?, ?> recordWriter = hadoopOutputFormat.getRecordWriter(fakeTaskAttemptContext);

        logger.debug("The class to use to print key-value pairs is: " + recordWriter.getClass().getName());

        // org.apache.hadoop.mapreduce.StatusReporter
        StatusReporter fakeHadoopStatusReporter = new FakeHadoopStatusReporter();

        // org.apache.hadoop.mapreduce.OutputCommitter
        OutputCommitter fakeHadoopOutputCommitter = hadoopOutputFormat
                .getOutputCommitter(fakeTaskAttemptContext);

        // org.apache.hadoop.io.RawComparator
        RawComparator rawComparator = ((PAHadoopJobConfiguration) hadoopJobConfiguration)
                .getOutputKeyComparator();
        logger.debug("The RawComparator class is: " + rawComparator.getClass().getName());

        // Hadoop mapOutputKeyClass and mapOutputValueClass
        Class<?> mapOutputKeyClass = ((PAHadoopJobConfiguration) hadoopJobConfiguration)
                .getMapperOutputKeyClass();
        Class<?> mapOutputValueClass = ((PAHadoopJobConfiguration) hadoopJobConfiguration)
                .getMapperOutputValueClass();
        logger.debug("The class of the key to use in the framework is: " + mapOutputKeyClass.getName());
        logger.debug("The class of the key to use in the framework is: " + mapOutputValueClass.getName());

        RawKeyValueIterator rawKeyValueIterator = null;

        /*
         * In the version of the ProActive MapReduce framework that uses the
         * Hadoop logic to create the intermediate files the ReducerPATask
         * receives in input all the output index files of the MapperPATask
         * tasks. From that files the ReducerPATask selects the IndexRecord
         * corresponding to the partition it has to elaborate. For each not
         * empty IndexRecord it selects the corresponding MapperPATask output
         * file and if the data space the ReducerPATask has to use as input is
         * randomly accessible in reading, only the segment identified by the
         * IndexRecord is transferred to the local data space of the
         * ReducerPATask; otherwise all the data in the MapperPATask output file
         * are transferred. The transferred data will be stored in files whose
         * names will be the same as the original files:
         * "intermediate_<mapperId>.out". Just after the file is created the
         * Path to that file will be added to the list of the files to merge. To
         * do that, first of all we must list the data space this ReducerPATask
         * use as input data space to retrieve all the
         * "intermediate_<mapperId>.index" files. Then for each file we must
         * retrieve the IndexRecord that identifies the data contained in that
         * file the ReducerPATAsk must elaborate. Lastly we choose to transfer
         * only the data the ReducerPATask must elaborate or the whole file (
         * and then to create another file containing only the data the
         * ReducerPATask must elaborate ). We must notice that we need another
         * FileSystem instance, created wrapping the LOCAL DataSpacesFileObject
         * instance. In fact we need to write in files that live in that
         * FileSystem. This means also that we must modify the constructor of
         * the SpillRecord class to have a constructor that accepts a FileSystem
         * instance that must be used to resolve the file names.
         */

        /*
         * The MapperPATask index output files are always transferred on the
         * node the ReducerPATask executes on. This means those "index" files
         * are stored, always, in the ReducerPATask local space. While the
         * MapperPATask "actual output" files can be stored in the ProActive
         * MapReduce API/framework output space or in the ReducerPATask local
         * space depending on the value of the ReducerPATask "readMode":
         * respectively, if it is equal to "remoteRead" or to "fullLocalRead".
         * Hence, we can have two possible scenarios: the "index" files are in
         * the ReducerPATaks local space and the "actual output" files are in
         * the ProActive MapReduce API/framework output space or either the
         * "index" files either the "actual output" are in the ReducerPATask
         * local space. In the first scenarios we must access the "index" file,
         * recover the IndexRecord that identifies the data, of the
         * "actual output" file corresponding to the considered "index" file,
         * that the ReducerPATask must elaborate. Then we must transfer in the
         * local space of the ReducerPATask only the data the ReducerPATask must
         * elaborate or the whole file (that will be transferred in a temporary
         * file to successively create a new file, whose name will be the same as the
         * original file, that will contain only the data the ReducerPATask must
         * elaborate) depending on the fact that the output space (that store
         * the "actual output" file) is, respectively, random accessible or not.
         * We can delete the temporary files created in the ReducerPATask local
         * space. In the second scenarios the "actual data" are already in the
         * ReducerPATask local space so we must create temporary files that will
         * contain only the data the ReducerPATask must elaborate and then we
         * must rename this temporary files in such a way their new name is
         * equal to the name of the original "actual data" file. We can delete
         * the temporary files created in the ReducerPATask local space. To
         * select between the two scenarios we check the value of the argument
         * that represents the "readMode" of the ReducerPATask.
         */
        String indexFileNamePrefix = PAMapReduceFramework.MAPPER_INTERMEDIATE_FILE_NAME_PREFIX +
            PAMapReduceFramework.FILE_NAME_CONCATENATOR;
        String indexFileNameSuffix = PAMapReduceFramework.FILE_EXTENSION_SEPARATOR +
            PAMapReduceFramework.SPILL_INDEX_SUFFIX;
        String regexPart = "([^\\.]*)\\";
        // indexFileNameRegex will be equal to "intermediate_([^\\.]*)\\.index"
        String indexFileNameRegex = indexFileNamePrefix + regexPart + indexFileNameSuffix;

        FileSystem inputFileSystem = PAMapReduceFramework.getFileSystem(inputDataSpace);
        FileSystem localFileSystem = PAMapReduceFramework.getFileSystem(getLocalSpace());
        logger.debug("inputFileSystem=" + inputFileSystem.getWorkingDirectory());
        logger.debug("localFileSystem=" + localFileSystem.getWorkingDirectory());

        /*
         * we must notice "new Path( File.separator )" forces the list of the
         * input file system root directory
         */
        PathFilter indexFilePathFilter = new IndexFilePathFilter(indexFileNameRegex);
        FileStatus[] fileStatusArray = localFileSystem.listStatus(new Path(File.separator),
                indexFilePathFilter);
        logger.debug("The number of files selected by the IndexFilePathFilter is '" + fileStatusArray.length +
            "'");

        List<String> mapperIdCapturingGroupList = ((IndexFilePathFilter) indexFilePathFilter)
                .getCapturingGroupList();

        if (fileStatusArray.length > 0) {

            logger.debug("The number of captured mappers' id is '" + mapperIdCapturingGroupList.size() + "'");

            Path[] pathArray = new Path[fileStatusArray.length];

            if (readMode.equals(ReadMode.remoteRead)) {

                /** @START_PROFILE */
                //logger.profile("Start of [REDUCER_REMOTE_READING_PHASE]");
                long reducerPATaskRemoteReadingStartTime = System.currentTimeMillis();
                long reducerPATaskTotalReadingTime = 0;
                /** @END_PROFILE */

                boolean inputDataSpaceHasRandomAccessRead = false;
                inputDataSpaceHasRandomAccessRead = inputDataSpace
                        .hasSpaceCapability(Capability.RANDOM_ACCESS_READ);
                logger.debug("Input space is randomly accessible? " + inputDataSpaceHasRandomAccessRead);

                Path currentIndexFilePath = null;
                String currentMapperIdString = null;
                String currentFileName = null;
                Path currentFilePath = null;
                FSDataInputStream fsDataInputStream = null;
                FSDataOutputStream fsDataOutputStream = null;
                IndexRecord currentIndexRecord = null;
                // TODO retrieve the size to use for the buffer from the
                // configuration
                int bufferSize = 4096;
                long bytesCopied = 0;
                long bytesToCopy = 0;
                String temporaryFileName = null;
                Path temporaryFilePath = null;
                FSDataOutputStream fsTemporaryDataOutputStream = null;

                for (int i = 0; i < fileStatusArray.length; i++) {
                    currentIndexFilePath = fileStatusArray[i].getPath();
                    /*
                     * We must notice that the capturing group list of the
                     * mapper identifiers has the same size as the array of
                     * FileStatus. Moreover given the FileStatus in position "i"
                     * the capturing group list of mapper identifiers at the
                     * same index contains the "mapperId" value that is
                     * contained in the name of the file that corresponds to the
                     * considered FileStatus. This is valid only if the
                     * FileStatus array is not altered (ordered etc...).
                     */
                    currentMapperIdString = mapperIdCapturingGroupList.get(i);
                    currentFileName = indexFileNamePrefix + currentMapperIdString +
                        PAMapReduceFramework.FILE_EXTENSION_SEPARATOR + PAMapReduceFramework.SPILL_SUFFIX;
                    currentFilePath = new Path(currentFileName);

                    logger.debug("Iteration '" + i + "': mapperIdString=" + currentMapperIdString +
                        ", indexFileName=" + currentIndexFilePath.toUri().toString() + ", fileName=" +
                        currentFileName);
                    logger.debug("Iteration '" + i + "': filePath.toUri().toString()=" +
                        currentFilePath.toUri().toString());

                    /*
                     * The following code is correct because when the "readMode"
                     * of the ReducerPATask is equal to
                     * PAMapReduceFramework.READ_MODE_REMOTE_READ the space the
                     * ReducerPATask uses as input space is different from the
                     * space it uses as its local space. This means the two
                     * following streams are created on files with the same name
                     * but in different file systems.
                     */
                    fsDataInputStream = inputFileSystem.open(currentFilePath);
                    logger.debug("The FSDataInputStream is " + fsDataInputStream);
                    fsDataOutputStream = localFileSystem.create(currentFilePath);
                    logger.debug("The FSDataOutputStream is " + fsDataOutputStream);

                    if (!inputDataSpaceHasRandomAccessRead) {
                        /*
                         * If the input data space is not randomly accessible in
                         * reading we must first copy the whole
                         * MapperPATask tasks output files into temporary
                         * files in the LOCAL data space of the ReducerPATask.
                         * Then, using the IndexRecord, we copy in the final
                         * output files (that will be the input files for the
                         * ReducerPATask) only the data belonging to the
                         * partition the ReducerPATask must elaborate. The name
                         * of the final output files will be the same of the
                         * MapperPATask output files. While the name of the
                         * temporary file will follow the format
                         * "intermediate_<mapperId>.out.temporary" (where
                         * temporary will indicate that the file was a temporary
                         * one)
                         */

                        /** @START_PROFILE */
                        long reducerPATaskRemoteNotRandomAccessReadingStartTime = System.currentTimeMillis();
                        //logger.profile("Start of [REDUCER_REMOTE_NOT_RANDOM_ACCESS_READING_PHASE].");
                        /** @END_PROFILE */

                        temporaryFileName = currentFileName + PAMapReduceFramework.FILE_EXTENSION_SEPARATOR +
                            PAMapReduceFramework.TEMPORARY;
                        temporaryFilePath = new Path(temporaryFileName);
                        fsTemporaryDataOutputStream = localFileSystem.create(temporaryFilePath);
                        bytesToCopy = inputFileSystem.getFileStatus(currentFilePath).getLen();
                        bytesCopied = copyFiles(fsDataInputStream, fsTemporaryDataOutputStream, 0,
                                bytesToCopy, bufferSize);
                        if (bytesCopied == bytesToCopy) {
                            logger.debug("The data from the file '" + currentFileName +
                                "' are copied correctly");
                        } else {
                            logger.debug("The number of bytes to copy from the file '" + currentFileName +
                                "' were " + bytesToCopy + " while the number of bytes copied is " +
                                bytesCopied);
                        }

                        fsDataInputStream = localFileSystem.open(temporaryFilePath);
                        logger.debug("The FSDataInputStream is " + fsDataInputStream);

                        /** @START_PROFILE */
                        long reducerPATaskRemoteNotRandomAccessReadingEndTime = System.currentTimeMillis();
                        reducerPATaskTotalReadingTime += reducerPATaskRemoteNotRandomAccessReadingEndTime -
                            reducerPATaskRemoteNotRandomAccessReadingStartTime;
                        logger
                                .profile("End of [REDUCER_REMOTE_NOT_RANDOM_ACCESS_READING_PHASE]. It takes '" +
                                    (reducerPATaskRemoteNotRandomAccessReadingEndTime - reducerPATaskRemoteNotRandomAccessReadingStartTime) +
                                    "' milliseconds");
                        /** @END_PROFILE */
                    }

                    /*
                     * As we said the "index" files are always in the
                     * ReducerPATask local space (so in the local file system)
                     */
                    currentIndexRecord = new SpillRecord(currentIndexFilePath, localFileSystem)
                            .getIndex(Integer.parseInt(System
                                    .getProperty(SchedulerVars.JAVAENV_TASK_REPLICATION
                                            .toString())));

                    logger.debug("Iteration '" + i + "' currentIndexRecord.startOffset=" +
                        currentIndexRecord.startOffset + ", currentIndexRecord.rawLength=" +
                        currentIndexRecord.rawLength + ", currentIndexRecord.partLength=" +
                        currentIndexRecord.partLength);
                    bytesToCopy = currentIndexRecord.partLength;
                    logger.debug("Iteration '" + i + "': bytesToCopy=" + bytesToCopy);

                    bytesCopied = copyFiles(fsDataInputStream, fsDataOutputStream,
                            currentIndexRecord.startOffset, currentIndexRecord.partLength, bufferSize);

                    if (bytesCopied == bytesToCopy) {
                        logger.debug("The data from the file '" + currentFileName + "' are copied correctly");
                    } else {
                        logger.debug("The number of bytes to copy from the file '" + currentFileName +
                            "' were " + bytesToCopy + " while the number of bytes copied is " + bytesCopied);
                    }

                    // add the file to the list of files to merge
                    pathArray[i] = currentFilePath;

                    if (!inputDataSpaceHasRandomAccessRead) {
                        /*
                         * this means we have created a temporary file in the
                         * ReducerPATask local space, so we delete it
                         */
                        localFileSystem.delete(temporaryFilePath, true);
                    }
                }

                /** @START_PROFILE */
                long reducerPATaskRemoteReadingEndTime = System.currentTimeMillis();
                reducerPATaskTotalReadingTime += reducerPATaskRemoteReadingEndTime -
                    reducerPATaskRemoteReadingStartTime;
                logger.profile("End of [REDUCER_REMOTE_READING_PHASE]. It takes '" +
                    reducerPATaskTotalReadingTime + "' milliseconds");
                /** @END_PROFILE */

            } else if (readMode.equals(ReadMode.fullLocalRead)) {

                /** @START_PROFILE */
                //logger.profile("Start of [REDUCER_FULL_LOCAL_READING_PHASE]");
                long reducerPATaskFullLocalReadingStartTime = System.currentTimeMillis();
                /** @END_PROFILE */

                /*
                 * In this case the MapperPATask "actual output" files are already in the
                 * ReducerPATask local space. Hence, we create temporary files
                 * in which we copy only the data the ReducerPATask must
                 * elaborate and then we rename those files in such a way their
                 * names will be equal to the name of the original
                 * "actual output" files.
                 */
                Path currentIndexFilePath = null;
                String currentMapperIdString = null;
                String currentFileName = null;
                Path currentFilePath = null;
                String temporaryFileName = null;
                Path temporaryFilePath = null;
                IndexRecord currentIndexRecord = null;
                FSDataInputStream fsDataInputStream = null;
                FSDataOutputStream fsDataOutputStream = null;
                // TODO retrieve the size to use for the buffer from the
                // configuration
                int bufferSize = 4096;
                long bytesCopied = 0;
                long bytesToCopy = 0;

                for (int i = 0; i < fileStatusArray.length; i++) {
                    currentIndexFilePath = fileStatusArray[i].getPath();
                    /*
                     * We must notice that the capturing group list of the
                     * mapper identifiers has the same size as the array of
                     * FileStatus. Moreover given the FileStatus in position "i"
                     * the capturing group list of mapper identifiers at the
                     * same index contains the "mapperId" value that is
                     * contained in the name of the file that corresponds to the
                     * considered FileStatus. This is valid only if the
                     * FileStatus array is not altered (ordered etc...).
                     */
                    currentMapperIdString = mapperIdCapturingGroupList.get(i);
                    currentFileName = indexFileNamePrefix + currentMapperIdString +
                        PAMapReduceFramework.FILE_EXTENSION_SEPARATOR + PAMapReduceFramework.SPILL_SUFFIX;
                    currentFilePath = new Path(currentFileName);
                    temporaryFileName = currentFileName + PAMapReduceFramework.FILE_EXTENSION_SEPARATOR +
                        PAMapReduceFramework.TEMPORARY;
                    temporaryFilePath = new Path(temporaryFileName);

                    logger.debug("Iteration '" + i + "': mapperIdString=" + currentMapperIdString +
                        ", indexFileName=" + currentIndexFilePath.toUri().toString() + ", fileName=" +
                        currentFileName);
                    logger.debug("Iteration '" + i + "': filePath.toUri().toString()=" +
                        currentFilePath.toUri().toString());
                    logger.debug("Iteration '" + i + "': temporaryFileName=" + temporaryFileName);
                    logger.debug("Iteration '" + i + "': temporaryFilePath.toUri().toString()=" +
                        temporaryFilePath.toUri().toString());

                    /*
                     * copy a part of "currentFilePath" into "temporaryFilePath"
                     * We must notice that in the case the "readMode" for the
                     * ReducerPATask is equal to the
                     * PAMapReduceFramework.READ_MODE_FULL_LOCAL_READ the space
                     * the ReducerPATask uses as its input space and the space
                     * the ReducerPATask uses as its local space are the same
                     */
                    fsDataInputStream = localFileSystem.open(currentFilePath);
                    logger.debug("The FSDataInputStream is " + fsDataInputStream);

                    fsDataOutputStream = localFileSystem.create(temporaryFilePath);
                    logger.debug("The FSDataOutputStream is " + fsDataOutputStream);

                    currentIndexRecord = new SpillRecord(currentIndexFilePath, localFileSystem)
                            .getIndex(Integer.parseInt(System
                                    .getProperty(SchedulerVars.JAVAENV_TASK_REPLICATION
                                            .toString())));

                    logger.debug("Iteration '" + i + "' currentIndexRecord.startOffset=" +
                        currentIndexRecord.startOffset + ", currentIndexRecord.rawLength=" +
                        currentIndexRecord.rawLength + ", currentIndexRecord.partLength=" +
                        currentIndexRecord.partLength);
                    bytesToCopy = currentIndexRecord.partLength;
                    logger.debug("Iteration '" + i + "': bytesToCopy=" + bytesToCopy);

                    bytesCopied = copyFiles(fsDataInputStream, fsDataOutputStream,
                            currentIndexRecord.startOffset, currentIndexRecord.partLength, bufferSize);

                    if (bytesCopied == bytesToCopy) {
                        logger.debug("The data from the file '" + currentFileName + "' are copied correctly");
                    } else {
                        logger.debug("The number of bytes to copy from the file '" + currentFileName +
                            "' were " + bytesToCopy + " while the number of bytes copied is " + bytesCopied);
                    }

                    /*
                     * rename "temporaryFilePath" into "currentFilePath" and
                     * delete the "temporaryFilePath". But we must notice we do
                     * not need to invoke
                     * "localFileSystem.delete(temporaryFilePath, true);"
                     * because the method
                     * "FileSystem.moveFromLocalFile(Path src, Path dst)" by
                     * default delete the source ("src") path
                     */
                    localFileSystem.moveFromLocalFile(temporaryFilePath, currentFilePath);

                    // add the file to the list of files to merge
                    pathArray[i] = currentFilePath;
                }

                /** @START_PROFILE */
                long reducerPATaskFullLocalReadingEndTime = System.currentTimeMillis();
                logger.profile("End of [REDUCER_FULL_LOCAL_READING_PHASE]. It takes '" +
                    (reducerPATaskFullLocalReadingEndTime - reducerPATaskFullLocalReadingStartTime) +
                    "' milliseconds");
                /** @END_PROFILE */

            } else {
                throw new Exception("Unknwown 'readMode' '" + readMode + "'");
            }

            // do the merge
            if (pathArray.length > 0) {
                int mergeFactor = hadoopJobConfiguration
                        .getInt(
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_FACTOR_PROPERTY_NAME
                                                .getKey()),
                                PAMapReduceFrameworkProperties
                                        .getPropertyAsInteger(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_FACTOR_DEFAULT_VALUE
                                                .getKey()));

                /** @START_PROFILE */
                //logger.profile("Start of the [REDUCER_MERGING_PHASE]");
                long reducerMergeStartTime = System.currentTimeMillis();
                /** @END_PROFILE */

                rawKeyValueIterator = org.ow2.proactive.scheduler.ext.mapreduce.Merger.merge(
                        hadoopJobConfiguration, localFileSystem, mapOutputKeyClass, mapOutputValueClass,
                        pathArray, true, mergeFactor, null, rawComparator);

                /** @START_PROFILE */
                long reducerMergeEndTime = System.currentTimeMillis();
                logger.profile("End of the [REDUCER_MERGING_PHASE]. It takes '" +
                    (reducerMergeEndTime - reducerMergeStartTime) + "' milliseconds");
                /** @END_PROFILE */
            }
        }

        /*
         * now we have all the instances of the classes we need to create the
         * Hadoop Reducer.Context instance
         */
        hadoopReducerContext = new FakeHadoopReducerContext(hadoopReducer, hadoopJobConfiguration,
            hadoopTaskAttemptId, rawKeyValueIterator, fakeHadoopInputKeyCounter, fakeHadoopInputValueCounter,
            recordWriter, fakeHadoopOutputCommitter, fakeHadoopStatusReporter, rawComparator,
            mapOutputKeyClass, mapOutputValueClass);

        /** @START_PROFILE */
        //logger.profile("Start of the [REDUCER_HADOOP_PHASE]");
        long hadoopReducerStartTime = System.currentTimeMillis();
        /** @END_PROFILE */

        hadoopReducer.run(hadoopReducerContext);

        /** @START_PROFILE */
        long hadoopReducerEndTime = System.currentTimeMillis();
        logger.profile("End of the [REDUCER_HADOOP_PHASE]. It takes '" +
            (hadoopReducerEndTime - hadoopReducerStartTime) + "' milliseconds");
        /** @END_PROFILE */

        recordWriter.close(fakeTaskAttemptContext);

        /*
         * TODO we must call the OutputCommitter.commitTask(TaskAttemptContext)
         * but we must notice that in that method Hadoop use full uris
         * ("pads:///filePath") and the PADataFileSystem only accepts uris
         * realtive to the DataSpace ("filePath"). We must also notice that the
         * invocation of that method is not necessary because to put the
         * temporary output to their final destination we can build an
         * appropriate selection string. This string will select all files in
         * the subdirectories
         * $OUTPUT_DIRECTORY/_temporary/_attempt_<jtIdentifier
         * >_<jobId>_r_<taskId>_<taskAttemptId>/part-r-<taskId> This selection
         * string can be something like:
         * "$OUTPUT_DIRECTORY/_temporary/_attempt_*\/part-r-*" The problem using
         * the selection string is that in the output directory we will have the
         * same directory structure and not only the files
         */
        // if (
        // fakeHadoopOutputCommitter.needsTaskCommit(fakeTaskAttemptContext) ) {
        // fakeHadoopOutputCommitter.commitTask(fakeTaskAttemptContext);
        // logger.debug("The ReducerPATask is committed");
        // }
        // }
        return null;
    }

    /**
     * Copy length bytes from the specified FSDataInputStream to the specified
     * FSDataOutputStream starting at the given offsets and using the specified
     * bufferSize for the temporary buffer
     *
     * @param fsDataInputStream
     *            from where the bytes must be read
     * @param fsDataOutputStream
     *            to where the bytes must be written
     * @param offset
     *            where to start to copy bytes
     * @param length
     *            how many bytes to copy
     * @param bufferSize
     *            the size of the temporary buffer when copying
     * @return the number of copied bytes
     * @throws IOException
     */
    protected long copyFiles(FSDataInputStream fsDataInputStream, FSDataOutputStream fsDataOutputStream,
            long offset, long length, int bufferSize) throws IOException {
        long bytesCopied = 0;
        /*
         * to understand because we must use
         * "PAMapReduceFramework.MAX_BYTES_TO_READ" to create the buffer to use
         * to read data to copy and why it will throw a
         * "java.lang.IndexOutOfBoundsException" exception if we use a size less
         * than "PAMapReduceFramework.MAX_BYTES_TO_READ" to initialize the
         * buffer see InputStream.read(byte b[], int off, int len)
         */
        byte[] buffer = new byte[PAMapReduceFramework.MAX_BYTES_TO_READ];
        fsDataInputStream.seek(offset);
        long bytesToCopy = length;
        try {
            int bytesRead = fsDataInputStream.read(buffer, 0, ((int) Math.min(bytesToCopy,
                    PAMapReduceFramework.MAX_BYTES_TO_READ)));
            // TODO maybe we must use "bytesRead > 0" without the equal
            while ((bytesRead >= 0) && (bytesToCopy > 0)) {
                bytesToCopy -= bytesRead;
                fsDataOutputStream.write(buffer, 0, bytesRead);
                fsDataOutputStream.flush();
                bytesCopied += bytesRead;
                bytesRead = fsDataInputStream.read(buffer, 0, ((int) Math.min(bytesToCopy,
                        PAMapReduceFramework.MAX_BYTES_TO_READ)));
            }
        } finally {
            fsDataInputStream.close();
            fsDataOutputStream.close();
        }
        return bytesCopied;
    }

    /**
     * The {@link IndexFilePathFilter} represents the filter the ReducerPATask
     * must use to select only the MapperPATask output index files (i.e., the
     * files whose names are compliant to the format
     * "intermediate_&lt:mapperId&gt;.index"). Usually this class is
     * instantiated using as argument a regular expression like the following
     * "intermediate_([^\\.]*).index". That regular expression specify a
     * capturing group and, in particular, that capturing group "capture" the
     * mapperId (that actually is the replication id of the MapperPATask) of the
     * MapperPATask that produced that file as its output file.
     *
     * @author The ProActive Team
     *
     */
    protected class IndexFilePathFilter implements PathFilter {

        protected Pattern pattern = null;
        protected List<String> capturingGroupList = null;

        public IndexFilePathFilter(String regex) {
            pattern = Pattern.compile(regex);
            capturingGroupList = new ArrayList<String>();
        }

        @Override
        public boolean accept(Path path) {
            Matcher matcher = pattern.matcher(path.toUri().getPath().toString());
            if (matcher.matches()) {
                if (matcher.groupCount() > 0) {
                    capturingGroupList.add(matcher.group(1));
                }
                return true;
            }

            return false;
        }

        /**
         * Retrieve the list of the "captured" groups. Each captured group is
         * captured during an invocation of the method
         * {@link IndexFilePathFilter#accept(Path)}.
         *
         * @return the list of capturing groups
         */
        public List<String> getCapturingGroupList() {
            if (capturingGroupList.size() == 0) {
                return null;
            }
            return capturingGroupList;
        }
    }
}
