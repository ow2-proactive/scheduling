package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * An interface Front-End for the User to communicate with
 * the Infrastructure Manager
 */
public interface IMUser {
    // for testing
    public String echo();

    /**
     * Reserves nb nodes, if the infrastructure manager (IM) don't have nb free nodes
     * then it returns the max of free nodes
     * @param nb the number of nodes
     * @return an arraylist of nodes
     * @throws NodeException
     */
    public Node[] getAtLeastNNodes(int nb) throws NodeException;

    /**
     * Method to get only one node
     * @return node
     * @throws NodeException
     */
    public Node getNode() throws NodeException;

    /**
     * Release the node reserve by the user
     * @param node : the node to release
     * @throws NodeException
     */
    public void freeNode(Node node) throws NodeException;

    /**
     * Release the nodes reserve by the user
     * @param nodes : a table of nodes to release
     * @throws NodeException
     */
    public void freeNodes(Node[] nodes) throws NodeException;
}
