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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.log4j.Logger;


public class BroadcastDiscoveryClient {
    public static final String BROADCAST_ENCODING = "UTF-8";

    private static final Logger logger = Logger.getLogger(BroadcastDiscoveryClient.class);

    private static final int URL_MAX_LENGTH = 1024;

    private int port;

    public BroadcastDiscoveryClient(int port) {
        this.port = port;
    }

    public String discover(int timeoutInMs) throws IOException {
        DatagramSocket broadcastSocket = null;
        try {
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);

            broadcastToDefaultInterface(broadcastSocket);
            broadcastToAllInterfaces(broadcastSocket);

            return readBroadcastReply(broadcastSocket, timeoutInMs);
        } catch (IOException e) {
            logger.warn("Could not retrieve URL using broadcast discovery", e);
            throw e;
        } finally {
            if (broadcastSocket != null) {
                broadcastSocket.close();
            }
        }
    }

    private String readBroadcastReply(DatagramSocket broadcastSocket, int timeoutInMs) throws IOException {
        byte[] replyBuffer = new byte[URL_MAX_LENGTH];
        DatagramPacket replyPacket = new DatagramPacket(replyBuffer, replyBuffer.length);
        broadcastSocket.setSoTimeout(timeoutInMs);
        broadcastSocket.receive(replyPacket);

        if (replyPacket.getLength() > 0) {
            return new String(replyPacket.getData(), 0, replyPacket.getLength(), BROADCAST_ENCODING);
        } else {
            throw new IllegalStateException("Could not retrieve URL using broadcast discovery, received a malformed packet from " +
                                            replyPacket.getAddress());
        }
    }

    private void broadcastToDefaultInterface(DatagramSocket broadcastSocket) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(new byte[0],
                                                           0,
                                                           InetAddress.getByName("255.255.255.255"),
                                                           port);
            broadcastSocket.send(sendPacket);
        } catch (Exception e) {
            logger.warn("Could not broadcast to default interface", e);
        }
    }

    private void broadcastToAllInterfaces(DatagramSocket c) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (isLoopBack(networkInterface)) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    try {
                        DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, broadcast, port);
                        c.send(sendPacket);
                    } catch (Exception e) {
                        logger.warn("Could not broadcast to interface " + networkInterface.getName(), e);
                    }

                }
            }
        } catch (SocketException e) {
            logger.warn("Could not broadcast to all interfaces", e);
        }
    }

    private boolean isLoopBack(NetworkInterface networkInterface) {
        try {
            return networkInterface.isLoopback() || !networkInterface.isUp();
        } catch (SocketException e) {
            return false;
        }
    }
}
