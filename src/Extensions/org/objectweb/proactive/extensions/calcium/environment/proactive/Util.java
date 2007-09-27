/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.environment.proactive;

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


public class Util {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);

    //LocalNode node= NodeFactory.getDefaultNode();
    static public ActiveTaskPool createActiveTaskPool(Node node)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object TaskPool.");
        }

        ActiveTaskPool aom = (ActiveTaskPool) ProActiveObject.newActive(ActiveTaskPool.class.getName(),
                new Object[] {  }, node);

        return aom;
    }

    static public ActiveInterpreterPool createActiveInterpreterPool(Node node)
        throws ActiveObjectCreationException, NodeException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Interpreter Pool.");
        }

        ActiveInterpreterPool aip = (ActiveInterpreterPool) ProActiveObject.newActive(ActiveInterpreterPool.class.getName(),
                new Object[] {  }, node);

        return aip;
    }

    static public AOInterpreter[] createAOinterpreter(Node[] nodes)
        throws ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Active Object Interpreters in nodes.");
        }

        Object[][] params = new Object[nodes.length][0];

        AOInterpreter[] aip = (AOInterpreter[]) ProActiveObject.newActiveInParallel(AOInterpreter.class.getName(),
                params, nodes);

        return aip;
    }

    static public Node[] getNodes(String descriptorPath, String virtualNodeName)
        throws ProActiveException {
        ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(descriptorPath);

        return getNodes(pad, virtualNodeName);
    }

    static public Node[] getNodes(ProActiveDescriptor pad,
        String virtualNodeName) throws NodeException {
        VirtualNode vn = pad.getVirtualNode(virtualNodeName);

        return getNodes(vn);
    }

    static public Node[] getNodes(VirtualNode vn) throws NodeException {
        vn.activate();

        return vn.getNodes();
    }
}
