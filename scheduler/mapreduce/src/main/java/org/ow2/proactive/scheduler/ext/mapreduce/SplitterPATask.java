package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.mapreduce.fs.PADataSpacesFileSystem;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.StringUtils;


/**
 * The parameters the {@link SplitterPATask} must receives are the followings:
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
 * PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE
 * .getKey()</li>
 * </ul>
 *
 * Then, we must note that an eventually user defined "read mode" will change
 * the data space the SplitterPATask must read data from in the
 * {@link PAMapReduceJobConfiguration} instance (see {@link PAMapReduceJob}) to
 * get more details.
 *
 * We must also notice that the name of the file to which the MapReduce
 * operation must be applied is retrieved, internally to the Hadoop reused
 * {@link InputFormat}, using the {@link PAHadoopJobConfiguration} instance.
 * This is valid also for the size of the {@link InputSplit}
 *
 * Changes against the
 * {@link org.ow2.proactive.scheduler.ext.mapreduce.SplitterPATask}:
 * <ul>
 * <li>the name of the input file must be not received explicitly anymore
 * because it is specified invoking the method
 * {@link FileInputFormat#addInputPath(org.apache.hadoop.mapreduce.Job, Path)}
 * and it goes in the Hadoop {@link Configuration} instance. This means it can
 * be retrieved from the Hadoop Configuration instance;</li>
 * <li>the size of the input split is not needed anymore because it will be
 * passed through the Hadoop Configuration and the Hadoop InputFormat retrieve
 * it from the that Hadoop Configuration;</li>
 * <li>we must always identify the data space this {@link SplitterPATask} must
 * read data from, but then this data space is used to instantiate a
 * {@link PADataSpacesFileSystem}. This means we must not do the resolution of
 * the file name explicitly, because it is implicitly done by the
 * PADataSpacesFileSystem instance</li>
 * <li>the class that represents the Hadoop Configuration instance is actually
 * the {@link PAHadoopJobConfiguration} that extends {@link Configuration} and
 * in addition stores the information about the {@link DataSpacesFileObject} the
 * PADataSpacesFileSystem must be built from</li>
 * </ul>
 *
 * @author The ProActive Team
 *
 */
public class SplitterPATask extends JavaExecutable {

    protected ClassLoader classLoader = null;
    protected InputFormat<WritableComparable, WritableComparable> hadoopInputFormat = null;
    protected Configuration hadoopJobConfiguration = null;

    protected final Logger logger = DefaultLogger.getInstance();

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        super.init(args);

        /*
         * if the OUTPUT space of the ProActive MapReduce workflow is not
         * writable we abort the execution. Otherwise only the MapperPATask are
         * executed but their output data cannot be stored in the OUTPUT space
         * (that is not writable) so that the execution of the MapperPATask is a
         * waste of time.
         */
        if (!getOutputSpace().isWritable()) {
            throw new FileSystemException(
                "The OUTPUT space is not writable. The ProActive MapReduce workflow cannot execute!");
        }

        // initialize the class loader
        classLoader = Thread.currentThread().getContextClassLoader();

