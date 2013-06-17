package functionaltests.ext.mapreduce;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on Hadoop test org.apache.hadoop.mapreduce.TestMapReduceLocal.
 * 
 * Modified for ProActive MapReduce.
 */

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceFramework;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJobConfiguration;
import org.ow2.proactive.scheduler.ext.mapreduce.ReadMode;
import org.ow2.proactive.scheduler.ext.mapreduce.WriteMode;

import functionaltests.SchedulerConsecutive;


/**
 * MapReduce test using word count example. Test basic functionality, various
 * read modes, setting the number of reducers and input split size, using
 * combiner class.
 */
public class TestMapReduce extends SchedulerConsecutive {

    /** 
     * Together with {@link IntSumReducer} implements word count algorithm.
     */
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    /** 
     * Together with {@link TokenizerMapper} implements word count algorithm.
     */
    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    private static final MapReduceTHelper helper = new MapReduceTHelper(TestMapReduce.class.getName());

    private static final String INPUT1 = "this is a test\nof a word count test\nword\nword\n";
    private static final String INPUT2 = "more test";
    private static final String EXPECTED_OUTPUT = "a\t2\ncount\t1\nis\t1\nmore\t1\nof\t1\ntest\t3\nthis\t1\nword\t3\n";

    @org.junit.Test
    public void run() throws Throwable {

        // default configuration
        Job job = prepareHadoopJob(true);
        PAMapReduceJobConfiguration pamrjc = MapReduceTHelper.getConfiguration();
        JobResult result = submitAndCheck(job, pamrjc);
        JobInfo jobInfo = result.getJobInfo();
        // Splitter, 2*Mapper (1 per input file), MapperJoin, Reducer,
        // ReducerJoin -- 6 tasks overall
        Assert.assertEquals(6, jobInfo.getTotalNumberOfTasks());

        // fullLocalRead + remoteWrite
        job = prepareHadoopJob(false);
        pamrjc = MapReduceTHelper.getConfiguration();
        pamrjc.setReadMode(PAMapReduceFramework.SPLITTER_PA_TASK, ReadMode.fullLocalRead);
        pamrjc.setReadMode(PAMapReduceFramework.MAPPER_PA_TASK, ReadMode.fullLocalRead);
        pamrjc.setReadMode(PAMapReduceFramework.REDUCER_PA_TASK, ReadMode.fullLocalRead);
        pamrjc.setWriteMode(PAMapReduceFramework.MAPPER_PA_TASK, WriteMode.remoteWrite);
        pamrjc.setWriteMode(PAMapReduceFramework.REDUCER_PA_TASK, WriteMode.remoteWrite);
        submitAndCheck(job, pamrjc);

        // partialLocalRead + remoteRead
        job = prepareHadoopJob(true);
        pamrjc = MapReduceTHelper.getConfiguration();
        pamrjc.setReadMode(PAMapReduceFramework.SPLITTER_PA_TASK, ReadMode.remoteRead);
        pamrjc.setReadMode(PAMapReduceFramework.MAPPER_PA_TASK, ReadMode.partialLocalRead);
        pamrjc.setReadMode(PAMapReduceFramework.REDUCER_PA_TASK, ReadMode.remoteRead);
        submitAndCheck(job, pamrjc);

        // 3 reducers
        job = prepareHadoopJob(true);
        job.setNumReduceTasks(3);
        pamrjc = MapReduceTHelper.getConfiguration();
        // the output of multiple reducers is not sorted globally, sort it
        // before comparing
        result = submitAndCheck(job, pamrjc, true);
        jobInfo = result.getJobInfo();
        // Splitter, 2*Mapper (1 per input file), MapperJoin, 3*Reducer,
        // ReducerJoin -- 8 tasks overall
        Assert.assertEquals(8, jobInfo.getTotalNumberOfTasks());

        // inputSplitSize = 10
        job = prepareHadoopJob(false);
        pamrjc = MapReduceTHelper.getConfiguration();
        pamrjc.setInputSplitSize(10);
        result = submitAndCheck(job, pamrjc);
        jobInfo = result.getJobInfo();
        // Splitter, 6*Mapper, MapperJoin, Reducer, ReducerJoin -- 10 tasks
        // overall
        Assert.assertEquals(10, jobInfo.getTotalNumberOfTasks());

        helper.cleanup();

    }

    private Job prepareHadoopJob(boolean combiner) throws Throwable {

        helper.cleanup();

        // generate input
        helper.writeFile("in/part1", INPUT1);
        helper.writeFile("in/part2", INPUT2);

        // create and configure Hadoop job
        Configuration conf = new Configuration();
        Job job = new Job(conf, "word count");
        job.setMapperClass(TokenizerMapper.class);
        if (combiner) {
            job.setCombinerClass(IntSumReducer.class);
        }
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setInputFormatClass(TextInputFormat.class);
        FileInputFormat.addInputPath(job, new Path("part1"));
        FileInputFormat.addInputPath(job, new Path("part2"));
        FileOutputFormat.setOutputPath(job, new Path("output"));

        return job;
    }

    /**
     * Submit the job, wait for completion, check that the result is correct. Do
     * not sort the output before checking.
     */
    private JobResult submitAndCheck(Job job, PAMapReduceJobConfiguration pamrjc) throws Throwable {
        return submitAndCheck(job, pamrjc, false);
    }

    /**
     * Submit the job, wait for completion, check that the result is correct.
     * Optionally sort the output before checking.
     */
    private JobResult submitAndCheck(Job job, PAMapReduceJobConfiguration pamrjc, boolean sort)
            throws Throwable {

        pamrjc.setInputSpace((new File(helper.getRootDir(), "in")).toURI().toURL().toString());
        pamrjc.setOutputSpace((new File(helper.getRootDir(), "out")).toURI().toURL().toString());

        // submit PAMapReduceJob and wait for completion
        JobResult result = MapReduceTHelper.submit(job, pamrjc);

        // check output
        String out = helper.readFiles("out/output", sort);
        System.out.println(out);
        Assert.assertEquals("Output of the job", EXPECTED_OUTPUT, out);
        return result;
    }

}
