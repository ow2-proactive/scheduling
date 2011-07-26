package org.ow2.proactive.scheduler.ext.mapreduce.examples;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.security.PublicKey;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.AlreadyConnectedException;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceFrameworkProperties;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJob;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJobConfiguration;
import org.ow2.proactive.scheduler.ext.mapreduce.examples.PAMapReduceJobMain.IntSumReducer;
import org.ow2.proactive.scheduler.ext.mapreduce.examples.PAMapReduceJobMain.IntSumReducerCombiner;
import org.ow2.proactive.scheduler.ext.mapreduce.examples.PAMapReduceJobMain.TokenizerMapper;


public class PAMapReduceJobMainJavaExecutable extends JavaExecutable {

    protected String configurationFilePath = null;
    protected String numberOfReducers = null;
    protected String inputFilePath = null;
    protected String outputFolderPath = null;

    protected String numberOfMappers = null;

    protected String submittedJobName = null;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        /*
         * We print the parameters
         */
        //System.out.println("The configuration file path is: " + configurationFilePath);
        System.out.println("The name of the job to submit is: " + submittedJobName);
        System.out.println("The number of mappers is: " + numberOfMappers);
        System.out.println("The number of reducers is: " + numberOfReducers);
        System.out.println("The input file path is: " + inputFilePath);
        System.out.println("The outputFolderPath is: " + outputFolderPath);

        Configuration conf = new Configuration();

        Job job = null;
        try {
            job = new Job(conf, submittedJobName);
            job.setJarByClass(PAMapReduceJobMain.class);
            job.setMapperClass(TokenizerMapper.class);

            job.setCombinerClass(IntSumReducerCombiner.class);

            job.setReducerClass(IntSumReducer.class);

            /*
             * The definition of the class of the MapReduce job output key-value
             * pairs is not compulsory because they have a default value as we
             * can see from the methods
             * PAHadoopJobConfiguration.getOutputKeyClass and
             * PAHadoopJobConfiguration.getOutputValueClass that defines
             * respectively the org.apache.hadoop.io.LongWritable class as the
             * default value of the key and org.apache.hadoop.io.Text as the
             * default value for the value. The user can define its own
             * classes. We must notice that when the default Mapper and Reducer
             * Hadoop classes are used the job will be executed only if the user
             * define the org.apache.hadoop.io.LongWritable and
             * org.apache.hadoop.io.Text respectively as the class of the key
             * and the value or if the user does not specify at all the classes
             * of the output key and output value.
             */
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);

            /*
             * We set the number of reducer tasks to execute. We must notice
             * that if that value is not specify then the default value will be
             * "1" as we can see looking at the
             * org.apache.hadoop.mapreduce.JobContext.getNumReduceTasks()
             */
            try {
                job.setNumReduceTasks(Integer.parseInt(numberOfReducers));
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
            FileInputFormat.addInputPath(job, new Path(inputFilePath));

            /*
             * We set the output directory for the Hadoop job. We must notice
             * that we are forced to create the output directory as a
             * sub-directory in the OUTPUT data space defined for the mapreduce
             * job (because we cannot create an empty org.apache.hadoop.fs.Path)
             */
            FileOutputFormat.setOutputPath(job, new Path(outputFolderPath));
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
        if (configurationFilePath != null) {
            System.out.println("The configuration file path is: " + configurationFilePath);
            File paMapReduceConfigurationFile = new File(configurationFilePath);
            pamrjc = new PAMapReduceJobConfiguration(paMapReduceConfigurationFile);
        } else {
            System.out.println("The configuration file path is null");
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
         * In proactive, as in Hadoop, we cannot explicitly define the number of mappers
         * to execute. But we can only define the size of the input split. So if we let
         * the user define the number of mappers to execute, then we must calculate the
         * size of the input split and modify the PAMapReduceJobConfiguration we build
         * previously.
         * The input file is in the INPUT space
         */
        DataSpacesFileObject dsfo = getInputFile(inputFilePath);
        long dsfoSize = dsfo.getContent().getSize();
        long inputSplitSize = (long) (dsfoSize / Long.parseLong(numberOfMappers));
        pamrjc.setInputSplitSize(inputSplitSize);

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

        /*
         * We wait for the job to finish
         * 	1) Connect to the ProActive Scheduler
         * 	2) Wait for the job to finish
         * 	3) Close the connection to the ProActive Scheduler
         * 	4) Return the ProActive MapReduce job id as result
         */
        JobId actualMapReduceJobId = pamrj.getJobId();
        System.out.println("The Actual MapReduce JobId is: " + actualMapReduceJobId);

        SchedulerAuthenticationInterface sai = null;
        try {
            sai = SchedulerConnection.join(pamrjc
                    .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_URL.getKey()));
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Scheduler scheduler = null;
        try {
            scheduler = sai.login(Credentials.getCredentials());
            System.out.println("The authentication to the scheduler is done using the credentials: " +
                System.getProperty(Credentials.credentialsPathProperty));
        } catch (LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AlreadyConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyException e) {
            try {

                System.out
                        .println("The authentication to the scheduler cannot be done using the credentials: " +
                            System.getProperty(Credentials.credentialsPathProperty));
                // (2) alternative authentication method
                PublicKey pubKey = null;
                try {
                    pubKey = sai.getPublicKey();
                } catch (LoginException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (pubKey == null) {
                    pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
                }
                try {
                    String username = pamrjc
                            .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_USERNAME.getKey());
                    String password = pamrjc
                            .getPropertyAsString(PAMapReduceFrameworkProperties.SCHEDULER_PASSWORD.getKey());
                    scheduler = sai.login(Credentials.createCredentials(new CredData(username, password),
                            pubKey));
                } catch (LoginException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (AlreadyConnectedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (KeyException ke2) {
                // cannot find public key !
            }
        }

        if (scheduler != null) {
            try {
                int sleepingTime = 15000;
                while (scheduler.getJobResult(actualMapReduceJobId) == null) {
                    Thread.sleep(sleepingTime);
                    System.out.println("Sleeping for: " + sleepingTime);
                }
            } catch (NotConnectedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (PermissionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("The scheduler is null");
        }

        try {
            scheduler.disconnect();
        } catch (NotConnectedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Return the JobId of the executed ProActive MapReduce job
        return actualMapReduceJobId;
    }

}
