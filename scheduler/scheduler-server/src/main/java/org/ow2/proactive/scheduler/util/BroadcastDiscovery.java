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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.utils.BroadcastDiscoveryClient;


/**
 * Discovery service for nodes using UDP broadcast.
 *
 * It expects empty packet from {@link BroadcastDiscoveryClient }
 */
public class BroadcastDiscovery {

    private static Logger logger = Logger.getLogger(BroadcastDiscovery.class);

    private String url;

    private int port;

    private Thread discoveryThread;

    private DatagramSocket broadcastSocket;

    private boolean needsToStop;

    public BroadcastDiscovery(int port, String url) {
        this.port = port;
        this.url = url;
    }

    /**
     *  Starts a new thread to reply to broadcast discovery requests from nodes.
     *
     * @throws SocketException if cannot bind UDP socket
     * @throws UnknownHostException if cannot bind to 0.0.0.0
     */
    public void start() throws SocketException, UnknownHostException {
        broadcastSocket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        broadcastSocket.setBroadcast(true);

        discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        DatagramPacket nodePacket = new DatagramPacket(new byte[0], 0);
                        broadcastSocket.receive(nodePacket);

                        byte[] urlAsBytes = url.getBytes(BroadcastDiscoveryClient.BROADCAST_ENCODING);
                        DatagramPacket replyPacket = new DatagramPacket(urlAsBytes,
                                                                        urlAsBytes.length,
                                                                        nodePacket.getAddress(),
                                                                        nodePacket.getPort());
                        broadcastSocket.send(replyPacket);
                    } catch (SocketException e) {
                        if (needsToStop) {
                            return;
                        } else {
                            logger.warn("Could not broadcast URL to node", e);
                        }
                    } catch (Exception e) {
                        logger.warn("Could not broadcast URL to node", e);
                    }
                }
            }
        }, "BroadcastDiscovery");
        discoveryThread.start();
    }

    /**
     * Stops the discovery thread and close the broadcast socket.
     * @throws IllegalStateException if stopped when not yet started
     */
    public void stop() {
        if (broadcastSocket == null || discoveryThread == null) {
            throw new IllegalStateException("Broadcast service discovery not started, cannot be stopped");
        }

        needsToStop = true;
        broadcastSocket.close();
        try {
            discoveryThread.join(1000);
        } catch (InterruptedException e) {
            logger.warn("Could not stop properly broadcast discovery thread", e);
        }
    }

    public int getPort() {
        if (broadcastSocket == null || broadcastSocket.isClosed()) {
            throw new IllegalStateException("Broadcast discovery not yet started or stopped, port cannot be retrieved.");
        }
        return broadcastSocket.getLocalPort();
    }
}
