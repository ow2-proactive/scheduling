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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.topology.descriptor.SingleHostDescriptor;

import functionalTests.FunctionalTest;
import functionaltests.SchedulerTHelper;


/**
 * Tests various task properties and characteristics are preserved upon replication
 * 
 * 
 * 
 * @since ProActive Scheduling 3.0.2
 * 
 */
public class TestWorkflowTask extends FunctionalTest {

    private static final String jobs_path = TestWorkflowTask.class.getResource(
            "/functionaltests/workflow/descriptors/").getPath();

    @org.junit.Test
    public void run() throws Throwable {

        String dsOut = null;

        String job_path = jobs_path + "test_wf_replica.xml";
        JobId job = SchedulerTHelper.submitJob(job_path);

        JobInfo jInfo = SchedulerTHelper.waitForEventJobFinished(job);
        Assert.assertEquals(JobStatus.FINISHED, jInfo.getStatus());

        JobResult result = SchedulerTHelper.getJobResult(job);
        Assert.assertFalse(result.hadException());

        /*
         * check exactly all expected tasks are here
         */
        Assert.assertTrue("Expected 8 task results", result.getAllResults().size() == 8);
        for (String name : new String[] { "split", "split#1", "replica", "replica*1", "replica#1",
                "replica#1*1", "loop", "loop#1" }) {
            Assert.assertTrue("No result for task " + name, result.getAllResults().containsKey(name));
        }

        /*
         * retrieve TaskStates
         */
        Map<String, TaskState> tasks = new HashMap<String, TaskState>();
        for (TaskState t : SchedulerTHelper.getSchedulerInterface().getJobState(job).getTasks()) {
            tasks.put(t.getName(), t);
        }

        /*
         * check the properties of the replicated 'replica' task
         */
        for (Entry<String, TaskResult> results : result.getAllResults().entrySet()) {
            String name = results.getKey();
            TaskResult res = results.getValue();
            TaskId tid = res.getTaskId();

            if (name.startsWith("replica")) {

                /*
                 * check task name 
                 */
                String resultRegex = ".*result " + tid.getIterationIndex() + " " + tid.getReplicationIndex() +
                    ".*";
                Assert.assertTrue("Unexpected result for " + name + ": " + res.value().toString(), res
                        .value().toString().matches(resultRegex));

                TaskState ts = tasks.get(name);

                /*
                 * check various simple properties
                 */
                Assert.assertEquals(4, ts.getMaxNumberOfExecution());
                Assert.assertEquals(2, ts.getNumberOfNodesNeeded());
                Assert.assertTrue(ts.isParallel());
                Assert.assertTrue(ts.isPreciousResult());
                Assert.assertTrue(ts.isWallTimeSet());
                Assert.assertFalse(ts.isCancelJobOnError());
                Assert.assertEquals("foo.bar.Baz", ts.getResultPreview());

                /*
                 * check input files
                 */
                for (InputSelector in : ts.getInputFilesList()) {
                    Assert.assertEquals(InputAccessMode.TransferFromOutputSpace, in.getMode());
                    Assert.assertEquals(1, in.getInputFiles().getIncludes().length);

                    String inc = in.getInputFiles().getIncludes()[0];
                    Assert.assertTrue(inc.equals("foo_$IT_$REP.in") || inc.equals("bar_$IT_$REP.in"));
                }
                /*
                 * check output files
                 */
                for (OutputSelector out : ts.getOutputFilesList()) {
                    Assert.assertEquals(OutputAccessMode.TransferToOutputSpace, out.getMode());
                    Assert.assertEquals(1, out.getOutputFiles().getIncludes().length);

                    String inc = out.getOutputFiles().getIncludes()[0];
                    Assert.assertTrue(inc.equals("foo_$IT_$REP.out") || inc.equals("bar_$IT_$REP.out"));
                }

                /*
                 * Generic informations
                 */
                Assert.assertEquals(ts.getGenericInformations().get("info1"), "value1");
                Assert.assertEquals(ts.getGenericInformations().get("info2"), "value2");

                /*
                 * Dependencies
                 */
                if (tid.getIterationIndex() == 0) {
                    Assert.assertEquals("split", ts.getDependences().get(0).getName());
                } else {
                    Assert.assertEquals("split#1", ts.getDependences().get(0).getName());
                }

                /*
                 * Topology
                 */
                Assert.assertTrue(SingleHostDescriptor.class.isAssignableFrom(ts.getParallelEnvironment()
                        .getTopologyDescriptor().getClass()));

                /*
                 * Selection script
                 */
                Assert.assertFalse(ts.getSelectionScripts().isEmpty());

                String out = res.getOutput().getAllLogs(false);

                /*
                 * Pre script execution
                 */
                Assert.assertTrue(out.contains("Pre script " + tid.getIterationIndex() + " " +
                    tid.getReplicationIndex()));
                /*
                 * Post script execution
                 */
                Assert.assertTrue(out.contains("Post script " + tid.getIterationIndex() + " " +
                    tid.getReplicationIndex()));
                /*
                 * ForkedJavaExecutable output
                 */
                Assert.assertTrue(out.contains("hello " + tid.getIterationIndex() + " " +
                    tid.getReplicationIndex()));

            }

            else if (name.startsWith("loop")) {

                String out = res.getOutput().getAllLogs(false);

                /*
                 * generation script command output
                 */
                Assert.assertTrue(out.contains("generated hello " + tid.getIterationIndex()));

                if (tid.getIterationIndex() == 1) {
                    String[] lines = out.split("\n");
                    for (String line : lines) {
                        if (line.matches("out[ ].*")) {
                            dsOut = line.split(" ")[1];
                        }
                    }
                }
            }
        }

        /*
         * check files creation/deletion by scripts
         */
        String tmp = System.getProperty("java.io.tmpdir");
        File f1 = new File(tmp + "/sched_wf_test_toclean");
        File f2 = new File(tmp + "/sched_wf_test_toNOTclean");

        Assert.assertFalse(f1.exists());
        Assert.assertTrue(f2.exists());

        /*
         * check files creation through DS in Forked executable
         */
        for (String base : new String[] { "foo", "bar" }) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    String name = dsOut + "/" + base + "_" + i + "_" + j + ".out";
                    File f = new File(name);

                    Assert.assertTrue(name + " does not exist ", f.exists());

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                    String line = br.readLine();

                    String content = name.substring(0, name.length() - 4) + ".in foo_val bar_val";
                    Assert.assertTrue(name + "' does not contains '" + content, line.contains(content));
                }
            }
        }
    }
}
