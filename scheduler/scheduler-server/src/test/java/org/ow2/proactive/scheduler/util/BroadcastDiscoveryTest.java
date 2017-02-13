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
package org.ow2.proactive.scheduler.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.junit.Test;
import org.ow2.proactive.resourcemanager.utils.BroadcastDiscoveryClient;


public class BroadcastDiscoveryTest {

    @Test
    public void base_case() throws Exception {
        BroadcastDiscovery broadcaster = new BroadcastDiscovery(0, "pnp://localhost:64738");
        broadcaster.start();

        assertEquals("pnp://localhost:64738", new BroadcastDiscoveryClient(broadcaster.getPort()).discover(5000));

        broadcaster.stop();
    }

    @Test(expected = SocketTimeoutException.class)
    public void broadcaster_not_started() throws Exception {
        assertEquals("pnp://localhost:64738", new BroadcastDiscoveryClient(64738).discover(500));
    }

    @Test
    public void start_and_stop() throws Exception {
        BroadcastDiscovery broadcaster = new BroadcastDiscovery(0, "pnp://localhost:64738");

        assertCannotGetBroadcastPort(broadcaster);

        broadcaster.start();
        int port = broadcaster.getPort();
        assertPortIsUsed(port);

        broadcaster.stop();
        assertPortIsFree(port);
        assertCannotGetBroadcastPort(broadcaster);
    }

    @Test(expected = IllegalStateException.class)
    public void stop_when_not_started() throws Exception {
        BroadcastDiscovery broadcaster = new BroadcastDiscovery(0, "pnp://localhost:64738");

        broadcaster.stop();
    }

    @Test(expected = BindException.class)
    public void port_already_used() throws Exception {
        DatagramSocket useAPort = new DatagramSocket(0);
        BroadcastDiscovery broadcaster = new BroadcastDiscovery(useAPort.getLocalPort(), "pnp://localhost:64738");
        broadcaster.start();
    }

    private void assertPortIsFree(int port) {
        try {
            new DatagramSocket(port);
        } catch (IOException expected) {
            fail("Port should be free");
        }
    }

    private void assertPortIsUsed(int port) {
        try {
            new DatagramSocket(port);
            fail("Port should be used by broadcaster");
        } catch (IOException expected) {
        }
    }

    private void assertCannotGetBroadcastPort(BroadcastDiscovery broadcaster) {
        try {
            broadcaster.getPort();
            fail("Port should not be accessible");
        } catch (IllegalStateException expected) {
        }
    }
}
