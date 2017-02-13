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
package functionaltests.scripts.nonforked;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;


@Ignore
public class TestNonForkedScriptTask extends SchedulerFunctionalTestNonForkedModeNoRestart {

    private static URL nonForked_jobDescriptor = TestNonForkedScriptTask.class.getResource("/functionaltests/descriptors/Job_non_forked_script_task.xml");

    @Ignore
    @Test
    public void nonForkedTasks_SystemExitScript_KillsANode() throws Throwable {

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory()
                                                      .createJob(new File(nonForked_jobDescriptor.toURI()).getAbsolutePath());

        schedulerHelper.submitJob(job);

        // busy event when task is scheduler
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // down event when node is killed
        RMNodeEvent nodeKilledEvent = schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals(NodeState.DOWN, nodeKilledEvent.getNodeState());
    }
}
