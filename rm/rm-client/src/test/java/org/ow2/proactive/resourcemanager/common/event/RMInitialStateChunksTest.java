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
package org.ow2.proactive.resourcemanager.common.event;

import static org.junit.Assert.assertEquals;
import static org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties.RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;


public class RMInitialStateChunksTest {

    private RMInitialState rmInitialState;

    private long counter = 0;

    @Before
    public void init() {
        rmInitialState = new RMInitialState();

        RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE.updateProperty("50");

        IntStream.range(0, 4).forEach(nodeSourceId -> {
            counter += 500;
            rmInitialState.nodeSourceAdded(new RMNodeSourceEvent("LocalNodes" + nodeSourceId, counter++));
            IntStream.range(0, 50).forEach(nodeId -> {
                rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0" + nodeSourceId + nodeId, counter++));
            });
        });

    }

    @Test
    public void clientKnowsNothing() {

        final RMInitialState response = this.rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE);
        assertEquals(1, response.getNodeSource().size());
        assertEquals(49, response.getNodesEvents().size());
        assertEquals(549, response.getLatestCounter());

    }

    @Test
    public void clientKnowsSomething() {

        final RMInitialState response = this.rmInitialState.cloneAndFilter(549);
        assertEquals(1, response.getNodeSource().size());
        assertEquals(49, response.getNodesEvents().size());
        assertEquals(1099, response.getLatestCounter());
    }

    @Test
    public void clientKnowsEverything() {
        long latestExisted = counter - 1;
        final RMInitialState response = this.rmInitialState.cloneAndFilter(latestExisted);

        assertEquals(0, response.getNodeSource().size());
        assertEquals(0, response.getNodesEvents().size());
        assertEquals(latestExisted, response.getLatestCounter());
    }

    @Test
    public void clientWentCrazy() {
        long latestExisted = counter - 1;
        final RMInitialState response = this.rmInitialState.cloneAndFilter(latestExisted + 1000);
        assertEquals(1, response.getNodeSource().size());
        assertEquals(49, response.getNodesEvents().size());
        assertEquals(549, response.getLatestCounter());

    }
}
