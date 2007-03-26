package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * An interface Front-End for the User to communicate with
 * the Resource Manager
 */
public interface IMUser {
    // pour tester
    public String echo();

    /**
     * Reserves nb nodes, if the RM don't have nb free nodes
     * then it returns the max of free nodes
     * @param nb the number of nodes
     * @return an arraylist of nodes
     */
    public Node[] getAtLeastNNodes(int nb) throws NodeException;

    /**
     * Method to get only one node
     * @return node
     */
    public Node getNode() throws NodeException;

    /**
     * Release a node reserve by the user
     */
    public void freeNode(Node node) throws NodeException;

    public void freeNodes(Node[] nodes) throws NodeException;
}
