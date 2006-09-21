package org.objectweb.proactive.scheduler;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * This is the class that is responsible for the "management" (allocation, disallocation ...)
 * of the "ressources" (processing power).
 *
 * @author cjarjouh
 *
 */
public class RessourceManager {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.RESSOURCE_MANAGER);

    // There are 3 HashMaps one for the idle nodes another for the nodes that 
    // are busy and one for the reservedNodes that haven't been activated yet. 
    // We can check the size of the HashMap unusedNodes to find out the number 
    // of nodes available ...
    private Vector unusedNodes;
    private Vector reservedNodes;
    private Vector usedNodes;

    public RessourceManager() {
    }

    /**
     *  the constructor of the class ... We don't use the constructor by default
     *  because of several problems of instanciation ...
     * @param b nothing special
     */
    public RessourceManager(BooleanWrapper b) {
        unusedNodes = new Vector();
        reservedNodes = new Vector();
        usedNodes = new Vector();

        // launches the specific ressourceListener that shall listen for the nodes
        // created and add the newly created node to the queue.
        String xmlURL = RessourceManager.class.getResource(
                "/org/objectweb/proactive/scheduler/test.xml").getPath();

        Vector vnNames = new Vector();
        vnNames.add("SchedulerVN");
        new RessourceListener(this.unusedNodes, xmlURL, vnNames);
        logger.debug("ressource manager created");
    }

    /**
     * This method returns the number of ressources available.
     * @return the number of unallocated ressources.
     */
    public int getAvailableNodesNb() {
        return this.unusedNodes.size();
    }

    /**
     * frees the allocated nodes of the job associated to the specified jobId
     * @param jobId the id of the job
     * @param mainIsDead true if the main node is dead
     */
    public void freeNodes(String jobId, boolean mainIsDead) {
        int index = 0;
        Vector nodes;

        while ((index != this.usedNodes.size()) &&
                (!((Node) this.usedNodes.get(index)).getNodeInformation()
                       .getJobID().equals(jobId))) {
            index++;
        }
        if (index == this.usedNodes.size()) {
            nodes = this.reservedNodes;
        } else {
            nodes = this.usedNodes;
        }

        this.nodeFreer(nodes, jobId, mainIsDead);
    }

    /**
     * frees the nodes and does the cleaning. This method is used because we are
     * unsure of the place of the nodes wether they are in the usedNodes queue
     * or in the reservedNodes queue.
     * @param nodes is the vector containting the nodes we need to free
     * @param jobId is the ID of the job in question
     * @param mainIsDead true if the main node is dead
     */
    private void nodeFreer(Vector nodes, String jobId, boolean mainIsDead) {
        int ressourceNumber = 0;
        int indexOfFirstNode = 0;
        int i;

        while (!(((Node) nodes.get(indexOfFirstNode)).getNodeInformation()
                      .getJobID().equals(jobId)))
            indexOfFirstNode++;

        int index = indexOfFirstNode;

        while ((index != nodes.size()) &&
                (((Node) nodes.get(index)).getNodeInformation().getJobID()
                      .equals(jobId))) {
            ressourceNumber++;
            index++;
        }

        if (mainIsDead) {
            nodes.remove(indexOfFirstNode);
            logger.debug("removing dead node");
            i = 1;
        } else {
            i = 0;
        }

        for (; i < ressourceNumber; ++i) {
            Node node = (Node) nodes.get(indexOfFirstNode);
            try {
                if (node.getNumberOfActiveObjects() != 0) {
                    node.killAllActiveObjects();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            node.getNodeInformation().setJobID("-");
            nodes.remove(indexOfFirstNode);
            this.unusedNodes.add(node);
            logger.debug("node freed " + node.getNodeInformation().getURL());
        }
    }

    /**
     * Reserve "ressourceNumber" of ressources and returns the first reserved node.
     * @param ressourceNumber is the number of ressources to be granted.
     * @param jobId is the number of the job reserving these ressources
     */
    public Node reserveNodes(String jobId, int ressourceNumber) {
        if (ressourceNumber == 0) {
            return null;
        }
        Node node = (Node) this.unusedNodes.remove(0);
        node.getNodeInformation().setJobID(jobId);
        this.reservedNodes.add(node);
        logger.debug("node reserved " + node.getNodeInformation().getURL());

        for (int i = 1; i < ressourceNumber; ++i) {
            Node tmpNode = (Node) this.unusedNodes.remove(0);
            tmpNode.getNodeInformation().setJobID(jobId);
            this.reservedNodes.add(tmpNode);
        }

        return node;
    }

    /**
     * Returns all the nodes that were allocated to the job and moves them from
     * the waiting queue to the used queue.
     * @param jobId is the id of the job.
     */
    public Node[] getNodes(String jobId, int askedNodes) {
        int indexOfFirstNode = 0;
        while (!(((Node) this.reservedNodes.get(indexOfFirstNode)).getNodeInformation()
                      .getJobID().equals(jobId)))
            indexOfFirstNode++;

        Node[] nodes = new Node[askedNodes];

        for (int i = 0; i < askedNodes; ++i) {
            Node node = (Node) this.reservedNodes.remove(indexOfFirstNode);
            this.usedNodes.add(node);
            nodes[i] = node;
            logger.debug("acquiring reserved node " +
                node.getNodeInformation().getURL());
        }

        return nodes;
    }

    /**
     * Tests the availlability of "ressourceNumber" of ressources.
     * @param ressourceNumber is the number of ressources needed.
     * @return true if the amount is available, false otherwise.
     */
    public BooleanWrapper isAvailable(int ressourceNumber) {
        return new BooleanWrapper(ressourceNumber <= this.getAvailableNodesNb());
    }

    /**
     * Provides the information about the nodes (state, job running, properties ...)
     * Returns a vector of the nodes description
     * @return information about the nodes in forms of a Vector and null if
     *         the node is not defined
     */
    public Vector nodes(String nodeURL) {
        Vector nodes = new Vector();

        nodes.addAll(this.unusedNodes);
        nodes.addAll(this.reservedNodes);
        nodes.addAll(this.usedNodes);

        if (nodeURL != null) {
            Iterator iterator = nodes.iterator();
            Node node = null;

            while (iterator.hasNext()) {
                Node tmpNode = (Node) iterator.next();
                if (tmpNode.getNodeInformation().getURL().equals(nodeURL)) {
                    node = tmpNode;
                    break;
                }
            }

            nodes.clear();
            if (node != null) {
                nodes.add(node);
            }
        }

        logger.debug("fetching node(s) description");

        return nodes;
    }

    public BooleanWrapper checkReservation(String jobId) {
        logger.debug("checking for node reservation");
        for (int i = 0; i < this.reservedNodes.size(); ++i) {
            Node node = (Node) this.reservedNodes.get(i);
            if (node.getNodeInformation().getJobID().equals(jobId)) {
                return new BooleanWrapper(true);
            }
        }

        return new BooleanWrapper(false);
    }
}
