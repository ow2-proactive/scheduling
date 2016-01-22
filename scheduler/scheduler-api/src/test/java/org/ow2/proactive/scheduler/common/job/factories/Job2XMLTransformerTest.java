package org.ow2.proactive.scheduler.common.job.factories;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class Job2XMLTransformerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void forkEnvironmentIsPreserved() throws Exception {
        File xmlFile = tmpFolder.newFile();

        TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("forkedTask");
        task.setExecutableClassName("oo.Bar");
        task.setForkEnvironment(new ForkEnvironment());
        job.addTask(task);

        new Job2XMLTransformer().job2xmlFile(job, xmlFile);
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

        assertNotNull(recreatedJob.getTask("forkedTask").getForkEnvironment());
    }

    @Test
    public void walltimeIsPreserved() throws Exception {
        File xmlFile = tmpFolder.newFile();
        String taskName = "walltimeTask";

        // tests for various values including one second, one minute, one minute and one second, big value, etc. miliseconds are discarded
        long[] walltimesToTest = {0, 1000, 60000, 61000, 3600000, 3601000, 3660000, 3661000, 999999000};

        for (int i = 1; i < walltimesToTest.length; i++) {
            TaskFlowJob job = new TaskFlowJob();
            JavaTask task = new JavaTask();
            task.setName(taskName);
            task.setExecutableClassName("oo.Bar");
            task.setWallTime(walltimesToTest[i]);
            job.addTask(task);

            new Job2XMLTransformer().job2xmlFile(job, xmlFile);
            TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

            assertEquals("Walltimes between original and recreated job must be equal", walltimesToTest[i], recreatedJob.getTask(taskName).getWallTime());
        }
    }
}