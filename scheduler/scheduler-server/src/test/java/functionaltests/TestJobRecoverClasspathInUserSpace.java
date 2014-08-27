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

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.tests.FunctionalTest;

import org.junit.Test;

import static functionaltests.TestPauseJobRecover.CommunicationObject;
import static functionaltests.TestPauseJobRecover.createJob;


public class TestJobRecoverClasspathInUserSpace extends FunctionalTest {

    @Test
    // SCHEDULING-2077
    public void run() throws Throwable {
        CommunicationObject controlJobExecution = PAActiveObject.newActive(CommunicationObject.class,
                new Object[] {});

        TaskFlowJob job = createJob(PAActiveObject.getUrl(controlJobExecution));
        JobEnvironment env = new JobEnvironment();
        env.setJobClasspath(new String[] { "$USERSPACE/test.jar" });
        job.setEnvironment(env);

        JobId idJ1 = SchedulerTHelper.submitJob(job);
        SchedulerTHelper.waitForEventJobRunning(idJ1);

        SchedulerTHelper.log("Kill Scheduler");
        SchedulerTHelper.killSchedulerAndNodesAndRestart(new File(SchedulerTHelper.class.getResource(
                "config/functionalTSchedulerProperties-updateDB.ini").toURI()).getAbsolutePath());

        SchedulerTHelper.log("Finish job 1");
        controlJobExecution.setCanFinish(true);

        SchedulerTHelper.log("Waiting for job 1 to finish");
        SchedulerTHelper.waitForEventJobFinished(idJ1, 30 * 1000);
    }

}
