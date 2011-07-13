package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;


/**
 * The {@link PAMapReduceFramework} class provide access to information directly
 * related to the ProActive MapReduce framework. Those informations are, for
 * example, the identifiers of the XxxxxPATask tasks that compose the ProActive
 * MapReduce workflow, the list of read modes the XxxxxPATask can use to access
 * their input files, the string the ReducerPATask must use to select its input
 * files etc... The {@link PAMapReduceFramework} access the
 * {@link PAMapReduceFrameworkProperties} to retrieve properties and
 * configuration parameters. Those properties and configuration parameters are
 * not modifiable by the user that wants to create a ProActive MapReduce job
 * since they are internal to the ProActive MapReduce framework.
 *
 * @author The ProActive Team
 *
 */
public class PAMapReduceFramework {

    /**
     * The name of the file from which the configuration properties must be read
     * That file is stored in the $SCHEDULER/extensions/mapreduce folder
     */
    protected static final String PA_MAPREDUCE_FRAMEWORK_PROPERTIES_FILE_NAME = "proactive.mapreduce.framework.configuration";

    /**
     * The "user.dir" system property is used by Hadoop to resolve relative
     * {@link Path}. We store that string in a static variable to limit the hard
     * coding.
     */
    protected static final String USER_DIR = "user.dir";

    /**
     * Refer the {@link TaskLauncher} property with the same name and the same
     * value. We need to maintain a copy of that variable because it is
     * protected and so not accessible from the {@link PAMapReduceFramework}
     * class.
     *
     * @see {@link TaskLauncher} attributes for more details
     */
    protected static final String REPLICATION_INDEX_TAG = "$REP";

