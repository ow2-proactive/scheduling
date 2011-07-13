package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.serializer.SerializationFactory;
import org.apache.hadoop.io.serializer.Serializer;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.IndexedSortable;
import org.apache.hadoop.util.IndexedSorter;
import org.apache.hadoop.util.Progress;
import org.apache.hadoop.util.QuickSort;
import org.apache.hadoop.util.ReflectionUtils;
import org.ow2.proactive.scheduler.ext.mapreduce.IntermediateFile.Writer;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.DefaultLogger;
import org.ow2.proactive.scheduler.ext.mapreduce.logging.Logger;


/**
 * The {@link MapperBufferedOutputCollector} contains code that is a copy and
 * paste of the code of the Hadoop MapTask.NewOutputCollector. We chose to copy
 * and paste the Hadoop code to have more control on how the intermediary
 * results are handled in the ProActive MapReduce framework, and also because
 * the classes that in Hadoop handle intermediary results are not visible from
 * the outside (i.e, the org.apache.hadoop.mapred.MapTask class is not visible).
 * We must notice that in the first implementation we do not provide support for
 * the Combiner neither for the CompressionCodec neither for the progress. We
 * must notice that this class collect the output of the Mapper and bufferize it
 * before to store that output into files. Since the output must be sorted we
 * can define a sorter that take the buffer and sort it. We provide the sorter
 * will accept a "sortable" argument and since the information to use to effect
 * the sort are stored in attrbutes of this class we define this class
 * "sortable"
 *
 * @author The ProActive Team
 *
 * @param <K>
 *            the class of the key
 * @param <V>
 *            the class of the value
 */
public class MapperBufferedOutputCollector<K, V> implements MapperOutputCollector<K, V>, IndexedSortable {

    protected static final Logger logger = DefaultLogger.getInstance();

    /**
     * keep track of the total execution time of the spent by the MapperPATask
     * in spilling and sorting its output
     */
    protected static long spillingAndSortingExecutionTime = 0;

    /**
     * keep track of the total execution time of the spent by the MapperPATask
     * in running the combiner
     */
    protected static long combinerExecutionTime = 0;

    /**
     * The size of each record in the index file for the map-outputs.
     */
    protected final static int MAP_OUTPUT_INDEX_RECORD_LENGTH = 24;

    protected final static int APPROX_HEADER_LENGTH = 150;

    protected final static String LOCAL_TEMPORARY_DIRECTORY = "temporary";

    protected final static String SPILL_FILE_PREFIX = "spill";

    protected final int partitions;
    protected Configuration configuration = null;

    protected final Class<K> keyClass;
    protected final Class<V> valClass;
    protected final RawComparator<K> comparator;
    protected final SerializationFactory serializationFactory;
    protected final Serializer<K> keySerializer;
    protected final Serializer<V> valSerializer;

    protected CombinerRunner<K, V> combinerRunner;
    protected final CombineOutputCollector<K, V> combineOutputCollector;

    // k/v accounting
    protected volatile int kvstart = 0; // marks beginning of spill
    protected volatile int kvend = 0; // marks beginning of collectable
    protected int kvindex = 0; // marks end of collected
    protected final int[] kvoffsets; // indices into kvindices
    protected final int[] kvindices; // partition, k/v offsets into kvbuffer
    protected volatile int bufstart = 0; // marks beginning of spill
    protected volatile int bufend = 0; // marks beginning of collectable
    protected volatile int bufvoid = 0; // marks the point where we should stop
    // reading at the end of the buffer
    protected int bufindex = 0; // marks end of collected
    protected int bufmark = 0; // marks end of record
    protected byte[] kvbuffer; // main output buffer
    protected static final int PARTITION = 0; // partition offset in acct
    protected static final int KEYSTART = 1; // key offset in acct
    protected static final int VALSTART = 2; // val offset in acct
    protected static final int ACCTSIZE = 3; // total #fields in acct
    protected static final int RECSIZE = (ACCTSIZE + 1) * 4; // acct bytes per
    // record

    // spill accounting
    protected volatile int numSpills = 0;
    protected volatile Throwable sortSpillException = null;
    protected final int softRecordLimit;
    protected final int softBufferLimit;
    protected final int minSpillsForCombine;
    protected final IndexedSorter sorter;
    protected final BlockingBuffer bb = new BlockingBuffer();

    /**
     * The lock "associated" to the buffers kvbuffer, kvoffsets and kvindices to
     * be able to let this object or the SpillThread to read/modify the buffers
     * exclusively
     */
    protected final ReentrantLock spillLock = new ReentrantLock();

    /**
     * The condition (waiting queue) of the entities waiting for the spilling
     * ends (in our case the only entity that can wait for the spilling to end
     * is this object)
     */
    protected final Condition spillDone = spillLock.newCondition();

    /**
     * The condition (waiting queue) of the entities waiting for the spilling is
     * ready (in our case the only entity that can wait for the spilling to be
     * ready is the SpilThread)
     */
    protected final Condition spillReady = spillLock.newCondition();

    /**
     * The variable that affirm if the SpillThread is running or not. We must
     * notice that the value read/written by this object is always equal to the
     * value read/written by the SpillThread (because the variable is a
     * "volatile" one; that means the variable is never cached on the side of
     * the thread executing in this object or on the side of the SpillerThread)
     */
    protected volatile boolean spillThreadRunning = false;
    protected final SpillThread spillThread = new SpillThread();

    protected final FileSystem fileSystem;

