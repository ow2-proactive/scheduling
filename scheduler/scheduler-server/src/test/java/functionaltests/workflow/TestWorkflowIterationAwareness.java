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
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Map.Entry;

import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.junit.Assert;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;


/**
 * Ensures iteration and replication index are properly propagated and replaced
 * when needed in workflow-enabled tasks
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowIterationAwareness extends SchedulerConsecutive {

    private static final String tmp_dir_Windows = "C:\\tmp\\";

    private static final URL java_job = TestWorkflowIterationAwareness.class
            .getResource("/functionaltests/workflow/descriptors/flow_it_1.xml");

    private static final URL native_job = TestWorkflowIterationAwareness.class
            .getResource("/functionaltests/workflow/descriptors/flow_it_2.xml");

    private static final String preScript = //
    "def f = new File(\"" + System.getProperty("java.io.tmpdir") + "/PRE_" + "\"+ variables.get('PA_TASK_ITERATION') + \"" + "_" + "\"+ variables.get('PA_TASK_REPLICATION') +\"" + "\"); \n" + //
        "f.createNewFile(); \n";

    private static final String postScript = //
      "def f = new File(\"" + System.getProperty("java.io.tmpdir") + "/POST_" + "\"+ variables.get('PA_TASK_ITERATION') + \"" + "_" + "\"+ variables.get('PA_TASK_REPLICATION') +\"" + "\"); \n" + //
        "f.createNewFile(); \n";

    /**
     * Checks Java and Native executables
     * on a loop/replicate job for propagation of iteration and replication indexes in :
     * native arguments, java arguments, pre/post scripts, native environment variables, java properties
     * 
     * @throws Throwable
     */
    @org.junit.Test
    public void run() throws Throwable {

        File tmpDir = new File(tmp_dir_Windows);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }

        testJavaJob();
        testNativeJob();
    }

    /**
     * java task through xml
     */
    private static void testJavaJob() throws Throwable {

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(java_job.toURI()).getAbsolutePath());
        ((JavaTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "groovy"));
        ((JavaTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "groovy"));

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
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(native_job.toURI()).getAbsolutePath());
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                ((NativeTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "groovy"));
                ((NativeTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "groovy"));
                String[] tab = ((NativeTask) job.getTask("T1")).getCommandLine();
                tab[0] = "\"" + tab[0].replace("it.sh", "it.bat") + "\"";
                tab[1] = tmp_dir_Windows;
                ((NativeTask) job.getTask("T1")).setCommandLine(tab);
                break;
            case unix:
                ((NativeTask) job.getTask("T1")).setPreScript(new SimpleScript(preScript, "groovy"));
                ((NativeTask) job.getTask("T1")).setPostScript(new SimpleScript(postScript, "groovy"));
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        JobId id = TWorkflowJobs.testJobSubmission(job, null);
        JobResult res = SchedulerTHelper.getJobResult(id);
        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());

        int n = 4;
        for (Entry<String, TaskResult> result : res.getAllResults().entrySet()) {
            String path = "";
            switch (OperatingSystem.getOperatingSystem()) {
                case windows:
                    File tmpdir = new File(tmp_dir_Windows, "native_result_");
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

        File pre = null;
        File post = null;
        switch (OperatingSystem.getOperatingSystem()) {
            case windows:
                pre = new File(tmp_dir_Windows, "PRE_" + it + "_" + dup);
                post = new File(tmp_dir_Windows, "POST_" + it + "_" + dup);
                break;
            case unix:
                pre = new File(System.getProperty("java.io.tmpdir"), "PRE_" + it + "_" + dup);
                post = new File(System.getProperty("java.io.tmpdir"), "POST_" + it + "_" + dup);
                break;
            default:
                throw new IllegalStateException("Operating system not supported");
        }

        Assert.assertTrue("Could not find PRE file: " + pre.getAbsolutePath(), pre.exists());
        Assert.assertTrue("Could not find POST file: " + post.getAbsolutePath(), post.exists());
        pre.delete();
        post.delete();
    }

}
