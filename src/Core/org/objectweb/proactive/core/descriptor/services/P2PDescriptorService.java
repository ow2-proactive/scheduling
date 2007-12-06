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

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.service.P2PService;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.objectweb.proactive.p2p.service.util.P2PConstants;


/**
 * This class represents a service to acquire ProActiveRuntime(JVMs) with the ProActive P2P infrastructure
 * This service can be defined and used transparently when using XML Deployment descriptor
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class P2PDescriptorService implements UniversalService, P2PConstants {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.P2P_DESC_SERV);
    protected static String serviceName = P2P_NODE_NAME;
    protected int askedNodes = 0;
    protected P2PService serviceP2P;
    protected int port;
    private Vector peerList;
    protected String nodeFamilyRegexp = null;
    private String acquistion;

    public P2PDescriptorService() {
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        if (this.serviceP2P == null) {
            // Testing  if a P2P ssrvice is already running ?
            P2PService precedentService = this.getPrecedentService();
            if (precedentService != null) {
                // Keep precedent
                this.serviceP2P = precedentService;
                this.serviceP2P.firstContact(this.peerList);
            } else {
                // Start a new one
                try {
                    JVMProcessImpl process = new JVMProcessImpl(new StandardOutputMessageLogger());
                    process.setClassname(
                        "org.objectweb.proactive.p2p.service.StartP2PService");

                    if (this.acquistion == null) {
                        this.acquistion = ProProperties.PA_COMMUNICATION_PROTOCOL.getValue();
                    }

                    process.setParameters("-port " + this.port + " -acq " +
                        this.acquistion);

                    process.startProcess();
                    Thread.sleep(7000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

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
        String url = URIBuilder.buildURI("localhost", P2P_NODE_NAME,
                ProProperties.PA_P2P_ACQUISITION.getValue(),
                Integer.parseInt(ProProperties.PA_P2P_PORT.getValue()))
                               .toString();

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
                    logger.info("No P2P service is running");
                }
                return null;
            }
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("No P2P service is running");
            }
            return null;
        }
    }

    /**
     * @return Returns the askedNodes.
     */
    public int getNodeNumber() {
        return this.askedNodes;
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
        this.acquistion = acq;
        ProProperties.PA_P2P_ACQUISITION.setValue(acq);
    }

    /**
     * Specify which port the P2P service will listen.
     * @param port the listening port.
     */
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
        ProProperties.PA_P2P_PORT.setValue(port);
    }

    /**
     * Specify NOA parameter for the P2P service.
     * @param noa Number Of Acquaintances.
     */
    public void setNoa(String noa) {
        ProProperties.PA_P2P_NOA.setValue(noa);
    }

    /**
     * Specify TTU parameter for the P2P service.
     * @param ttu Time To Update.
     */
    public void setTtu(String ttu) {
        ProProperties.PA_P2P_TTU.setValue(ttu);
    }

    /**
     * Specify TTL parameter for the P2P service.
     * @param ttl Time To Live.
     */
    public void setTtl(String ttl) {
        ProProperties.PA_P2P_TTL.setValue(ttl);
    }

    /**
     * Sharing 1 node /cpu or only 1 node.
     * @param multi_proc_nodes true or false.
     */
    public void setMultiProcNodes(String multi_proc_nodes) {
        ProProperties.PA_P2P_MULTI_PROC_NODES.setValue(multi_proc_nodes);
    }

    /**
     * For sharing nodes which are deployed from xml.
     * @param xml_path the file path.
     */
    public void setXmlPath(String xml_path) {
        ProProperties.PA_P2P_XML_PATH.setValue(xml_path);
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

    /**
     * Set the regexp for the node family.
     * @param nodeFamilyRegexp the regexp in Java Regexp String format.
     */
    public void setNodeFamilyRegexp(String nodeFamilyRegexp) {
        this.nodeFamilyRegexp = nodeFamilyRegexp;
    }

    /**
     * @return thr node family regexp specified inside the XML descriptor.
     */
    public String getNodeFamilyRegexp() {
        return this.nodeFamilyRegexp;
    }
}
