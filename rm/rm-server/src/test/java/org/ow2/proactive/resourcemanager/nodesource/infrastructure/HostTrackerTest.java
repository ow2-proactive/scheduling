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
package org.ow2.proactive.resourcemanager.nodesource.infrastructure;

import static com.google.common.truth.Truth.assertThat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;


public class HostTrackerTest {

    private static final int CONFIGURED_NODE_NUMBER = 4;

    private static final String HOSTNAME = "localhost";

    private static final String NODE_URL_BASE = "pnp://" + HOSTNAME;

    private HostTracker hostTracker;

    @Before
    public void setup() {
        try {
            hostTracker = new HostTracker(InetAddress.getByName(HOSTNAME), CONFIGURED_NODE_NUMBER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHostTracker() {
        hostTracker.getNeededNodeNumber();
        hostTracker.hasAliveNodes();
        /*
         * hostTracker.managesNodeUrl();
         * hostTracker.putAliveNodeUrl();
         * hostTracker.putDownNodeUrl();
         * hostTracker.putRemovedNodeUrl();
         */
    }

    @Test
    public void testNewHostTrackerNeedsNodes() {
        assertThat(hostTracker.needsNodes()).isTrue();
    }

    @Test
    public void testHostTrackerWithConfiguredNumberOfNodesAliveDoesNotNeedNodes() {
        hostTracker.setNeedsNodes(false);
        assertThat(hostTracker.needsNodes()).isFalse();
    }

    @Test
    public void testAllNodesAliveMakeNeededNodeNumberEqualToZero() {
        putAllNodesAlive();
        assertThat(hostTracker.getNeededNodeNumber()).isEqualTo(0);
    }

    @Test
    public void testRemovedNodesAreNotCountedInNeededNodeNumber() {
        putAllNodesAlive();
        hostTracker.putRemovedNodeUrl(NODE_URL_BASE + "1");
        hostTracker.putRemovedNodeUrl(NODE_URL_BASE + "2");
        assertThat(hostTracker.getNeededNodeNumber()).isEqualTo(0);
    }

    @Test
    public void testDownNodesAreCountedInNeededNodeNumber() {
        putAllNodesAlive();
        hostTracker.putDownNodeUrl(NODE_URL_BASE + "1");
        hostTracker.putDownNodeUrl(NODE_URL_BASE + "3");
        assertThat(hostTracker.getNeededNodeNumber()).isEqualTo(2);
    }

    private void putAllNodesAlive() {
        for (int i = 0; i < CONFIGURED_NODE_NUMBER; i++) {
            hostTracker.putAliveNodeUrl(NODE_URL_BASE + i);
        }
    }

}
