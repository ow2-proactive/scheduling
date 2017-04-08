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
package functionaltests.api;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import functionaltests.executables.ResultAsArray;
import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * This class test a particular behavior.
 * It will submit one job of 50 tasks, then listen to the task events.
 * Each terminate tasks event will instantly generate a call to getTaskResult (to the core).
 * This could lead to concurrent access to database.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TestJobInstantGetTaskResult extends SchedulerFunctionalTestNoRestart {

    @Test
    public void testJobInstantGetTaskResult() throws Throwable {
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
        PAActiveObject.terminateActiveObject(client, true);
    }

}
