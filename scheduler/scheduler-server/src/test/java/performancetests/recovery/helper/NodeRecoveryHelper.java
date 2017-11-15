/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package performancetests.recovery.helper;

import functionaltests.recover.TaskReconnectionToRecoveredNodeTest;
import functionaltests.recover.TaskReconnectionWithForkedTaskExecutorTest;
import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;

<<<<<<< f2f1e4ccdb1ba87d54bdad4f24b647fb37396949
=======
import functionaltests.recover.TaskReconnectionToRecoveredNodeTest;
import functionaltests.recover.TaskReconnectionWithForkedTaskExecutorTest;
>>>>>>> Use Java request instead of JUnit request
import functionaltests.utils.SchedulerTHelper;
import functionaltests.utils.TestNode;
import functionaltests.utils.TestScheduler;
import performancetests.recovery.NodeRecoveryTest;


public class NodeRecoveryHelper {

    private static final URL SCHEDULER_CONFIGURATION_START = NodeRecoveryTest.class.getResource("/functionaltests/config/functionalTSchedulerProperties.ini");

    private static final URL RM_CONFIGURATION_START = NodeRecoveryHelper.class.getResource("/functionaltests/config/functionalTRMProperties-clean-db.ini");

    private SchedulerTHelper schedulerHelper;

    private List<TestNode> nodes;

    public void startKillStartScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);

        final JMeterVariables vars = new JUnitSampler().getThreadContext().getVariables();
        Integer nodesNumber = Integer.valueOf(vars.get("nodesNumber"));

        // start nodes
        ResourceManager rm = schedulerHelper.getResourceManager();

        nodes = schedulerHelper.createRMNodeStarterNodes(TaskReconnectionWithForkedTaskExecutorTest.class.getSimpleName(),
                                                         nodesNumber);

        //schedulerHelper.getJobServerLogs()
        // kill server
        TestScheduler.kill();

        schedulerHelper = new SchedulerTHelper(false,
                                               new File(SCHEDULER_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               new File(RM_CONFIGURATION_START.toURI()).getAbsolutePath(),
                                               null);
    }

    public int timeSpentToRecoverNodes() {

        return 9000;
    }

    public void shutdown() throws Exception {
        if (nodes != null) {
            for (TestNode node : nodes) {
                try {
                    node.kill();
                } catch (Exception e) {
                    // keep exceptions there silent
                }
            }
        }
        TestScheduler.kill();

    }

}
