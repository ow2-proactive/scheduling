/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import static junit.framework.Assert.assertTrue;

import org.ow2.proactive.scheduler.common.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.SchedulerEvent;

import functionalTests.FunctionalTest;


/**
 * This class tests the different job events of the ProActive scheduler :
 * pauseJob, resumeJob, killjob, changePriority, etc...
 *
 *
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive Scheduling 1.0
 */
public class TestSchedulerMiscEvents extends FunctionalTest {

    /**
     * Tests starts here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {

        AdminSchedulerInterface schedAdminInterface = SchedulerTHelper.getAdminInterface();

        SchedulerTHelper.log("Try many tests about scheduler state");

        assertTrue(!schedAdminInterface.start().booleanValue());
        assertTrue(!schedAdminInterface.resume().booleanValue());
        assertTrue(schedAdminInterface.stop().booleanValue());
        SchedulerTHelper.log("waiting scheduler stopped  event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.STOPPED);

        assertTrue(!schedAdminInterface.pause().booleanValue());
        assertTrue(!schedAdminInterface.freeze().booleanValue());
        assertTrue(schedAdminInterface.start().booleanValue());
        SchedulerTHelper.log("waiting scheduler started event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.STARTED);

        assertTrue(schedAdminInterface.pause().booleanValue());
        SchedulerTHelper.log("waiting scheduler paused event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);

        assertTrue(schedAdminInterface.freeze().booleanValue());
        SchedulerTHelper.log("waiting scheduler frozen event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.FROZEN);

        assertTrue(schedAdminInterface.stop().booleanValue());
        SchedulerTHelper.log("waiting scheduler stopped  event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.STOPPED);

        assertTrue(!schedAdminInterface.resume().booleanValue());
        //TODO resume from stopped doesn't throw any event ?

        assertTrue(schedAdminInterface.start().booleanValue());
        SchedulerTHelper.log("waiting scheduler started event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.STARTED);

        assertTrue(schedAdminInterface.freeze().booleanValue());
        SchedulerTHelper.log("waiting scheduler frozen event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.FROZEN);

        assertTrue(schedAdminInterface.resume().booleanValue());
        SchedulerTHelper.log("waiting scheduler resumed  event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RESUMED);

        assertTrue(schedAdminInterface.pause().booleanValue());
        SchedulerTHelper.log("waiting scheduler paused event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.PAUSED);

        assertTrue(schedAdminInterface.resume().booleanValue());
        SchedulerTHelper.log("waiting scheduler resumed  event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.RESUMED);

        assertTrue(!schedAdminInterface.start().booleanValue());
        SchedulerTHelper.log("waiting scheduler shutting down event");
        assertTrue(schedAdminInterface.shutdown().booleanValue());

        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.SHUTTING_DOWN);

        SchedulerTHelper.log("waiting scheduler shutted down event");
        SchedulerTHelper.waitForEventSchedulerState(SchedulerEvent.SHUTDOWN);
    }
}
