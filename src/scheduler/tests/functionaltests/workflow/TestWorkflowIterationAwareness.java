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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Assert;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowBlock;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;

import org.ow2.tests.FunctionalTest;
import functionaltests.SchedulerTHelper;


/**
 * Ensures iteration and replication index are properly propagated and replaced
 * when needed in workflow-enabled tasks
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowIterationAwareness extends FunctionalTest {

    private static final URL java_job = TestWorkflowIterationAwareness.class
            .getResource("/functionaltests/workflow/descriptors/flow_it_1.xml");

    private static final URL native_job = TestWorkflowIterationAwareness.class
            .getResource("/functionaltests/workflow/descriptors/flow_it_2.xml");

    private static final String preScript = //
    "importPackage(java.io); \n" //
        +
        "var f = new File(\"" +
        PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
        "/PRE_$IT_$REP\"); \n" + //
        "f.createNewFile(); \n";

    private static final String preScriptWindows = //
    "importPackage(java.io); \n" //
        +
        "var f = new File(\"" +
        PASchedulerProperties.SCHEDULER_HOME.getValueAsString().replace("\\", "\\\\") +
        "\\\\PRE_$IT_$REP\"); \n" + //
        "f.createNewFile(); \n";

    private static final String postScript = //
    "importPackage(java.io); \n" //
        +
        "var f = new File(\"" //
        + PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + "/POST_$IT_$REP\"); \n" //
        + "f.createNewFile(); \n";

    private static final String postScriptWindows = //
    "importPackage(java.io); \n" //
        +
        "var f = new File(\"" //
        +
        PASchedulerProperties.SCHEDULER_HOME.getValueAsString().replace("\\", "\\\\") +
        "\\\\POST_$IT_$REP\"); \n" //
        + "f.createNewFile(); \n";

    private static final String dupScript = "enabled = true; \n" + "runs = 2; \n";

    private static final String loopScript = //
    "importPackage(java.io); \n" //
        + "var ID   = 3; \n" //
        + "var RUNS = 2; \n" //
        + "var f = new File(\"test_flow_lock_\" + ID); \n" //
        + "var it = 0; \n" //
        + "if (f.exists()) { \n" //
        + "var input = new BufferedReader(new FileReader(f)); \n" //
        + "it = java.lang.Integer.parseInt(input.readLine()); \n" + "input.close(); \n" //
        + "f[\"delete\"](); \n" //
        + "} \n" //
        + "it++;\n" //
        + "if (it < RUNS) { \n" //
        + "loop = true; \n" //
        + "f.createNewFile(); \n" //
        + "var output = new BufferedWriter(new FileWriter(f)); \n" //
        + "output.write(\"\" + it); \n" //
        + "output.close(); \n" //
        + "} else { \n" //
        + "loop = false; \n" //
        + "} \n";

    /**
     * Checks Java, Native and ForkedJava executables
     * on a loop/replicate job for propagation of iteration and replication indexes in :
     * native arguments, java arguments, pre/post scripts, native environment variables, java properties
     * 
     * @throws Throwable
     */
    @org.junit.Test
    public void run() throws Throwable {
        testJavaJob();
        testNativeJob();
        testForkedJob();
    }

    /**
     * java task through xml
     */
    private static void testJavaJob() throws Throwable {

        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(java_job.toURI()).getAbsolutePath());
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                ((JavaTask) job.getTask("T1")).setPreScript(new SimpleScript(preScriptWindows, "js"));
                ((JavaTask) job.getTask("T1")).setPostScript(new SimpleScript(postScriptWindows, "js"));
                break;
            case unix:
                ((JavaTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "js"));
                ((JavaTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "js"));
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        JobId id = TWorkflowJobs.testJobSubmission(job, null);
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        int n = 4;
        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            if (result.getKey().equals("T1")) {
                n--;
                checkResult(result.getValue().toString(), "T1", "0", "0");
            } else if (result.getKey().equals("T1*1")) {
                n--;
                checkResult(result.getValue().toString(), "T1*1", "0", "1");
            } else if (result.getKey().equals("T1#1")) {
                n--;
                checkResult(result.getValue().toString(), "T1#1", "1", "0");
            } else if (result.getKey().equals("T1#1*1")) {
                n--;
                checkResult(result.getValue().toString(), "T1#1*1", "1", "1");
            }
        }
        Assert.assertTrue("Expected 4 tasks, misses " + n, n == 0);
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }

    /**
     * native task through xml
     */
    private static void testNativeJob() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(native_job.toURI()).getAbsolutePath());
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                ((NativeTask) job.getTask("T1")).setPreScript(new SimpleScript(preScriptWindows, "js"));
                ((NativeTask) job.getTask("T1")).setPostScript(new SimpleScript(postScriptWindows, "js"));
                String[] tab = ((NativeTask) job.getTask("T1")).getCommandLine();
                tab[0] = tab[0].replace("it.sh", "it.bat");
                ((NativeTask) job.getTask("T1")).setCommandLine(tab);
                break;
            case unix:
                ((NativeTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "js"));
                ((NativeTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "js"));
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        JobId id = TWorkflowJobs.testJobSubmission(job, null);

        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        int n = 4;
        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            String path = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + "/native_result_";
            if (result.getKey().equals("T1")) {
                n--;
                File f = new File(path + "0_0");
                BufferedReader in = new BufferedReader(new FileReader(f));
                checkResult(in.readLine(), "T1", "0", "0");
                in.close();
                f.delete();
            } else if (result.getKey().equals("T1*1")) {
                n--;
                File f = new File(path + "0_1");
                BufferedReader in = new BufferedReader(new FileReader(f));
                checkResult(in.readLine(), "T1*1", "0", "1");
                in.close();
                f.delete();
            } else if (result.getKey().equals("T1#1")) {
                n--;
                File f = new File(path + "1_0");
                BufferedReader in = new BufferedReader(new FileReader(f));
                checkResult(in.readLine(), "T1#1", "1", "0");
                in.close();
                f.delete();
            } else if (result.getKey().equals("T1#1*1")) {
                n--;
                File f = new File(path + "1_1");
                BufferedReader in = new BufferedReader(new FileReader(f));
                checkResult(in.readLine(), "T1#1*1", "1", "1");
                in.close();
                f.delete();
            }
        }
        Assert.assertTrue("Expected 4 tasks, misses " + n, n == 0);
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }

    /**
     * forked java task through API
     */
    private static void testForkedJob() throws Throwable {
        TaskFlowJob job = new TaskFlowJob();
        job.setName("Test flow");

        JavaTask t = new JavaTask();
        t.setName("T");
        t.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t.setForkEnvironment(new ForkEnvironment());
        t.setMaxNumberOfExecution(4);
        t.setFlowBlock(FlowBlock.START);
        FlowScript dup = FlowScript.createReplicateFlowScript(dupScript);
        t.setFlowScript(dup);
        job.addTask(t);

        JavaTask t1 = new JavaTask();
        t1.setName("T1");
        t1.setExecutableClassName("org.ow2.proactive.scheduler.examples.IterationAwareJob");
        t1.addArgument("it", "$IT");
        t1.addArgument("dup", "$REP");
        t1.setForkEnvironment(new ForkEnvironment());
        t1.setMaxNumberOfExecution(4);
        t1.addDependence(t);
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                t1.setPreScript(new SimpleScript(preScriptWindows, "javascript"));
                t1.setPostScript(new SimpleScript(postScriptWindows, "javascript"));
                break;
            case unix:
                t1.setPreScript(new SimpleScript(preScript, "javascript"));
                t1.setPostScript(new SimpleScript(postScript, "javascript"));
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        job.addTask(t1);

        JavaTask t2 = new JavaTask();
        t2.setName("T2");
        t2.addDependence(t1);
        t2.setExecutableClassName("org.ow2.proactive.scheduler.examples.EmptyTask");
        t2.setForkEnvironment(new ForkEnvironment());
        t2.setMaxNumberOfExecution(4);
        t2.setFlowBlock(FlowBlock.END);
        FlowScript loop = FlowScript.createLoopFlowScript(loopScript, t.getName());
        loop.setActionType(FlowActionType.LOOP);
        loop.setActionTarget(t);
        t2.setFlowScript(loop);
        job.addTask(t2);

        JobId id = TWorkflowJobs.testJobSubmission(job, null);
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        int n = 4;
        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            if (result.getKey().equals("T1")) {
                n--;
                checkResult(result.getValue().toString(), "T1", "0", "0");
            } else if (result.getKey().equals("T1*1")) {
                n--;
                checkResult(result.getValue().toString(), "T1*1", "0", "1");
            } else if (result.getKey().equals("T1#1")) {
                n--;
                checkResult(result.getValue().toString(), "T1#1", "1", "0");
            } else if (result.getKey().equals("T1#1*1")) {
                n--;
                checkResult(result.getValue().toString(), "T1#1*1", "1", "1");
            }
        }
        Assert.assertTrue("Expected 4 tasks, misses " + n, n == 0);
        SchedulerTHelper.removeJob(id);
        SchedulerTHelper.waitForEventJobRemoved(id);
    }

    private static void checkResult(String res, String name, String it, String dup) throws Throwable {
        String[] array = res.split(":");
        for (String str : array) {
            String[] ar = str.split(" ");
            if (ar[0].equals("arg")) {
                if (ar[1].equals("it")) {
                    Assert.assertEquals("Wrong iteration index in arguments for task " + name, it, ar[2]);
                } else if (ar[1].equals("dup")) {
                    Assert.assertEquals("Wrong replication index in arguments for task " + name, dup, ar[2]);
                }
            } else if (ar[0].equals("prop")) {
                if (ar[1].equals("it")) {
                    Assert.assertEquals("Wrong iteration index in properties for task " + name, it, ar[2]);
                } else if (ar[1].equals("dup")) {
                    Assert.assertEquals("Wrong replication index in properties for task " + name, dup, ar[2]);
                }
            }
        }

        File pre = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + "/PRE_" + it + "_" +
            dup);
        File post = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + "/POST_" + it + "_" +
            dup);
        Assert.assertTrue("Could not find PRE file: " + pre.getAbsolutePath(), pre.exists());
        Assert.assertTrue("Could not find POST file: " + post.getAbsolutePath(), post.exists());
        pre.delete();
        post.delete();
    }

}
