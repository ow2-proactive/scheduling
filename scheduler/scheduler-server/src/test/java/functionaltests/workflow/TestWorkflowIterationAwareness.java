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
package functionaltests.workflow;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.SimpleScript;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * Ensures iteration and replication index are properly propagated and replaced
 * when needed in workflow-enabled tasks
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowIterationAwareness extends SchedulerFunctionalTestNoRestart {

    private static final URL java_job = TestWorkflowIterationAwareness.class.getResource("/functionaltests/workflow/descriptors/flow_it_1.xml");

    private static final URL native_job = TestWorkflowIterationAwareness.class.getResource("/functionaltests/workflow/descriptors/flow_it_2.xml");

    private static final URL java_job_Schema33 = TestWorkflowIterationAwareness.class.getResource("/functionaltests/workflow/descriptors/flow_it_1_Schema33.xml");

    private static final URL native_job_Schema33 = TestWorkflowIterationAwareness.class.getResource("/functionaltests/workflow/descriptors/flow_it_2_Schema33.xml");

    private static final String tmpFolder = System.getProperty("java.io.tmpdir");

    private static final String preScript = //
                                          "def f = new File(System.getProperty(\"java.io.tmpdir\")+\"" + "/PRE_" +
                                            "\"+ variables.get('PA_TASK_ITERATION') + \"" + "_" +
                                            "\"+ variables.get('PA_TASK_REPLICATION') +\"" + "\"); \n" + //
                                            "f.createNewFile(); \n";

    private static final String postScript = //
                                           "def f = new File(System.getProperty(\"java.io.tmpdir\")+\"" + "/POST_" +
                                             "\"+ variables.get('PA_TASK_ITERATION') + \"" + "_" +
                                             "\"+ variables.get('PA_TASK_REPLICATION') +\"" + "\"); \n" + //
                                             "f.createNewFile(); \n";

    /**
     * Checks Java and Native executables
     * on a loop/replicate job for propagation of iteration and replication indexes in :
     * native arguments, java arguments, pre/post scripts, native environment variables, java properties
     * 
     * @throws Throwable
     */
    @Test
    public void testWorkflowIterationAwareness() throws Throwable {
        testJavaJob(new File(java_job.toURI()).getAbsolutePath());
        testNativeJob(new File(native_job.toURI()).getAbsolutePath());
    }

    /**
     * Checks Java and Native executables workflows in schema 33
     * on a loop/replicate job for propagation of iteration and replication indexes in :
     * native arguments, java arguments, pre/post scripts, native environment variables, java properties
     *
     * @throws Throwable
     */
    @Test
    public void testWorkflowIterationAwarenessCompatibilitySchema33() throws Throwable {
        testJavaJob(new File(java_job_Schema33.toURI()).getAbsolutePath());
        testNativeJob(new File(native_job_Schema33.toURI()).getAbsolutePath());
    }

    /**
     * java task through xml
     */
    private void testJavaJob(String jobDescriptorPath) throws Throwable {

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(jobDescriptorPath);
        ((JavaTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "groovy"));
        ((JavaTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "groovy"));

        JobId id = TWorkflowJobs.testJobSubmission(schedulerHelper, job, null);
        JobResult res = schedulerHelper.getJobResult(id);
        Assert.assertFalse(schedulerHelper.getJobResult(id).hadException());

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
        assertTrue("Expected 4 tasks, misses " + n, n == 0);
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }

    /**
     * native task through xml
     */
    private void testNativeJob(String jobDescriptorPath) throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(jobDescriptorPath);
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                ((NativeTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "groovy"));
                ((NativeTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "groovy"));
                String[] tab = ((NativeTask) job.getTask("T1")).getCommandLine();
                tab[0] = "\"" + tab[0].replace("it.sh", "it.bat") + "\"";
                tab[1] = tmpFolder;
                ((NativeTask) job.getTask("T1")).setCommandLine(tab);
                break;
            case unix:
                job.getTask("T1").setPreScript(new SimpleScript(preScript, "groovy"));
                job.getTask("T1").setPostScript(new SimpleScript(postScript, "groovy"));
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        JobId id = TWorkflowJobs.testJobSubmission(schedulerHelper, job, null);
        JobResult res = schedulerHelper.getJobResult(id);
        Assert.assertFalse(schedulerHelper.getJobResult(id).hadException());

        int n = 4;
        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            String path = "";
            switch (OperatingSystem.getOperatingSystem()) {
                case windows:
                    File tmpdir = new File(tmpFolder, "native_result_");
                    path = tmpdir.getAbsolutePath();
                    break;

                case unix:
                    path = System.getProperty("java.io.tmpdir") + "/native_result_";
                    break;
                default:
                    throw new IllegalStateException("Operating system not supported");
            }

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
        assertTrue("Expected 4 tasks, misses " + n, n == 0);

        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
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

        File pre = null;
        File post = null;
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                pre = new File(tmpFolder, "PRE_" + it + "_" + dup);
                post = new File(tmpFolder, "POST_" + it + "_" + dup);
                break;
            case unix:
                pre = new File(System.getProperty("java.io.tmpdir"), "PRE_" + it + "_" + dup);
                post = new File(System.getProperty("java.io.tmpdir"), "POST_" + it + "_" + dup);
                break;
            default:
                throw new IllegalStateException("Operating system not supported");
        }

        assertTrue("Could not find PRE file: " + pre.getAbsolutePath(), pre.exists());
        assertTrue("Could not find POST file: " + post.getAbsolutePath(), post.exists());
        pre.delete();
        post.delete();
    }

}
