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
package org.objectweb.proactive.extensions.branchnbound;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.branchnbound.core.Manager;
import org.objectweb.proactive.extensions.branchnbound.core.Task;
import org.objectweb.proactive.extensions.branchnbound.core.queue.TaskQueue;


/**
 * <p>This class contains all static methods to get <i>Manager</i> for
 * solving the Branch and Bound porblem with a given root task.</p>
 * <p>The returned <i>Manager</i> is a ProActive Active Object. This object aims
 * to configure and to handle the computation.</p>
 *
 * @see org.objectweb.proactive.branchnbound.core.Manager
 *
 * @author Alexandre di Costanzo
 *
 * Created on Apr 20, 2005
 */
@PublicAPI
public class ProActiveBranchNBound {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS);

    static {
        // Loading the ProActive's configuration
        ProActiveConfiguration.load();
    }

    // -------------------------------------------------------------------------
    // Manager static constructor
    // -------------------------------------------------------------------------

    /**
     * Create a new activate Manager with the given root task.
     *
     * @param root the root Task.
     * @param managerNode the Node where the Manager will be activate.
     * @param nodes an array of Nodes for distributing the computation.
     * @param queueType the Java class name of the Branch and Bound Queue. This
     * class must implement the interface TaskQueue.
     *
     * @return a ProActive Active Object which is the Manager.
     *
     * @throws ActiveObjectCreationException a problem occured while activating
     * the Manager.
     * @throws NodeException a problem with <code>managerNode</code>.
     *
     * @see org.objectweb.proactive.branchnbound.core.Manager
     * @see Task
     * @see TaskQueue
     */
    public static Manager newBnB(Task root, Node managerNode, Node[] nodes,
        String queueType) throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = managerNode;
        args[2] = nodes;
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
    }

    /**
     * Create a new activate Manager with the given root task. The Manager is
     * activate in the local default node of the current JVM.
     *
     * @param root the root Task.
     * @param virtualNode this contains a set of Nodes for distributing the
     * computation.
     * @param queueType the Java class name of the Branch and Bound Queue. This
     * class must implement the interface TaskQueue.
     *
     * @return a ProActive Active Object which is the Manager.
     *
     * @throws ActiveObjectCreationException a problem occured while activating
     * the Manager.
     * @throws NodeException a problem with the default node.
     *
     * @see org.objectweb.proactive.branchnbound.core.Manager
     * @see Task
     * @see TaskQueue
     */
    public static Manager newBnB(Task root, VirtualNode virtualNode,
        String queueType) throws ActiveObjectCreationException, NodeException {
        virtualNode.activate();
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = null;
        args[2] = virtualNode.getNodes();
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
    }

    /**
     * Create a new activate Manager with the given root task. Using hierachic
     * ProActive group communication.
     *
     * @param root the root Task.
     * @param managerNode the Node where the Manager will be activate.
     * @param nodes an array of array of Nodes for distributing the computation
     * with using hierachic group communication, with <code>node[i][j]</code>
     * is a sub-group.
     * @param queueType the Java class name of the Branch and Bound Queue. This
     * class must implement the interface TaskQueue.
     *
     * @return a ProActive Active Object which is the Manager.
     *
     * @throws ActiveObjectCreationException a problem occured while activating
     * the Manager.
     * @throws NodeException a problem with <code>managerNode</code>.
     *
     * @see org.objectweb.proactive.branchnbound.core.Manager
     * @see Task
     * @see TaskQueue
     */
    public static Manager newBnB(Task root, Node managerNode, Node[][] nodes,
        String queueType) throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = managerNode;
        args[2] = nodes;
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
    }

    /**
     * <p>Create a new activate Manager with the given root task. Using hierachic
     * ProActive group communication. The Manager is activate in the local
     * default node of the current JVM.</p>
     * <p><b>It is strongly recommended to **NOT ACTIVATE** the passed virtual
     * nodes. Because, the framework uses an optimzed way to deploying the
     * computation.</b></p>
     * <p><b>Also, we recomend to you that to use a vitural node by clusters or
     * sites for optimizing communication and active object creation.</b></p>
     *
     * @param root the root Task.
     * @param virtualNodes for distributing the computation with using hierachic
     * group communication, with <code>virtualNodes[i]</code> is a sub-group.
     * @param queueType e Java class name of the Branch and Bound Queue. This
     * class must implement the interface TaskQueue.
     *
     * @return a ProActive Active Object which is the Manager.
     *
     * @throws ActiveObjectCreationException a problem occured while activating
     * the Manager.
     * @throws NodeException a problem with the default node.
     *
     * @see org.objectweb.proactive.branchnbound.core.Manager
     * @see Task
     * @see TaskQueue
     */
    public static Manager newBnB(Task root, VirtualNode[] virtualNodes,
        String queueType) throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = null;
        args[2] = virtualNodes;
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
    }

    // -------------------------------------------------------------------------
    // Private methods
    // -------------------------------------------------------------------------

    /**
     * Activate a Manager with given arguments.
     *
     * @param args an array in 4 length, with as elements:
     * <ul>
     *         <li><code>args[0]</code>: is the root Task.</li>
     *         <li><code>args[1]</code>: is the manager Node or null, in that case
     * the current JVM default Node is used.</li>
     *         <li><code>args[2]</code>: is  nodes for distributing the computation
     * (Node[], Node[][]).</li>
     *         <li><code>args[3]</code>: is a String which is the class name of the
     * queue</li>
     * </ul>
     *
     * @return a ProActive Active Object which is the Manager.
     *
     * @throws ActiveObjectCreationException a problem occured while activating
     * the Manager.
     * @throws NodeException a problem with the manager node.
     *
     * @see org.objectweb.proactive.branchnbound.core.Manage
     */
    private static Manager activingTheManager(Object[] args)
        throws ActiveObjectCreationException, NodeException {
        assert args.length == 4 : args;
        if (logger.isDebugEnabled()) {
            logger.debug("New Active Manager with these arguments:\n" + "\t" +
                args[0].getClass().getName() + "\n" + "\t" +
                args[1].getClass().getName() + "\n" + "\t" +
                args[2].getClass().getName() + "\n" + "\t" +
                args[3].getClass().getName() + "\n");
        }
        assert args[0] instanceof Task : args[0];
        args[1] = (args[1] == null) ? NodeFactory.getDefaultNode()
                                    : (Node) args[1];
        assert args[1] instanceof Node : args[1];
        assert args[2] instanceof Node[] || args[2] instanceof Node[][] ||
        args[2] instanceof VirtualNode[] : args[2];
        assert args[3] instanceof String : args[3];

        return (Manager) PAActiveObject.newActive(Manager.class.getName(),
            args, (Node) args[1]);
    }
}
