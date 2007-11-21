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
package org.objectweb.proactive.extra.infrastructuremanager.nodesource.pad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class IMDeploymentFactory {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_DEPLOYMENT_FACTORY);

    // Attributes
    private static ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Deploy all virtual node of the proactive descriptor <I>pad</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     */
    public static void deployAllVirtualNodes(PADNodeSource nodeSource,
        String padName, ProActiveDescriptor pad) {
        if (logger.isInfoEnabled()) {
            logger.info("deployAllVirtualNodes");
        }

        IMDeploy d = new IMDeploy(nodeSource, padName, pad);
        executor.execute(d);
    }

    /**
     * Deploy only the virtual node <I>vnName</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     * @param vnName  : the name of the virtual node to deploy
     */
    public static void deployVirtualNode(PADNodeSource nodeSource,
        String padName, ProActiveDescriptor pad, String vnName) {
        if (logger.isInfoEnabled()) {
            logger.info("deployVirtualNode : " + vnName);
        }

        deployVirtualNodes(nodeSource, padName, pad, new String[] { vnName });
    }

    /**
     * Deploy only the virtual nodes <I>vnNames</I>
     * @param imCore
     * @param padName : the name of the proactive descriptor
     * @param pad     : the procative descriptor
     * @param vnNames : the name of the virtual nodes to deploy
     */
    public static void deployVirtualNodes(PADNodeSource nodeSource,
        String padName, ProActiveDescriptor pad, String[] vnNames) {
        if (logger.isInfoEnabled()) {
            String concatVnNames = "";

            for (String vnName : vnNames) {
                concatVnNames += (vnName + " ");
            }

            logger.info("deployVirtualNodes : " + concatVnNames);
        }

        IMDeploy d = new IMDeploy(nodeSource, padName, pad, vnNames);
        executor.execute(d);
    }
}
