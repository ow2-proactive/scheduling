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
package functionalTests.masterworker.userexception;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import functionalTests.FunctionalTest;
import functionalTests.masterworker.A;
import static junit.framework.Assert.assertTrue;


/**
 * Test load balancing
 */
public class TestUserEx extends FunctionalTest {
    private URL descriptor = TestUserEx.class
            .getResource("/functionalTests/masterworker/TestMasterWorker.xml");
    private Master<A, Integer> master;
    private List<A> tasks;
    public static final int NB_TASKS = 4;

    @org.junit.Test
    public void action() throws Exception {
        boolean catched = false;

        master.solve(tasks);
        try {
            List<Integer> ids = master.waitAllResults();
            // we don't care of the results
            ids.clear();
        } catch (TaskException e) {
            assertTrue("Expected exception is the cause", e.getCause() instanceof ArithmeticException);
            catched = true;
        }
        assertTrue("Exception caught as excepted", catched);
    }

    @Before
    public void initTest() throws Exception {
        tasks = new ArrayList<A>();
        for (int i = 0; i < NB_TASKS; i++) {
            // tasks that throw an exception
            A t = new A(i, 0, true);
            tasks.add(t);
        }
        //@snippet-start master_creation        
        master = new ProActiveMaster<A, Integer>();
        //@snippet-end master_creation   
        master.addResources(descriptor);
    }

    @After
    public void endTest() throws Exception {
        master.terminate(true);
    }
}
