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
package functionaltests.masterworker.workermemory;

import static junit.framework.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.masterworker.ConstantMemoryFactory;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;


/**
 * Test load balancing
 */
public class TestWorkerMemory extends SchedulerConsecutive {
    //private URL descriptor = TestWorkerMemory.class
    //        .getResource("/functionalTests/masterworker/workermemory/TestWorkerMemory.xml");
    private Master<MemoryTask2, String> master;
    private List<MemoryTask2> tasks;
    public static final int NB_TASKS = 3;

    @org.junit.Test
    public void run() throws Throwable {

        SchedulerTHelper.init();

        //before
        tasks = new ArrayList<MemoryTask2>();
        for (int i = 0; i < NB_TASKS; i++) {
            tasks.add(new MemoryTask2());
        }
        HashMap<String, Serializable> memory = new HashMap<String, Serializable>();
        memory.put("message", "Hello0");

        master = new ProActiveMaster<MemoryTask2, String>(new ConstantMemoryFactory(memory));
        String[] classpath = new String[] {
                System.getProperty("pa.scheduler.home") + "/classes/schedulerTests",
                System.getProperty("pa.scheduler.home") + "/classes/scheduler" };
        master.addResources("rmi://localhost:" + CentralPAPropertyRepository.PA_RMI_PORT.getValue() + "/",
                "demo", "demo", classpath);

        master.setResultReceptionOrder(Master.SUBMISSION_ORDER);

        //middle
        master.solve(tasks);
        List<String> ids = master.waitAllResults();
        for (int i = 0; i < ids.size(); i++) {
            String mes = ids.get(i);
            assertTrue("Check Correct message", mes.equals("Hello1"));
        }

        //after
        master.terminate(true);
    }

}
