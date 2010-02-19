/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.masterworker.clear;

import functionaltests.*;
import functionalTests.FunctionalTest;
import functionaltests.masterworker.A;
import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TestClear extends FunctionalTest {

    private Master<A, Integer> master;
    private List<A> tasks1;
    private List<A> tasks2;
    private List<A> tasks3;
    public static final int NB_TASKS = 30;
    public static final int WAIT_STEP = 20;

    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.startScheduler();

        //before
        tasks1 = new ArrayList<A>();
        tasks2 = new ArrayList<A>();
        tasks3 = new ArrayList<A>();
        for (int i = 0; i < NB_TASKS; i++) {
            A t1 = new A(i, (NB_TASKS - i) * WAIT_STEP, false);
            A t2 = new A(i + NB_TASKS, (NB_TASKS - i) * WAIT_STEP, false);
            A t3 = new A(i + NB_TASKS * 2, (NB_TASKS - i) * WAIT_STEP, false);
            tasks1.add(t1);
            tasks2.add(t2);
            tasks3.add(t3);
        }

        master = new ProActiveMaster<A, Integer>();
        String[] classpath = new String[] {
                System.getProperty("pa.scheduler.home") + "/classes/schedulerTests",
                System.getProperty("pa.scheduler.home") + "/classes/scheduler" };
        master.addResources("rmi://localhost:" + PAProperties.PA_RMI_PORT.getValue() + "/", "demo", "demo",
                classpath);

        master.setResultReceptionOrder(Master.SUBMISSION_ORDER);

        //middle
        // We send a set of tasks to warm up the masterworker
        master.solve(tasks1);
        master.waitAllResults();
        // We send a set of tasks that will be canceled in the middle of their computation
        master.solve(tasks2);
        Thread.sleep((NB_TASKS / 2) * WAIT_STEP);
        master.clear();

        // We send a final set of tasks
        master.solve(tasks3);

        List<Integer> ids = master.waitAllResults();

        // We check that we received the results of the last set of tasks (and only these ones)
        Iterator<Integer> it = ids.iterator();
        int last = it.next();
        assertTrue("First received should be n0" + NB_TASKS * 2 + " here it's " + last, last == NB_TASKS * 2);
        while (it.hasNext()) {
            int next = it.next();
            assertTrue("Results recieved in submission order", last < next);
            last = next;
        }

        //after
        master.terminate(true);
    }

}
