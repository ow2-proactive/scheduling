package org.ow2.proactive.scheduler.common.job.factories;

import java.io.File;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
}