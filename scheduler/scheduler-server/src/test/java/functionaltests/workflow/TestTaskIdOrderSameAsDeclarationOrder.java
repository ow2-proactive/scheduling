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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


public class TestTaskIdOrderSameAsDeclarationOrder extends SchedulerFunctionalTestNoRestart {

    private static URL jobDescriptor = TestTaskIdOrderSameAsDeclarationOrder.class.getResource("/functionaltests/descriptors/Job_Id_Order.xml");

    @Test
    public void task_ids_should_be_ordered_as_task_declaration_order() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = schedulerHelper.submitJob(job);
        JobState jobState = schedulerHelper.waitForEventJobSubmitted(id);

        List<TaskState> sortedTasksById = sortTasksById(jobState);

        assertEquals("premiere", sortedTasksById.get(0).getName());
        assertEquals("deuxieme", sortedTasksById.get(1).getName());
        assertEquals("troisieme", sortedTasksById.get(2).getName());

        //remove job
        schedulerHelper.waitForEventJobFinished(id);
        schedulerHelper.removeJob(id);
        schedulerHelper.waitForEventJobRemoved(id);
    }

    private List<TaskState> sortTasksById(JobState jobState) {
        TreeMap<TaskId, TaskState> sortedTasks = new TreeMap<>(new Comparator<TaskId>() {
            @Override
            public int compare(TaskId o1, TaskId o2) {
                return Integer.parseInt(o1.value()) - Integer.parseInt(o2.value());
            }
        });
        sortedTasks.putAll(jobState.getHMTasks());
        return new ArrayList<>(sortedTasks.values());
    }
}
