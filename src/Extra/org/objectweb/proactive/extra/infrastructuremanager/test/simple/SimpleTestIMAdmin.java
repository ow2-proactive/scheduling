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
package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;


public class SimpleTestIMAdmin {
    //public static String URL_PAD_LOCAL  = "/home/ellendir/ProActive/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
    public static String URL_PAD_LOCAL = "/workspace/ProActive-New2/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
    public static String[] vnodesName = new String[] { "Idefix", "Asterix" };

    public static void main(String[] args) {
        System.out.println("# --oOo-- Simple Test  Admin --oOo-- ");

        try {
            URI uriIM = new URI("rmi://localhost:1099/");
            IMAdmin admin = IMFactory.getAdmin(uriIM);
            // OR
            // IMAdmin admin = IMFactory.getAdmin();
            // to get admin from the local IM
            System.out.println("#[SimpleTestIMAdmin] Echo admin : " +
                admin.echo());

            System.out.println("#[SimpleTestIMAdmin] deployAllVirtualNodes : " +
                URL_PAD_LOCAL);
            admin.deployVirtualNodes(new File(URL_PAD_LOCAL),
                NodeFactory.getDefaultNode(), vnodesName);

            System.out.println("Sleep 12s");
            Thread.sleep(12000);

            HashMap<String, ArrayList<VirtualNode>> deployedVNodesByPad = admin.getDeployedVirtualNodeByPad();

            //System.out.println("hashNext : " + deployedVNodesByPad.keySet().iterator().hasNext());
            String padName = deployedVNodesByPad.keySet().iterator().next();
            System.out.println("padName : " + padName);

            System.out.println("#[SimpleTestIMAdmin] killPAD : vnode Idefix");
            admin.killPAD(padName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("##[TestIMAdmin] END TEST");
    }
}
