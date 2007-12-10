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
package org.objectweb.proactive.extensions.resourcemanager.test.simple;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.frontend.NodeSet;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMConnection;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMUser;


public class SimpleTestRMUser {
    public static void afficheNodeInfo(Node node) {
        if (node != null) {
            NodeInformation nodeInfo = node.getNodeInformation();
            String mes = "#[SimpleTestIMUser] NodeInformation : \n";
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
        } else {
            System.out.println("##[TestIMUser] Aucun node disponible");
        }
    }

    public static void main(String[] args)
        throws ActiveObjectCreationException, IOException {
        System.out.println("# --oOo-- Simple Test User --oOo-- ");

        try {
            String url;
            if (args.length > 0) {
                url = args[0];
            } else {
                url = "rmi://localhost:1099/" +
                    RMConstants.NAME_ACTIVE_OBJECT_RMUSER;
            }

            RMUser user = RMConnection.connectAsUser(url);
            System.out.println("#[SimpleTestIMUser] Echo user : " +
                user.echo());

            int nbAskedNodes = 2;
            System.out.println("#[SimpleTestIMUser] User ask to the RM " +
                nbAskedNodes + " Nodes");

            NodeSet nodes = user.getAtMostNodes(new IntWrapper(nbAskedNodes),
                    null);

            for (Node aNode : nodes) {
                afficheNodeInfo(aNode);
            }

            // FREE NODE(S)
            user.freeNodes(nodes);
        } catch (Exception e) {
            System.out.println("##[TestIMUser-catch] Pas de node dispo");
        }

        System.out.println("##[SimpleTestIMUser] END TEST");
    }
}
