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

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;


public class SimpleTestIMMonitoring {
    private IMMonitoring imMonitoring;

    //----------------------------------------------------------------------//
    public SimpleTestIMMonitoring(IMMonitoring imMonitoring) {
        this.imMonitoring = imMonitoring;
    }

    //----------------------------------------------------------------------//
    public String descriptionIMNode(IMNode imnode) {
        String mes = "";
        mes += (imnode.getPADName() + " - ");
        mes += (imnode.getVNodeName() + " - ");
        mes += (imnode.getHostName() + " - ");
        mes += (imnode.getDescriptorVMName() + " - ");
        mes += (imnode.getNodeURL() + ".\n");
        return mes;
    }

    /*
     * IMNode's ToString :
     *
     *  | Name of this Node  :  Asterix-1623542303
     *  +-----------------------------------------------+
     *  | Node is free ?    : true
     *  | Name of PAD               : 3VNodes-3Jvms-10Nodes19412.xml
     *  | VNode                     : Asterix
     *  | Host                      : r5p6
     *  | Name of the VM    : Jvm1
     *  +-----------------------------------------------+
    *
     */
    public void printAllIMNodes() {
        System.out.println("printAllIMNodes");
        ArrayList<IMNode> imNodes = imMonitoring.getListAllIMNodes();
        for (IMNode imNode : imNodes) {
            System.out.println(imNode);
        }
    }

    public void printDeployedVNodes() {
        System.out.println("printDeployedVNodes");
        HashMap<String, ArrayList<VirtualNode>> deployedVNodesByPad = imMonitoring.getDeployedVirtualNodeByPad();
        for (String padName : deployedVNodesByPad.keySet()) {
            System.out.println("padName : " + padName);
            ArrayList<VirtualNode> deployedVnodes = deployedVNodesByPad.get(padName);
            System.out.println("Number of deployed vn : " +
                deployedVnodes.size());
            for (VirtualNode vn : deployedVnodes) {
                System.out.println("Name of deployed vnode : " + vn.getName());
            }
        }
    }

    /*
     * PAD
     *           +-- VirtualNode
     *                        +-- Hostname
     *                                +-- VirtualMachine
     *                                                  +-- Node                                    state
     *
     * state : {"free","busy","down"}
     *
     *
     * example :
     *
     * 3VNodes-3Jvms-10Nodes19412.xml
    *      +-- Asterix
    *              +-- r5p6
    *                     +-- Jvm1
    *                             +-- Asterix-1623542303        busy
    *                             +-- Asterix-951276804        free
    *                             +-- Asterix390993068                busy
    *     +-- Idefix
    *              +-- r5p6
    *                     +-- Jvm3
    *                             +-- Idefix-1717183521        down
    *                             +-- Idefix-347200095                free
    *                             +-- Idefix1522374025                free
    *                             +-- Idefix1823860328                busy
    *                             +-- Idefix485990567                free
    *
     * For sorting a list or a table see :
     *                 Arrays.sort(Object[], Comparator)
     * or
     *                 Collections.sort(List, Comparator)
     */
    @SuppressWarnings("unchecked")
    public void printIMNodesByVNodeByPad() {
        ArrayList<IMNode> imNodes = imMonitoring.getListAllIMNodes();
        Object[] tableOfIMNodes = imNodes.toArray();
        Arrays.sort(tableOfIMNodes);
        /*
        for(int i = 0 ; i < tableOfIMNodes.length ; i ++ ) {
                System.out.println(i + ". " + descriptionIMNode((IMNode)tableOfIMNodes[i]));
        }*/
        System.out.println("printTree :  ");
        System.out.println("-----------\n");

        IMNode imnode = (IMNode) tableOfIMNodes[0];

        System.out.println(" " + imnode.getPADName());
        System.out.println("\t+-- " + imnode.getVNodeName());
        System.out.println("\t\t+-- " + imnode.getHostName());
        System.out.println("\t\t\t+-- " + imnode.getDescriptorVMName());
        System.out.print("\t\t\t\t+-- " + imnode.getNodeURL());
        try {
            if (imnode.isFree()) {
                System.out.println(" \tfree");
            } else {
                System.out.println(" \tbusy");
            }
        } catch (NodeException e) {
            System.out.println(" \tdown");
        }

        boolean change = false;
        for (int i = 1; i < tableOfIMNodes.length; i++) {
            IMNode imnode1 = (IMNode) tableOfIMNodes[i - 1];
            IMNode imnode2 = (IMNode) tableOfIMNodes[i];
            change = false;

            if (!imnode1.getPADName().equals(imnode2.getPADName())) {
                System.out.println(" " + imnode2.getPADName());
                change = true;
            }
            if (change |
                    !imnode1.getVNodeName().equals(imnode2.getVNodeName())) {
                System.out.println("\t+-- " + imnode2.getVNodeName());
                change = true;
            }
            if (change | !imnode1.getHostName().equals(imnode2.getHostName())) {
                System.out.println("\t\t+-- " + imnode2.getHostName());
                change = true;
            }
            if (change |
                    !imnode1.getDescriptorVMName()
                                .equals(imnode2.getDescriptorVMName())) {
                System.out.println("\t\t\t+-- " +
                    imnode2.getDescriptorVMName());
                change = true;
            }
            System.out.print("\t\t\t\t+-- " + imnode2.getNodeURL());
            try {
                if (imnode2.isFree()) {
                    System.out.println(" \tfree");
                } else {
                    System.out.println(" \tbusy");
                }
            } catch (NodeException e) {
                System.out.println(" \tdown");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("# --oOo-- Simple Test  Monitoring --oOo-- ");

        try {
            IMMonitoring imMonitoring = IMFactory.getMonitoring();
            System.out.println("#[SimpleTestIMMonitoring] Echo monitoring : " +
                imMonitoring.echo());

            SimpleTestIMMonitoring test = new SimpleTestIMMonitoring(imMonitoring);

            test.printAllIMNodes();
            test.printDeployedVNodes();
            test.printIMNodesByVNodeByPad();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
