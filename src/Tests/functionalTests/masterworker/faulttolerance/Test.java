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
package functionalTests.masterworker.faulttolerance;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import functionalTests.FunctionalTest;
import functionalTests.masterworker.A;
import static junit.framework.Assert.assertTrue;


/**
 * Test load balancing
 */
public class Test extends FunctionalTest {
    private URL descriptor = Test.class
            .getResource("/functionalTests/masterworker/faulttolerance/MasterWorkerFT.xml");
    private Master<A, Integer> master;
    private List<A> tasks;
    private ProActiveDescriptor pad;
    private VirtualNode vn1;
    private VirtualNode vn2;
    public static final int NB_TASKS = 4;

    @org.junit.Test
    public void action() throws Exception {
        master.solve(tasks);

        List<Integer> ids = master.waitKResults(1);
        vn1.killAll(false);
        List<Integer> ids2 = master.waitAllResults();
        ids.addAll(ids2);
        assertTrue("Only one worker left", master.workerpoolSize() == 1);

        Iterator<Integer> it = ids.iterator();
        int last = it.next();
        while (it.hasNext()) {
            int next = it.next();
            assertTrue("Results recieved in submission order", last < next);
            last = next;
        }
    }

    @Before
    public void initTest() throws Exception {
        tasks = new ArrayList<A>();
        for (int i = 0; i < NB_TASKS; i++) {
            A t = new A(i, (NB_TASKS - i) * 2000, false);
            tasks.add(t);
        }

        this.pad = PADeployment.getProactiveDescriptor(descriptor.getPath());
        this.pad.activateMappings();
        this.vn1 = this.pad.getVirtualNode("VN1");

        this.vn2 = this.pad.getVirtualNode("VN2");

        master = new ProActiveMaster<A, Integer>();
        master.addResources(this.vn1);
        master.addResources(this.vn2);
        master.setResultReceptionOrder(Master.OrderingMode.SubmitionOrder);
        master.setPingPeriod(100);
    }

    @After
    public void endTest() throws Exception {
        master.terminate(false);
        vn1.killAll(false);
    }
}
