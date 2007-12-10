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
package org.objectweb.proactive.extensions.resourcemanager.test.util;

import java.io.File;
import java.util.Map;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;


public class TestDeployment {
    public static String URL_PAD1 = "/home/ellendir/ProActive/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";

    //public static String URL_PAD2  = "infrastructuremanager/descriptor/3VNodes-4Jvms-10Nodes.xml";
    public static void DeployPAD(String urlPAD) {
        try {
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(urlPAD);
            //pad.activateMappings();
            pad.activateMapping("Asterix");

            System.out.println("pad name : " +
                new File(pad.getUrl()).getName());

            VirtualNode[] vnodes = pad.getVirtualNodes();

            for (int i = 0; i < vnodes.length; i++) {
                //vnodes[i].activate();
                System.out.println("VNode n�" + i + " : " +
                    vnodes[i].getName() + " is deployed : " +
                    vnodes[i].isActivated());
            }

            /*
            VirtualNode         vnode;
            Node[]                         nodes;
            Node                         node;
            NodeInformation nodeInfo;


            System.out.println("-");
            System.out.println("+--> Nombre de VNodes : " + vnodes.length);
            for(int i = 0 ; i < vnodes.length ; i++ ) {
                    vnode = vnodes[i];
                    nodes = vnode.getNodes();
                    System.out.println("--");
                    System.out.println("+----> " + nodes.length + " Nodes appartiennent au VNode " + vnode.getName());

                    for(int j = 0 ; j < nodes.length ; j++ ) {
                            node          = nodes[i];
                            nodeInfo = node.getNodeInformation();

                            String mes = "NodeInformation : \n";
                            mes += "+--------------------------------------------------------------------\n";
                            mes += "+--> getCreationProtocolID : " + nodeInfo.getCreationProtocolID() + "\n";
                            mes += "+--> getDescriptorVMName   : " + nodeInfo.getDescriptorVMName()   + "\n";
                            mes += "+--> getHostName           : " + nodeInfo.getHostName()           + "\n";
                            mes += "+--> getJobID              : " + nodeInfo.getJobID()              + "\n";
                            mes += "+--> getName               : " + nodeInfo.getName()               + "\n";
                            mes += "+--> getProtocol           : " + nodeInfo.getProtocol()           + "\n";
                            mes += "+--> getURL                : " + nodeInfo.getURL()                + "\n";
                            mes += "+--------------------------------------------------------------------\n";

                            System.out.println(mes);
                    }
            }
            */
            System.out.println("Map map = pad.getVirtualNodeMapping();");

            Map map = pad.getProActiveDescriptorInternal()
                         .getVirtualNodeMapping();
            for (Object vnodeObject : map.keySet()) {
                String vnodeName = (String) vnodeObject;
                System.out.print(vnodeName + " : ");
                VirtualNode mappingVNode = (VirtualNode) map.get(vnodeName);
                System.out.println(mappingVNode.getName());
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Descripteur de d�ploimenent n�1 : ");
        DeployPAD(URL_PAD1);

        /*
        System.out.println("Descripteur de d�ploimenent n�2 : ");
        DeployPAD(URL_PAD1);
        */
    }
}
