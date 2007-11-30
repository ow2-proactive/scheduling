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
package org.objectweb.proactive.extensions.scilab;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * MSDeployEngine contains all methods to deploy Scilab Engines from
 * a deployment descriptor
 * @author amangin
 *
 */
public class MSDeployEngine {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCILAB_DEPLOY);
    private static HashMap<String, Node> mapNode = new HashMap<String, Node>(); // List of deployed VNs 

    /**
     * @param pathDescriptor
     * @return list of virtual node contained in the deployment descriptor
     */
    public static String[] getListVirtualNode(String pathDescriptor) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:getListVirtualNode:" +
                pathDescriptor);
        }

        ProActiveDescriptor desc;
        VirtualNode[] arrayVn;
        String[] arrayNameVn = null;
        try {
            desc = ProDeployment.getProactiveDescriptor("file:" +
                    pathDescriptor);
            arrayVn = desc.getVirtualNodes();
            arrayNameVn = new String[arrayVn.length];

            for (int i = 0; i < arrayVn.length; i++) {
                arrayNameVn[i] = arrayVn[i].getName();
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return arrayNameVn;
    }

    public static synchronized int getNbMappedNodes(String nameVirtualNode,
        String pathDescriptor) {
        ProActiveDescriptor desc;
        VirtualNode vn;
        try {
            desc = ProDeployment.getProactiveDescriptor("file:" +
                    pathDescriptor);
            vn = desc.getVirtualNode(nameVirtualNode);
            return vn.getNbMappedNodes();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @param nameVirtualNode
     * @param pathDescriptor
     * @param arrayIdEngine
     * @return HashMap of deployed Scilab Engines
     */
    public synchronized static HashMap<String, MSEngine> deploy(
        String nameVirtualNode, String pathDescriptor, String[] arrayIdEngine) {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:deploy:" + pathDescriptor);
        }

        ProActiveDescriptor desc;
        VirtualNode vn;
        Node[] nodes;
        MSEngine mSEngine;
        HashMap<String, MSEngine> mapEngine = new HashMap<String, MSEngine>();

        try {
            desc = ProDeployment.getProactiveDescriptor("file:" +
                    pathDescriptor);
            vn = desc.getVirtualNode(nameVirtualNode);
            vn.activate();
            nodes = vn.getNodes();

            int length = (nodes.length > arrayIdEngine.length)
                ? arrayIdEngine.length : nodes.length;

            for (int i = 0; i < length; i++) {
                mSEngine = deploy(arrayIdEngine[i], nodes[i]);
                mapEngine.put(arrayIdEngine[i], mSEngine);
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        return mapEngine;
    }

    /**
     *
     * @param idEngine
     * @param currentNode
     * @return a Scilab Engine deployed on the current node
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private synchronized static MSEngine deploy(String idEngine,
        Node currentNode) throws ActiveObjectCreationException, NodeException {
        Object[] param = new Object[] { idEngine };
        MSEngine mSEngine = (MSEngine) ProActiveObject.newActive(MSEngine.class.getName(),
                param, currentNode);
        mapNode.put(idEngine, currentNode);
        mSEngine.setImmediateServices();
        return mSEngine;
    }

    /**
     *
     * @param idEngine
     * @return a local Scilab Engine
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    public static MSEngine deploy(String idEngine)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("->MSDeployEngine In:deploy");
        }

        Object[] param = new Object[] { idEngine };
        MSEngine mSEngine = (MSEngine) ProActiveObject.newActive(MSEngine.class.getName(),
                param);
        mSEngine.setImmediateServices();
        return mSEngine;
    }

    public static Node getEngineNode(String idEngine) {
        return mapNode.get(idEngine);
    }
}
