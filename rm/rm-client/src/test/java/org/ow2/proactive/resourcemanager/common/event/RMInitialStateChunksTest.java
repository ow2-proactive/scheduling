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

        IntStream.range(0, 10).forEach(nodeSourceId -> {
            rmInitialState.nodeSourceAdded(new RMNodeSourceEvent("LocalNodes" + nodeSourceId, counter++));
            IntStream.range(0, 100).forEach(nodeId -> {
                rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0" + nodeSourceId + nodeId, counter++));
            });
        });

    }

    @Test
    public void testProperty() {
        RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE.updateProperty("50");

        final RMInitialState cloned = this.rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE);
        assertEquals(RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE.getValueAsInt() - 1, cloned.getLatestCounter());

        RMInitialState cloned2 = this.rmInitialState.cloneAndFilter(cloned.getLatestCounter());
        assertEquals(2 * RM_REST_MONITORING_MAXIMUM_CHUNK_SIZE.getValueAsInt() - 1, cloned2.getLatestCounter());
    }

}
