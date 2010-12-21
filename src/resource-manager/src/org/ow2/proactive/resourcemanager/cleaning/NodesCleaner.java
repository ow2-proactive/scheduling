/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.cleaning;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.rmnode.RMNode;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * This class is responsible for the node cleaning.
 * It does it in parallel in a dedicated thread pool.
 */
public class NodesCleaner {
    /** class' logger */
    private static final Logger logger = ProActiveLogger.getLogger(RMLoggers.CLEANER);

    private ExecutorService scriptExecutorThreadPool = Executors
            .newFixedThreadPool(PAResourceManagerProperties.RM_CLEANING_MAX_THREAD_NUMBER.getValueAsInt());

    /** RMCore reference to be able to set nodes free after the cleaning procedure */
    private RMCore rmcore;

    /** PA Constructor */
    public NodesCleaner() {
    }

    public NodesCleaner(RMCore rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * Cleans nodes in parallel for the nodes specified.
     *
     * @param nodes to be cleaned
     * @return true if success, false if there is any error for any node
     */
    public BooleanWrapper cleanAndRelease(List<RMNode> nodes) {
        List<NodeCleaner> cleaners = new LinkedList<NodeCleaner>();
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
                        } else {
                            logger.debug("The node " + node.getNodeURL() + " has been successfully cleaned");
                        }
                    } catch (ExecutionException e) {
                        logger.warn("Cannot clean the node " + node.getNodeURL(), e);
                    }
                } else {
                    logger.warn("Cannot clean the node " + node.getNodeURL());
                }
                index++;
            }

            return rmcore.setFreeNodes(nodes);
        } catch (InterruptedException e) {
            logger.error("", e);
        }

        return new BooleanWrapper(false);
    }
}
