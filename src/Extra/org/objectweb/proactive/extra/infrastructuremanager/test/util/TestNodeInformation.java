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
package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import java.rmi.AlreadyBoundException;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.NodeInformation;


public class TestNodeInformation {
    public static void main(String[] args) {
        try {
            Node nodeIM = NodeFactory.createNode("IM");
            NodeInformation nodeInfo = nodeIM.getNodeInformation();
            String mes = "## NodeInformation : \n";
            mes += "+--------------------------------------------------------------------\n";
            mes += ("+--> getCreationProtocolID : " + nodeInfo.getProtocol() +
            "\n");
            mes += ("+--> getDescriptorVMName   : " +
            nodeInfo.getVMInformation().getDescriptorVMName() + "\n");
            mes += ("+--> getHostName           : " +
            nodeInfo.getVMInformation().getHostName() + "\n");
            mes += ("+--> getJobID              : " + nodeInfo.getJobID() +
            "\n");
            mes += ("+--> getName               : " + nodeInfo.getName() +
            "\n");
            mes += ("+--> getProtocol           : " + nodeInfo.getProtocol() +
            "\n");
            mes += ("+--> getURL                : " + nodeInfo.getURL() + "\n");
            mes += "+--------------------------------------------------------------------\n";

            System.out.println(mes);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
