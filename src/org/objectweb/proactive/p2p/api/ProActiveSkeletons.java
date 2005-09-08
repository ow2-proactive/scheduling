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
package org.objectweb.proactive.p2p.api;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.p2p.api.core.Manager;
import org.objectweb.proactive.p2p.api.core.Task;
import org.objectweb.proactive.p2p.api.core.queue.BasicQueueImpl;
import org.objectweb.proactive.p2p.api.core.queue.LargerQueueImpl;
import org.objectweb.proactive.p2p.api.core.queue.TaskQueue;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Apr 20, 2005
 */
public class ProActiveSkeletons {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS);

    static {
        // Loading the ProActive's configuration
        ProActiveConfiguration.load();
    }

    public static Manager newFarm(Task root, Node managerNode, Node[] nodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveSkeletons.newFarmWithSpecifiedQueue(root, managerNode,
            nodes, BasicQueueImpl.class.getName());
    }

    public static Manager newFarm(Task root, VirtualNode virtualNode)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
        return ProActiveSkeletons.newFarmWithSpecifiedQueue(root, virtualNode,
            BasicQueueImpl.class.getName());
    }

    public static Manager newFarmWithLargerQueue(Task root, Node managerNode,
        Node[] nodes) throws ActiveObjectCreationException, NodeException {
        return ProActiveSkeletons.newFarmWithSpecifiedQueue(root, managerNode,
            nodes, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmWithLargerQueue(Task root,
        VirtualNode virtualNode)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
         return ProActiveSkeletons.newFarmWithSpecifiedQueue(root, virtualNode,
            LargerQueueImpl.class.getName());
    }

    public static Manager newFarmWithSpecifiedQueue(Task root,
        Node managerNode, Node[] nodes, String queueType)
        throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = nodes;
        args[2] = managerNode;
        args[3] = queueType;
        return ProActiveSkeletons.activingTheManager(args);
    }

    public static Manager newFarmWithSpecifiedQueue(Task root,
        VirtualNode virtualNode, String queueType)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = virtualNode;
        args[2] = null;
        args[3] = queueType;
        return ProActiveSkeletons.activingTheManager(args);
    }

    private static Manager activingTheManager(Object[] args)
        throws ActiveObjectCreationException, NodeException {
        assert args.length == 4 : args;
        assert args[0] instanceof Task : args[0];
        assert args[1] instanceof Node[] || args[1] instanceof VirtualNode : args[1];
        assert args[2] instanceof Node : args[2];
        assert args[3] instanceof TaskQueue : args[3];

        Node managerNode = (args[2] == null) ? NodeFactory.getDefaultNode()
                                             : (Node) args[2];
        args[2] = managerNode;
        return (Manager) ProActive.newActive(Manager.class.getName(), args,
            managerNode);
    }
}