        // initialize the logger
        boolean debugLogLevel = Boolean.parseBoolean((String) (args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_DEBUG.getKey())));
        logger.setDebugLogLevel(debugLogLevel);

        // retrieve the configuration of the Hadoop job
        hadoopJobConfiguration = (PAHadoopJobConfiguration) args
                .get(PAMapReduceFrameworkProperties.HADOOP_JOB_CONFIGURATION.getKey());
        if (hadoopJobConfiguration != null) {
            logger.debug("The Hadoop Job Configuration IS NOT null");
        } else {
            logger.debug("The Hadoop Job Configuration IS null");
        }

        /*
         * identify the dataspace this SplitterPATask must use to read files
         * from
         */
        String inputDataSpaceString = (String) args
                .get(PAMapReduceFrameworkProperties.WORKFLOW_SPLITTER_PA_TASK_INPUT_DATASPACE.getKey());
        logger.debug("The string that identifies the input space the SplitterPATask must use is: " +
            inputDataSpaceString);
        DataSpacesFileObject inputDataSpace = null;
        if (inputDataSpaceString
                .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_INPUT_DATASPACE.getKey())) {
            inputDataSpace = getInputSpace();
        } else if (inputDataSpaceString
                .equalsIgnoreCase(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOCAL_DATASPACE.getKey())) {
            inputDataSpace = getLocalSpace();
        } else {
            inputDataSpace = getOutputSpace();
        }
        logger.debug("The real URI of the data space to use as input data space is: " +
            inputDataSpace.getRealURI());
        logger.debug("The virtual URI of the data space to use as the input data space is: " +
            inputDataSpace.getVirtualURI());

        /*
         * We add the data space this SplitterPATask must read from to the
         * PAHadoopJobConfiguration instance to be able to instantiate the
         * PADataSpacesFileSystem instance
         */
        ((PAHadoopJobConfiguration) hadoopJobConfiguration).setDataSpacesFileObject(inputDataSpace);

        /*
         * we check if the input path from which this SplitterPATask must read
         * data from is a file or a directory. In the case the input path is a
         * directory we list all the files stored inside that directory and we
         * add them to the Hadoop Configuration instance as comma separated
         * strings. We must notice that only the files that are direct child of
         * the given directory will be considered.
         */
        FileSystem inputFileSystem = PAMapReduceFramework.getFileSystem(inputDataSpace);
        String inputPathStringList = hadoopJobConfiguration.get(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME
                        .getKey()));

        logger.debug("The comma separated list of files received is " + inputPathStringList);

        if ((inputPathStringList != null) && (!inputPathStringList.trim().equalsIgnoreCase(""))) {
            String newInputPathStringList = "";
            String[] inputPathStringListElments = StringUtils.split(inputPathStringList);
            Path currentPath = null;
            FileStatus[] currentFileStatusArray = null;
            for (int i = 0; i < inputPathStringListElments.length; i++) {
                currentPath = new Path(StringUtils.escapeString(inputPathStringListElments[i]));

                logger.debug("Current path is: " + currentPath);
                FileStatus currentPathFileStatus = inputFileSystem.getFileStatus(currentPath);
                logger.debug("Current path file status information: " +
                    currentPathFileStatus.getPath().toUri().toString());
                String inputFileSystemWorkingDirectory = inputFileSystem.getWorkingDirectory().toUri()
                        .toString();
                logger.debug("FileSystem working directory: " + inputFileSystemWorkingDirectory);

                if (inputFileSystem.getFileStatus(currentPath).isDir()) {
                    currentFileStatusArray = inputFileSystem.listStatus(currentPath,
                            new ExcludeHiddenFilePathFilter(inputFileSystem));

                    logger.debug("The number of children of the directory '" +
                        currentPath.toUri().toString() + "' is  " + currentFileStatusArray.length + "'");

                    for (int j = 0; j < currentFileStatusArray.length; j++) {
                        // TODO we choose to not navigate recursively the
                        // sub-directories, maybe we must navigate recursively
                        // them
                        if (!currentFileStatusArray[j].isDir()) {
                            if (newInputPathStringList.isEmpty()) {
                                newInputPathStringList += currentFileStatusArray[j].getPath().toUri()
                                        .toString();
                            } else {
                                newInputPathStringList += StringUtils.COMMA_STR +
                                    currentFileStatusArray[j].getPath().toUri().toString();
                            }
                        }
                    }
                } else {
                    if (newInputPathStringList.isEmpty()) {
                        newInputPathStringList += inputPathStringListElments[i];
                    } else {
                        newInputPathStringList += StringUtils.COMMA_STR + inputPathStringListElments[i];
                    }
                }
            }

            logger.debug("The modified list of input paths is '" + newInputPathStringList + "'");

            hadoopJobConfiguration.set(PAMapReduceFrameworkProperties
                    .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME
                            .getKey()), newInputPathStringList);

            logger
                    .debug("The list of modified input files is " +
                        hadoopJobConfiguration
                                .get(PAMapReduceFrameworkProperties
                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_INPUT_DIRECTORY_PROPERTY_NAME
                                                .getKey())));
        }

        /*
         * retrieve the Hadoop InputFormat to use to create the input splits. In
         * the implementation of the ProActive MapReduce framework that reuses
         * the Hadoop InputFormat class the name of the class to use as the
         * InputFormat is retrieved directly from the PAHadoopJobConfiguration
         * this task receive as argument
         */
        hadoopInputFormat = (InputFormat<WritableComparable, WritableComparable>) classLoader.loadClass(
                ((PAHadoopJobConfiguration) hadoopJobConfiguration).getInputFormatClassName())
                .getConstructor().newInstance();
    }

    @Override
    public Serializable execute(TaskResult... taskResults) throws Throwable {
        if (hadoopInputFormat != null) {
            /**
             * We must notice we have to "translate" the List<InputSplit> in an
             * ArrayList<InputSplit> because List<T> is not Serializable. We
             * must notice that re-using the Hadoop InputFormat class to invoke
             * the InputFormat.getSplits method we must create a
             * org.apache.hadoop.mapreduce.JobContext instance. In fact from
             * that instance the Hadoop InputFormat will be able to instantiate
             * a FileSystem. In the ProActive MapReduce framework we must notice
             * that instantiate a FileSystem means to create a wrapper around a
             * DataSpacesFileObject. The problem is that we cannot create a
             * FileSystem through something like "new FileSystem()" but the
             * FileSystem instance is create by Hadoop accessing configuration
             * parameters stored in a Configuration object. We can set a
             * DataSpacesFileObject in the Configuration object (because that
             * object is, actually, an instance of our PAHadoopJobConfiguration
             * class) but we cannot modify the Hadoop code to retrieve that
             * DataSpacesFileObject. We found a solution: look at the method
             * PADataSpacesFileSystem#initialize() to see how we solved that
             * problem.
             */

            /*
             * We must create an Hadoop JobContext that will be used in the
             * Hadoop InputFormat to create an instance of the FileSystem to use
             * to access the file to which the MapReduce operation must be
             * applied and to create splits for that file
             */
            int jobId = 0;
            String jobIdString = System.getProperty(TaskLauncher.SchedulerVars.JAVAENV_JOB_ID_VARNAME
                    .toString());
            if ((jobIdString != null) && (!(jobIdString.equalsIgnoreCase("")))) {
                jobId = Integer.parseInt(jobIdString);
                logger.debug("The jobId of the SplitterPATask is: " + jobId);
            } else {
                logger.debug("The jobId string is null or empty");
            }
            String jtIdentifier = "";
            JobID hadoopJobId = new JobID(jtIdentifier, jobId);
            JobContext fakeHadoopJobContext = new FakeHadoopJobContext(hadoopJobConfiguration, hadoopJobId);

            /*
             * In addition to translate the List, we get from the invocation of
             * the method InputFormat.getSplits(), into an ArrayList (that is
             * serializable in Java) we must notice that the Hadoop InputSplit
             * instances we get are not serializable in Java but they comes with
             * some methods that represent the serialization/deserialization
             * mechanism in Hadoop. Hence we wrap them into a
             * SerializableHadoopInputSplit (that is Java serializable) that has
             * the readObject and writeObject methods that simply calls the
             * wrapped Hadoop InputSplit methods for serialization and
             * deserialization respectively. The use of the Serializable
             * HadoopInputSplit instances imply that we must retrieve the
             * wrapped Hadoop InputSplit instance on the MapperPATask side
             */
            List<InputSplit> inputSplitList = hadoopInputFormat.getSplits(fakeHadoopJobContext);
            ArrayList<InputSplit> inputSplitArrayList = new ArrayList<InputSplit>(inputSplitList.size());
            for (InputSplit inputSplit : inputSplitList) {
                inputSplitArrayList.add(new SerializableHadoopInputSplit(inputSplit, inputSplit.getClass()));
            }

            logger.debug("The number of created splits is: " + inputSplitArrayList.size());

            return inputSplitArrayList;
        }
        return null;
    }

    /**
     * The {@link ExcludeHiddenFilePathFilter} is an inner class that represents
     * a filter for the hidden files. This means that if we have a set of files
     * the hidden files are left out.
     *
     * @author The ProActive Team
     *
     */
    protected class ExcludeHiddenFilePathFilter implements PathFilter {

        protected FileSystem fileSystem = null;

        public ExcludeHiddenFilePathFilter(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        @Override
        public boolean accept(Path path) {
            try {
                if (!((PADataSpacesFileSystem) fileSystem).isHidden(path)) {
                    return true;
                }
            } catch (FileSystemException fse) {
                return false;
            }
            return false;
        }
    }
}
