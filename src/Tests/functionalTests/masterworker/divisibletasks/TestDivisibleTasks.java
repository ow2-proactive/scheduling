/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.masterworker.divisibletasks;

import functionalTests.FunctionalTest;
import functionalTests.masterworker.basicordered.TestBasicOrdered;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Test Master/Worker divisible task a merge/sort algorithm test
 */
public class TestDivisibleTasks extends FunctionalTest {
    private URL descriptor = TestBasicOrdered.class
            .getResource("/functionalTests/masterworker/TestMasterWorker.xml");
    private Master<DaCSort, ArrayList<Integer>> master;
    private List<DaCSort> tasks;
    public static final int NB_ELEM = 50000;

    @org.junit.Test
    public void action() throws Exception {
        System.out.println(descriptor);
        tasks = new ArrayList<DaCSort>();
        ArrayList<Integer> bigList = new ArrayList<Integer>();
        for (int i = 0; i < NB_ELEM; i++) {
            bigList.add((int) Math.round(Math.random() * NB_ELEM));
        }
        tasks.add(new DaCSort(bigList));

        master.solve(tasks);

        ArrayList<Integer> answer = master.waitOneResult();

        for (int i = 0; i < answer.size() - 1; i++) {
            assertTrue("List sorted", answer.get(i) <= answer.get(i + 1));
        }
    }

    @Before
    public void initTest() throws Exception {

        master = new ProActiveMaster<DaCSort, ArrayList<Integer>>();
        master.addResources(descriptor);
        master.setResultReceptionOrder(Master.SUBMISSION_ORDER);

    }

    @After
    public void endTest() throws Exception {
        master.terminate(true);
    }
}