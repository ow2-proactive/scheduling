package org.ow2.proactive.scheduler.util;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.ow2.proactive.resourcemanager.utils.BroadcastDiscoveryClient;
import org.junit.Test;

import static org.junit.Assert.*;


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