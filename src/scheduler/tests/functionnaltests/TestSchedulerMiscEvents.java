/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionnaltests;

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEvent;
import org.ow2.proactive.scheduler.common.job.JobFactory;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.scheduler.AdminSchedulerInterface;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerConnection;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerEvent;
import org.ow2.proactive.scheduler.common.scheduler.SchedulerUsers;
import org.ow2.proactive.scheduler.common.task.TaskEvent;
import org.ow2.proactive.scheduler.common.task.TaskResult;


/**
 * This class tests the different events of the ProActive scheduler :
 * Start, resume, freeze, pause, stop, etc...
 * 
 * 
 * @author The ProActive Team
 * @date 2 jun 08
 * @since ProActive 4.0
 */
public class TestSchedulerMiscEvents extends FunctionalTDefaultScheduler {

    private String username = "jl";
    private String password = "jl";

    private SchedulerEventReceiver receiver = null;
    private AdminSchedulerInterface schedAdminInterface;

    /**
     *  Starting and linking new scheduler ! <br/>
     *  This method will join a new scheduler and connect it as user.<br/>
     *  Then, it will register an event receiver to check the dispatched event.
     */
    @Before
    public void preRun() throws Exception {
        System.out.println("------------------------------ Starting and linking new scheduler !...");
        //join the scheduler
        schedulerAuth = SchedulerConnection.join(schedulerDefaultURL);
        //Log as user
        schedAdminInterface = schedulerAuth.logAsAdmin(username, password);
        //Create an Event receiver AO in order to observe jobs and tasks states changes
        receiver = (SchedulerEventReceiver) PAActiveObject.newActive(SchedulerEventReceiver.class.getName(),
                new Object[] {});
        //Register as EventListener AO previously created
        schedAdminInterface.addSchedulerEventListener(receiver);
    }

    /**
     * Tests start here.
     *
     * @throws Throwable any exception that can be thrown during the test.
     */
    @org.junit.Test
    public void run() throws Throwable {
        System.out.println("------------------------------ Test 1 : onStarted...");
        assertTrue(!schedAdminInterface.start().booleanValue());
        assertTrue(!schedAdminInterface.resume().booleanValue());
        //try to pause
        schedAdminInterface.pause();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.PAUSED));
        //try to freeze
        schedAdminInterface.freeze();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.FROZEN));
        //try to resume
        schedAdminInterface.resume();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.RESUMED));
        //try to stop
        schedAdminInterface.stop();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.STOPPED));

        System.out.println("------------------------------ Test 2 : onStopped...");
        assertTrue(!schedAdminInterface.stop().booleanValue());
        assertTrue(!schedAdminInterface.pause().booleanValue());
        assertTrue(!schedAdminInterface.freeze().booleanValue());
        assertTrue(!schedAdminInterface.resume().booleanValue());
        //try to start
        schedAdminInterface.start();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.STARTED));
        //try to pause
        schedAdminInterface.pause();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.PAUSED));

        System.out.println("------------------------------ Test 3 : onPaused...");
        assertTrue(!schedAdminInterface.pause().booleanValue());
        assertTrue(!schedAdminInterface.start().booleanValue());
        //try to resume
        schedAdminInterface.resume();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.RESUMED));
        //try to pause
        schedAdminInterface.freeze();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.FROZEN));

        System.out.println("------------------------------ Test 4 : onFrozen...");
        assertTrue(!schedAdminInterface.freeze().booleanValue());
        assertTrue(!schedAdminInterface.start().booleanValue());
        //try to pause
        schedAdminInterface.pause();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.PAUSED));
        //try to resume
        schedAdminInterface.resume();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.RESUMED));

        System.out.println("------------------------------ Test 4 : Shuttingdown...");
        //try to shutdown Scheduler
        schedAdminInterface.shutdown();
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.SHUTTING_DOWN));
        receiver.waitForNEvent(1);
        assertTrue(receiver.checkLastMiscEvents(SchedulerEvent.SHUTDOWN));
    }

}
