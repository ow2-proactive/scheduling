/*
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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

/**
 * This is a FIFO implementation of the Policy
 *
 * @author walzouab
 *
 */
package org.objectweb.proactive.extra.scheduler.policy;

import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extra.scheduler.ActiveExecuter;
import org.objectweb.proactive.extra.scheduler.Info;
import org.objectweb.proactive.extra.scheduler.InternalTask;
import org.objectweb.proactive.extra.scheduler.NodeNExecuter;
import org.objectweb.proactive.extra.scheduler.resourcemanager.GenericResourceManager;


/**
 * An implementaion of the Generic Policy interface.
 * a faster implementation would be an active object version which its thread does the retrieval from the resource manager on its own
 * The default policy in the secheduler.
 * Has two queues one is the normal one, and the other is the failed which has a higher priority
 * @author walzouab
 *
 */
public class FIFOPolicy implements GenericPolicy {
    LinkedList<InternalTask> list; //normal queue
    LinkedList<InternalTask> failedList; //used in failed conditions
    GenericResourceManager rm;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);

    /**
     * this is a function that simulates what a nodepool would do
     * @param n
     * @return
     */
    private Vector<NodeNExecuter> getAtMostNNodesNExecuters(int n) {
        Vector<NodeNExecuter> executers = new Vector<NodeNExecuter>();
        Vector<Node> nodes = rm.getAtMostNNodes(new IntWrapper(n));
        Node node;
        ActiveExecuter AE;
        Vector<Node> troubledNodes = new Vector<Node>(); //avector of nodes to be freed if they cause trouble
        if (logger.isDebugEnabled() && (nodes.size() > 0)) {
            logger.debug("recived " + nodes.size() + " from resource manager");
        }
        while (!nodes.isEmpty()) {
            node = nodes.remove(0);

            try {
                //creates a new active executer and then pings it to make sure it is alive then adds it to the pool, it also sets killing it as an immediate service
                AE = ((ActiveExecuter) ProActive.newActive(ActiveExecuter.class.getName(),
                        null, node));
                ProActive.setImmediateService(AE, "kill");
                AE.ping();
                executers.add(new NodeNExecuter(AE, node));
            } catch (Exception e) {
                logger.error("Node " + node.getNodeInformation().getURL() +
                    " has problems, will be returned to resource manager" +
                    e.toString());
                troubledNodes.add(node);
            }
        }
        if (!troubledNodes.isEmpty()) { //There are troubled nodes to be freed
            if (logger.isDebugEnabled()) {
                logger.debug("will free from schedule");
            }
            rm.freeNodes(troubledNodes);
        }
        return executers;
    }

    public Vector<InternalTask> getReadyTasks() {
        Vector<InternalTask> readyTasks = new Vector<InternalTask>();

        Vector<NodeNExecuter> executers = getAtMostNNodesNExecuters(list.size() +
                failedList.size());

        while (!executers.isEmpty()) {
            NodeNExecuter executer = executers.remove(0);
            InternalTask tempTask = failedList.poll();
            if (tempTask == null) {
                tempTask = list.poll();
            }

            tempTask.nodeNExecuter = executer;
            readyTasks.add(tempTask);
        }
        return readyTasks;
    }

    //append tasks to the queue
    public void insert(Vector<InternalTask> t) {
        list.addAll(t);
    }

    //intialization done here
    //creates a new list
    public FIFOPolicy(GenericResourceManager rm) {
        list = new LinkedList<InternalTask>();
        failedList = new LinkedList<InternalTask>();
        this.rm = rm;
    }

    public static GenericPolicy getNewPolicy(GenericResourceManager rm) {
        return new FIFOPolicy(rm);
    }

    public FIFOPolicy() {
    }

    public void finished(InternalTask Task) {
        //		doesnt need to be implemented for FIFO
    }

    public void failed(InternalTask t) {
        //push the failed element to the begininng of the queue so that it is the next to be executed
        failedList.addFirst(t);
    }

    public void flush() {
        this.failedList.clear();
        this.list.clear();
    }

    public GenericTypeWrapper<InternalTask> getTask(String TaskID) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTaskID().equals(TaskID)) {
                return new GenericTypeWrapper<InternalTask>(list.get(i));
            }
        }

        for (int i = 0; i < failedList.size(); i++) {
            if (failedList.get(i).getTaskID().equals(TaskID)) {
                return new GenericTypeWrapper<InternalTask>(failedList.get(i));
            }
        }

        return new GenericTypeWrapper<InternalTask>(null);
    }

    public GenericTypeWrapper<InternalTask> removeTask(String TaskID) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTaskID().equals(TaskID)) {
                return new GenericTypeWrapper<InternalTask>(list.remove(i));
            }
        }

        for (int i = 0; i < failedList.size(); i++) {
            if (failedList.get(i).getTaskID().equals(TaskID)) {
                return new GenericTypeWrapper<InternalTask>(list.remove(i));
            }
        }

        return new GenericTypeWrapper<InternalTask>(null);
    }

    public Vector<String> getFailedID() {
        Vector<String> failed = new Vector<String>();

        for (int i = 0; i < failedList.size(); i++) {
            failed.add(failedList.get(i).getTaskID());
        }

        return failed;
    }

    public Vector<String> getQueuedID() {
        Vector<String> queued = new Vector<String>();
        for (int i = 0; i < list.size(); i++) {
            queued.add(list.get(i).getTaskID());
        }
        return queued;
    }

    public Vector<Info> getInfo_all() {
        Vector<Info> info = new Vector<Info>();

        for (int i = 0; i < failedList.size(); i++) {
            info.add(failedList.get(i).getTaskINFO());
        }

        for (int i = 0; i < list.size(); i++) {
            info.add(list.get(i).getTaskINFO());
        }

        return info;
    }
}
