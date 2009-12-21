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

import java.util.Map.Entry;

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
        //        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());
        SchedulerTHelper.waitForEventJobFinished(id);

        // check result are not null
        JobResult res = SchedulerTHelper.getJobResult(id);
        //        Assert.assertFalse(SchedulerTHelper.getJobResult(id).hadException());
        //
        //        for (Entry<String, TaskResult> entry : res.getAllResults().entrySet()) {
        //            Assert.assertNotNull(entry.getValue().value());
        //        }
        //        //remove job
        //        SchedulerTHelper.removeJob(id);
        //        SchedulerTHelper.waitForEventJobRemoved(id);

        for (String n : res.getAllResults().keySet()) {
            TaskResult r = res.getResult(n);
            System.out.println(n + " result : [" + r + "]");
            System.out.println(n + " logs : [" + r.getOutput().getAllLogs(false) + "]");
        }

    }
}
