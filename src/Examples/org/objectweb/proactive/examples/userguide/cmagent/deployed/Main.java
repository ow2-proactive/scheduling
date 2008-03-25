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

//snippet-start cma_deploy_full
package org.objectweb.proactive.examples.userguide.cmagent.deployed;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
    //snippet-start cma_deploy_method
    //deployment method
    private static VirtualNode deploy(String descriptor) {
        try {
            //TODO 1. Create object representation of the deployment file
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(descriptor);

            //TODO 2. Activate all Virtual Nodes
            pad.activateMappings();

            //TODO 3. Get the first Virtual Node specified in the descriptor file
            VirtualNode vn = pad.getVirtualNodes()[0];

            //TODO 4. Return the virtual node
            return vn;
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ProActiveException proExcep) {
            System.err.println(proExcep.getMessage());
        }
        return null;
    }

    //snippet-end cma_deploy_method
    public static void main(String args[]) {
        try {
            //TODO 5. Get the virtual node through the deploy method
            VirtualNode vn = deploy(args[0]);
            //snippet-start cma_deploy_object
            //TODO 6. Create the active object using a node on the virtual node
            CMAgentInitialized ao = (CMAgentInitialized) PAActiveObject.newActive(CMAgentInitialized.class
                    .getName(), new Object[] {}, vn.getNode());
            //snippet-end cma_deploy_object
            //TODO 7. Get the current state from the active object
            String currentState = ao.getCurrentState().toString();

            //TODO 8. Print the state
            System.out.println(currentState);

            //TODO 9. Stop the active object
            PAActiveObject.terminateActiveObject(ao, false);

            //TODO 10. Stop the virtual node
            vn.killAll(true);
            PALifeCycle.exitSuccess();
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        }
    }
}
//snippet-end cma_deploy_full