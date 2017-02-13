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
package org.ow2.proactive.scheduler.core.db.schedulerdb;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.*;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.topology.descriptor.ThresholdProximityDescriptor;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


public class TestTaskAttributes extends BaseSchedulerDBTest {

    @Test
    public void testFlowBlock() throws Throwable {
        TaskFlowJob jobToSubmit = (TaskFlowJob) JobFactory.getFactory()
                                                          .createJob(new File(TestTaskAttributes.class.getResource("/functionaltests/workflow/descriptors/flow_crash_int_2.xml")
                                                                                                      .toURI()).getAbsolutePath());

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobToSubmit);

        for (Task task : jobToSubmit.getTasks()) {
            int expectedDeps = task.getDependencesList() != null ? task.getDependencesList().size() : 0;
            InternalTask internalTask = job.getTask(task.getName());
            int actualDeps = internalTask.getIDependences() != null ? internalTask.getIDependences().size() : 0;
            Assert.assertEquals("Wrong dependecies for " + task.getName(), expectedDeps, actualDeps);
            Assert.assertEquals("Wrong flow block for " + task.getName(),
                                task.getFlowBlock(),
                                internalTask.getFlowBlock());
        }
    }

    @Test
    public void testScripts() throws Exception {
        TaskFlowJob jobDef = new TaskFlowJob();
        JavaTask task1 = createDefaultTask("task1");

        task1.addSelectionScript(new SelectionScript("selection1", "js", new String[] { "param1", "param2" }, true));
        task1.addSelectionScript(new SelectionScript("selection2", "js", new String[] { "param3" }, false));
        task1.addSelectionScript(new SelectionScript("selection3", "js"));

        task1.setCleaningScript(new SimpleScript("cleanscript", "js", new String[] { "p1", "p2" }));
        task1.setPreScript(new SimpleScript("prescript", "js", new String[] { "p1", "p2" }));
        task1.setPostScript(new SimpleScript("postscript", "js", new String[] { "p1", "p2" }));
        task1.setFlowScript(FlowScript.createContinueFlowScript());

        jobDef.addTask(task1);

        InternalJob job = defaultSubmitJobAndLoadInternal(true, jobDef);

        InternalTask task = job.getTask("task1");

        Assert.assertEquals("cleanscript", task.getCleaningScript().getScript());
        Assert.assertArrayEquals(new String[] { "p1", "p2" }, task.getCleaningScript().getParameters());

        Assert.assertEquals("prescript", task.getPreScript().getScript());
        Assert.assertArrayEquals(new String[] { "p1", "p2" }, task.getPreScript().getParameters());

        Assert.assertEquals("postscript", task.getPostScript().getScript());
        Assert.assertArrayEquals(new String[] { "p1", "p2" }, task.getPostScript().getParameters());

        Assert.assertEquals(FlowActionType.CONTINUE.toString(), task.getFlowScript().getActionType());

        Assert.assertEquals(3, task.getSelectionScripts().size());

        Set<String> scripts = new HashSet<>();
        for (SelectionScript script : task.getSelectionScripts()) {
            scripts.add(script.getScript());
            if (script.getScript().equals("selection1")) {
                Assert.assertArrayEquals(new String[] { "param1", "param2" }, script.getParameters());
            }
            if (script.getScript().equals("selection2")) {
                Assert.assertArrayEquals(new String[] { "param3" }, script.getParameters());
            }
            if (script.getScript().equals("selection3")) {
                Assert.assertArrayEquals(new String[] {}, script.getParameters());
            }
        }
        Set<String> expected = new HashSet<>();
        expected.add("selection1");
        expected.add("selection2");
        expected.add("selection3");

        Assert.assertEquals(expected, scripts);
    }

    @Test
    public void testRestartMode() throws Exception {
        JavaTask task = createDefaultTask("task1");
        task.setRestartTaskOnError(RestartMode.ANYWHERE);

        InternalTask taskData = saveSingleTask(task).getTask("task1");
        Assert.assertSame(RestartMode.ANYWHERE, taskData.getRestartTaskOnError());

        task.setRestartTaskOnError(RestartMode.ELSEWHERE);
        taskData = saveSingleTask(task).getTask("task1");
        Assert.assertSame(RestartMode.ELSEWHERE, taskData.getRestartTaskOnError());
    }

    @Test
    public void testEmptyAttributes() throws Exception {
        JavaTask javaTask = createDefaultTask("task1");
        InternalTask taskData = saveSingleTask(javaTask).getTask("task1");
        Assert.assertNotNull(taskData.getId());

        NativeTask nativeTask = new NativeTask();
        nativeTask.setName("task1");
        nativeTask.setCommandLine("commandline");
        taskData = saveSingleTask(nativeTask).getTask("task1");
        Assert.assertNotNull(taskData.getId());
    }

    @Test
    public void testAttributes() throws Exception {
        JavaTask task = createDefaultTask("task");
        task.setOnTaskError(OnTaskError.CANCEL_JOB);
        task.setDescription("desc");
        // TODO: create test using valid flow
        // task.setFlowBlock(FlowBlock.START);
        task.setMaxNumberOfExecution(7);
        task.setPreciousLogs(true);
        task.setPreciousResult(true);
        task.setRunAsMe(true);
        task.setWallTime(123);
        InternalTask taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(OnTaskError.CANCEL_JOB, taskData.getOnTaskErrorProperty().getValue());
        Assert.assertEquals("desc", taskData.getDescription());
        // Assert.assertEquals(FlowBlock.START, taskData.getFlowBlock());
        Assert.assertEquals(7, taskData.getMaxNumberOfExecution());
        Assert.assertEquals("task", taskData.getName());
        Assert.assertEquals(true, taskData.isPreciousLogs());
        Assert.assertEquals(true, taskData.isPreciousResult());
        Assert.assertEquals(true, taskData.isRunAsMe());
        Assert.assertEquals(123, taskData.getWallTime());
    }

    @Test
    public void testForkEnv() throws Exception {
        JavaTask task = createDefaultTask("task");
        ForkEnvironment env = new ForkEnvironment();
        task.setForkEnvironment(env);
        InternalTask taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertNotNull(taskData.getForkEnvironment());

        env = new ForkEnvironment();
        env.setEnvScript(new SimpleScript("forkenvscript", "js", new String[] { "p1", "p2" }));
        task.setForkEnvironment(env);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertNotNull(taskData.getForkEnvironment().getEnvScript());
        Assert.assertArrayEquals(new String[] { "p1", "p2" }, task.getForkEnvironment().getEnvScript().getParameters());

        env = new ForkEnvironment();
        env.setJavaHome("javahome");
        env.setWorkingDir("workingdir");
        env.addAdditionalClasspath("classpath");
        env.addJVMArgument("jvmargument");
        env.addSystemEnvironmentVariable("var1", "value1");
        StringBuilder longString = buildLongString();
        env.addSystemEnvironmentVariable("longvar", longString.toString());

        task.setForkEnvironment(env);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals("javahome", taskData.getForkEnvironment().getJavaHome());
        Assert.assertEquals("workingdir", taskData.getForkEnvironment().getWorkingDir());
        Assert.assertEquals(1, taskData.getForkEnvironment().getAdditionalClasspath().size());
        Assert.assertEquals("classpath", taskData.getForkEnvironment().getAdditionalClasspath().get(0));
        Assert.assertEquals(1, taskData.getForkEnvironment().getJVMArguments().size());
        Assert.assertEquals("jvmargument", taskData.getForkEnvironment().getJVMArguments().get(0));
        Assert.assertEquals(2, taskData.getForkEnvironment().getSystemEnvironment().size());
        Assert.assertEquals("value1", taskData.getForkEnvironment().getSystemEnvironment().get("var1"));
        Assert.assertEquals(longString.toString(), taskData.getForkEnvironment().getSystemEnvironment().get("longvar"));

    }

    @Test
    public void testParallelEnv() throws Exception {
        JavaTask task = createDefaultTask("task");
        ParallelEnvironment env = new ParallelEnvironment(5);
        task.setParallelEnvironment(env);

        InternalTask taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(5, taskData.getParallelEnvironment().getNodesNumber());
        Assert.assertNull(taskData.getParallelEnvironment().getTopologyDescriptor());

        TopologyDescriptor[] descs = { TopologyDescriptor.ARBITRARY, TopologyDescriptor.BEST_PROXIMITY,
                                       TopologyDescriptor.DIFFERENT_HOSTS_EXCLUSIVE,
                                       TopologyDescriptor.MULTIPLE_HOSTS_EXCLUSIVE, TopologyDescriptor.SINGLE_HOST,
                                       TopologyDescriptor.SINGLE_HOST_EXCLUSIVE,
                                       new ThresholdProximityDescriptor(123) };

        for (TopologyDescriptor desc : descs) {
            task = createDefaultTask("task");
            env = new ParallelEnvironment(10, desc);
            task.setParallelEnvironment(env);
            taskData = saveSingleTask(task).getTask(task.getName());
            Assert.assertEquals(10, taskData.getParallelEnvironment().getNodesNumber());
            Assert.assertEquals(taskData.getParallelEnvironment().getTopologyDescriptor().getClass(), desc.getClass());
            if (desc instanceof ThresholdProximityDescriptor) {
                Assert.assertEquals(((ThresholdProximityDescriptor) taskData.getParallelEnvironment()
                                                                            .getTopologyDescriptor()).getThreshold(),
                                    123);
            }
        }
    }

    @Test
    public void testGenericInfo() throws Exception {
        JavaTask task = createDefaultTask("task");
        Map<String, String> genericInfo;
        InternalTask taskData;

        genericInfo = new HashMap<>();
        task.setGenericInformation(genericInfo);
        taskData = saveSingleTask(task).getTask(task.getName());

        Assert.assertNotNull(taskData.getGenericInformation());
        Assert.assertTrue(taskData.getGenericInformation().isEmpty());

        genericInfo = new HashMap<>();
        genericInfo.put("p1", "v1");
        genericInfo.put("p2", "v2");
        task.setGenericInformation(genericInfo);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(2, taskData.getGenericInformation().size());
        Assert.assertEquals("v1", taskData.getGenericInformation().get("p1"));
        Assert.assertEquals("v2", taskData.getGenericInformation().get("p2"));

        StringBuilder longString = buildLongString();
        genericInfo = new HashMap<>();
        genericInfo.put("longProperty", longString.toString());
        task.setGenericInformation(genericInfo);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(1, taskData.getGenericInformation().size());
        Assert.assertEquals(longString.toString(), taskData.getGenericInformation().get("longProperty"));
    }

    private StringBuilder buildLongString() {
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longString.append("0123456789abcdefghijklmnopqrstuvwxyz");
        }
        return longString;
    }

    @Test
    public void testTaskVariables() throws Exception {
        JavaTask task = createDefaultTask("task");
        Map<String, TaskVariable> variables;
        InternalTask taskData;

        variables = new HashMap<>();
        task.setVariables(variables);
        taskData = saveSingleTask(task).getTask(task.getName());

        Assert.assertNotNull(taskData.getVariables());
        Assert.assertTrue(taskData.getVariables().isEmpty());

        variables = new HashMap<>();
        TaskVariable v1 = new TaskVariable();
        v1.setName("p1");
        v1.setValue("v1");
        TaskVariable v2 = new TaskVariable();
        v2.setName("p2");
        v2.setValue("v2");
        variables.put("p1", v1);
        variables.put("p2", v2);
        TaskVariable v3 = new TaskVariable();
        v3.setName("emptyvar");
        variables.put("emptyvar", v3);
        task.setVariables(variables);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(3, taskData.getVariables().size());
        Assert.assertEquals("p1", taskData.getVariables().get("p1").getName());
        Assert.assertEquals("p2", taskData.getVariables().get("p2").getName());
        Assert.assertEquals("v1", taskData.getVariables().get("p1").getValue());
        Assert.assertEquals("v2", taskData.getVariables().get("p2").getValue());
        Assert.assertEquals("emptyvar", taskData.getVariables().get("emptyvar").getName());
        Assert.assertNull(taskData.getVariables().get("emptyvar").getValue());

        StringBuilder longString = buildLongString();
        variables = new HashMap<>();
        TaskVariable longVariable = new TaskVariable();
        longVariable.setName("longProperty");
        longVariable.setValue(longString.toString());
        variables.put("longProperty", longVariable);
        task.setVariables(variables);
        taskData = saveSingleTask(task).getTask(task.getName());
        Assert.assertEquals(1, taskData.getVariables().size());
        Assert.assertEquals(longString.toString(), taskData.getVariables().get("longProperty").getValue());
    }

}
