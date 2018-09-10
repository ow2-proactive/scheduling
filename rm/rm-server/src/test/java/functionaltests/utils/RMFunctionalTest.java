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
package functionaltests.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.exception.NotConnectedException;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.tests.ProActiveTest;

import functionaltests.monitor.RMMonitorEventReceiver;


public class RMFunctionalTest extends ProActiveTest {

    public static final boolean NODES_NOT_RECOVERABLE = false;

    static {
        configureLogging();
        ProActiveConfiguration.load();
    }

    protected static final Logger logger = Logger.getLogger("RMTests");

    @Rule
    public Timeout testTimeout = new Timeout(CentralPAPropertyRepository.PA_TEST_TIMEOUT.getValue(),
                                             TimeUnit.MILLISECONDS);

    protected RMTHelper rmHelper;

    // For tests which use a single test node (separate JVM process)
    protected TestNode testNode;

    // For tests which use multiple test nodes (separate JVM processes)
    protected List<TestNode> testNodes = new ArrayList<>();

    @Before
    public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_TEST.setValue(true);
        CentralPAPropertyRepository.PA_RUNTIME_PING.setValue(false);

        startIAMIfNeeded();

        rmHelper = new RMTHelper();
        try {
            cleanState();
        } catch (IllegalArgumentException | NotConnectedException ignored) {
            // ns extra not found
        }
    }

    /**
     * Kill all standalone nodes created by the test
     */
    private void killTestNodes() {
        try {
            if (testNode != null) {
                testNode.kill();
            }
        } catch (Exception e) {
        }
        for (TestNode tn : testNodes) {
            try {
                tn.kill();
            } catch (Exception e) {
            }
        }
    }

    @After
    public void cleanForNextTest() throws Exception {
        killTestNodes();

        try {
            cleanState();
        } catch (IllegalArgumentException | NotConnectedException ignored) {
            // ns extra not found
        }
        try {
            rmHelper.disconnect();
        } catch (NotConnectedException alreadyDisconnected) {

        }
    }

    private static void configureLogging() {
        if (System.getProperty(CentralPAPropertyRepository.LOG4J.getName()) == null) {
            URL defaultLog4jConfig = RMFunctionalTest.class.getResource("/log4j-junit");
            System.setProperty(CentralPAPropertyRepository.LOG4J.getName(), defaultLog4jConfig.toString());
            PropertyConfigurator.configure(defaultLog4jConfig);
        }
    }

    /**
     * Remove all node sources and nodes in the RM
     *
     * @throws Exception
     */
    private void cleanState() throws Exception {
        if (rmHelper.isRMStarted()) {
            rmHelper.disconnect(); // force reconnection
            ResourceManager rm = rmHelper.getResourceManager();
            int nodeNumber = rm.getState().getTotalNodesNumber();

            RMInitialState state = ((RMMonitorEventReceiver) rmHelper.getResourceManager()).getInitialState();
            for (RMNodeSourceEvent sourceEvent : state.getNodeSourceEvents()) {
                String nodeSource = sourceEvent.getSourceName();
                rm.removeNodeSource(nodeSource, true);
                rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, nodeSource);
            }

            for (int i = 0; i < nodeNumber; i++) {
                rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED);
            }
        }
    }

}
