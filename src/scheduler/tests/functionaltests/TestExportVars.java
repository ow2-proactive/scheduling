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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.proactive.scheduler.common.task.NativeTask;
import org.ow2.proactive.scheduler.common.task.TaskResult;


public class TestExportVars extends SchedulerConsecutive {

    private static URL jobDescriptor = TestJobPrePostSubmission.class
            .getResource("/functionaltests/descriptors/Job_exportVars.xml");

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) JobFactory_stax.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            ((NativeTask) job.getTask("task2")).setCommandLine("cmd", "/C", "date", "/t");
            String[] pathTable = ((NativeTask) job.getTask("task5")).getCommandLine();
            String path = "";
            for (String peace : pathTable) {
                path += peace + " ";
            }
            path = path.substring(0, path.length() - 4) + ".bat";
            ((NativeTask) job.getTask("task5")).setCommandLine(path);
        }
        JobId id = SchedulerTHelper.submitJob(job);
        SchedulerTHelper.waitForEventJobFinished(id);
        JobResult res = SchedulerTHelper.getJobResult(id);

        String taskid = "task1";
        TaskResult r = res.getResult(taskid);
        Map<String, String> exVal = r.getPropagatedProperties();

        Assert.assertTrue(exVal != null);
        Assert.assertTrue(exVal.get("key1").equals("value1"));
        Assert.assertTrue(exVal.get("key2").equals("value2"));

        taskid = "task2";
        r = res.getResult(taskid);
        // no exception in post script evaluation
        Assert.assertTrue(!r.hadException());

        taskid = "task3ex";
        r = res.getResult(taskid);
        // exception in post script evaluation
        Assert.assertTrue(r.hadException());

        // nothing to test for task4

        taskid = "task5";
        r = res.getResult(taskid);
        // exception in post script evaluation
        Assert.assertEquals((Integer) 0, (Integer) r.value());

    }

}
