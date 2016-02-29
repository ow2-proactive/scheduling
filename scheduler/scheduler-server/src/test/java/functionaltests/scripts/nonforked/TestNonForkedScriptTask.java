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
package functionaltests.scripts.nonforked;

import functionaltests.utils.SchedulerFunctionalTestNonForkedModeNoRestart;
import org.junit.Ignore;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;


@Ignore
public class TestNonForkedScriptTask extends SchedulerFunctionalTestNonForkedModeNoRestart {

    private static URL nonForked_jobDescriptor = TestNonForkedScriptTask.class
            .getResource("/functionaltests/descriptors/Job_non_forked_script_task.xml");


    @Ignore
    @Test
    public void nonForkedTasks_SystemExitScript_KillsANode() throws Throwable {

        TaskFlowJob job = (TaskFlowJob) StaxJobFactory.getFactory().createJob(
                new File(nonForked_jobDescriptor.toURI()).getAbsolutePath());

        schedulerHelper.submitJob(job);

        // busy event when task is scheduler
        schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);

        // down event when node is killed
        RMNodeEvent nodeKilledEvent = schedulerHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
        assertEquals(NodeState.DOWN, nodeKilledEvent.getNodeState());
    }
}
