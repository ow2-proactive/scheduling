package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptID;


public class FakeHadoopMapperContext extends Context {

    public FakeHadoopMapperContext(Mapper mapper, Configuration configuration, TaskAttemptID taskAttemptID,
            RecordReader recordReader, RecordWriter recordWriter, OutputCommitter outputCommitter,
            // StatusReporter statusReporter,
            InputSplit inputSplit) throws IOException, InterruptedException {

        /*
         * notice that the status reporter can be easily nullified as we can see
         * in the class org.apache.hadoop.mapreduce.TaskInputOutputContext While
         * the TaskAttempID cannot be null because during the creation of the
         * Hadoop Mapper.Context object the method TaskAttemptID.getJobID() is
         * called to retrieve the org.apache.hadoop.mapreduce.JobID
         */
        mapper.super(configuration, taskAttemptID, recordReader, recordWriter, outputCommitter, null,
                inputSplit);
    }

}
