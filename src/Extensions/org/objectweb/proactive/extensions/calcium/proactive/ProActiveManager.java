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
package org.objectweb.proactive.extensions.calcium.proactive;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.extensions.calcium.Skernel;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


public class ProActiveManager extends AbstractProActiveManager {
    public ProActiveManager(Node[] nodes) {
        super(nodes);
    }

    public ProActiveManager(VirtualNode vn) {
        super(vn);
    }

    public ProActiveManager(String descriptorPath, String virtualNodeName) {
        super(descriptorPath, virtualNodeName);
    }

    @Override
    public Skernel boot(Skernel skernel) {
        ActiveObjectSkernel aom = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating Active Object Skernel");
            }
            aom = (ActiveObjectSkernel) ProActive.newActive(ActiveObjectSkernel.class.getName(),
                    new Object[] { skernel });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //copy already inputted tasks
        while (skernel.hasReadyTask()) {
            aom.addReadyTask(skernel.getReadyTask(0));
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating Active Object Interpreters");
            }
            for (int i = 0; i < nodes.length; i++) {
                ActiveObjectInterpreter interp = (ActiveObjectInterpreter) ProActive.newActive(ActiveObjectInterpreter.class.getName(),
                        null, nodes[i]);
                interp.start(aom);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aom;
    }
}
