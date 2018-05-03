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
package functionaltests.nodesource;

import static functionaltests.utils.RMTHelper.log;
import static org.ow2.proactive.utils.Lambda.repeater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.LocalInfrastructure;
import org.ow2.proactive.resourcemanager.nodesource.policy.TimeSlotPolicy;

import functionaltests.nodesource.helper.LocalInfrastructureTestHelper;
import functionaltests.nodesource.helper.TimeSlotPolicyTestHelper;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;


/**
 * Test of the {@link TimeSlotPolicy}. Acquires the nodes of a {@link LocalInfrastructure}
 * immediately, then alternate remove and add every 45 seconds.
 */
public class TestLocalInfrastructureTimeSlotPolicy extends RMFunctionalTest {

    private static final int NODES_NUMBER = 1;

    private static final String NODE_SOURCE_NAME = "LocalInfrastructureTimeSlotPolicyTestNodeSource";

    private ResourceManager resourceManager;

    @Before
    public void setup() throws Exception {
        this.resourceManager = this.rmHelper.getResourceManager();
    }

    @Test
    public void action() throws Exception {

        log("Create an remove an empty local node source with time slot policy");
        createEmptyNodeSource(NODE_SOURCE_NAME);
        removeNodeSource(NODE_SOURCE_NAME);

        log("Create a local node source with " + NODES_NUMBER + " nodes with time slot policy");
        createDefaultNodeSource(NODE_SOURCE_NAME);

        log("Waiting for the time slot policy to remove the nodes");
        repeater.accept(NODES_NUMBER, () -> this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED));

        log("Waiting for the time slot policy to add the nodes again");
        repeater.accept(NODES_NUMBER, () -> this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED));

        log("Waiting for the time slot policy to remove the nodes again");
        repeater.accept(NODES_NUMBER, () -> this.rmHelper.waitForAnyNodeEvent(RMEventType.NODE_REMOVED));
    }

    @After
    public void tearDown() throws Exception {
        removeNodeSource(NODE_SOURCE_NAME);
    }

    private void createEmptyNodeSource(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(nodeSourceName,
                                              LocalInfrastructure.class.getName(),
                                              LocalInfrastructureTestHelper.getParameters(0),
                                              TimeSlotPolicy.class.getName(),
                                              TimeSlotPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);
        this.rmHelper.waitForNodeSourceCreation(nodeSourceName);
    }

    private void createDefaultNodeSource(String nodeSourceName) throws Exception {

        this.resourceManager.defineNodeSource(nodeSourceName,
                                              LocalInfrastructure.class.getName(),
                                              LocalInfrastructureTestHelper.getParameters(NODES_NUMBER),
                                              TimeSlotPolicy.class.getName(),
                                              TimeSlotPolicyTestHelper.getParameters(),
                                              NODES_NOT_RECOVERABLE);
        this.resourceManager.deployNodeSource(nodeSourceName);
        RMTHelper.waitForNodeSourceCreation(nodeSourceName, NODES_NUMBER, this.rmHelper.getMonitorsHandler());
    }

    private void removeNodeSource(String sourceName) throws Exception {
        this.resourceManager.removeNodeSource(sourceName, true);
        this.rmHelper.waitForNodeSourceEvent(RMEventType.NODESOURCE_REMOVED, sourceName);
    }

}
