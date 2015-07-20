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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests;

import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.tests.Consecutive;
import org.ow2.tests.ProActiveTest;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;


/**
 * 
 * The parent class for all consecutive functional tests.
 *
 */
@Consecutive
public class SchedulerConsecutive extends ProActiveTest {

    protected SchedulerTHelper schedulerHelper;
    protected static final Logger logger = Logger.getLogger("SchedulerTests");

    @Test
    public void startSchedulerIfNeeded() throws Exception {
        schedulerHelper = SchedulerTHelper.startScheduler();
    }

    @After
    public void killAllProcessesIfNeeded() throws Exception {

        if (RMFunctionalTest.shouldBeExecutedInConsecutiveMode(this.getClass())) {

            try {
                schedulerHelper.getResourceManager().removeNodeSource("extra", true).getBooleanValue();
            } catch (IllegalArgumentException e) {
                // ns extra not found
            }
            RMInitialState state = RMTHelper.getDefaultInstance(SchedulerTHelper.PNP_PORT)
                    .getResourceManager().getMonitoring().getState();
            System.out.println("RMState after the test execution");
            for (RMNodeEvent nodeEvent : state.getNodesEvents()) {
                System.out.println(nodeEvent);
            }
        }
        // super.killAllProcessesIfNeeded();
    }
}
