package org.objectweb.proactive.extra.infrastructuremanager.dataresource;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


public interface IMDataResource {
    // TEST
    public String echo();

    //----------------------------------------------------------------------//
    // INIT
    public void init();

    //----------------------------------------------------------------------//
    // ADD NODE and PUT PAD 
    public void addNewDeployedNode(Node node, String vnName, String padName);

    public void putPAD(String padName, ProActiveDescriptor pad);

    //----------------------------------------------------------------------//
    // MONITORING
    // Nb All Node : free + down + busy
    public int getSizeListFreeIMNode();

    public int getSizeListBusyIMNode();

    public int getSizeListDownIMNode();

    /**
     * Nb All IMNode = free + down + busy
     * @return number of all resources (IMNode)
     */
    public int getNbAllIMNode();

    public int getSizeListPad();

    public HashMap<String, ProActiveDescriptor> getListPad();

    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad();

    public ArrayList<IMNode> getListFreeIMNode();

    public ArrayList<IMNode> getListBusyIMNode();

    public ArrayList<IMNode> getListAllIMNode();

    public ProActiveDescriptor getDeployedPad(String padName);

    //----------------------------------------------------------------------//
    // IS

    /**
     * @return true if the name of proactive descriptor <I>padName</I> exist
     * in the dataresource, else false.
     */
    public boolean isDeployedPad(String padName);

    //----------------------------------------------------------------------//
    // REMOVE
    public void removePad(String padName);

    public void removeNode(String padName);

    public void removeNode(String padName, String vnName);

    public void removeNode(String padName, String[] vnNames);

    //----------------------------------------------------------------------//
    // FREE
    public void freeNode(Node node) throws NodeException;

    public void freeNodes(Node[] nodes) throws NodeException;

    public void freeNodes(VirtualNode vnode);

    //----------------------------------------------------------------------//
    // GET NODE 
    public Node getNode() throws NodeException;

    public Node[] getAtLeastNNodes(int nb) throws NodeException;

    public void nodeIsDown(IMNode imNode);
}
