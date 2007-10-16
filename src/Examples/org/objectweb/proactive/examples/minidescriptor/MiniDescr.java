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
package org.objectweb.proactive.examples.minidescriptor;

import java.io.IOException;

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
 *
 *
 * @author Jerome+Sylvain
 */
public class MiniDescr {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    MiniDescrActive minidesca = null;

    public MiniDescr(String location) {
        VirtualNode virtualnode = null;

        ProActiveDescriptor pad = null;
        logger.info("-+-+-+-+-+-+-+- MiniDescr launched -+-+-+-+-+-+-+-");

        try {
            pad = ProDeployment.getProactiveDescriptor(location);
            virtualnode = pad.getVirtualNode("MiniVN");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        virtualnode.activate();

        try {
            Node[] nodes = virtualnode.getNodes();
            Object[] param = null;

            for (int i = 0; i < nodes.length; i++) {
                MiniDescrActive desc = (MiniDescrActive) ProActiveObject.newActive(MiniDescrActive.class.getName(),
                        param, nodes[i]);
                Message msg = desc.getComputerInfo();
                logger.info("-+-+-+-+-+-+-+- " + msg + " -+-+-+-+-+-+-+-");
            }
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        virtualnode.killAll(false);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("mini descriptor example");
        new MiniDescr("descriptors/examples/minidescriptor.xml");

        System.exit(0);
    }
}
