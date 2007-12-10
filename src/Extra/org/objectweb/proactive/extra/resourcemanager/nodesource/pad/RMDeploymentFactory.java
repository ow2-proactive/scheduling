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
package org.objectweb.proactive.extra.resourcemanager.nodesource.pad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.resourcemanager.nodesource.frontend.PadDeployInterface;


/**
 * Manages for the Resource Manager
 * a thread pool for {@link RMDeploy} objects that perform deployment.
 *
 * @author PrActive team.
 *
 */
public class RMDeploymentFactory {

    /** associated Log4j logger */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.RM_DEPLOYMENT_FACTORY);

    /** Thread pool for RMDeploy objects */
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Deploy all virtual node of the ProActive descriptor <I>pad</I>
     * @param nodeSource Stub of NodeSource object that initiated the deployment.
     * @param padName : name of the ProActive descriptor.
     * @param pad     : the ProcAtive descriptor.
     */
    public static void deployAllVirtualNodes(PadDeployInterface nodeSource,
        ProActiveDescriptor pad) {
        if (logger.isInfoEnabled()) {
            logger.info("deployAllVirtualNodes");
        }
        RMDeploy d = new RMDeploy(nodeSource, pad);
        executor.execute(d);
    }

    /**
     * Deploy only the virtual node <I>vnName</I>
     * @param nodeSource Stub of NodeSource object that initiated the deployment.
     * @param pad     : ProActive descriptor to deploy.
     * @param vnName  : name of the virtual node to deploy.
     */
    public static void deployVirtualNode(PadDeployInterface nodeSource,
        ProActiveDescriptor pad, String vnName) {
        if (logger.isInfoEnabled()) {
            logger.info("deployVirtualNode : " + vnName);
        }
        deployVirtualNodes(nodeSource, pad, new String[] { vnName });
    }

    /**
     * Deploy only the virtual nodes <I>vnNames</I>
     * @param nodeSource Stub of NodeSource object that initiated the deployment.
     * @param pad     : the ProActive descriptor
     * @param vnNames : the name of the virtual nodes to deploy
     */
    public static void deployVirtualNodes(PadDeployInterface nodeSource,
        ProActiveDescriptor pad, String[] vnNames) {
        if (logger.isInfoEnabled()) {
            String concatVnNames = "";
            for (String vnName : vnNames) {
                concatVnNames += (vnName + " ");
            }
            logger.info("deployVirtualNodes : " + concatVnNames);
        }
        RMDeploy d = new RMDeploy(nodeSource, pad, vnNames);
        executor.execute(d);
    }
}
