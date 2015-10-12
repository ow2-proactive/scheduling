/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.job.recover;

import java.io.File;
import java.nio.file.Path;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.util.FileLock;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.utils.SchedulerTHelper;

import static functionaltests.utils.SchedulerTHelper.log;
import static functionaltests.job.recover.TestPauseJobRecover.createJob;


public class TestJobRecoverClasspathInUserSpace extends SchedulerFunctionalTest {

    // SCHEDULING-2077
    @Test
    public void run() throws Throwable {
        FileLock controlJobExecution = new FileLock();
        Path controlJobExecutionPath = controlJobExecution.lock();

        TaskFlowJob job = createJob(controlJobExecutionPath.toString());

        ForkEnvironment forkEnvironment = new ForkEnvironment();
        forkEnvironment.addAdditionalClasspath("$USERSPACE/test.jar");

        for (Task task : job.getTasks()) {
            task.setForkEnvironment(forkEnvironment);
        }

        JobId idJ1 = schedulerHelper.submitJob(job);
        schedulerHelper.waitForEventJobRunning(idJ1);

        log("Kill Scheduler");
        schedulerHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "/functionaltests/config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        log("Finish job 1");
        controlJobExecution.unlock();

        log("Waiting for job 1 to finish");
        schedulerHelper.waitForEventJobFinished(idJ1, 30000);
    }

}
