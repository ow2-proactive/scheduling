package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.ReflectionUtils;


/**
 * The {@link MapperRecordWriter} class contains code that is a copy and paste
 * of the code of the Hadoop MapTask.NewOutputCollector. We chose to copy and
 * paste the Hadoop code to have more control on how the intermediary results
 * are handled in the ProActive MapReduce framework, and also because the
 * classes the in Hadoop handle intermediary results are not visible from the
 * outside
 *
 * @author The ProActive Team
 *
 * @param <K>
 *            the class of the key
 * @param <V>
 *            the class of the value
 */
public class MapperRecordWriter<K, V> extends RecordWriter<K, V> {

    protected MapperOutputCollector<K, V> mapperOutputCollector = null;
    protected Partitioner<K, V> partitioner = null;
    protected final int partitions;

    /**
     * Create the {@link MapperRecordWriter} instance. That instance is used to
     * have the same behavior as Hadoop when the mapper (of a mapreduce job)
     * create intermediate files to store its own output data
     *
     * @param jobContext
     *            the configuration of the mapreduce job
     * @param fileSystem
     *            the file system in which the intermediate data must be stored
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public MapperRecordWriter(TaskAttemptContext taskAttemptContext, FileSystem fileSystem)
            throws ClassNotFoundException, IOException {
        mapperOutputCollector = new MapperBufferedOutputCollector<K, V>(taskAttemptContext, fileSystem);
        partitions = taskAttemptContext.getNumReduceTasks();
        if (partitions > 0) {
            partitioner = (Partitioner<K, V>) ReflectionUtils.newInstance(taskAttemptContext
                    .getPartitionerClass(), taskAttemptContext.getConfiguration());
        } else {
            partitioner = new Partitioner<K, V>() {
                @Override
                public int getPartition(K key, V value, int numPartitions) {
                    return -1;
                }
            };
        }
    }

    @Override
    public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        try {
            mapperOutputCollector.flush();
        } catch (ClassNotFoundException cnfe) {
            throw new IOException("Cannot find class " + cnfe);
        }
        mapperOutputCollector.close();
    }

    @Override
    public void write(K key, V value) throws IOException, InterruptedException {
        mapperOutputCollector.collect(key, value, partitioner.getPartition(key, value, partitions));
    }
}