    protected ArrayList<SpillRecord> indexCacheList;
    /**
     * when the variable is not explicitly initialized the initialization value
     * is 0 for integers
     */
    protected int totalIndexCacheMemory;
    protected static final int INDEX_CACHE_MEMORY_LIMIT = 1024 * 1024;

    /**
     * Create a new instance of the {@link MapperBufferedOutputCollector}. To do
     * this we must notice we need a FileSystem instance even if we could
     * instantiate a {@link FileSystem} invoking the method
     * {@link FileSystem#get(Configuration)}. The problem is that we cannot use
     * the same configuration instance to instantiate different kinds of
     * FileSystem. For example if in the MapperPATask we use a Configuration
     * instance to create a FileSystem on the OUTPUT DataSpacesFileObject, then
     * we cannot use the same configuration also to create another FileSystem
     * but this time on the LOCAL DataSpacesFileObject. To do this we might
     * change the DataSpacesFileObject stored in the Configuration but since the
     * configuration stores only a reference to that object and since the
     * FileSystem created on the OUTPUT DataSpacesFileSystem (in the example)
     * maintain a reference to the the configuration instance that was used to
     * create that FileSystem then if we change the DataSpacesFileObject in the
     * configuration instance we will change the DataSpacesFileObject the
     * FileSystem was built on (this means that we built a FileSystem on the
     * OUTPUT DataSpacesFileObject but we get a FileSystem built on the LOCAL
     * DataSpacesFileObject)
     *
     * @param taskAttemptContext
     * @param fileSystem
     * @throws IOException
     */
    public MapperBufferedOutputCollector(TaskAttemptContext taskAttemptContext, FileSystem fileSystem)
            throws IOException {
        // initialize the profile level of the logger
        logger.setProfileLogLevel(Boolean.parseBoolean(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.WORKFLOW_JAVA_TASK_LOGGING_PROFILE.key)));

