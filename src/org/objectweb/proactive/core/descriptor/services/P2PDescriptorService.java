/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor.services;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;

import java.util.Vector;


/**
 * This class represents a service to acquire ProActiveRuntime(JVMs) with the ProActive P2P infrastructure
 * This service can be defined and used transparently when using XML Deployment descriptor
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class P2PDescriptorService implements UniversalService, P2PConstants {
    private static final Logger logger = Logger.getLogger(Loggers.P2P_DESC_SERV);
    protected static String serviceName = P2P_NODE_NAME;
    protected int askedNodes = 0;
    protected P2PService serviceP2P;
    private Vector peerList;

    public P2PDescriptorService() {
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        if (serviceP2P == null) {
            // Testing  if a P2P ssrvice is already running ?
            P2PService precedentService = this.getPrecedentService();
            if (precedentService != null) {
                // Keep precedent
                this.serviceP2P = precedentService;
                this.serviceP2P.firstContact(this.peerList);
            } else {
                // Start a new one
                StartP2PService startServiceP2P = new StartP2PService(this.peerList);
                startServiceP2P.start();
                this.serviceP2P = startServiceP2P.getP2PService();
            }
        }

        //return this.serviceP2P.getNodes(askedNodes);
        return null;
    }

    /**
     * @return a precedent running P2P servie or <code>null</code>
     */
    private P2PService getPrecedentService() {
        String url = System.getProperty(PROPERTY_ACQUISITION) +
            "://localhost:" + System.getProperty(PROPERTY_PORT) + "/" +
            P2P_NODE_NAME;
        try {
            Node serviceNode = NodeFactory.getNode(url);
            Object[] ao = serviceNode.getActiveObjects(P2PService.class.getName());
            if (ao.length == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("A precedent P2P service is running");
                }
                return (P2PService) ao[0];
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("No P2P service is runned");
                }
                return null;
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("No P2P service is runned");
            }
            return null;
        }
    }

    /**
     * @return Returns the askedNodes.
     */
    public int getNodeNumber() {
        return askedNodes;
    }

    /**
     * Sets the number of nodes to be acquired with this P2P service
     * @param nodeNumber The askedNodes to set.
     */
    public void setNodeNumber(int nodeNumber) {
        this.askedNodes = nodeNumber;
    }

    /**
     * Sets the number of nodes to be acquired to 10000(Max Value)
     * This method is usefull to acquire an undefined number of nodes. Indeed the number
     * of nodes expected is never reached, the operation will be done again after lookupFrequence ms
     * and if the timout has not expired
     */
    public void setNodeNumberToMAX() {
        this.askedNodes = MAX_NODE;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Specify P2P service acquisition method, as such rmi, http, etc.
     * @param acq the acquisition method.
     */
    public void setAcq(String acq) {
        System.setProperty(PROPERTY_ACQUISITION, acq);
    }

    /**
     * Specify which port the P2P service will listen.
     * @param port the listening port.
     */
    public void setPort(String port) {
        System.setProperty(PROPERTY_PORT, port);
    }

    /**
     * Specify NOA parameter for the P2P service.
     * @param noa Number Of Acquaintances.
     */
    public void setNoa(String noa) {
        System.setProperty(PROPERTY_NOA, noa);
    }

    /**
     * Specify TTU parameter for the P2P service.
     * @param ttu Time To Update.
     */
    public void setTtu(String ttu) {
        System.setProperty(PROPERTY_TTU, ttu);
    }

    /**
     * Specify TTL parameter for the P2P service.
     * @param ttl Time To Live.
     */
    public void setTtl(String ttl) {
        System.setProperty(PROPERTY_TTL, ttl);
    }

    /**
     * Sharing 1 node /cpu or only 1 node.
     * @param multi_proc_nodes true or flase.
     */
    public void setMultiProcNodes(String multi_proc_nodes) {
        System.setProperty(PROPERTY_MULTI_PROC_NODES, multi_proc_nodes);
    }

    /**
     * For sharing nodes which are deployed from xml.
     * @param xml_path the file path.
     */
    public void setXmlPath(String xml_path) {
        System.setProperty(PROPERPY_XML_PATH, xml_path);
    }
	
    /**
     * @param peerList
     */
    public void setPeerList(String[] peerList) {
        this.peerList = new Vector();
        for (int i = 0; i < peerList.length; i++) {
            this.peerList.add(peerList[i]);
        }
    }

    /**
     * @return <code>MAX_NODE</code> P2P constant.
     */
    public int getMAX() {
        return MAX_NODE;
    }

    /**
     * @return the associated P2P service.
     */
    public P2PService getP2PService() {
        return this.serviceP2P;
    }

}
