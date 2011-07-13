package org.ow2.proactive.scheduler.ext.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.mapred.RawKeyValueIterator;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.StatusReporter;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.Reducer.Context;


/**
 * The {@link FakeHadoopReducerContext} simulate the behavior of the
 * {@link Reducer.Context} class. We need this class to be able to execute an
 * Hadoop Reducer isnide the ProActive MapReduce API/framework.
 *
 * @author The ProActive Team
 *
 */
public class FakeHadoopReducerContext extends Context {

    public FakeHadoopReducerContext(Reducer reducer, Configuration configuration,
            TaskAttemptID taskAttemptID, RawKeyValueIterator rawKeyValueIterator, Counter counter1,
            Counter counter2, RecordWriter recordWriter, OutputCommitter outputCommitter,
            StatusReporter statusReporter, RawComparator rawComparator, Class class1, Class class2)
            throws IOException, InterruptedException {
        reducer.super(configuration, taskAttemptID, rawKeyValueIterator, counter1, counter2, recordWriter,
                outputCommitter, statusReporter, rawComparator, class1, class2);
    }
}