        this.configuration = taskAttemptContext.getConfiguration();
        partitions = configuration.getInt(PAMapReduceFrameworkProperties
                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_NUMBER_OF_REDUCERS_PROPERTY_NAME
                        .getKey()), 1);
        this.fileSystem = fileSystem;

        indexCacheList = new ArrayList<SpillRecord>();

        final float spillper = configuration
                .getFloat(
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_SPILL_PERCENT_PROPERTY_NAME
                                        .getKey()), (float) 0.8);
        final float recper = configuration
                .getFloat(
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_RECORD_PERCENT_PROPERTY_NAME
                                        .getKey()), (float) 0.05);
        final int sortmb = configuration.getInt(
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_MB_PROPERTY_NAME
                                .getKey()), 100);
        if (spillper > (float) 1.0 || spillper < (float) 0.0) {
            throw new IOException(
                "Invalid \"" +
                    PAMapReduceFrameworkProperties
                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_SPILL_PERCENT_PROPERTY_NAME
                                    .getKey()) + "\": " + spillper);
        }
        if (recper > (float) 1.0 || recper < (float) 0.01) {
            throw new IOException(
                "Invalid \"" +
                    PAMapReduceFrameworkProperties
                            .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_RECORD_PERCENT_PROPERTY_NAME
                                    .getKey()) + "\": " + recper);
        }
        if ((sortmb & 0x7FF) != sortmb) {
            throw new IOException("Invalid \"" +
                PAMapReduceFrameworkProperties
                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_MB_PROPERTY_NAME
                                .getKey()) + "\": " + sortmb);
        }
        sorter = ReflectionUtils
                .newInstance(
                        configuration
                                .getClass(
                                        PAMapReduceFrameworkProperties
                                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_MAP_SORT_CLASS_PROPERTY_NAME
                                                        .getKey()), QuickSort.class, IndexedSorter.class),
                        configuration);

        int maxMemUsage = sortmb << 20;
        int recordCapacity = (int) (maxMemUsage * recper);
        recordCapacity -= recordCapacity % RECSIZE;
        kvbuffer = new byte[maxMemUsage - recordCapacity];
        bufvoid = kvbuffer.length;
        recordCapacity /= RECSIZE;
        kvoffsets = new int[recordCapacity];
        kvindices = new int[recordCapacity * ACCTSIZE];
        softBufferLimit = (int) (kvbuffer.length * spillper);
        softRecordLimit = (int) (kvoffsets.length * spillper);

        /*
         * In retrieving the comparator we must notice that the class the mapper
         * use to emits its output keys is the same class the reducer uses to
         * emits its own output keys. This is valid for the values too. In fact
         * the only operation the reducer does is to aggregate mapper outputs,
         * but it does not change the class of the key and/or value it receives
         * in input.
         */
        comparator = ((PAHadoopJobConfiguration) configuration).getOutputKeyComparator();
        keyClass = (Class<K>) ((PAHadoopJobConfiguration) configuration).getMapperOutputKeyClass();
        valClass = (Class<V>) ((PAHadoopJobConfiguration) configuration).getMapperOutputValueClass();
        serializationFactory = new SerializationFactory(configuration);
        keySerializer = serializationFactory.getSerializer(keyClass);
        keySerializer.open(bb);
        valSerializer = serializationFactory.getSerializer(valClass);
        valSerializer.open(bb);

        /*
         * TODO set the minimum number of spill property in the configuration
         * instance before the MapperBufferedOutputCollector is instantiated
         */
        minSpillsForCombine = configuration
                .getInt(
                        PAMapReduceFrameworkProperties
                                .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_MINIMUM_NUMBER_OF_SPILL_FOR_COMBINE_PROPERTY_NAME
                                        .getKey()), 3);

        try {
            combinerRunner = new CombinerRunner<K, V>(taskAttemptContext);
        } catch (ClassNotFoundException cnfe) {
            combinerRunner = null;
        }
        combineOutputCollector = new CombineOutputCollector<K, V>();

        spillThread.setDaemon(true);
        spillThread.setName("SpillThread");
        spillLock.lock();
        try {
            spillThread.start();
            while (!spillThreadRunning) {
                spillDone.await();
            }
        } catch (InterruptedException e) {
            throw (IOException) new IOException("Spill thread failed to initialize")
                    .initCause(sortSpillException);
        } finally {
            spillLock.unlock();
        }
        if (sortSpillException != null) {
            throw (IOException) new IOException("Spill thread failed to initialize")
                    .initCause(sortSpillException);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        // do nothing, also in Hadoop this method do nothing
    }

    /**
     * @inheritDoc We must notice, in the first implementation, we do not grant
     *             support to the report on the progress of the collect and on
     *             the counting of the collected records and bytes We must
     *             notice the synchronized key word is needed because this
     *             object (and so its state) will be modified by the
     *             RecordWriter (that will be used by a mapper) that will
     *             encapsulate an instance of this object and by the SpillThread
     *             when it does the (indexed) sort of the spill to write into a
     *             file. This means either the RecordWriter either the
     *             SpillThread can modify the state of this object. To avoid
     *             concurrent modification we must affirm that when the
     *             RecordWriter wants to modify the sate of this Object it must
     *             acquire the exclusive lock to the this Object and we
     *             implement this using the synchronized keyword either on both
     *             the methods
     *             {@link MapperBufferedOutputCollector#collect(Object, Object, int)}
     *             and {@link MapperBufferedOutputCollector#flush()}.
     */
    @Override
    public synchronized void collect(K key, V value, int partition) throws IOException, InterruptedException {
        if (key.getClass() != keyClass) {
            throw new IOException("Type mismatch in key from map: expected " + keyClass.getName() +
                ", recieved " + key.getClass().getName());
        }
        if (value.getClass() != valClass) {
            throw new IOException("Type mismatch in value from map: expected " + valClass.getName() +
                ", recieved " + value.getClass().getName());
        }
        final int kvnext = (kvindex + 1) % kvoffsets.length;
        spillLock.lock();
        try {
            boolean kvfull;
            do {
                if (sortSpillException != null) {
                    throw (IOException) new IOException("Spill failed").initCause(sortSpillException);
                }
                // sufficient acct space
                kvfull = kvnext == kvstart;
                final boolean kvsoftlimit = ((kvnext > kvend) ? kvnext - kvend > softRecordLimit : kvend -
                    kvnext <= kvoffsets.length - softRecordLimit);
                if (kvstart == kvend && kvsoftlimit) {
                    startSpill();
                }
                if (kvfull) {
                    try {
                        while (kvstart != kvend) {
                            spillDone.await();
                        }
                    } catch (InterruptedException e) {
                        throw (IOException) new IOException(
                            "Collector interrupted while waiting for the writer").initCause(e);
                    }
                }
            } while (kvfull);
        } finally {
            spillLock.unlock();
        }

        try {
            // serialize key bytes into buffer
            int keystart = bufindex;
            keySerializer.serialize(key);
            if (bufindex < keystart) {
                // wrapped the key; reset required
                bb.reset();
                keystart = 0;
            }
            // serialize value bytes into buffer
            final int valstart = bufindex;
            valSerializer.serialize(value);
            int valend = bb.markRecord();

            if (partition < 0 || partition >= partitions) {
                throw new IOException("Illegal partition for " + key + " (" + partition + ")");
            }

            // update accounting info
            int ind = kvindex * ACCTSIZE;
            kvoffsets[kvindex] = ind;
            kvindices[ind + PARTITION] = partition;
            kvindices[ind + KEYSTART] = keystart;
            kvindices[ind + VALSTART] = valstart;
            kvindex = kvnext;
        } catch (MapBufferTooSmallException e) {
            return;
        }
    }

    /**
     * @inheritDoc
     * @see MapperBufferedOutputCollector#collect(Object, Object, int) to
     *      understand why the synchronized keyword is required for this method
     */
    @Override
    public synchronized void flush() throws IOException, InterruptedException, ClassNotFoundException {
        spillLock.lock();
        try {
            while (kvstart != kvend) {
                spillDone.await();
            }
            if (sortSpillException != null) {
                throw (IOException) new IOException("Spill failed").initCause(sortSpillException);
            }
            if (kvend != kvindex) {
                kvend = kvindex;
                bufend = bufmark;

                /** @START_PROFILE */
                // logger.profile("Start of the [MAPPER_SPILLING_AND_SORTING_PHASE] in the flush method");
                long spillAndSortStartTime = System.currentTimeMillis();
                /** @END_PROFILE */

                sortAndSpill();

                /** @START_PROFILE */
                long spillAndSortEndTime = System.currentTimeMillis();
                spillingAndSortingExecutionTime += (spillAndSortEndTime - spillAndSortStartTime);
                // logger.profile("End of the [MAPPER_SPILLING_AND_SORTING_PHASE] in the flush method. It takes '"
                // + ( spillAndSortEndTime - spillAndSortStartTime ) +
                // "' milliseconds");
                logger.profile("End of the [MAPPER_SPILLING_AND_SORTING_PHASE]. It takes '" +
                    spillingAndSortingExecutionTime + "' milliseconds");
                /** @END_PROFILE */

            }
        } catch (InterruptedException e) {
            throw (IOException) new IOException("Buffer interrupted while waiting for the writer")
                    .initCause(e);
        } finally {
            spillLock.unlock();
        }
        assert !spillLock.isHeldByCurrentThread();
        // shut down spill thread and wait for it to exit. Since the preceding
        // ensures that it is finished with its work (and sortAndSpill did not
        // throw), we elect to use an interrupt instead of setting a flag.
        // Spilling simultaneously from this thread while the spill thread
        // finishes its work might be both a useful way to extend this and also
        // sufficient motivation for the latter approach.
        try {
            spillThread.interrupt();
            spillThread.join();
        } catch (InterruptedException e) {
            throw (IOException) new IOException("Spill failed").initCause(e);
        }
        // release sort buffer before the merge
        kvbuffer = null;

        /** @START_PROFILE */
        // logger.profile("Start of the [MAPPER_MERGING_PHASE]");
        long mapperMergeStartTime = System.currentTimeMillis();
        /** @END_PROFILE */

        mergeParts();

        /** @START_PROFILE */
        long mapperMergeEndTime = System.currentTimeMillis();
        logger.profile("End of the [MAPPER_MERGING_PHASE]. It takes '" +
            (mapperMergeEndTime - mapperMergeStartTime) + "' milliseconds");
        // logger.profile("The total execution time of the various phases related to the mapper output data (sort, spill and merge) is '"
        // + (spillingAndSortingExecutionTime + (mapperMergeEndTime -
        // mapperMergeStartTime)) + "' milliseconds");
        /** @END_PROFILE */

        /** @START_PROFILE */
        logger.profile("End of the [MAPPER_COMBINING_PHASE]. It takes '" + combinerExecutionTime +
            "' milliseconds");
        /** @END_PROFILE */
    }

    /**
     * The method {@link MapperBufferedOutputCollector#mergeParts()} is invoked
     * by the method {@link MapperBufferedOutputCollector#flush()} just before
     * the mapper close the RecordWriter (in fact the flush() method is invoked
     * by the close method of the RecordWriter that wraps this
     * MapperBufferedOutputFile). Before the RecordWriter is closed the mapper
     * for each spill has produced a file (that contains the actual spill data)
     * and an index file that contains informations about the start and the end
     * point of each segment (where a segment is the set of contiguous records
     * belonging to the same partition). When the flush method is called, and so
     * when the merge method is called, the spill files are aggregated in a
     * unique file. The same happens for the spill index files.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    protected void mergeParts() throws IOException, InterruptedException, ClassNotFoundException {
        // get the approximate size of the final output/index files
        long finalOutFileSize = 0;
        long finalIndexFileSize = 0;
        final Path[] filename = new Path[numSpills];
        final String mapperId = configuration.get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER
                .getKey());
        // final TaskAttemptID mapId = getTaskID();

        for (int i = 0; i < numSpills; i++) {
            filename[i] = getSpillFile(mapperId, "" + i);
            /*
             * we begin to compute the size of the unique output file of the
             * mapper as the sum of the size of the various spill files
             */
            finalOutFileSize += fileSystem.getFileStatus(filename[i]).getLen();
        }

        if (numSpills == 1) { // the spill is the final output
            fileSystem.rename(filename[0], new Path(PAMapReduceFramework
                    .getMapperIntermediateFileName(configuration
                            .get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey()))));
            if (indexCacheList.size() == 0) {
                fileSystem.rename(getSpillIndexFile(mapperId, "" + 0), new Path(PAMapReduceFramework
                        .getMapperIntermediateIndexFileName(configuration
                                .get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey()))));
            } else {
                // indexCacheList.get(0).writeToFile( new Path(
                // PAMapReduceFramework.getMapperIntermediateIndexFileName(mapperId)
                // ), configuration );
                indexCacheList.get(0).writeToFile(
                        new Path(PAMapReduceFramework.getMapperIntermediateIndexFileName(configuration
                                .get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey()))),
                        fileSystem);
            }
            return;
        }

        // read in paged indices
        for (int i = indexCacheList.size(); i < numSpills; ++i) {
            Path indexFileName = getSpillIndexFile(mapperId, "" + i);
            indexCacheList.add(new SpillRecord(indexFileName, fileSystem));
        }

        // make correction in the length to include the sequence file header
        // lengths for each partition
        finalOutFileSize += partitions * APPROX_HEADER_LENGTH;
        finalIndexFileSize = partitions * MAP_OUTPUT_INDEX_RECORD_LENGTH;

        /**
         * TODO the difference with the Hadoop code is that we do not check if
         * in the file system we will use we have enough space to memorize the
         * output of the mapper for both the intermediate output file and for
         * its corresponding index. So I must ask to Christian/Franca
         */
        Path finalOutputFile = new Path(PAMapReduceFramework.getMapperIntermediateFileName(configuration
                .get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey())));
        Path finalIndexFile = new Path(PAMapReduceFramework.getMapperIntermediateIndexFileName(configuration
                .get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER.getKey())));

        // The output stream for the final single output file
        FSDataOutputStream finalOut = fileSystem.create(finalOutputFile, true, 4096);

        if (numSpills == 0) {
            // create dummy files
            IndexRecord rec = new IndexRecord();
            SpillRecord sr = new SpillRecord(partitions);
            try {
                for (int i = 0; i < partitions; i++) {
                    long segmentStart = finalOut.getPos();
                    Writer<K, V> writer = new Writer<K, V>(configuration, finalOut, keyClass, valClass);
                    writer.close();
                    rec.startOffset = segmentStart;
                    rec.rawLength = writer.getRawLength();
                    rec.partLength = writer.getCompressedLength();
                    sr.putIndex(rec, i);
                }
                // sr.writeToFile(finalIndexFile, configuration);
                sr.writeToFile(finalIndexFile, fileSystem);
            } finally {
                finalOut.close();
            }
            return;
        }
        {
            IndexRecord rec = new IndexRecord();
            final SpillRecord spillRec = new SpillRecord(partitions);
            for (int parts = 0; parts < partitions; parts++) {
                // create the segments to be merged
                List<Merger.Segment<K, V>> segmentList = new ArrayList<Merger.Segment<K, V>>(numSpills);
                for (int i = 0; i < numSpills; i++) {
                    IndexRecord indexRecord = indexCacheList.get(i).getIndex(parts);
                    Merger.Segment<K, V> s = new Merger.Segment<K, V>(configuration, fileSystem, filename[i],
                        indexRecord.startOffset, indexRecord.partLength, true);
                    segmentList.add(i, s);
                }

                // TODO in the following code to do the merge we must notice
                // that the merge method maybe require a temp directory: new
                // Path(mapperId)
                // merge
                @SuppressWarnings("unchecked")
                RawKeyValueIterator kvIter = Merger
                        .merge(
                                configuration,
                                fileSystem,
                                keyClass,
                                valClass,
                                segmentList,
                                configuration
                                        .getInt(
                                                PAMapReduceFrameworkProperties
                                                        .getPropertyAsString(PAMapReduceFrameworkProperties.HADOOP_IO_SORT_FACTOR_PROPERTY_NAME
                                                                .getKey()), 100), new Path(mapperId),
                                ((PAHadoopJobConfiguration) configuration).getOutputKeyComparator());

                // write merged output to disk
                long segmentStart = finalOut.getPos();
                Writer<K, V> writer = new Writer<K, V>(configuration, finalOut, keyClass, valClass);
                if (combinerRunner == null || numSpills < minSpillsForCombine) {
                    Merger.writeFile(kvIter, writer, configuration);
                } else {
                    combineOutputCollector.setWriter(writer);
                    long combinerStartTime = System.currentTimeMillis();
                    combinerRunner.combine(kvIter, combineOutputCollector);
                    long combinerEndTime = System.currentTimeMillis();
                    combinerExecutionTime += (combinerEndTime - combinerStartTime);
                }

                // close
                writer.close();

                // record offsets
                rec.startOffset = segmentStart;
                rec.rawLength = writer.getRawLength();
                rec.partLength = writer.getCompressedLength();
                spillRec.putIndex(rec, parts);
            }
            // spillRec.writeToFile(finalIndexFile, configuration);
            spillRec.writeToFile(finalIndexFile, fileSystem);
            finalOut.close();
            for (int i = 0; i < numSpills; i++) {
                fileSystem.delete(filename[i], true);
            }
        }
    }

    /**
     * Auxiliary method to retrieve a spill file previously created
     *
     * @param mapperId
     *            the identifier of the mapper that created the file
     * @param spillId
     *            the identifier of the spill contained in the file
     * @return {@link Path} the path to the file
     */
    protected Path getSpillFile(String mapperId, String spillId) {
        return new Path(PAMapReduceFramework.getSpillFileName(mapperId, spillId));
    }

    /**
     * Auxiliary method to retrieve a spill index file previously created
     *
     * @param mapperId
     *            the identifier of the mapper that created the file
     * @param spillIdthe
     *            the identifier of the spill contained in the file
     * @return {@link Path} the path to the file
     */
    protected Path getSpillIndexFile(String mapperId, String spillId) {
        return new Path(PAMapReduceFramework.getSpillIndexFileName(mapperId, spillId));
    }

    /**
     * Inner class managing the spill of serialized records to disk.
     */
    protected class BlockingBuffer extends DataOutputStream {

        public BlockingBuffer() {
            this(new Buffer());
        }

        protected BlockingBuffer(OutputStream out) {
            super(out);
        }

        /**
         * Mark end of record. Note that this is required if the buffer is to
         * cut the spill in the proper place.
         */
        public int markRecord() {
            bufmark = bufindex;
            return bufindex;
        }

        /**
         * Set position from last mark to end of writable buffer, then rewrite
         * the data between last mark and kvindex. This handles a special case
         * where the key wraps around the buffer. If the key is to be passed to
         * a RawComparator, then it must be contiguous in the buffer. This
         * recopies the data in the buffer back into itself, but starting at the
         * beginning of the buffer. Note that reset() should <b>only</b> be
         * called immediately after detecting this condition. To call it at any
         * other time is undefined and would likely result in data loss or
         * corruption.
         *
         * @see #markRecord()
         */
        protected synchronized void reset() throws IOException {
            // spillLock unnecessary; If spill wraps, then
            // bufindex < bufstart < bufend so contention is impossible
            // a stale value for bufstart does not affect correctness, since
            // we can only get false negatives that force the more
            // conservative path
            int headbytelen = bufvoid - bufmark;
            bufvoid = bufmark;
            if (bufindex + headbytelen < bufstart) {
                System.arraycopy(kvbuffer, 0, kvbuffer, headbytelen, bufindex);
                System.arraycopy(kvbuffer, bufvoid, kvbuffer, 0, headbytelen);
                bufindex += headbytelen;
            } else {
                byte[] keytmp = new byte[bufindex];
                System.arraycopy(kvbuffer, 0, keytmp, 0, bufindex);
                bufindex = 0;
                out.write(kvbuffer, bufmark, headbytelen);
                out.write(keytmp);
            }
        }
    }

    protected class Buffer extends OutputStream {
        protected final byte[] scratch = new byte[1];

        @Override
        public synchronized void write(int v) throws IOException {
            scratch[0] = (byte) v;
            write(scratch, 0, 1);
        }

        /**
         * Attempt to write a sequence of bytes to the collection buffer. This
         * method will block if the spill thread is running and it cannot write.
         *
         * @throws MapBufferTooSmallException
         *             if record is too large to deserialize into the collection
         *             buffer.
         */
        @Override
        public synchronized void write(byte b[], int off, int len) throws IOException {
            boolean buffull = false;
            boolean wrap = false;
            spillLock.lock();
            try {
                do {
                    if (sortSpillException != null) {
                        throw (IOException) new IOException("Spill failed").initCause(sortSpillException);
                    }

                    // sufficient buffer space?
                    if (bufstart <= bufend && bufend <= bufindex) {
                        buffull = bufindex + len > bufvoid;
                        wrap = (bufvoid - bufindex) + bufstart > len;
                    } else {
                        // bufindex <= bufstart <= bufend
                        // bufend <= bufindex <= bufstart
                        wrap = false;
                        buffull = bufindex + len > bufstart;
                    }

                    if (kvstart == kvend) {
                        // spill thread not running
                        if (kvend != kvindex) {
                            // we have records we can spill
                            final boolean bufsoftlimit = (bufindex > bufend) ? bufindex - bufend > softBufferLimit
                                    : bufend - bufindex < bufvoid - softBufferLimit;
                            if (bufsoftlimit || (buffull && !wrap)) {
                                startSpill();
                            }
                        } else if (buffull && !wrap) {
                            // We have no buffered records, and this record
                            // is
                            // too large
                            // to write into kvbuffer. We must spill it
                            // directly
                            // from
                            // collect
                            final int size = ((bufend <= bufindex) ? bufindex - bufend : (bufvoid - bufend) +
                                bufindex) +
                                len;
                            bufstart = bufend = bufindex = bufmark = 0;
                            kvstart = kvend = kvindex = 0;
                            bufvoid = kvbuffer.length;
                            throw new IOException("The collection buffer is too small " + size + " bytes");
                        }
                    }

                    if (buffull && !wrap) {
                        try {
                            while (kvstart != kvend) {
                                spillDone.await();
                            }
                        } catch (InterruptedException e) {
                            throw (IOException) new IOException(
                                "Buffer interrupted while waiting for the writer").initCause(e);
                        }
                    }
                } while (buffull && !wrap);
            } finally {
                spillLock.unlock();
            }
            // here, we know that we have sufficient space to write
            if (buffull) {
                final int gaplen = bufvoid - bufindex;
                System.arraycopy(b, off, kvbuffer, bufindex, gaplen);
                len -= gaplen;
                off += gaplen;
                bufindex = 0;
            }
            System.arraycopy(b, off, kvbuffer, bufindex, len);
            bufindex += len;
        }
    }

    protected synchronized void startSpill() {
        kvend = kvindex;
        bufend = bufmark;
        spillReady.signal();
    }

    protected void sortAndSpill() throws IOException, ClassNotFoundException, InterruptedException {
        // approximate the length of the output file to be the length of the
        // buffer + header lengths for the partitions
        long size = (bufend >= bufstart ? bufend - bufstart : (bufvoid - bufend) + bufstart) + partitions *
            APPROX_HEADER_LENGTH;
        FSDataOutputStream out = null;
        try {
            // create spill file
            final SpillRecord spillRec = new SpillRecord(partitions);

            /*
             * TODO the name of the spill file must be in the form
             * "INTERMEDIATE_<taskId>_<spillId>_<reducerId>" This means that a
             * mapper can generate one or more spills that will contribute to
             * the reducer input. That does not change the fact that more mapper
             * tasks will contribute the same reducer. TODO we must set the id
             * of the mapper task before to invoke the sortAndSpill method We
             * must notice that the spill file that will be produced will
             * contain values belonging to more than one partition ( this means
             * that the contribute of the mapper to one partition is spawned
             * into different spill files so that, on the reducer side, the
             * reducer beyond merging the files it fetches from different mapper
             * tasks it must merge the (spill) files fetched from the same
             * reducer ). In building the name of the spill file we must notice
             * that the mapper taskId and the number of the spill file are
             * sufficient to grant the uniqueness of the intermediary files in
             * the output file system TODO the name of the spill file has to
             * contain also the identifier of the reducer. Otherwise each
             * reducer must open all the files of all the mappers to find the
             * contributions to the partition it has to elaborate. But if we put
             * the identifier of a reducer in the name of a spill file we must
             * assure that the spill file will contain only the data for that
             * reducer. This means the spill file will contain only the values
             * belonging to one partition and that we will split the contribute
             * of a mapper task for a partition to more than one file ( while in
             * the first implementation in writing the intermediate file we have
             * that each intermediate file contains the whole set of values a
             * mapper produced as its own contribution to a given partition ).
             * So this solution appears not to be suitable. A possible solution
             * is to maintain the hadoop logic in producing the intermediate
             * spill files ( that is to say a spill file can contain data
             * belonging to more than one partition ) and to build an auxiliary
             * (and serializable ) data structure that will be passed to each
             * reducer. That auxiliary data structure will tell in which file
             * the data for a given partition is stored. The file in which the
             * data for a partition are stored is identified by two integer, the
             * id of the mapper task that writes the file and the id of the
             * spill the same mapper put into that file. The task that follows
             * the mapper replicas in the execution flow could merge all the
             * contributions of the different mapper task to build a global list
             * that will say into which files the values belonging to a given
             * partition reside. Then each reducer will receive that list. With
             * it a reducer can find which files it has to open. The only
             * problem is that we have to check if the same file can be read by
             * more than one reducer (ProActive task).
             */
            String mapperId = configuration.get(PAMapReduceFrameworkProperties.PA_MAPREDUCE_TASK_IDENTIFIER
                    .getKey());
            final Path filename = new Path(PAMapReduceFramework.getSpillFileName(mapperId, "" + numSpills));
            out = fileSystem.create(filename);

            final int endPosition = (kvend > kvstart) ? kvend : kvoffsets.length + kvend;
            sorter.sort(MapperBufferedOutputCollector.this, kvstart, endPosition);
            int spindex = kvstart;
            IndexRecord rec = new IndexRecord();
            InMemValBytes value = new InMemValBytes();
            for (int i = 0; i < partitions; ++i) {
                IntermediateFile.Writer<K, V> writer = null;
                // IFile.Writer<K, V> writer = null;
                try {
                    long segmentStart = out.getPos();
                    writer = new Writer<K, V>(configuration, out, keyClass, valClass);
                    if (combinerRunner == null) {
                        // spill directly
                        DataInputBuffer key = new DataInputBuffer();
                        while (spindex < endPosition &&
                            kvindices[kvoffsets[spindex % kvoffsets.length] + PARTITION] == i) {
                            final int kvoff = kvoffsets[spindex % kvoffsets.length];
                            getVBytesForOffset(kvoff, value);
                            key.reset(kvbuffer, kvindices[kvoff + KEYSTART],
                                    (kvindices[kvoff + VALSTART] - kvindices[kvoff + KEYSTART]));
                            writer.append(key, value);
                            ++spindex;
                        }
                    } else {
                        int spstart = spindex;
                        while (spindex < endPosition &&
                            kvindices[kvoffsets[spindex % kvoffsets.length] + PARTITION] == i) {
                            ++spindex;
                        }
                        // Note: we would like to avoid the combiner if we've
                        // fewer
                        // than some threshold of records for a partition
                        if (spstart != spindex) {
                            combineOutputCollector.setWriter(writer);
                            RawKeyValueIterator kvIter = new MRResultIterator(spstart, spindex);
                            long combinerStartTime = System.currentTimeMillis();
                            combinerRunner.combine(kvIter, combineOutputCollector);
                            long combinerEndTime = System.currentTimeMillis();
                            combinerExecutionTime += (combinerEndTime - combinerStartTime);
                        }
                    }

                    // close the writer
                    writer.close();

                    // record offsets
                    rec.startOffset = segmentStart;
                    rec.rawLength = writer.getRawLength();
                    rec.partLength = writer.getCompressedLength();
                    spillRec.putIndex(rec, i);

                    writer = null;
                } finally {
                    if (null != writer)
                        writer.close();
                }
            }

            if (totalIndexCacheMemory >= INDEX_CACHE_MEMORY_LIMIT) {
                // create spill index file
                Path indexFilename = new Path(PAMapReduceFramework.getSpillIndexFileName(mapperId, "" +
                    numSpills));
                // spillRec.writeToFile(indexFilename, configuration);
                spillRec.writeToFile(indexFilename, fileSystem);
            } else {
                indexCacheList.add(spillRec);
                totalIndexCacheMemory += spillRec.size() * MAP_OUTPUT_INDEX_RECORD_LENGTH;
            }
            // increment the number of spills so that the next spill will have a
            // different index
            ++numSpills;
        } finally {
            if (out != null)
                out.close();
        }
    }

    protected class SpillThread extends Thread {
        @Override
        public void run() {
            spillLock.lock();
            spillThreadRunning = true;
            try {
                while (true) {
                    spillDone.signal();
                    while (kvstart == kvend) {
                        spillReady.await();
                    }
                    try {
                        spillLock.unlock();

                        /** @START_PROFILE */
                        // logger.profile("Start of the spillAndSort in the SpillThread");
                        long spillAndSortStartTime = System.currentTimeMillis();
                        /** @END_PROFILE */

                        sortAndSpill();

                        /** @START_PROFILE */
                        long spillAndSortEndTime = System.currentTimeMillis();
                        spillingAndSortingExecutionTime += (spillAndSortEndTime - spillAndSortStartTime);
                        // logger.profile("End of the spillAndSort in the SpillThread. It takes '"
                        // + ( spillAndSortEndTime - spillAndSortStartTime ) +
                        // "' milliseconds");
                        /** @END_PROFILE */

                    } catch (Exception e) {
                        sortSpillException = e;
                    } catch (Throwable t) {
                        sortSpillException = t;
                        /*
                         * TODO change the way this error is managed for example
                         * look at the corresponding Hadoop class
                         * MapTask.MapOutputBuffer.SpillThread
                         */
                    } finally {
                        spillLock.lock();
                        if (bufend < bufindex && bufindex < bufstart) {
                            bufvoid = kvbuffer.length;
                        }
                        kvstart = kvend;
                        bufstart = bufend;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                spillLock.unlock();
                spillThreadRunning = false;
            }
        }
    }

    /**
     * {@inheritDoc} We must notice that the kvindices array is the array that
     * stores the accounting informations about the key-value pairs. That
     * informations are: - the partition the key-value pair belongs to - the
     * offset of the key relative to the beginning of the kvbuffer buffer - the
     * offset of the value relative to the beginning of the kvbuffer buffer
     * MapperBufferedOutputCollector.compare(i, j) means we must compare the
     * record i and the record j. Particularly we must compare the partition,
     * and then the key. The information about the partition and about the
     * offset (in the kvbuffer) at which the serialized key starts are stored in
     * the accounting buffer, the kvindices buffer. The kvindices buffer stores
     * the accounting informations of all the records and to retrieve the
     * informations corresponding to the desired buffer we need an index. That
     * index is stored in the kvoffset buffer ( which actually stores indices
     * into the kvindices buffer as the comment near the declaration of the
     * kvoffsets assert). We must also notice that the kvoffset buffer is
     * circular so we use the mod operator to bound the value of the records
     * index inside the buffer. Hence the code of the method works as follow: -
     * retrieve the indices with which we can retrieve the accounting
     * informations corresponding to the two records we must compare - retrieve
     * the partitions to which each of records to compare belongs to - compare
     * the partitions - retrieve the offset (relative to the kvbuffer, the
     * buffer that stores in memory the serialized versions of the key and
     * values of each record) at which the keys to compare start - compare the
     * keys roughly (this means in byte, without deserialize them). To do this
     * we must notice that the the length in bytes of the key is computed as the
     * difference between the offset at which the value start and the offset at
     * which the key itself starts.
     */
    @Override
    public int compare(int i, int j) {
        /*
         * We must notice that this class first sort by partition and then by
         * key. While it does not sort by value.
         */
        final int ii = kvoffsets[i % kvoffsets.length];
        final int ij = kvoffsets[j % kvoffsets.length];
        // sort by partition
        if (kvindices[ii + PARTITION] != kvindices[ij + PARTITION]) {
            return kvindices[ii + PARTITION] - kvindices[ij + PARTITION];
        }
        // sort by key
        return comparator.compare(kvbuffer, kvindices[ii + KEYSTART], kvindices[ii + VALSTART] -
            kvindices[ii + KEYSTART], kvbuffer, kvindices[ij + KEYSTART], kvindices[ij + VALSTART] -
            kvindices[ij + KEYSTART]);
    }

    @Override
    public void swap(int i, int j) {
        i %= kvoffsets.length;
        j %= kvoffsets.length;
        int tmp = kvoffsets[i];
        kvoffsets[i] = kvoffsets[j];
        kvoffsets[j] = tmp;
    }

    /**
     * Given an offset, populate vbytes with the associated set of deserialized
     * value bytes. Should only be called during a spill.
     */
    protected void getVBytesForOffset(int kvoff, InMemValBytes vbytes) {
        final int nextindex = (kvoff / ACCTSIZE == (kvend - 1 + kvoffsets.length) % kvoffsets.length) ? bufend
                : kvindices[(kvoff + ACCTSIZE + KEYSTART) % kvindices.length];
        int vallen = (nextindex >= kvindices[kvoff + VALSTART]) ? nextindex - kvindices[kvoff + VALSTART]
                : (bufvoid - kvindices[kvoff + VALSTART]) + nextindex;
        vbytes.reset(kvbuffer, kvindices[kvoff + VALSTART], vallen);
    }

    /**
     * Inner class wrapping valuebytes, used for appendRaw.
     */
    protected class InMemValBytes extends DataInputBuffer {
        protected byte[] buffer;
        protected int start;
        protected int length;

        public void reset(byte[] buffer, int start, int length) {
            this.buffer = buffer;
            this.start = start;
            this.length = length;

            if (start + length > bufvoid) {
                this.buffer = new byte[this.length];
                final int taillen = bufvoid - start;
                System.arraycopy(buffer, start, this.buffer, 0, taillen);
                System.arraycopy(buffer, 0, this.buffer, taillen, length - taillen);
                this.start = 0;
            }

            super.reset(this.buffer, this.start, this.length);
        }
    }

    /**
     * Exception indicating that the allocated sort buffer is insufficient to
     * hold the current record. We must notice we are forced to copy and paste
     * also the code of this exception because the
     * org.apache.hadoop.mapred.MapTask.MapBufferTooSmallException is not
     * visible from the outside of the org.apache.hadoop.mapred.MapTask
     *
     * @author The ProActive Team
     *
     */
    protected static class MapBufferTooSmallException extends IOException {
        public MapBufferTooSmallException(String s) {
            super(s);
        }
    }

    protected class MRResultIterator implements RawKeyValueIterator {
        private final DataInputBuffer keybuf = new DataInputBuffer();
        private final InMemValBytes vbytes = new InMemValBytes();
        private final int end;
        private int current;

        public MRResultIterator(int start, int end) {
            this.end = end;
            current = start - 1;
        }

        public boolean next() throws IOException {
            return ++current < end;
        }

        public DataInputBuffer getKey() throws IOException {
            final int kvoff = kvoffsets[current % kvoffsets.length];
            keybuf.reset(kvbuffer, kvindices[kvoff + KEYSTART], kvindices[kvoff + VALSTART] -
                kvindices[kvoff + KEYSTART]);
            return keybuf;
        }

        public DataInputBuffer getValue() throws IOException {
            getVBytesForOffset(kvoffsets[current % kvoffsets.length], vbytes);
            return vbytes;
        }

        public Progress getProgress() {
            return null;
        }

        public void close() {
        }
    }
}
