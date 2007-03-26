package org.objectweb.proactive.extra.infrastructuremanager.dataresource.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMDataResource;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;


public class IMDataResourceImpl implements IMDataResource, Serializable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DATARESOURCE);

    // Attributes
    private ArrayList<IMNode> listFreeIMNode = new IMArrayListFree();
    private ArrayList<IMNode> listBusyIMNode = new IMArrayListBusy();
    private HashMap<String, ProActiveDescriptor> listPad = new HashMap<String, ProActiveDescriptor>();

    //----------------------------------------------------------------------//
    // CONSTRUCTORS
    public IMDataResourceImpl() {
    }

    public void init() {
        listFreeIMNode = new ArrayList<IMNode>();
        listBusyIMNode = new ArrayList<IMNode>();
        listPad = new HashMap<String, ProActiveDescriptor>();
    }

    //----------------------------------------------------------------------//
    // TEST
    public String echo() {
        return "Je suis la IMDataResource";
    }

    //----------------------------------------------------------------------//
    // ACCESSORS
    public int getSizeListFreeIMNode() {
        return listFreeIMNode.size();
    }

    public int getSizeListBusyIMNode() {
        return listBusyIMNode.size();
    }

    public int getSizeListPad() {
        return listPad.size();
    }

    public ArrayList<IMNode> getListFreeIMNode() {
        return listFreeIMNode;
    }

    public ArrayList<IMNode> getListBusyIMNode() {
        return listBusyIMNode;
    }

    public ArrayList<IMNode> getListAllIMNode() {
        ArrayList<IMNode> listAllIMNode = (ArrayList<IMNode>) listFreeIMNode.clone();
        listAllIMNode.addAll(listBusyIMNode);
        return listAllIMNode;
    }

    public HashMap<String, ProActiveDescriptor> getListPad() {
        return listPad;
    }

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        if (logger.isInfoEnabled()) {
            logger.info("getDeployedVirtualNodeByPad");
        }
        HashMap<String, ArrayList<VirtualNode>> deployedVNodesByPadName = new HashMap<String, ArrayList<VirtualNode>>();

        for (String padName : listPad.keySet()) {
            if (logger.isInfoEnabled()) {
                logger.info("pad name : " + padName);
            }
            ProActiveDescriptor pad = listPad.get(padName);
            if (logger.isInfoEnabled()) {
                logger.info("pad url : " + pad.getUrl());
            }
            ArrayList<VirtualNode> deployedVNodes = new ArrayList<VirtualNode>();
            VirtualNode[] vns = pad.getVirtualNodes();
            if (logger.isInfoEnabled()) {
                logger.info("nb vnodes of this pad : " + vns.length);
            }
            for (VirtualNode vn : vns) {
                if (logger.isInfoEnabled()) {
                    logger.info("vn is actif ? " + vn.isActivated());
                }
                if (vn.isActivated()) {
                    deployedVNodes.add(vn);
                }
            }
            deployedVNodesByPadName.put(padName, deployedVNodes);
        }
        return deployedVNodesByPadName;
    }

    public ProActiveDescriptor getDeployedPad(String padName) {
        return listPad.get(padName);
    }

    //----------------------------------------------------------------------//
    // IS
    public boolean isDeployedPad(String padName) {
        return listPad.containsKey(padName);
    }

    //----------------------------------------------------------------------//
    // ADD
    public void addNewDeployedNode(Node node, String vnName, String padName) {
        this.listFreeIMNode.add(new IMNodeImpl(node, vnName, padName));
    }

    public void putPAD(String padName, ProActiveDescriptor pad) {
        listPad.put(padName, pad);
    }

    //----------------------------------------------------------------------//
    // REMOVE NODE BUSY AND FREE
    public void removePad(String padName) {
        listPad.remove(padName);
    }

    public void removeNode(String padName) {
        for (IMNode imnode : listBusyIMNode) {
            if (imnode.getPADName().equals(padName)) {
                listBusyIMNode.remove(imnode);
            }
        }
        for (IMNode imnode : listFreeIMNode) {
            if (imnode.getPADName().equals(padName)) {
                listBusyIMNode.remove(imnode);
            }
        }
    }

    public void removeNode(String padName, String vnName) {
        for (IMNode imnode : listBusyIMNode) {
            if (imnode.getPADName().equals(padName) &
                    imnode.getVNodeName().equals(vnName)) {
                listBusyIMNode.remove(imnode);
            }
        }
        for (IMNode imnode : listBusyIMNode) {
            if (imnode.getPADName().equals(padName) &
                    imnode.getVNodeName().equals(vnName)) {
                listFreeIMNode.remove(imnode);
            }
        }
    }

    public void removeNode(String padName, String[] vnNames) {
        for (String vnName : vnNames) {
            removeNode(padName, vnName);
        }
    }

    //----------------------------------------------------------------------//
    // FREE NODE(S) - NODE : BUSY -> FREE
    public void freeNode(Node node) throws NodeException {
        for (IMNode imnode : listBusyIMNode) {
            if (imnode.getNodeName().equals(node.getNodeInformation().getName())) {
                listBusyIMNode.remove(imnode);
                listFreeIMNode.add(imnode);
                break;
            }
        }
    }

    public void freeNodes(Node[] nodes) throws NodeException {
        for (Node node : nodes) {
            freeNode(node);
        }
    }

    public void freeNodes(VirtualNode vnode) {
        for (IMNode imnode : listBusyIMNode) {
            if (imnode.getVNodeName().equals(vnode.getName())) {
                listBusyIMNode.remove(imnode);
                listFreeIMNode.add(imnode);
            }
        }
    }

    //----------------------------------------------------------------------//
    // GET NODE(S)
    public Node[] getAtLeastNNodes(int nb) throws NodeException {
        int nbFreeIMNode = listFreeIMNode.size();
        if (logger.isInfoEnabled()) {
            logger.info("getAtLeastNNodes, user want " + nb +
                " nodes and there is " + nbFreeIMNode + " free node(s)");
        }
        int sizeListUser = Math.min(nb, nbFreeIMNode);
        Node[] nodes = new Node[sizeListUser];
        for (int i = 0; i < sizeListUser; i++) {
            nodes[i] = getNode();
        }
        return nodes;
    }

    public Node getNode() throws NodeException {
        int nbFreeIMNode = listFreeIMNode.size();
        if (logger.isInfoEnabled()) {
            logger.info("getNode, user want a node, and there is " +
                nbFreeIMNode + " free node(s)");
        }
        if (nbFreeIMNode != 0) {
            IMNode imnode = listFreeIMNode.remove(0);
            listBusyIMNode.add(imnode);
            return imnode.getNode();
        } else {
            throw new NodeException("There isn't free node");
        }
    }

    //----------------------------------------------------------------------//
    // PAD
}
