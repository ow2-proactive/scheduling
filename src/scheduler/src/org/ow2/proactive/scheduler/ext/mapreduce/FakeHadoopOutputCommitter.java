package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;


public class FakeHadoopOutputCommitter extends OutputCommitter {

    @Override
    public void abortTask(TaskAttemptContext taskAttemptContext) throws IOException {
        // do nothing
    }

    @Override
    public void cleanupJob(JobContext jobContext) throws IOException {
        // do nothing
    }

    @Override
    public void commitTask(TaskAttemptContext taskAttemptContext) throws IOException {
        /*
         * do nothing because the in ProActive the Task output is automatically
         * transferred to the job output directory after the Task ends via the
         * DataSpaces mechanism
         */
    }

    @Override
    public boolean needsTaskCommit(TaskAttemptContext taskAttemptContext) throws IOException {
        // do nothing
        return false;
    }

    @Override
    public void setupJob(JobContext jobContext) throws IOException {
        // do nothing
    }

    @Override
    public void setupTask(TaskAttemptContext taskAttemptContext) throws IOException {
        // do nothing
    }

}
