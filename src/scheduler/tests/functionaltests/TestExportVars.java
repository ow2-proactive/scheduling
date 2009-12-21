/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.util.Map;

import org.junit.Assert;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import functionalTests.FunctionalTest;


public class TestExportVars extends FunctionalTest {

    private static String jobDescriptor = TestJobPrePostSubmission.class.getResource(
            "/functionaltests/descriptors/Job_exportVars.xml").getPath();

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        JobId id = SchedulerTHelper.submitJob(jobDescriptor);
        SchedulerTHelper.waitForEventJobFinished(id);
        JobResult res = SchedulerTHelper.getJobResult(id);

        for (String i : res.getAllResults().keySet()) {
            System.out.println("====> Output " + i + " : " + res.getResult(i).getOutput().getAllLogs(true));
        }

        String taskid = "task1";
        TaskResult r = res.getResult(taskid);
        Map<String, String> exVal = r.getPropagatedProperties();

        System.out.println("+++++++++++++");
        for (String k : r.getPropagatedProperties().keySet()) {
            System.out.println("+++++++++++++" + k);
        }

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
        System.out.println("***************************************" + r.value());
        Assert.assertEquals(0, (Integer) r.value());

    }

}
