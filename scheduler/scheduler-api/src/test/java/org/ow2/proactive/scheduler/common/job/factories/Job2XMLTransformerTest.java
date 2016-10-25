package org.ow2.proactive.scheduler.common.job.factories;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class Job2XMLTransformerTest {

    private static final String matchEvery = "[\\w\\W]*";
    // Regex do match possible characters in xml but not the >
    private static final String notMatchSmallerSign = "[\\w\\d\\s=_/.\"-]*";
    private static final String matchSmallerSign = ">";
    private static final String matchJobTagOpening = "<job";
    private static final String matchTaskTagOpening = "<task";

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
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(
                xmlFile.getAbsolutePath()));

        assertNotNull(recreatedJob.getTask("forkedTask").getForkEnvironment());
    }

    private String matchOnTaskErrorEquals(OnTaskError onTaskError) {
        return "onTaskError=\"" + onTaskError.toString() + "\"";
    }

    @Test
    public void onTaskErrorIsCancelJobInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.CANCEL_JOB);
    }

    @Test
    public void onTaskErrorIsPauseJobInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.PAUSE_JOB);
    }

    @Test
    public void onTaskErrorIsPauseTaskInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.PAUSE_TASK);
    }

    @Test
    public void onTaskErrorIsContinueJobExecutionInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.CONTINUE_JOB_EXECUTION);
    }

    @Test
    public void onTaskErrorIsNoneInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.NONE);
    }

    private void checkIfOnTaskErrorIsInJobInXML(
            OnTaskError jobOnTaskErrorSetting) throws TransformerException, ParserConfigurationException, IOException {
        TaskFlowJob jobWithCancelJobOnErrorTrue = new TaskFlowJob();
        jobWithCancelJobOnErrorTrue.setOnTaskError(jobOnTaskErrorSetting);

        // Check that onTaskError is inside the <job [here] > xml tag
        assertThat("XML must contain onTaskError=\\\"" + jobOnTaskErrorSetting.toString() + "\\\"",
                new Job2XMLTransformer().jobToxmlString(jobWithCancelJobOnErrorTrue),
                org.hamcrest.Matchers.matchesPattern(
                        matchEvery + matchJobTagOpening + notMatchSmallerSign + matchOnTaskErrorEquals(
                                jobOnTaskErrorSetting) + matchEvery + matchSmallerSign + matchEvery));
    }

    @Test
    public void onTaskErrorIsCancelJobInTaskandJob() throws IOException, TransformerException, ParserConfigurationException, UserException {
        checkIfOnTaskErrorIsInJobAndTaskInXML(OnTaskError.CANCEL_JOB, OnTaskError.CANCEL_JOB);
    }

    @Test
    public void onTaskErrorEveryPossibleCombinationInTaskandJob() throws IOException, TransformerException, ParserConfigurationException, UserException {
        Set<OnTaskError> allPossibleOnTaskErrorStates = new HashSet<>(Arrays.asList(OnTaskError.CANCEL_JOB, OnTaskError.CONTINUE_JOB_EXECUTION, OnTaskError.NONE, OnTaskError.PAUSE_JOB, OnTaskError.PAUSE_TASK));
        for(OnTaskError jobSetting : allPossibleOnTaskErrorStates) {
            for (OnTaskError taskSetting : allPossibleOnTaskErrorStates) {
                checkIfOnTaskErrorIsInJobAndTaskInXML(jobSetting, taskSetting);
            }
        }
    }

    private void checkIfOnTaskErrorIsInJobAndTaskInXML(OnTaskError jobOnTaskErrorSetting,
            OnTaskError taskOnTaskErrorSetting) throws UserException, TransformerException, ParserConfigurationException, IOException {
        String taskName = "taskName";
        TaskFlowJob jobWithCancelJobOnErrorTrue = new TaskFlowJob();
        jobWithCancelJobOnErrorTrue.setOnTaskError(jobOnTaskErrorSetting);
        JavaTask task = new JavaTask();
        task.setName(taskName);
        task.setExecutableClassName("oo.Bar");
        task.setOnTaskError(taskOnTaskErrorSetting);
        jobWithCancelJobOnErrorTrue.addTask(task);

        // Check that onTaskError is inside the <job [here] > xml tag
        assertThat("XML must contain onTaskError=\\\"" + jobOnTaskErrorSetting.toString() + "\\\"",
                new Job2XMLTransformer().jobToxmlString(jobWithCancelJobOnErrorTrue),
                org.hamcrest.Matchers.matchesPattern(
                        matchEvery + matchJobTagOpening + notMatchSmallerSign + matchOnTaskErrorEquals(
                                jobOnTaskErrorSetting) + matchEvery + matchSmallerSign + matchEvery));


        // Check that onTaskError is inside the <task [here] > xml tag
        assertThat("XML must contain onTaskError=\\\"" + taskOnTaskErrorSetting.toString() + "\\\"",
                new Job2XMLTransformer().jobToxmlString(jobWithCancelJobOnErrorTrue),
                org.hamcrest.Matchers.matchesPattern(
                        matchEvery + matchTaskTagOpening + notMatchSmallerSign + matchOnTaskErrorEquals(
                                taskOnTaskErrorSetting) + matchEvery + matchSmallerSign + matchEvery));
    }

    @Test
    public void checkTaskVariables() throws Exception {
        File xmlFile = tmpFolder.newFile();
        Map<String, TaskVariable> variablesMap = new HashMap<>();
        TaskVariable taskVariable1 = new TaskVariable();
        taskVariable1.setName("name1");
        taskVariable1.setValue("value1");
        taskVariable1.setJobInherited(false);
        taskVariable1.setModel("model1");
        TaskVariable taskVariable2 = new TaskVariable();
        taskVariable2.setName("name2");
        taskVariable2.setValue("value2");
        taskVariable2.setJobInherited(true);
        taskVariable2.setModel("model2");
        variablesMap.put(taskVariable1.getName(), taskVariable1);
        variablesMap.put(taskVariable2.getName(), taskVariable2);

        TaskFlowJob job = new TaskFlowJob();
        JavaTask task = new JavaTask();
        task.setName("task");
        task.setExecutableClassName("oo.Bar");
        task.setVariables(variablesMap);
        job.addTask(task);

        new Job2XMLTransformer().job2xmlFile(job, xmlFile);
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(
                xmlFile.getAbsolutePath()));

        Map<String, TaskVariable> resVariables = recreatedJob.getTask("task").getVariables();
        assertEquals(2, resVariables.size());
        assertEquals(taskVariable1, resVariables.get("name1"));
        assertEquals(taskVariable2, resVariables.get("name2"));
    }

    @Test
    public void walltimeIsPreserved() throws Exception {
        File xmlFile = tmpFolder.newFile();
        String taskName = "walltimeTask";

        // tests for various values including one second, one minute, one minute and one second, big value, etc. miliseconds are discarded
        long[] walltimesToTest = { 0, 1000, 60000, 61000, 3600000, 3601000, 3660000, 3661000, 999999000 };

        for (int i = 1; i < walltimesToTest.length; i++) {
            TaskFlowJob job = new TaskFlowJob();
            JavaTask task = new JavaTask();
            task.setName(taskName);
            task.setExecutableClassName("oo.Bar");
            task.setWallTime(walltimesToTest[i]);
            job.addTask(task);

            new Job2XMLTransformer().job2xmlFile(job, xmlFile);
            TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(
                    xmlFile.getAbsolutePath()));

            assertEquals("Walltimes between original and recreated job must be equal", walltimesToTest[i],
                    recreatedJob.getTask(taskName).getWallTime());
        }
    }
}