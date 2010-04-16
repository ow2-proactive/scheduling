/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.masterworker.basicordered;

import functionalTests.FunctionalTest;
import functionaltests.masterworker.A;
import functionaltests.*;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Test Master/Worker ordering
 */
public class TestBasicOrdered extends FunctionalTest {
    private Master<A, Integer> master;
    private List<A> tasks;
    public static final int NB_TASKS = 30;

    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.startScheduler();

        //before
        master = new ProActiveMaster<A, Integer>();
        String[] classpath = new String[] {
                System.getProperty("pa.scheduler.home") + "/classes/schedulerTests",
                System.getProperty("pa.scheduler.home") + "/classes/scheduler" };
        master.addResources("rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/",
                "demo", "demo", classpath);
        master.setResultReceptionOrder(Master.SUBMISSION_ORDER);

        //middle
        tasks = new ArrayList<A>();
        for (int i = 0; i < NB_TASKS; i++) {
            A t = new A(i, (NB_TASKS - i) * 100, false);
            tasks.add(t);
        }
        master.solve(tasks);
        // We stress the ordering heavily by calling multiple wait methods
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(master.waitOneResult());
        int nbResults = master.countAvailableResults();
        ids.addAll(master.waitKResults(5));
        ids.add(master.waitOneResult());
        ids.addAll(master.waitAllResults());
        nbResults = master.countAvailableResults();
        assertTrue(nbResults == 0);
        assertTrue(master.isEmpty());

        // We check that the correct order is received
        Iterator<Integer> it = ids.iterator();
        int last = it.next();
        while (it.hasNext()) {
            int next = it.next();
            assertTrue("Results recieved in submission order", last < next);
            last = next;
        }

        //after
        master.terminate(true);
    }

}
