package org.ow2.proactive.scheduler.ext.mapreduce.examples;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJob;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJobConfiguration;
import org.ow2.proactive.scheduler.ext.mapreduce.exception.PAJobConfigurationException;


public class PAMapReduceJobMain {

    protected static String HADOOP_DEFAULT = "HadoopDefault";
    protected static String HADOOP_WORD_COUNT = "HadoopWordCount";
    protected static String TALEND_SORT = "TalendSort";
    protected static String TALEND_AVERAGE = "TalendAverage";
    protected static String TALEND_COUNT = "TalendCount";

    /**
     * To work, this main method needs, potentially, the path to a configuration
     * file to create an instance of the {@link PAMapReduceJobConfiguration}
     * invoking the constructor
     * {@link PAMapReduceJobConfiguration#PAMapReduceJobConfiguration(File)}. If
     * the path to the configuration file is not specified than the user can
     * create an instance from scratch using the PAMapReduceJobConfiguration
     * no-args constructor and invoking all the needed setters on that instance.
     *
     * Another argument is the number of Reducer tasks to execute.
     *
     * This main method needs also the path of the input file the MapReduce job
     * must elaborate. We must notice that if that path is relative then the
     * Hadoop MapReduce framework will resolve that path against the absolute
     * path pointed by the "user.dir" system property (that value in the case
     * this main method is executed from inside eclipse will be
     * "/home/eborelli/workspace/scheduler_mapreduce_client"). In the
     * construction of the ProActive MapReduce job we must resolve the relative
     * path of the input file against the user defined INPUT data space. If the
     * input path point to a single file then only that file will be
     * "mapreduced" while if the input path point to a folder, then all the
     * files in that folder are "mapreduced".
     *
     * This main method also needs the path of the output folder (the folder in
     * which the user wants to store the MapReduce output data). This path will
     * be resolved against the user defined OUTPUT data space for the ProActive
     * MapReduce job. That output path cannot be empty because otherwise an
     * exception will occur when the Hadoop reused classes try to instantiate a
     * Path object from an empty string. This means that the user specified
     * output path will point to a sub-directory of the user specified OUTPUT
     * space.
     *
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     * @throws PAJobConfigurationException
     */
    public static void main(String[] args) throws IOException, URISyntaxException,
            PAJobConfigurationException {
        int paMapReduceConfigurationFileArgumentIndex = 0;
        int inputFilePathArgumentIndex = 0;
        int outputFolderPathArgumentIndex = 0;
        int numberOfReducersArgumentIndex = 0;
        int applicationNameIndex = 0;
        if (args.length < 1) {
            System.err
                    .println("Usage: hadoopMapReduceApplication [<paMapReduceConfigurationFile>] numberOfReducers inputFilePath outputFolder applicationName");
            System.exit(2);
        } else {
            if (args.length == 4) {
                numberOfReducersArgumentIndex = 0;
                inputFilePathArgumentIndex = 1;
                outputFolderPathArgumentIndex = 2;
                applicationNameIndex = 3;
            } else {
                paMapReduceConfigurationFileArgumentIndex = 0;
                numberOfReducersArgumentIndex = 1;
                inputFilePathArgumentIndex = 2;
                outputFolderPathArgumentIndex = 3;
                applicationNameIndex = 4;
            }
        }

        Configuration conf = new Configuration();

        // we build an instance of the Hadoop MapReduce Job
        Job job = null;
        try {

            /*
             * Before we build the job we must notice that
             * the definition of the class of the MapReduce job output key-value
             * pairs is not compulsory because they have a default value as we
             * can see from the methods
             * PAHadoopJobConfiguration.getOutputKeyClass and
             * PAHadoopJobConfiguration.getOutputValueClass that defines
             * respectively the org.apache.hadoop.io.LongWritable class as the
             * default value of the key and org.apache.hadoop.io.Text as the
             * default value for the value. But the user can define its own
             * classes. We must notice that when the default Mapper and Reducer
             * Hadoop classes are used the job will be executed only if the user
             * define the org.apache.hadoop.io.LongWritable and
             * org.apache.hadoop.io.Text respectively as the class of the key
             * and the value or if the user does not specify at all the classes
             * of the output key and output value.
             */

            if ((args[applicationNameIndex]).equalsIgnoreCase(HADOOP_DEFAULT)) {
                job = new Job(conf, HADOOP_DEFAULT);
                job.setJarByClass(PAMapReduceJobMain.class);
                job.setMapperClass(Mapper.class);
                job.setReducerClass(Reducer.class);
            } else if ((args[applicationNameIndex]).equalsIgnoreCase(HADOOP_WORD_COUNT)) {
                job = new Job(conf, HADOOP_WORD_COUNT);
                job.setJarByClass(PAMapReduceJobMain.class);
                job.setMapperClass(TokenizerMapper.class);
                job.setReducerClass(IntSumReducer.class);
                job.setCombinerClass(IntSumReducerCombiner.class);
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(IntWritable.class);
            } else if ((args[applicationNameIndex]).equalsIgnoreCase(TALEND_AVERAGE)) {
                job = new Job(conf, TALEND_AVERAGE);
                job.setJarByClass(PAMapReduceJobMain.class);
                job.setMapperClass(TalendAverageLineitemDiscountForItemMapper.class);
                job.setReducerClass(TalendAverageLineitemDiscountForItemReducer.class);
                job.setCombinerClass(TalendAverageLineitemDiscountForItemReducer.class);
                //job.setMapOutputKeyClass(LongWritable.class);
                //job.setMapOutputValueClass(DoubleWritable.class);
                job.setOutputKeyClass(LongWritable.class);
                job.setOutputValueClass(DoubleWritable.class);
            } else if ((args[applicationNameIndex]).equalsIgnoreCase(TALEND_SORT)) {
                job = new Job(conf, TALEND_SORT);
                job.setJarByClass(PAMapReduceJobMain.class);
                job.setMapperClass(TalendSortLineitemShipModeMapper.class);
                job.setReducerClass(TalendSortLineitemShipModeReducer.class);
                job.setMapOutputKeyClass(Text.class);
                job.setMapOutputValueClass(Text.class);
                job.setOutputKeyClass(NullWritable.class);
                job.setOutputValueClass(Text.class);
            } else if ((args[applicationNameIndex]).equalsIgnoreCase(TALEND_COUNT)) {
                job = new Job(conf, TALEND_COUNT);
                job.setJarByClass(PAMapReduceJobMain.class);
                job.setMapperClass(TalendCountOrderLinesShipModeShipDateYearMapper.class);
                job.setReducerClass(TalendCountOrderLinesShipModeShipDateYearReducer.class);
                job.setCombinerClass(TalendCountOrderLinesShipModeShipDateYearReducer.class);
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(IntWritable.class);
            } else {
                System.out.println("The specified application is not implemented yet");
                System.exit(0);
            }

            /*
             * We set the number of reducer tasks to execute. We must notice
             * that if that value is not specify then the default value will be
             * "1" as we can see looking at the
             * org.apache.hadoop.mapreduce.JobContext.getNumReduceTasks()
             */
            try {
                job.setNumReduceTasks(Integer.parseInt(args[numberOfReducersArgumentIndex]));
            } catch (NumberFormatException nfe) {
                /*
                 * do not set the number of reducer. This can be used to
                 * simulate the behavior of the ProActive MapReduce job when the
                 * number of reducers to execute is not specified
                 */
            }

            /*
             * We add the input files for the Hadoop job
             */
            FileInputFormat.addInputPath(job, new Path(args[inputFilePathArgumentIndex]));

            /*
             * We set the output directory for the Hadoop job. We must notice
             * that we are forced to create the output directory as a
             * sub-directory in the OUTPUT data space defined for the mapreduce
             * job (because we cannot create an empty org.apache.hadoop.fs.Path)
             */
            FileOutputFormat.setOutputPath(job, new Path(args[outputFolderPathArgumentIndex]));
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(2);
        }

        /*
         * We build the ProActive MapReduce configuration from the specified
         * configuration file or from scratch if such a configuration file was
         * not specified
         */
        PAMapReduceJobConfiguration pamrjc = null;
        if (paMapReduceConfigurationFileArgumentIndex >= 0) {
            File paMapReduceConfigurationFile = new File(args[paMapReduceConfigurationFileArgumentIndex]);
            pamrjc = new PAMapReduceJobConfiguration(paMapReduceConfigurationFile);
        } else {
            pamrjc = new PAMapReduceJobConfiguration();
            /*
             * the user here must explicitly declare the ProActive MapReduce
             * workflow configuration parameters (canceljobOnError attribute,
             * etc...) otherwise default values are used. E.g.,
             * pamrjc.setCancelJobOnError(PAMapReduceFramework.SPLITTER_PA_TASK,
             * true);
             */
        }

        /*
         * We build the ProActive MapReduce job
         */
        PAMapReduceJob pamrj = new PAMapReduceJob(job, pamrjc);

        // We run the Hadoop MapReduce Job
        if (pamrj != null) {
            if (pamrj.run()) {
                System.out.println("The ProActive MapReduce job is correctly submitted!");
            } else {
                System.out.println("The ProActive MapReduce job is NOT submitted");
            }
        }

        System.exit(0);
    }

