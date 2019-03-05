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
package org.ow2.proactive.resourcemanager.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.common.NodeState;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;


public class RMStatisticsConcurrencyTest {

    @Test
    public void concurrentTest() throws InterruptedException {

        RMStatistics holder = new RMStatistics();

        long zero = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            holder.nodeEvent(new RMNodeEvent(RMEventType.NODE_ADDED, NodeState.FREE));
        }

        final long start = System.currentTimeMillis();

        Thread busy = new Thread(() -> {
            for (int i = 0; i < 100000; ++i) {
                holder.nodeEvent(new RMNodeEvent(RMEventType.NODE_STATE_CHANGED, NodeState.BUSY, NodeState.FREE));
            }
        });

        Thread free = new Thread(() -> {
            for (int i = 0; i < 100000; ++i) {
                holder.nodeEvent(new RMNodeEvent(RMEventType.NODE_STATE_CHANGED, NodeState.FREE, NodeState.BUSY));
            }
        });

        Thread remover = new Thread(() -> {
            for (int i = 0; i < 100000; ++i) {
                holder.nodeEvent(new RMNodeEvent(RMEventType.NODE_REMOVED, NodeState.FREE));
                assertEquals(1000000 - i - 1, holder.getAvailableNodesCount());
            }
        });

        busy.start();
        free.start();
        remover.start();

        busy.join();
        free.join();
        remover.join();

        long ms = System.currentTimeMillis() - start;
        long total = System.currentTimeMillis() - zero;

        System.out.println("Concurrent time: " + ms + " ms.");
        System.out.println("Total time: " + total + " ms.");

        assertEquals(900000, holder.getAvailableNodesCount());

    }

}
