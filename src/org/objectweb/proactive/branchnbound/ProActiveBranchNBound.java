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
package org.objectweb.proactive.branchnbound;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.branchnbound.core.Manager;
import org.objectweb.proactive.branchnbound.core.Task;
import org.objectweb.proactive.branchnbound.core.queue.BasicQueueImpl;
import org.objectweb.proactive.branchnbound.core.queue.LargerQueueImpl;
import org.objectweb.proactive.branchnbound.core.queue.TaskQueue;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Apr 20, 2005
 */
public class ProActiveBranchNBound {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS);

    static {
        // Loading the ProActive's configuration
        ProActiveConfiguration.load();
    }

    public static Manager newFarm(Task root, Node managerNode, Node[] nodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            managerNode, nodes, BasicQueueImpl.class.getName());
    }

    public static Manager newFarm(Task root, VirtualNode virtualNode)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            virtualNode, BasicQueueImpl.class.getName());
    }

    public static Manager newFarm(Task root, VirtualNode[] virtualNodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            virtualNodes, BasicQueueImpl.class.getName());
    }

    public static Manager newFarmWithLargerQueue(Task root, Node managerNode,
        Node[] nodes) throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            managerNode, nodes, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmWithLargerQueue(Task root,
        VirtualNode virtualNode)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            virtualNode, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmWithLargerQueue(Task root,
        VirtualNode[] virtualNodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmWithSpecifiedQueue(root,
            virtualNodes, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmWithSpecifiedQueue(Task root,
        Node managerNode, Node[] nodes, String queueType)
        throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = nodes;
        args[2] = managerNode;
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
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
        return ProActiveBranchNBound.activingTheManager(args);
    }

    public static Manager newFarmWithSpecifiedQueue(Task root,
        VirtualNode[] virtualNodes, String queueType)
        throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[4];
        args[0] = root;
        args[1] = virtualNodes;
        args[2] = null;
        args[3] = queueType;
        return ProActiveBranchNBound.activingTheManager(args);
    }

    public static Manager newFarmFromBackup(String taskFile, String resultFile,
        Node managerNode, Node[] nodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, managerNode, nodes, BasicQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackup(String taskFile, String resultFile,
        VirtualNode virtualNode)
        throws IOException, ClassNotFoundException, 
            ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, virtualNode, BasicQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackup(String taskFile, String resultFile,
        VirtualNode[] virtualNodes)
        throws IOException, ClassNotFoundException, 
            ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, virtualNodes, BasicQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackupWithLargerQueue(String taskFile,
        String resultFile, Node managerNode, Node[] nodes)
        throws ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, managerNode, nodes, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackupWithLargerQueue(String taskFile,
        String resultFile, VirtualNode virtualNode)
        throws IOException, ClassNotFoundException, 
            ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, virtualNode, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackupWithLargerQueue(String taskFile,
        String resultFile, VirtualNode[] virtualNodes)
        throws IOException, ClassNotFoundException, 
            ActiveObjectCreationException, NodeException {
        return ProActiveBranchNBound.newFarmFromBackupWithSpecifiedQueue(taskFile,
            resultFile, virtualNodes, LargerQueueImpl.class.getName());
    }

    public static Manager newFarmFromBackupWithSpecifiedQueue(String taskFile,
        String resultFile, VirtualNode virtualNode, String queueType)
        throws ActiveObjectCreationException, NodeException {
        assert virtualNode.isActivated() == false : "The virtual must be not actived";
        Object[] args = new Object[2];
        args[0] = virtualNode;
        args[1] = queueType;
        Manager manager = (Manager) ProActive.newActive(Manager.class.getName(),
                args, NodeFactory.getDefaultNode());
        manager.loadTasks(taskFile);
        manager.loadResults(resultFile);
        return manager;
    }

    public static Manager newFarmFromBackupWithSpecifiedQueue(String taskFile,
        String resultFile, VirtualNode[] virtualNodes, String queueType)
        throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[2];
        args[0] = virtualNodes;
        args[1] = queueType;
        Manager manager = (Manager) ProActive.newActive(Manager.class.getName(),
                args, NodeFactory.getDefaultNode());
        manager.loadTasks(taskFile);
        manager.loadResults(resultFile);
        return manager;
    }

    public static Manager newFarmFromBackupWithSpecifiedQueue(String taskFile,
        String resultFile, Node managerNode, Node[] nodes, String queueType)
        throws ActiveObjectCreationException, NodeException {
        Object[] args = new Object[2];
        args[0] = nodes;
        args[1] = queueType;
        Manager manager = (Manager) ProActive.newActive(Manager.class.getName(),
                args, managerNode);
        manager.loadTasks(taskFile);
        manager.loadResults(resultFile);
        return manager;
    }

    private static Manager activingTheManager(Object[] args)
        throws ActiveObjectCreationException, NodeException {
        assert args.length == 4 : args;
        Node managerNode = (args[2] == null) ? NodeFactory.getDefaultNode()
                                             : (Node) args[2];
        args[2] = managerNode;
        assert args[0] instanceof Task : args[0];
        assert args[1] instanceof Node[] || args[1] instanceof VirtualNode ||
        args[1] instanceof VirtualNode[] : args[1];
        assert args[2] instanceof Node : args[2];
        assert args[3] instanceof TaskQueue : args[3];

        return (Manager) ProActive.newActive(Manager.class.getName(), args,
            managerNode);
    }
}
