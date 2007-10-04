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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeComparator;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeImpl;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.IMNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.PADNSInterface;
import org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;


public class PADNodeSource extends IMNodeSource implements Serializable,
    InitActive, PADNSInterface {
    private static final long serialVersionUID = 9195674290785820181L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.IM_CORE);
    private String id = "PADNodeSource";

    // FIELDS
    /** Free Nodes **/
    private ArrayList<IMNode> freeNodes;

    /** Busy Nodes **/
    private ArrayList<IMNode> busyNodes;

    /** Down Nodes **/
    private ArrayList<IMNode> downNodes;

    /** PADs **/
    private HashMap<String, ProActiveDescriptor> listPad;

    public PADNodeSource() {
    }

    public PADNodeSource(String id) {
        this.id = id;
    }

    public void initActivity(Body body) {
        freeNodes = new ArrayList<IMNode>();
        busyNodes = new ArrayList<IMNode>();
        downNodes = new ArrayList<IMNode>();
        listPad = new HashMap<String, ProActiveDescriptor>();
    }

    /**
     * Return the nodes in a specific order :
     * - if there is no script to verify, just return the free nodes ;
     * - if there is a script, tries to give the nodes in an efficient order :
     *                 -> First the nodes that verified the script before ;
     *                 -> Next, the nodes that haven't been tested ;
     *                 -> Next, the nodes that have allready verified the script, but no longer ;
     *                 -> To finish, the nodes that don't verify the script.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#getNodesByScript(org.objectweb.proactive.extra.scheduler.common.scripting.VerifyingScript)
     */
    public ArrayList<IMNode> getNodesByScript(VerifyingScript script,
        boolean ordered) {
        ArrayList<IMNode> result = getFreeNodes();
        if ((script != null) && ordered) {
            Collections.sort(result, new IMNodeComparator(script));
        }
        return result;
    }

    /**
     * remove a node from the structure.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#removeIMNode(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    public void removeIMNode(IMNode imnode) {
        removeFromAllLists(imnode);
    }

    /**
     * remove many nodes from the structure.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#removeIMNodes(java.util.Collection)
     */
    public void removeIMNodes(Collection<IMNode> imnodes) {
        for (IMNode imnode : imnodes)
            removeIMNode(imnode);
    }

    /**
     * Set the busy state, and move the node to the internal busy list.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#setBusy(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    public void setBusy(IMNode imnode) {
        removeFromAllLists(imnode);
        busyNodes.add(imnode);
        try {
            imnode.setBusy();
        } catch (NodeException e1) {
            // A down node shouldn't by busied...
            e1.printStackTrace();
        }
    }

    /**
     * Set the down state, and move the node to the internal down list.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#setDown(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    public void setDown(IMNode imnode) {
        removeFromAllLists(imnode);
        downNodes.add(imnode);
        imnode.setDown(true);
    }

    /**
     * Set the free state, and move the node to the internal free list.
     * Update the node status.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#setFree(org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode)
     */
    public void setFree(IMNode imnode) {
        removeFromAllLists(imnode);
        freeNodes.add(imnode);
        try {
            imnode.setFree();
        } catch (NodeException e) {
            // A down node shouldn't by busied...
            e.printStackTrace();
        }
        HashMap<VerifyingScript, Integer> verifs = imnode.getScriptStatus();
        for (Entry<VerifyingScript, Integer> entry : verifs.entrySet()) {
            if (entry.getKey().isDynamic() &&
                    (entry.getValue() == IMNode.VERIFIED_SCRIPT)) {
                entry.setValue(IMNode.ALREADY_VERIFIED_SCRIPT);
            }
        }
    }

    /**
     * Remove the imnode from all the lists it can appears.
     * @param imnode
     * @return
     */
    private boolean removeFromAllLists(IMNode imnode) {
        // Free
        boolean free = freeNodes.remove(imnode);

        // Busy
        boolean busy = busyNodes.remove(imnode);

        // Down
        boolean down = downNodes.remove(imnode);

        return free || busy || down;
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
        freeNodes.add(new IMNodeImpl(node, vnName, padName,
                (PADNodeSource) ProActiveObject.getStubOnThis()));
    }

    /**
     * Add the new proactive descriptor in the dataresource
     * @param padName : the name of the proactive descriptor
     * @param pad     : the proactive descriptor
     */
    public void addPAD(String padName, ProActiveDescriptor pad) {
        listPad.put(padName, pad);
    }

    // ----------------------------------------------------------------------//	
    // REDEPLOY
    // FIXME The redeploy (kill+activate) isn't support in the actualy 
    // version of ProActive
    private void redeployVNode(VirtualNode vnode, String padName,
        ProActiveDescriptor pad) throws RuntimeException {
        if (vnode.isActivated()) {
            vnode.killAll(false);
            ListIterator<IMNode> iterator = getAllNodes().listIterator();
            ArrayList<IMNode> toRemove = new ArrayList<IMNode>();
            while (iterator.hasNext()) {
                IMNode imnode = iterator.next();
                if (imnode.getPADName().equals(padName) &&
                        imnode.getVNodeName().equals(vnode.getName())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("remove node : " + imnode.getNodeURL());
                    }
                    toRemove.add(imnode);
                }
            }
            removeIMNodes(toRemove);
        }

        // FIXME uncomment this line below when the problem of redeploy will be fix 
        //IMDeploymentFactory.deployVirtualNode(this, padName, pad, vnode.getName());
        throw new RuntimeException(
            "The redeploy (kill+activate) isn't support in the actualy version of ProActive");
    }

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName) {
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            VirtualNode[] vnodes = pad.getVirtualNodes();
            for (VirtualNode vnode : vnodes) {
                redeployVNode(vnode, padName, pad);
            }
        }
    }

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnName  : the name of the virtual node of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String vnName) {
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            VirtualNode vnode = pad.getVirtualNode(vnName);
            redeployVNode(vnode, padName, pad);
        }
    }

    /**
     * Redeploy not supported by the current version of ProActive
     * @param padName : the name of the proactive descriptor
     * @param vnNames : the name of the virtual nodes of this pad to redeploy
     * @see redeployVNode(VirtualNode vnode, String padName, ProActiveDescriptor pad)
     */
    public void redeploy(String padName, String[] vnNames) {
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            VirtualNode vnode;
            for (String vnName : vnNames) {
                vnode = pad.getVirtualNode(vnName);
                redeployVNode(vnode, padName, pad);
            }
        }
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
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            try {
                pad.killall(false);
            } catch (ProActiveException e) {
                logger.error("error killing all active objects for pad " +
                    padName, e);
            }
            removeNode(padName);
            removePad(padName);
            // FIXME : delete the pad file
            // find the temp directory but how ????
            // File tempPAD = new File( tempDir + padName) 
            // if ( tempPAD.exists() ) tempPAD.delete();
        }
    }

    /**
    * Kill the virtual nodes of the proactive descriptors <I>padName>/I>
    * @param padName : the name of the Proactive Descriptor
    * @exception ProActiveException
    */
    public void killPAD(String padName, String vnName) {
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            VirtualNode vnode = pad.getVirtualNode(vnName);
            vnode.killAll(false);
            removeNode(padName, vnName);
        }
    }

    /**
    * Kill the virtual node <I>vnName</I> of the proactive descriptor <I>padName</I>
    * @param padName : the name of the Proactive Descriptor
    * @param vnName  : the name of the virtual node for killing
    * @see  killPAD(String padName)
    * @exception ProActiveException
    */
    public void killPAD(String padName, String[] vnNames) {
        if (listPad.containsKey(padName)) {
            ProActiveDescriptor pad = listPad.get(padName);
            VirtualNode[] vnodes = pad.getVirtualNodes();
            for (VirtualNode vnode : vnodes) {
                vnode.killAll(false);
            }
            removeNode(padName, vnNames);
        }
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
        ArrayList<String> pads = new ArrayList<String>();
        pads.addAll(listPad.keySet());
        for (String padName : pads) {
            killPAD(padName);
        }
    }

    private void removePad(String padName) {
        // TODO anything else to do ? 
        // what do we do for the IMNodes corresponding ?
        listPad.remove(padName);
    }

    private void removeNode(String padName) {
        ListIterator<IMNode> iterator = getAllNodes().listIterator();
        ArrayList<IMNode> toRemove = new ArrayList<IMNode>();
        while (iterator.hasNext()) {
            IMNode imnode = iterator.next();
            if (imnode.getPADName().equals(padName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("remove node : " + imnode.getNodeURL());
                }
                toRemove.add(imnode);
            }
        }
        removeIMNodes(toRemove);
    }

    private void removeNode(String padName, String vnName) {
        ListIterator<IMNode> iterator = getAllNodes().listIterator();
        ArrayList<IMNode> toRemove = new ArrayList<IMNode>();
        while (iterator.hasNext()) {
            IMNode imnode = iterator.next();
            if (imnode.getPADName().equals(padName) &&
                    imnode.getVNodeName().equals(vnName)) {
                if (logger.isInfoEnabled()) {
                    logger.info("remove node : " + imnode.getNodeURL());
                }
                toRemove.add(imnode);
            }
        }
        removeIMNodes(toRemove);
    }

    private void removeNode(String padName, String[] vnNames) {
        for (String vnName : vnNames) {
            removeNode(padName, vnName);
        }
    }

    @Override
    public String getSourceId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getBusyNodes() {
        return (ArrayList<IMNode>) busyNodes.clone();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getDownNodes() {
        return (ArrayList<IMNode>) downNodes.clone();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IMNode> getFreeNodes() {
        return (ArrayList<IMNode>) freeNodes.clone();
    }

    public IntWrapper getNbBusyNodes() {
        return new IntWrapper(busyNodes.size());
    }

    public IntWrapper getNbDownNodes() {
        return new IntWrapper(downNodes.size());
    }

    public IntWrapper getNbFreeNodes() {
        return new IntWrapper(freeNodes.size());
    }

    /**
     * The list of all {@link IMNode} in the Node Manager
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#getListAllIMNode()
     */
    public ArrayList<IMNode> getAllNodes() {
        ArrayList<IMNode> result = new ArrayList<IMNode>();
        result.addAll(freeNodes);
        result.addAll(busyNodes);
        result.addAll(downNodes);
        return result;
    }

    /**
     * return the number of nodes in the node manager, including
     * busy and waiting nodes.
     * @see org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNodeManager#getNbAllIMNode()
     */
    public IntWrapper getNbAllNodes() {
        return new IntWrapper(freeNodes.size() + busyNodes.size() +
            downNodes.size());
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        HashMap<String, ArrayList<VirtualNode>> deployedVNodesByPadName = new HashMap<String, ArrayList<VirtualNode>>();

        for (String padName : listPad.keySet()) {
            if (logger.isInfoEnabled()) {
                logger.info("pad name : " + padName);
            }
            ProActiveDescriptor pad = listPad.get(padName);

            ArrayList<VirtualNode> deployedVNodes = new ArrayList<VirtualNode>();
            VirtualNode[] vns = pad.getVirtualNodes();
            if (logger.isInfoEnabled()) {
                logger.info("nb vnodes of this pad : " + vns.length);
            }
            for (VirtualNode vn : vns) {
                if (logger.isInfoEnabled()) {
                    logger.info("virtualnode " + vn.getName() + " is actif ? " +
                        vn.isActivated());
                }
                if (vn.isActivated()) {
                    deployedVNodes.add(vn);
                }
            }
            deployedVNodesByPadName.put(padName, deployedVNodes);
        }
        return deployedVNodesByPadName;
    }

    public HashMap<String, ProActiveDescriptor> getListPAD() {
        return listPad;
    }

    public IntWrapper getSizeListPad() {
        return new IntWrapper(listPad.size());
    }

    public BooleanWrapper shutdown() {
        logger.info("Shutting down PAD Node Source");
        try {
            killAll();
            return new BooleanWrapper(true);
        } catch (ProActiveException e) {
            logger.error("Error during shutting down PAD Node Source : ", e);
            return new BooleanWrapper(false);
        }
    }

    public void deployAllVirtualNodes(File xmlDescriptor, Node remoteNode)
        throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployAllVirtualNodes((PADNodeSource) ProActiveObject.getStubOnThis(),
            localCopyPad.getName(), pad);
    }

    public void deployVirtualNode(File xmlDescriptor, Node remoteNode,
        String vnName) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployVirtualNode((PADNodeSource) ProActiveObject.getStubOnThis(),
            localCopyPad.getName(), pad, vnName);
    }

    public void deployVirtualNodes(File xmlDescriptor, Node remoteNode,
        String[] vnNames) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployVirtualNodes((PADNodeSource) ProActiveObject.getStubOnThis(),
            localCopyPad.getName(), pad, vnNames);
    }

    /**
     * @param filePAD    : the file proactive descriptor
     * @param remoteNode : the node in the host giving the file
     */
    private File pullPad(File filePAD, Node remoteNode)
        throws Exception {
        File localDest = File.createTempFile(filePAD.getName()
                                                    .replaceFirst("\\.xml\\z",
                    ""), ".xml");

        FileVector filePulled = FileTransfer.pullFile(remoteNode, filePAD,
                localDest);
        filePulled.waitForAll();

        if (logger.isInfoEnabled()) {
            logger.info("name of the pulled file pad : " + localDest);
        }

        return localDest;
    }
}
