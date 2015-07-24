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
package functionaltests.api;

import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;
import functionaltests.executables.ResultAsArray;


/**
 * This class test a particular behavior.
 * It will submit one job of 50 tasks, then listen to the task events.
 * Each terminate tasks event will instantly generate a call to getTaskResult (to the core).
 * This could lead to concurrent access to database.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobInstantGetTaskResult extends SchedulerFunctionalTest {

    @Test
    public void run() throws Throwable {
        //create Scheduler client as an active object
        SubmitJob client = (SubmitJob) PAActiveObject.newActive(SubmitJob.class.getName(), new Object[] {});
        //begin to use the client : must be a futur result in order to start the scheduler at next step
        client.begin();

        //create job
        TaskFlowJob job = new TaskFlowJob();

        for (int i = 0; i < 50; i++) {
            JavaTask t = new JavaTask();
            t.setExecutableClassName(ResultAsArray.class.getName());
            t.setName("task" + i);
            job.addTask(t);
        }

        JobId id = schedulerHelper.submitJob(job);
        client.setJobId(id);

        schedulerHelper.waitForEventJobRemoved(id);
    }

}