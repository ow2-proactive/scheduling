package org.ow2.proactive.scheduler.ext.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.ow2.proactive.scheduler.ext.mapreduce.fs.PADataSpacesFileSystem;


public class FakeHadoopTaskAttemptContext extends TaskAttemptContext {

    protected Configuration configuration = null;

    /**
     * The {@link FakeHadoopTaskAttemptContext} customize the behavior of the
     * {@link JobContext#getConfiguration()} method to return a true
     * {@link Configuration} instance and not a {@link JobConf} instances. We
     * need to retrieve the Configuration instance instead of the
     * {@link JobConf} one because the {@link JobConf} is deprecated and the
     * configuration used inside the ProActive MapReduce is an instance of the
     * class {@link PAMapReduceJobConfiguration} that extends
     * {@link Configuration} and we use the {@link PAMapReduceJobConfiguration}
     * instance to initialize the file system based on the data spaces. See the
     * method
     * {@link PADataSpacesFileSystem#initialize(java.net.URI, Configuration)} to
     * get more details.
     *
     * @author The ProActive Team
     *
     */
    public FakeHadoopTaskAttemptContext(Configuration conf, TaskAttemptID taskId) {
        super(conf, taskId);
        configuration = conf;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
