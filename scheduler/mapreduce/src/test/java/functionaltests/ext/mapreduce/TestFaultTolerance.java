/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */

package functionaltests.ext.mapreduce;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.Assert;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceFramework;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJobConfiguration;

import functionaltests.SchedulerConsecutive;


/**
 * Test that the sort MapReduce job produces correct result even if first
 * iterations of mapper and reducer fail, given that maxNumberOfExecutions is
 * greater than 1.
 */
public class TestFaultTolerance extends SchedulerConsecutive {

    /**
     * This Mapper will fail the first time it is run, but will work on
     * consecutive runs. Relies on a file created in tmpdir and thus requires
     * that reruns happen on the same host as the first run.
     */
    public static class FailingMapper extends Mapper<Object, Text, Text, Text> {

        private Text word = new Text();

        public static final File MAPPER_MARKER = new File(System.getProperty("java.io.tmpdir") +
            File.separator + TestFaultTolerance.class.getName() + ".mapper.marker");

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            if (MAPPER_MARKER.createNewFile()) {
                throw new RuntimeException("First iteration of the mapper fails");
            }
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, word);
            }
        }
    }

    /**
     * This Reducer will fail the first time it is run, but will work on
     * consecutive runs. Relies on a file created in tmpdir and thus requires
     * that reruns happen on the same host as the first run.
     */
    public static class FailingReducer extends Reducer<Text, Text, Text, NullWritable> {

        public static final File REDUCER_MARKER = new File(System.getProperty("java.io.tmpdir") +
            File.separator + TestFaultTolerance.class.getName() + ".reducer.marker");

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
                InterruptedException {
            if (REDUCER_MARKER.createNewFile()) {
                throw new RuntimeException("First iteration of the reducer fails");
            }
            for (Text val : values) {
                context.write(val, null);
            }
        }
    }

    private static final String INPUT = "a\nb\nsome\nwords\nin\nb\nno\nparticular\nwords\norder\n";
    private static final String EXPECTED_OUTPUT = "a\nb\nb\nin\nno\norder\nparticular\nsome\nwords\nwords\n";

    private static final MapReduceTHelper helper = new MapReduceTHelper(TestFaultTolerance.class.getName());

    private static void cleanup() throws IOException {

        helper.cleanup();
        FailingMapper.MAPPER_MARKER.delete();
        FailingReducer.REDUCER_MARKER.delete();

    }

    @org.junit.Test
    public void run() throws Throwable {

        cleanup();

        // generate input
        helper.writeFile("in/input", INPUT);
        helper.mkdirs("out");

        // create and configure Hadoop job
        Configuration conf = new Configuration();
        Job job = new Job(conf, "fault tolerance");
        job.setMapperClass(FailingMapper.class);
        job.setReducerClass(FailingReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(TextInputFormat.class);
        FileInputFormat.addInputPath(job, new Path("input"));
        FileOutputFormat.setOutputPath(job, new Path("output"));

        PAMapReduceJobConfiguration pamrjc = MapReduceTHelper.getConfiguration();

        pamrjc.setInputSpace((new File(helper.getRootDir(), "in")).toURI().toURL().toString());
        pamrjc.setOutputSpace((new File(helper.getRootDir(), "out")).toURI().toURL().toString());
        pamrjc.setInputSplitSize(20);
        //pamrjc.setMaxNumberOfExecutions(2);
        pamrjc.setMaxNumberOfExecutions(PAMapReduceFramework.MAPPER_PA_TASK, 2);
        pamrjc.setMaxNumberOfExecutions(PAMapReduceFramework.REDUCER_PA_TASK, 2);

        // submit PAMapReduceJob and wait for completion
        MapReduceTHelper.submit(job, pamrjc);

        // check output
        String out = helper.readFiles("out/output", false);
        System.out.println(out);
        Assert.assertEquals("Output of the sort job", EXPECTED_OUTPUT, out);
        Assert.assertTrue("File should be created by 1st iteration of mapper", FailingMapper.MAPPER_MARKER
                .exists());
        Assert.assertTrue("File should be created by 1st iteration of reducer", FailingReducer.REDUCER_MARKER
                .exists());

        cleanup();

    }

}
