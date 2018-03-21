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

import org.junit.Before;
import org.junit.Test;


public class RMInitialStateTest {

    private RMInitialState rmInitialState;

    private long counter = 0;

    @Before
    public void init() {
        rmInitialState = new RMInitialState();

        rmInitialState.nodeSourceAdded(new RMNodeSourceEvent("LocalNodes", counter++));
        rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0000", counter++));
        rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0001", counter++));

        rmInitialState.nodeSourceAdded(new RMNodeSourceEvent("AzureNodes", counter++));
        rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0002", counter++));
        rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0003", counter++));
        rmInitialState.nodeAdded(new RMNodeEvent("http://localhost:0004", counter++));

    }

    @Test
    public void testLatestCounter() {
        assertEquals(6, rmInitialState.getLatestCounter());
    }

    @Test
    public void testCloneAndFilter0() {
        final RMInitialState rmInitialState = this.rmInitialState.cloneAndFilter(1);
        assertEquals(4, rmInitialState.getNodesEvents().size());
        assertEquals(1, rmInitialState.getNodeSource().size());
    }

    @Test
    public void testCloneAndFilter1() {
        final RMInitialState rmInitialState = this.rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE);
        assertEquals(5, rmInitialState.getNodesEvents().size());
        assertEquals(2, rmInitialState.getNodeSource().size());
    }

    @Test
    public void testCloneAndFilter2() {
        final RMInitialState rmInitialState = this.rmInitialState.cloneAndFilter(20);
        assertEquals(5, rmInitialState.getNodesEvents().size());
        assertEquals(2, rmInitialState.getNodeSource().size());
    }

    @Test
    public void testRemoveNodeEvent() {
        rmInitialState.nodeRemoved(new RMNodeEvent("http://localhost:0003", 5));
        assertEquals(5, rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE).getNodesEvents().size());
    }

    @Test
    public void testChnageNodeEvent() {
        rmInitialState.nodeStateChanged(new RMNodeEvent("http://localhost:0003", 5));
        assertEquals(5, rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE).getNodesEvents().size());
    }

    @Test
    public void testRemoveNodeSourceEvent() {
        rmInitialState.nodeSourceRemoved(new RMNodeSourceEvent("LocalNodes", counter++));
        assertEquals(2, rmInitialState.cloneAndFilter(RMInitialState.EMPTY_STATE).getNodeSource().size());
    }

}
