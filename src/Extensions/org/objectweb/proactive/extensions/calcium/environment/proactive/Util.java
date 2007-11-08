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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;


public class Util {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);

    // LocalNode node= NodeFactory.getDefaultNode();
    static public AOTaskPool createActiveTaskPool(Node frameworkNode)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object TaskPool.");
        }

        AOTaskPool aom = (AOTaskPool) ProActiveObject.newActive(AOTaskPool.class.getName(),
                new Object[] {  }, frameworkNode);

        return aom;
    }

    public static FileServerClientImpl createFileServer(Node frameworkNode)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating File Server Proxy.");
        }

        FileServer fserver = (FileServer) ProActiveObject.newActive(FileServer.class.getName(),
                new Object[] {  }, frameworkNode);
        fserver.initFileServer();

        FileServerClientImpl fserverclient = new FileServerClientImpl(frameworkNode,
                fserver);

        return fserverclient;
    }

    static public AOInterpreterPool createAOInterpreterPool(
        AOTaskPool taskpool, FileServerClientImpl fserver, Node frameworknode,
        Node[] nodes, int times) throws ProActiveException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object Interpreters in nodes.");
        }

        Object[][] params = new Object[nodes.length][2];
        for (int i = 0; i < nodes.length; i++) {
            params[i] = new Object[] { taskpool, fserver };
        }

        AOInterpreter[] ai;
        try {
            ai = (AOInterpreter[]) ProActiveObject.newActiveInParallel(AOInterpreter.class.getName(),
                    new Object[][] {
                        { taskpool, fserver }
                    }, nodes);
        } catch (ClassNotFoundException e) {
            throw new ProActiveException(e);
        }

        AOInterpreterPool interpool = (AOInterpreterPool) ProActiveObject.newActive(AOInterpreterPool.class.getName(),
                new Object[] { Arrays.asList(ai), new Integer(times) },
                frameworknode);

        return interpool;
    }

    public static Node getFrameWorkNode(ProActiveDescriptor pad,
        VariableContract vc) throws NodeException {
        String vnName = vc.getValue("SKELETON_FRAMEWORK_VN");

        return getNode(pad, vnName);
    }

    public static Node[] getInterpreterNodes(ProActiveDescriptor pad,
        VariableContract vc) throws NodeException {
        String vnName = vc.getValue("INTERPRETERS_VN");

        return getNodes(pad, vnName);
    }

    static public Node[] getNodes(ProActiveDescriptor pad,
        String virtualNodeName) throws NodeException {
        VirtualNode vn = pad.getVirtualNode(virtualNodeName);
        vn.activate();

        return vn.getNodes();
    }

    static public Node getNode(ProActiveDescriptor pad, String virtualNodeName)
        throws NodeException {
        VirtualNode vn = pad.getVirtualNode(virtualNodeName);
        vn.activate();

        return vn.getNode();
    }
}
