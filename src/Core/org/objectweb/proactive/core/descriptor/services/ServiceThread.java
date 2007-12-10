/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor.services;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NodeNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class ServiceThread extends Thread {
    private static final long LOOK_UP_FREQ = new Long(PAProperties.PA_P2P_LOOKUP_FREQ.getValue()).longValue();
    private static final int MAX_NODE = P2PConstants.MAX_NODE;
    private VirtualNodeInternal vn;
    private UniversalService service;
    private VirtualMachine vm;
    private ProActiveRuntime localRuntime;
    int nodeCount = 0;
    long timeout = 0;
    int nodeRequested;
    public static Logger loggerDeployment = ProActiveLogger.getLogger(Loggers.DEPLOYMENT);
    private static final long TIMEOUT = Long.parseLong(PAProperties.PA_P2P_NODES_ACQUISITION_T0.getValue());
    private long expirationTime;

    public ServiceThread(VirtualNodeInternal vn, VirtualMachine vm) {
        this.vn = vn;
        this.service = vm.getService();
        this.vm = vm;
        this.localRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Override
    public void run() {
        ProActiveRuntime[] part = null;

        try {
            part = service.startService();
            nodeCount = nodeCount + ((part != null) ? part.length : 0);
            if (part != null) {
                notifyVirtualNode(part);
            }

            if (service.getServiceName().equals(P2PConstants.P2P_NODE_NAME)) {
                // Start asking nodes
                P2PService p2pService = ((P2PDescriptorService) service).getP2PService();
                String nodeFamilyRegexp = ((P2PDescriptorService) service).getNodeFamilyRegexp();
                nodeFamilyRegexp = (nodeFamilyRegexp != null)
                    ? nodeFamilyRegexp : "";
                P2PNodeLookup p2pNodesLookup = p2pService.getNodes(((P2PDescriptorService) service).getNodeNumber(),
                        nodeFamilyRegexp, this.vn.getName(), this.vn.getJobID());
                ((VirtualNodeImpl) vn).addP2PNodesLookup(p2pNodesLookup);
                this.nodeRequested = ((P2PDescriptorService) service).getNodeNumber();
                // Timeout
                long vnTimeout = vn.getTimeout();
                this.expirationTime = System.currentTimeMillis() + TIMEOUT;
                if (this.nodeRequested == MAX_NODE) {
                    this.expirationTime = Long.MAX_VALUE;
                    ((VirtualNodeImpl) vn).setTimeout(this.expirationTime, false);
                } else if (vnTimeout < TIMEOUT) {
                    ((VirtualNodeImpl) vn).setTimeout(TIMEOUT, false);
                }

                long step = 100;
                while (askForNodes() &&
                        ((nodeRequested == MAX_NODE) ? true
                                                         : (System.currentTimeMillis() < this.expirationTime))) {
                    if (step > LOOK_UP_FREQ) {
                        step = LOOK_UP_FREQ;
                    }

                    Vector nodes;
                    try {
                        Vector future = p2pNodesLookup.getAndRemoveNodes();
                        nodes = (Vector) PAFuture.getFutureValue(future);
                    } catch (Exception e) {
                        loggerDeployment.debug("Couldn't contact the lookup", e);
                        continue;
                    }
                    for (int i = 0; i < nodes.size(); i++) {
                        Node node = (Node) nodes.get(i);
                        nodeCount++;

                        // ProActiveEvent
                        ((VirtualNodeImpl) vn).nodeCreated(new NodeNotificationData(
                                node, vn.getName()), true);
                        // END ProActiveEvent
                        if (loggerDeployment.isInfoEnabled()) {
                            loggerDeployment.info(
                                "Service thread just created event for node: " +
                                node.getNodeInformation().getURL());
                        }
                    }

                    // Sleeping with FastStart algo
                    if ((this.nodeRequested == MAX_NODE) &&
                            (nodes.size() == 0) && (this.nodeCount != 0)) {
                        Thread.sleep(LOOK_UP_FREQ);
                    } else if (askForNodes() && (this.nodeCount == 0)) {
                        // still no node
                        Thread.sleep(step);
                        step += 100;
                    } else {
                        // normal waiting
                        if (step > LOOK_UP_FREQ) {
                            step = LOOK_UP_FREQ;
                            Thread.sleep(LOOK_UP_FREQ);
                        } else {
                            step += 100;
                            Thread.sleep(step);
                        }
                    }
                }
            }
        } catch (ProActiveException e) {
            loggerDeployment.error(
                "An exception occured while starting the service " +
                service.getServiceName() + " for the VirtualNode " +
                vn.getName() + " \n" + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyVirtualNode(ProActiveRuntime[] part) {
        for (int i = 0; i < part.length; i++) {
            String url = part[i].getURL();
            String protocol = URIBuilder.getProtocol(url);

            // JMX Notification
            ProActiveRuntimeWrapperMBean mbean = ProActiveRuntimeImpl.getProActiveRuntime()
                                                                     .getMBean();
            if (mbean != null) {
                RuntimeNotificationData notificationData = new RuntimeNotificationData(vn.getName(),
                        url, protocol, vm.getName());
                mbean.sendNotification(NotificationType.runtimeAcquired,
                    notificationData);
            }

            // END JMX Notification
        }
    }

    /**
     * Method used to know if we must ask other nodes
     * @return true if there are still nodes expected
     */
    private boolean askForNodes() {
        if (nodeRequested == MAX_NODE) {
            // nodeRequested = -1 means try to get the max number of nodes
            return true;
        }
        return nodeCount < nodeRequested;
    }
}
