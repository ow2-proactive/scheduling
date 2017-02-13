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
package org.ow2.proactive.scheduler.task;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ow2.proactive.scheduler.task.TaskAssertions.assertTaskResultOk;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.TaskTerminateNotification;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.common.task.util.SerializationUtil;
import org.ow2.proactive.scheduler.job.JobIdImpl;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


public class TaskLauncherDataSpacesTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TestTaskLauncherFactory taskLauncherFactory;

    @Before
    public void setUp() throws Exception {
        taskLauncherFactory = new TestTaskLauncherFactory(tmpFolder.newFolder());
    }

    @Test
    public void input_file_using_job_id_in_its_selector() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("println new File('.').listFiles();",
                                                                                                                      "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));
        initializer.setTaskInputFiles(singletonList(new InputSelector(new FileSelector("input_$PA_JOB_ID.txt"),
                                                                      InputAccessMode.TransferFromInputSpace)));

        File inputFile = new File(taskLauncherFactory.getDataSpaces().getInputURI(), "input_1000.txt");
        assertTrue(inputFile.createNewFile());

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, taskLauncherFactory);
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertFalse(taskResult.hadException());
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("input_1000.txt"));
    }

    @Test
    public void output_file_using_task_id_in_its_selector() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("new File('output_' + variables.get('PA_TASK_ID') + '.txt').text = 'hello'",
                                                                                                                      "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));
        initializer.setTaskOutputFiles(singletonList(new OutputSelector(new FileSelector("output_${PA_TASK_ID}.txt"),
                                                                        OutputAccessMode.TransferToGlobalSpace)));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, taskLauncherFactory);
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer);

        assertTaskResultOk(taskResult);
        assertTrue(new File(taskLauncherFactory.getDataSpaces().getGlobalURI(), "output_1000.txt").exists());
    }

    @Test
    public void input_file_using_variable_in_its_selector() throws Throwable {
        ScriptExecutableContainer executableContainer = new ScriptExecutableContainer(new TaskScript(new SimpleScript("println new File('.').listFiles();",
                                                                                                                      "groovy")));

        TaskLauncherInitializer initializer = new TaskLauncherInitializer();
        initializer.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));
        initializer.setJobVariables(singletonMap("aVar", new JobVariable("aVar", "foo")));
        initializer.setTaskInputFiles(singletonList(new InputSelector(new FileSelector("input_${aVar}_${aResultVar}.txt"),
                                                                      InputAccessMode.TransferFromInputSpace)));

        File inputFile = new File(taskLauncherFactory.getDataSpaces().getInputURI(), "input_foo_bar.txt");
        assertTrue(inputFile.createNewFile());

        TaskLauncher taskLauncher = TaskLauncherUtils.create(initializer, taskLauncherFactory);
        TaskResultImpl previousTaskResult = taskResult(Collections.<String, Serializable> singletonMap("aResultVar",
                                                                                                       "bar"));
        TaskResult taskResult = runTaskLauncher(taskLauncher, executableContainer, previousTaskResult);

        assertTaskResultOk(taskResult);
        assertTrue(taskResult.getOutput().getAllLogs(false).contains("input_foo_bar.txt"));
    }

    @Test
    public void output_file_using_variable_in_its_selector() throws Throwable {
        ScriptExecutableContainer createAFileAndWriteVariable = new ScriptExecutableContainer(new TaskScript(new SimpleScript("new File('output_foo_bar.txt').text = 'hello'; variables.put('aVar', 'foo')",
                                                                                                                              "groovy")));

        TaskLauncherInitializer copyOutputFile = new TaskLauncherInitializer();
        copyOutputFile.setTaskId(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1000"), "job", 1000L));
        copyOutputFile.setTaskOutputFiles(singletonList(new OutputSelector(new FileSelector("output_${aVar}_${aResultVar}.txt"),
                                                                           OutputAccessMode.TransferToGlobalSpace)));

        TaskLauncher taskLauncher = TaskLauncherUtils.create(copyOutputFile, taskLauncherFactory);
        TaskResultImpl previousTaskResult = taskResult(Collections.<String, Serializable> singletonMap("aResultVar",
                                                                                                       "bar"));
        TaskResult taskResult = runTaskLauncher(taskLauncher, createAFileAndWriteVariable, previousTaskResult);

        assertTaskResultOk(taskResult);
        assertTrue(new File(taskLauncherFactory.getDataSpaces().getGlobalURI(), "output_foo_bar.txt").exists());
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ScriptExecutableContainer executableContainer) {
        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();

        taskLauncher.doTask(executableContainer, null, taskResult);

        return taskResult.result;
    }

    private TaskResult runTaskLauncher(TaskLauncher taskLauncher, ScriptExecutableContainer executableContainer,
            TaskResult... taskResults) {
        TaskTerminateNotificationVerifier taskResult = new TaskTerminateNotificationVerifier();

        taskLauncher.doTask(executableContainer, taskResults, taskResult);

        return taskResult.result;
    }

    private static class TaskTerminateNotificationVerifier implements TaskTerminateNotification {

        TaskResult result;

        @Override
        public void terminate(TaskId taskId, TaskResult taskResult) {
            this.result = taskResult;
        }

    }

    private TaskResultImpl taskResult(Map<String, Serializable> variables) {
        TaskResultImpl previousTaskResult = new TaskResultImpl(TaskIdImpl.createTaskId(JobIdImpl.makeJobId("1001"),
                                                                                       "job",
                                                                                       1000L),
                                                               null);
        previousTaskResult.setPropagatedVariables(SerializationUtil.serializeVariableMap(variables));
        return previousTaskResult;
    }

}