    /**
     * The name of the script file containing the script that implements the
     * replication of the MapperPATask
     */
    protected static final String REPLICATE_MAPPER_PA_TASK_SCRIPT_NAME = PAMapReduceFrameworkProperties
            .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_MAPPER_PA_TASK_REPLICATION_SCRIPT.key);

    /**
     * The name of the script file containing the script that implements the
     * replication of the ReducerPATask
     */
    protected static final String REPLICATE_REDUCER_PA_TASK_SCRIPT_NAME = PAMapReduceFrameworkProperties
            .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_REDUCER_PA_TASK_REPLICATION_SCRIPT.key);

    /**
     * The name of the script file containing the script that implements the
     * transfer of the ReducerPATask output file towards the destination defined
     * by the user
     */
    protected static final String OUTPUT_FILE_TRANSFER_POST_SCRIPT_NAME = PAMapReduceFrameworkProperties
            .getPropertyAsString(PAMapReduceFrameworkProperties.REDUCER_PA_TASK_OUTPUT_FILE_TRANSFER_POST_SCRIPT_FILE_NAME
                    .getKey());

    /**
     * The identifier of the SplitterPATask
     */
    public static final int SPLITTER_PA_TASK = 0;

    /**
     * The identifier of the MapperPATask
     */
    public static final int MAPPER_PA_TASK = 1;

    /**
     * The identifier of the MapperJoinPATask
     */
    public static final int MAPPER_JOIN_PA_TASK = 2;

    /**
     * The identifier of the ReducerPATask
     */
    public static final int REDUCER_PA_TASK = 3;

    /**
     * The identifier of the ReducerJoinPATask
     */
    public static final int REDUCER_JOIN_PA_TASK = 4;

    /**
     * The value used as the size of a buffer when reading and/or writing data
     * using the buffers
     */
    public static final int MAX_BYTES_TO_READ = 64 * 1024;

    /**
     * The prefix the MapperPATask uses to build the name of the file when
     * spilling temporary data
     */
    public static String SPILL_PREFIX = "spill";

    /**
     * The suffix the MapperPATask uses to build the name of the file that will
     * contain its output data
     */
    public static String SPILL_SUFFIX = "out";

    /**
     * The suffix the MapperPATask uses to build the name of the file that will
     * contain the indices to its output data
     */
    public static String SPILL_INDEX_SUFFIX = "index";

    /**
     * The prefix the MapperPATask uses to build the name of the file that will
     * contain its output data
     */
    public static String MAPPER_INTERMEDIATE_FILE_NAME_PREFIX = "intermediate";

    /**
     * The string used by the ProActive MapReduce framework to build composite
     * name of files
     */
    public static String FILE_NAME_CONCATENATOR = "_";

    /**
     * The string used by the ProActive MapReduce framework to separate the name
     * of the file from its extension
     */
    public static String FILE_EXTENSION_SEPARATOR = ".";

    /**
     * The string used by the ReducerPATask to select files, when the target of
     * the selection are all the files of a determined set
     */
    public static String ALL = "*";

    /**
     * The string used by the ReducerPATask to build temporary file names when
     * the input data of the ReducerPATask are not randomly accessible (see
     * {@link ReducerPATask} to get more details)
     */
    public static String TEMPORARY = "temporary";

    /**
     * The string that represents the suffix the properties that identifies the
     * default values have
     */
    public static String DEFAULT_SUFFIX = "default";

    /**
     * The string used to concatenate strings to create property names
     */
    public static String PROPERTY_STRING_PART_SEPARATOR = ".";

    /**
     * Retrieve the name of the file in which the spill identified by the
     * specified spillId and mapperId must be stored. The name of the file will
     * follow the format: spill_&lt;mapperId&gt;_&lt;spillId&gt;.out
     *
     * @param mapperId
     *            the identifier of the mapper that want to write the spill into
     *            a file
     * @param spillId
     *            the identifier of the spill that must be written into a file
     * @return {@link String} the name of the file in which the spill must be
     *         written
     */
    public static String getSpillFileName(String mapperId, String spillId) {
        return SPILL_PREFIX + FILE_NAME_CONCATENATOR + mapperId + FILE_NAME_CONCATENATOR + spillId +
            FILE_EXTENSION_SEPARATOR + SPILL_SUFFIX;
    }

    /**
     * Retrieve the name of the file in which the index corresponding to the
     * spill identified by the specified spillId and mapperId must be stored.The
     * name of the file will follow the format:
     * spill_&lt;mapperId&gt;_&lt;spillId&gt;.index
     *
     * @param mapperId
     *            the identifier of the mapper that want to write the spill into
     *            a file
     * @param spillId
     *            the identifier of the spill that must be written into a file
     * @return {@link String} the name of the file in which the index
     *         corresponding to a spill must be written
     */
    public static String getSpillIndexFileName(String mapperId, String spillId) {
        return SPILL_PREFIX + FILE_NAME_CONCATENATOR + mapperId + FILE_NAME_CONCATENATOR + spillId +
            FILE_EXTENSION_SEPARATOR + SPILL_INDEX_SUFFIX;
    }

    /**
     * Build the name of the file the mapper must to use to store its output
     * data. The name of the file will follow the format:
     * intermediate_&lt;mapperId&gt;.out
     *
     * @param mapperId
     *            the identifier of the mapper that will write into the
     *            intermediate file
     * @return String the name of the file
     */
    public static String getMapperIntermediateFileName(String mapperId) {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + mapperId +
            FILE_EXTENSION_SEPARATOR + SPILL_SUFFIX;
    }

    /**
     * Build the name of the index file the mapper must to use to store the
     * indices of its output data. The name of the file will follow the format:
     * intermediate_&lt;mapperId&gt;.index
     *
     * @param mapperId
     *            the identifier of the mapper that will write into the
     *            intermediate index file
     * @return the name of the file
     */
    public static String getMapperIntermediateIndexFileName(String mapperId) {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + mapperId +
            FILE_EXTENSION_SEPARATOR + SPILL_INDEX_SUFFIX;
    }

    /**
     * Build the string we must use to select the output file of the mapper that
     * we want to transfer from the mapper local space into the job output
     * space. The selector string will select only the
     * "intermediate_&lt;mapperId&gt;.out" file
     *
     * @param mapperId
     *            the identifier of the mapper
     * @return the string to use to select the mapper output file
     */
    public static String getMapperIntermediateFileSelector(String mapperId) {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + mapperId +
            FILE_EXTENSION_SEPARATOR + SPILL_SUFFIX;
    }

    /**
     * Build the string we must use to select the output index file of the
     * mapper that we want to transfer from the mapper local space into the job
     * output space. The selector string will select only the
     * "intermediate_&lt;mapperId&gt;.index" file
     *
     * @param mapperId
     *            the identifier of the mapper
     * @return the string to use to select the mapper output index file
     */
    public static String getMapperIntermediateIndexFileSelector(String mapperId) {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + mapperId +
            FILE_EXTENSION_SEPARATOR + SPILL_INDEX_SUFFIX;
    }

    /**
     * Build the string the reducer must use to select its input files. Since
     * when we use the Hadoop logic to generate intermediate output files a
     * mapper generate only one intermediate file so that that intermediate file
     * will contain more that one partition (this means it must be read by more
     * than one reducer) we cannot select the files using the reducer identifier
     * as we did in the previous implementation of the MapReduce framework (we
     * remember that in that implementation the output file of a mapper has a
     * name compliant to the format "intermediate_&lt;mapperId&gt;.out" and that
     * a file in whose name there was a given reducerId contained data only for
     * the reducer identified by that reducerId ). Now the reducer must receive
     * the spill index files of the mappers (this means a reducer will receive
     * as many spill index files as many mappers executed). Hence, from each
     * spill index file the reducer will access the partition it must elaborate
     * and it retrieves the records it must read (this means that a reducer
     * access the output file of the mappers and that there will be more
     * reducers that will read data from the output file of the same mapper)
     *
     * @param reducerId
     *            the reducer that selects the input files
     * @return the string to select the input files (spill index files produced
     *         by the mappers)
     *
     *         TODO in a second implementation we can let a mapper produce an
     *         output file for each partition even if we continue to use the
     *         Hadoop logic for the remaining staffs. If we do that than we do
     *         not need the spill index files anymore.
     */
    public static String getReducerIntermediateFileSelector() {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + ALL +
            FILE_EXTENSION_SEPARATOR + SPILL_SUFFIX;
    }

    /**
     * Build the string the reducer must use to select its input index files.
     * The name of the index file (when each mapper produce only an output file
     * that contains data belonging to different partitions) is compliant to the
     * following format: intermediate_&lt;mapperId&gt;.index
     *
     * @return the string to select each index file generated by each mapper as
     *         an output file
     */
    public static String getReducerIntermediateIndexFileSelector() {
        return MAPPER_INTERMEDIATE_FILE_NAME_PREFIX + FILE_NAME_CONCATENATOR + ALL +
            FILE_EXTENSION_SEPARATOR + SPILL_INDEX_SUFFIX;
    }

    /**
     * Retrieve the regular expression that can be used by the user to retrieve
     * all the set of output files of the reducer. It will correspond to a
     * string like the following one: "_temporary/_attempt_* /part-r-*". We must
     * notice that this regular expression will not begin with a '/'
     *
     * @return
     */
    public static String getTemporaryOutputDirectoryRegex() {
        return "_temporary" + File.separator + "_attempt_*" + File.separator + "part-r-*";
    }

    /**
     * Retrieve the prefix to use to build the name of the reducer output files
     *
     * @return the prefix (i.e., "reducer_")
     */
    public static String getReducerOutputFileNamePrefix() {
        return getDefault(PAMapReduceFrameworkProperties.REDUCER_PA_TASK_OUTPUT_FILE_NAME_PREFIX.getKey()) +
            FILE_NAME_CONCATENATOR;
    }

    /**
     * Create a {@link FileSystem} instance the SplitterPATask, the MapperPATask
     * or the ReducerPATask can use to access files
     *
     * @param dataSpacesFileObject
     *            the {@link DataSpacesFileObject} on which the FileSystem
     *            instance must be built on
     * @return the {@link FileSystem} instance
     * @throws IOException
     *             if the FileSystem instance cannot be created
     */
    public static FileSystem getFileSystem(DataSpacesFileObject dataSpacesFileObject) throws IOException {
        /*
         * Why we need this method and why this method works as the following
         * code statements show? We need this method to be able to use the
         * Hadoop like logic in creating the intermediate files (we mean the
         * multi-threaded spills of intermediate MapperPATask output files) we
         * need a local file system. This means we need a FileSystem
         * implementation based on a LOCAL DataSpacesFileObject. This method
         * works because: to create a FileSystem instance we must notice that we
         * must invoke the method FileSystem.get(Configuration configuration)
         * and that that method will access the "fs.default.name" configuration
         * property (that we have previously set in the
         * "proactive.mapreduce.framework.configuration" file) and some others
         * configuration properties. We must also notice that just before the
         * FileSystem instance is created the method FileSystem.initialize(URI
         * name, Configuration configuration) is invoked. We have overridden
         * that method in our PADataSpacesFileSystem to be able to create a
         * FileSystem that actually is a wrapper around a ProActive
         * DataSpacesFileObject. The wrapped DataSpacesFileObject is passed
         * through the Configuration instance. Hence to create an instance of a
         * PADataSpacesFileSystem we must create a new Configuration instance in
         * which we set the DataSpacesFileObject we want to wrap and the other
         * needed informations. Actually the instance we must create to store
         * all the informations needed to create the FileSystem is an instance
         * of the PAHadoopConfiguration class because only that configuration
         * object has a method (setDataSpacesFileObject) we can use to specify
         * the DataSpacesFileObject the FileSystem must be built on.
         */

        FileSystem fileSystem = null;
        PAHadoopJobConfiguration fakeHadoopConfiguration = new PAHadoopJobConfiguration(new Configuration());
        fakeHadoopConfiguration.setDataSpacesFileObject(dataSpacesFileObject);
        fakeHadoopConfiguration
                .set(
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_IMPLEMENTATION_PROPERTY_NAME
                                        .getKey()),
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DEFAULT_IMPLEMENTATION
                                        .getKey()));
        fakeHadoopConfiguration.set(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DEFAULT_NAME_PROPERTY_NAME
                        .getKey()), PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DEFAULT_NAME
                        .getKey()));
        fakeHadoopConfiguration.set(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_FS_DISABLE_CACHE_PROPERTY_NAME
                        .getKey()), PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_FILE_SYSTEM_DISABLE_CACHE
                        .getKey()));

        fileSystem = FileSystem.get(fakeHadoopConfiguration);

        return fileSystem;
    }

    /**
     * Retrieve the default value of the specified property
     *
     * @param propertyName
     *            the name of the property whose default value is desired
     * @return the {@link String} representation of the default value of the
     *         property
     */
    public static String getDefault(String propertyName) {
        return PAMapReduceFrameworkProperties.getPropertyAsString(propertyName +
            PROPERTY_STRING_PART_SEPARATOR + DEFAULT_SUFFIX);
    }
}
