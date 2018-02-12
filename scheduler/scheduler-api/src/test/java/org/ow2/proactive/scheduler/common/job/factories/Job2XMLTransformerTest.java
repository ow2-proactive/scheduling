/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.OnTaskError;
import org.ow2.proactive.scheduler.common.task.ScriptTask;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


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
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

        assertNotNull(recreatedJob.getTask("forkedTask").getForkEnvironment());
    }

    @Test
    public void argumentsInScript() throws Exception {
        File xmlFile = tmpFolder.newFile();
        TaskFlowJob job = new TaskFlowJob();
        job.setName("simpleJob");
        String params[] = { "param1", "param2" };
        SimpleScript script = new SimpleScript("\nprint('arguments[0]='+arguments[0])\n", "javascript", params);
        ScriptTask task = new ScriptTask();
        task.setName("testTask");
        task.setScript(new TaskScript(script));
        job.addTask(task);

        new Job2XMLTransformer().job2xmlFile(job, xmlFile);
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

        Assert.assertEquals("param1",
                            ((ScriptTask) recreatedJob.getTask("testTask")).getScript().getParameters()[0].toString());
        Assert.assertEquals("param2",
                            ((ScriptTask) recreatedJob.getTask("testTask")).getScript().getParameters()[1].toString());
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
    public void onTaskErrorIsContinueJobExecutionInJob()
            throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.CONTINUE_JOB_EXECUTION);
    }

    @Test
    public void onTaskErrorIsNoneInJob() throws IOException, TransformerException, ParserConfigurationException {
        checkIfOnTaskErrorIsInJobInXML(OnTaskError.NONE);
    }

    private void checkIfOnTaskErrorIsInJobInXML(OnTaskError jobOnTaskErrorSetting)
            throws TransformerException, ParserConfigurationException, IOException {
        TaskFlowJob jobWithCancelJobOnErrorTrue = new TaskFlowJob();
        jobWithCancelJobOnErrorTrue.setOnTaskError(jobOnTaskErrorSetting);

        // Check that onTaskError is inside the <job [here] > xml tag
        assertThat("XML must contain onTaskError=\\\"" + jobOnTaskErrorSetting.toString() + "\\\"",
                   new Job2XMLTransformer().jobToxmlString(jobWithCancelJobOnErrorTrue),
                   org.hamcrest.Matchers.matchesPattern(matchEvery + matchJobTagOpening + notMatchSmallerSign +
                                                        matchOnTaskErrorEquals(jobOnTaskErrorSetting) + matchEvery +
                                                        matchSmallerSign + matchEvery));
    }

    @Test
    public void onTaskErrorIsCancelJobInTaskandJob()
            throws IOException, TransformerException, ParserConfigurationException, UserException {
        checkIfOnTaskErrorIsInJobAndTaskInXML(OnTaskError.CANCEL_JOB, OnTaskError.CANCEL_JOB);
    }

    @Test
    public void onTaskErrorEveryPossibleCombinationInTaskandJob()
            throws IOException, TransformerException, ParserConfigurationException, UserException {
        Set<OnTaskError> allPossibleOnTaskErrorStates = new HashSet<>(Arrays.asList(OnTaskError.CANCEL_JOB,
                                                                                    OnTaskError.CONTINUE_JOB_EXECUTION,
                                                                                    OnTaskError.NONE,
                                                                                    OnTaskError.PAUSE_JOB,
                                                                                    OnTaskError.PAUSE_TASK));
        for (OnTaskError jobSetting : allPossibleOnTaskErrorStates) {
            for (OnTaskError taskSetting : allPossibleOnTaskErrorStates) {
                checkIfOnTaskErrorIsInJobAndTaskInXML(jobSetting, taskSetting);
            }
        }
    }

    private void checkIfOnTaskErrorIsInJobAndTaskInXML(OnTaskError jobOnTaskErrorSetting,
            OnTaskError taskOnTaskErrorSetting)
            throws UserException, TransformerException, ParserConfigurationException, IOException {
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
                   org.hamcrest.Matchers.matchesPattern(matchEvery + matchJobTagOpening + notMatchSmallerSign +
                                                        matchOnTaskErrorEquals(jobOnTaskErrorSetting) + matchEvery +
                                                        matchSmallerSign + matchEvery));

        // Check that onTaskError is inside the <task [here] > xml tag
        assertThat("XML must contain onTaskError=\\\"" + taskOnTaskErrorSetting.toString() + "\\\"",
                   new Job2XMLTransformer().jobToxmlString(jobWithCancelJobOnErrorTrue),
                   org.hamcrest.Matchers.matchesPattern(matchEvery + matchTaskTagOpening + notMatchSmallerSign +
                                                        matchOnTaskErrorEquals(taskOnTaskErrorSetting) + matchEvery +
                                                        matchSmallerSign + matchEvery));
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
        TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

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
            TaskFlowJob recreatedJob = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

            assertEquals("Walltimes between original and recreated job must be equal",
                         walltimesToTest[i],
                         recreatedJob.getTask(taskName).getWallTime());
        }
    }
}
