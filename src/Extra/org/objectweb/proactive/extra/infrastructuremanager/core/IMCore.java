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
package org.objectweb.proactive.extra.infrastructuremanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.database.IMDataResourceImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdminImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoringImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUserImpl;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.NodeSet;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSourceManager;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.DynamicNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PADNSInterface;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad.PADNodeSource;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;


public class IMCore implements InitActive, IMConstants, Serializable {

    /**  */
    private static final long serialVersionUID = -6005871512766524208L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);

    // Attributes
    private Node nodeIM;
    private IMAdmin admin;
    private IMMonitoring monitoring;
    private IMUser user;
    private IMDataResource dataresource;
    private PADNodeSource padNS;
    private IMNodeSourceManager nodeManager;

    // test mkris
    IMActivityNode act;

    // ----------------------------------------------------------------------//
    // CONSTRUCTORS

    /** ProActive compulsory no-args constructor */
    public IMCore() {
    }

    public IMCore(Node nodeIM)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore constructor");
        }
        this.nodeIM = nodeIM;
    }

    // ----------------------------------------------------------------------//
    // INIT ACTIVE FRONT-END

    /**
     * Initialize the actif object : IMAdmin, IMMonitoring, IMUser
     * @param body
     */
    public void initActivity(Body body) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore start : initActivity");
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("active object IMAdmin");
            }
            admin = (IMAdminImpl) ProActiveObject.newActive(IMAdminImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object IMMonitoring");
            }
            monitoring = (IMMonitoringImpl) ProActiveObject.newActive(IMMonitoringImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("active object IMUser");
            }
            user = (IMUserImpl) ProActiveObject.newActive(IMUserImpl.class.getName(),
                    new Object[] { ProActiveObject.getStubOnThis() }, nodeIM);

            if (logger.isDebugEnabled()) {
                logger.debug("instanciation IMDataResourceImpl");
            }
            this.nodeManager = new IMNodeSourceManager("NSManager", nodeIM);
            this.dataresource = new IMDataResourceImpl(nodeManager);

            padNS = nodeManager.getPADNodeSource();

            act = new IMActivityNode((IMCore) (ProActiveObject.getStubOnThis()));
            new Thread(act).start();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore end : initActivity");
        }
    }

    // ----------------------------------------------------------------------//
    // TEST
    public String echo() {
        return "Je suis le IMCore";
    }

    // ----------------------------------------------------------------------//
    // ACCESSORS
    public Node getNodeIM() {
        return this.nodeIM;
    }

    public IMAdmin getAdmin() {
        return this.admin;
    }

    public IMMonitoring getMonitoring() {
        return this.monitoring;
    }

    public IMUser getUser() {
        return this.user;
    }

    // ----------------------------------------------------------------------//
    // ADMIN

    /**
     * Add the new deployed node in the dataresource
     * @param node    : the new deployed node
     * @param vnName  : the name of the virtual node
     * @param padName : the name of the proactive descriptor
     */
    public void addNode(Node node, String vnName, String padName) {
        if (logger.isDebugEnabled()) {
            logger.debug("IMCore - addNode : node=" +
                node.getNodeInformation().getName() + "\t\t vnName=" + vnName +
                "\t\t padName=" + padName);
        }
        padNS.addNode(node, vnName, padName);
    }

    /**
     * Add the new proactive descriptor in the dataresource
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     */
    public void addPAD(String padName, ProActiveDescriptor pad) {
        padNS.addPAD(padName, pad);
    }

    // ----------------------------------------------------------------------//	
    // REDEPLOY
    // FIXME The redeploy (kill+activate) isn't support in the actualy 
    // version of ProActive

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName) {
        padNS.redeploy(padName);
    }

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnName  : the name of the virtual node of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String vnName) {
        padNS.redeploy(padName, vnName);
    }

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnNames : the name of the virtual nodes of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String[] vnNames) {
        padNS.redeploy(padName, vnNames);
    }

    // ----------------------------------------------------------------------//	
    // KILL

    /**
     * Kill all virtual nodes of them proactive descriptors
     * TODO delete the pad file
     * @param padName :  the name of the proactive descriptor
     * @exception ProActiveException
     */
    public void killPAD(String padName) throws ProActiveException {
        padNS.killPAD(padName);
    }

    /**
     * Kill the virtual nodes of the proactive descriptors <I>padName>/I>
     * @param padName : the name of the Proactive Descriptor
     * @exception ProActiveException
     */
    public void killPAD(String padName, String vnName) {
        padNS.killPAD(padName, vnName);
    }

    /**
     * Kill the virtual node <I>vnName</I> of the proactive descriptor <I>padName</I>
     * @param padName : the name of the Proactive Descriptor
     * @param vnName  : the name of the virtual node for killing
     * @see  killPAD(String padName)
     * @exception ProActiveException
     */
    public void killPAD(String padName, String[] vnNames) {
        padNS.killPAD(padName, vnNames);
    }

    /**
     * Kill the virtual nodes <I>vnNames</I>
     * of the proactive descriptor <I>padName</I>
     * @param padName : the name of the Proactive Descriptor
     * @param vnNames : the name of the virtual nodes for killing
     * @see  killPAD(String padName)
     * @exception ProActiveException
     */
    public void killAll() throws ProActiveException {
        padNS.killAll();
    }

    // ----------------------------------------------------------------------//
    // MONITORING
    public IntWrapper getSizeListFreeIMNode() {
        return nodeManager.getNbFreeNodes();
    }

    public IntWrapper getSizeListBusyIMNode() {
        return nodeManager.getNbBusyNodes();
    }

    public IntWrapper getSizeListDownIMNode() {
        return nodeManager.getNbDownNodes();
    }

    public IntWrapper getNbAllIMNode() {
        return nodeManager.getNbAllNodes();
    }

    public IntWrapper getSizeListPad() {
        return padNS.getSizeListPad();
    }

    public HashMap<String, ProActiveDescriptor> getListPAD() {
        return padNS.getListPAD();
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        return padNS.getDeployedVirtualNodeByPad();
    }

    public ArrayList<IMNode> getListFreeIMNode() {
        return nodeManager.getFreeNodes();
    }

    public ArrayList<IMNode> getListBusyIMNode() {
        return nodeManager.getBusyNodes();
    }

    public ArrayList<IMNode> getListAllNodes() {
        return nodeManager.getAllNodes();
    }

    // ----------------------------------------------------------------------//
    // USER

    /**
     * Reserves nb nodes, if the infrastructure manager (IM) don't have nb free nodes
     * then it returns the max of free nodes
     * @param nb the number of nodes
     * @return an arraylist of nodes
     * @throws NodeException
     */
    public NodeSet getAtMostNodes(IntWrapper nb, VerifyingScript verifyingScript) {
        return this.dataresource.getAtMostNodes(nb, verifyingScript);
    }

    public NodeSet getExactlyNodes(IntWrapper nb,
        VerifyingScript verifyingScript) {
        return this.dataresource.getExactlyNodes(nb, verifyingScript);
    }

    /**
     * Release the node reserve by the user
     * @param node : the node to release
     * @throws NodeException
     */
    public void freeNode(Node node) {
        this.dataresource.freeNode(node);
    }

    /**
     * Release the nodes reserve by the user
     * @param nodes : a table of nodes to release
     * @throws NodeException
     */
    public void freeNodes(NodeSet nodes) {
        this.dataresource.freeNodes(nodes);
    }

    public void nodeIsDown(IMNode imNode) {
        //this.dataresource.nodeIsDown(imNode);
        this.dataresource.nodeIsDown(imNode);
    }

    // ----------------------------------------------------------------------//
    // SHUTDOWN

    /**
     * Kill all the proactive descriptor and quit the application
     * @see killall()
     * @exception ProActiveException
     */
    public void shutdown() throws ProActiveException {
        BooleanWrapper bool = nodeManager.shutdown();
        try {
            if (bool.booleanValue()) {
                logger.info("Infrastructure Manager successfully shut down.");
            } else {
                logger.warn("Infrastructure Manager shut down with errors.");
            }
        } catch (Exception e) {
            logger.error("Error during IM Shut down : ", e);
        }
        ProActive.exitSuccess();
    }

    public ArrayList<DynamicNSInterface> getDynamicNodeSources() {
        ArrayList<DynamicNSInterface> dns = new ArrayList<DynamicNSInterface>();
        dns.addAll(nodeManager.getDynamicNodeSources());
        return dns;
    }

    public PADNSInterface getPADNodeSource() {
        return padNS;
    }

    public void removeDynamicNodeSources(DynamicNodeSource dns) {
        nodeManager.removeDynamicNodeSource(dns);
    }

    public void addDynamicNodeSources(DynamicNodeSource dns) {
        ArrayList<DynamicNodeSource> dynNS = nodeManager.getDynamicNodeSources();
        if (!dynNS.contains(dns)) {
            nodeManager.addDynamicNodeSource(dns);
        }
    }

    // ----------------------------------------------------------------------//
}
