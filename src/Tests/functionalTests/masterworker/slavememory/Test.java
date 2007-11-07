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
package functionalTests.masterworker.slavememory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extra.masterworker.ProActiveMaster;
import org.objectweb.proactive.extra.masterworker.interfaces.Master;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Test load balancing
 */
public class Test extends FunctionalTest {
    private URL descriptor = Test.class.getResource(
            "/functionalTests/masterworker/slavememory/MasterSlaveMemory.xml");
    private Master<MemoryTask, String> master;
    private List<MemoryTask> tasks;
    public static final int NB_TASKS = 3;

    @org.junit.Test
    public void action() throws Exception {
        master.solve(tasks);
        List<String> ids = master.waitAllResults();
        for (int i = 0; i < ids.size(); i++) {
            String mes = ids.get(i);
            assertTrue("Check Correct message", mes.equals("Hello" + i));
        }
    }

    @Before
    public void initTest() throws Exception {
        tasks = new ArrayList<MemoryTask>();
        for (int i = 0; i < NB_TASKS; i++) {
            tasks.add(new MemoryTask());
        }
        HashMap<String, Object> memory = new HashMap<String, Object>();
        memory.put("message", "Hello0");
        master = new ProActiveMaster<MemoryTask, String>(memory);
        master.addResources(descriptor);
        master.setResultReceptionOrder(Master.OrderingMode.SubmitionOrder);
    }

    @After
    public void endTest() throws Exception {
        master.terminate(true);
    }
}
