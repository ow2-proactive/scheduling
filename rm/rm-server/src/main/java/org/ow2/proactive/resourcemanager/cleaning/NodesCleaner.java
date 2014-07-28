/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.cleaning;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;


/**
 * This class is responsible for the node cleaning.
 * It does it in parallel in a dedicated thread pool.
 */
@ActiveObject
public class NodesCleaner implements RunActive {
    /** class' logger */
    private static final Logger logger = Logger.getLogger(NodesCleaner.class);

    private ExecutorService scriptExecutorThreadPool;

    /** RMCore reference to be able to set nodes free after the cleaning procedure */
    private RMCore rmcore;

    /** PA Constructor */
    public NodesCleaner() {
    }

    public NodesCleaner(RMCore rmcore) {
        this.rmcore = rmcore;
        this.scriptExecutorThreadPool = Executors
                .newFixedThreadPool(PAResourceManagerProperties.RM_CLEANING_MAX_THREAD_NUMBER.getValueAsInt());
    }

    /**
     * Cleans nodes in parallel for the nodes specified.
     *
     * @param nodes to be cleaned
     * @return true if all the nodes were freed, false if error occurs on one of the node (it will be marked as down in this case)
     */
    public BooleanWrapper cleanAndRelease(List<RMNode> nodes) {
        List<Callable<Boolean>> cleaners = new LinkedList<Callable<Boolean>>();
        for (RMNode node : nodes) {
            logger.debug("Cleaning the node " + node.getNodeURL());
            cleaners.add(new NodeCleaner(node));
        }

        try {
            Collection<Future<Boolean>> cleanNodes = scriptExecutorThreadPool.invokeAll(cleaners);

            int index = 0;
            for (Future<Boolean> cleanNode : cleanNodes) {
                RMNode node = nodes.get(index);

                if (!cleanNode.isCancelled()) {
                    Boolean isClean = null;
                    try {
                        isClean = cleanNode.get();
                        if (!isClean.booleanValue()) {
                            logger.warn("Cannot clean the node " + node.getNodeURL());
                            rmcore.setDownNode(node.getNodeURL());
                        } else {
                            logger.debug("The node " + node.getNodeURL() + " has been successfully cleaned");
                        }
                    } catch (ExecutionException e) {
                        logger.warn("Cannot clean the node " + node.getNodeURL(), e);
                        rmcore.setDownNode(node.getNodeURL());
                    }
                } else {
                    logger.warn("Cannot clean the node " + node.getNodeURL());
                    rmcore.setDownNode(node.getNodeURL());
                }
                index++;
            }

            // if we had any error while cleaning a node, this node would have been already marked as down
            // in this case rmcore.setFreeNodes() will return false
            return rmcore.setFreeNodes(nodes);
        } catch (InterruptedException e) {
            logger.error("", e);
        }

        return new BooleanWrapper(false);
    }

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            Request request = null;
            try {
                request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        logger.error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("runActivity interrupted", e);
            }
        }
    }
}