    /**
     * This class is part of the Hadoop MapReduce WordCount example application
     * in Hadoop 0.20.2
     */
    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), "|");
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    /**
     * This class is part of the Hadoop MapReduce WordCount example application
     * in Hadoop 0.20.2
     */
    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

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

    /**
     * This class is part of the Hadoop MapReduce WordCount example application
     * in Hadoop 0.20.2
     */
    public static class IntSumReducerCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
                InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }

        @Override
        public void run(Context context) throws IOException, InterruptedException {
            setup(context);

            while (context.nextKey()) {
                reduce((Text) context.getCurrentKey(), context.getValues(), context);
            }
            cleanup(context);
        }
    }

    /**
     * This class implement the mapper of the Hadoop application that
     * sort the lineitem.tbl file generated using Dbgen on the
     * L_SHIPDATE column.
     * To do that the key value pair generated by the mapper are respectively
     * the value of the L_SHIPDATE column and the whole record (the set of all
     * columns, that is to say the whole line of the lineitem.tbl file)
     * This mapper is compliant with Hadoop 0.20.2
     */
    public static class TalendSortLineitemShipModeMapper extends Mapper<Object, Text, Text, Text> {

        private final static int L_SHIP_DATE_INDEX = 10;
        private Text lShipDate = new Text();
        private Text line = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            /*
             * key: the offset in bytes from the start of the lineitem.tbl file
             * value: a line of the lineitem.tbl file
             * We must notice that to use the "|" character in a regex we need
             * to double backslash it
             */
            String[] columns = value.toString().split("\\|");
            lShipDate.set(columns[L_SHIP_DATE_INDEX]);
            line.set(value);
            context.write(lShipDate, line);
        }
    }

    /**
     * This class implement the reducer of the Hadoop application that
     * sort the lineitem.tbl file generated using Dbgen on the
     * L_SHIPDATE column.
     * To do that the key value pair generated by the mapper are respectively
     * the value of the L_SHIPDATE column and the whole record (the set of all
     * columns, that is to say the whole line of the lineitem.tbl file).
     * The only thing the reducer must do is to output the value it receives.
     * This reducer is compliant with Hadoop 0.20.2
     */
    public static class TalendSortLineitemShipModeReducer extends Reducer<Text, Text, NullWritable, Text> {
        private NullWritable nullKey = NullWritable.get();
        private Text result = new Text();

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
                InterruptedException {
            for (Text val : values) {
                result.set(val);
                context.write(nullKey, result);
            }
        }
    }

    /**
     * This class implement the mapper of the Hadoop application that
     * compute the average discount (L_DISCOUNT) for each item (L_PARTKEY).
     * To do that the key value pair generated by the mapper are respectively
     * the value of the L_PARTKEY column and the value of the L_DISCOUNT column
     * of the lineitem.tbl file.
     * This mapper is compliant with Hadoop 0.20.2
     */
    public static class TalendAverageLineitemDiscountForItemMapper extends
            Mapper<Object, Text, LongWritable, DoubleWritable> {

        private final static int L_PART_KEY_INDEX = 1;
        private final static int L_DISCOUNT_INDEX = 6;
        private LongWritable lPartKey = new LongWritable();
        private DoubleWritable lDiscount = new DoubleWritable();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            /*
             * key: the offset in bytes from the start of the lineitem.tbl file
             * value: a line of the lineitem.tbl file
             * We must notice that to use the "|" character in a regex we need
             * to double backslash it
             */
            String[] columns = value.toString().split("\\|");
            lPartKey.set(Long.parseLong(columns[L_PART_KEY_INDEX]));
            lDiscount.set(Double.parseDouble(columns[L_DISCOUNT_INDEX]));
            context.write(lPartKey, lDiscount);
        }
    }

    /**
     * This class implement the reducer of the Hadoop application that
     * compute the average discount (L_DISCOUNT) for each item (L_PARTKEY).
     * To do that the key value pair generated by the mapper are respectively
     * the value of the L_PARTKEY column and the value of the L_DISCOUNT column
     * of the lineitem.tbl file. While the output key and the output value of
     * the reducer are respectively the L_PARTKEY and the average of the
     * L_DISCOUNT.
     * This reducer is compliant with Hadoop 0.20.2
     */
    public static class TalendAverageLineitemDiscountForItemReducer extends
            Reducer<LongWritable, DoubleWritable, LongWritable, DoubleWritable> {
        private DoubleWritable result = new DoubleWritable();

        public void reduce(LongWritable key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            double sum = 0.0;
            long count = 0;
            for (DoubleWritable val : values) {
                sum += val.get();
                count++;
            }
            result.set(sum / count);
            context.write(key, result);
        }
    }

    /**
     * This class implement the mapper of the Hadoop application that
     * count the number of order lines by shipment mode (L_SHIPMODE) and
     * the year of the shipment date (L_SHIPDATE).
     * To do that the key and the value generated by the mapper are respectively
     * the concatenation of the shipment mode and the year of the shipment date
     * and the number of order lines that have that shipment mode and that year
     * of shipment date.
     * This mapper is compliant with Hadoop 0.20.2
     */
    public static class TalendCountOrderLinesShipModeShipDateYearMapper extends
            Mapper<Object, Text, Text, IntWritable> {

        private final static int L_SHIP_MODE_INDEX = 14;
        private final static int L_SHIP_DATE_INDEX = 10;
        private Text outputKey = new Text();
        private final static IntWritable one = new IntWritable(1);

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            /*
             * key: the offset in bytes from the start of the lineitem.tbl file
             * value: a line of the lineitem.tbl file
             * We must notice that to use the "|" character in a regex we need
             * to double backslash it
             */
            String[] columns = value.toString().split("\\|");
            outputKey.set(columns[L_SHIP_MODE_INDEX] + " " + columns[L_SHIP_DATE_INDEX].split("-")[0]);
            context.write(outputKey, one);
        }
    }

    /**
     * This class implement the reducer of the Hadoop application that
     * count the number of order lines by shipment mode (L_SHIPMODE) and
     * the year of the shipment date (L_SHIPDATE).
     * To do that the key and the value generated by the mapper are respectively
     * the concatenation of the shipment mode and the year of the shipment date
     * and the number of order lines that have that shipment mode and that year
     * of shipment date. While for the output key and output value for the reducer
     * are respectively the concatenation of the shipment mode and the year of
     * the shipment date and the count of the order lines that have that
     * shipment mode and that year of shipment date.
     * This reducer is compliant with Hadoop 0.20.2
     */
    public static class TalendCountOrderLinesShipModeShipDateYearReducer extends
            Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

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
}
