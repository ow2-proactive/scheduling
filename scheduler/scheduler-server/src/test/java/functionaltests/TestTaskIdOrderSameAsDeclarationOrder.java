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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.junit.Test;

import static functionaltests.SchedulerTHelper.removeJob;
import static functionaltests.SchedulerTHelper.submitJob;
import static functionaltests.SchedulerTHelper.waitForEventJobFinished;
import static functionaltests.SchedulerTHelper.waitForEventJobRemoved;
import static functionaltests.SchedulerTHelper.waitForEventJobSubmitted;
import static org.junit.Assert.assertEquals;


public class TestTaskIdOrderSameAsDeclarationOrder extends SchedulerConsecutive {

    private static URL jobDescriptor = TestTaskIdOrderSameAsDeclarationOrder.class
            .getResource("/functionaltests/descriptors/Job_Id_Order.xml");

    @Test
    public void task_ids_should_be_ordered_as_task_declaration_order() throws Throwable {
        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(jobDescriptor.toURI()).getAbsolutePath());

        JobId id = submitJob(job);
        JobState jobState = waitForEventJobSubmitted(id);

        List<TaskState> sortedTasksById = sortTasksById(jobState);

        assertEquals("premiere", sortedTasksById.get(0).getName());
        assertEquals("deuxieme", sortedTasksById.get(1).getName());
        assertEquals("troisieme", sortedTasksById.get(2).getName());

        //remove job
        waitForEventJobFinished(id);
        removeJob(id);
        waitForEventJobRemoved(id);
    }

    private List<TaskState> sortTasksById(JobState jobState) {
        TreeMap<TaskId, TaskState> sortedTasks = new TreeMap<TaskId, TaskState>(new Comparator<TaskId>() {
            @Override
            public int compare(TaskId o1, TaskId o2) {
                return Integer.parseInt(o1.value()) - Integer.parseInt(o2.value());
            }
        });
        sortedTasks.putAll(jobState.getHMTasks());
        return new ArrayList<TaskState>(sortedTasks.values());
    }
}
