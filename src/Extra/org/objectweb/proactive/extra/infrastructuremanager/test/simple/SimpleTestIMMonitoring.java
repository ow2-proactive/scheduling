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
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;


public class SimpleTestIMMonitoring {
    private IMMonitoring imMonitoring;

    //----------------------------------------------------------------------//
    public SimpleTestIMMonitoring(IMMonitoring imMonitoring) {
        this.imMonitoring = imMonitoring;
    }

    //----------------------------------------------------------------------//
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

    /**
     * PAD
     *   |
     *   +-- VirtualNode
     *        |
     *        +-- Hostname
     *             |
     *             +-- VirtualMachine
     *                               |
     *                               +-- Node
     *
     */
    public void printIMNodesByVNodeByPad() {
        System.out.println("printIMNodesByVNodeByPad");
        ArrayList<IMNode> imNodes = imMonitoring.getListAllIMNodes();
        System.out.println("tableOfIMNodes");
        Object[] tableOfIMNodes = imNodes.toArray();
        for (int i = 0; i < tableOfIMNodes.length; i++) {
            System.out.println(i + ". " +
                descriptionIMNode((IMNode) tableOfIMNodes[i]));
        }
        Arrays.sort(tableOfIMNodes, 0, tableOfIMNodes.length,
            new ComparatorIMNode());
        for (int i = 0; i < tableOfIMNodes.length; i++) {
            System.out.println(i + ". " +
                descriptionIMNode((IMNode) tableOfIMNodes[i]));
        }

        printTree(tableOfIMNodes);
    }

    public String descriptionIMNode(IMNode imnode) {
        String mes = "";
        mes += (imnode.getPADName() + " - ");
        mes += (imnode.getVNodeName() + " - ");
        mes += (imnode.getHostName() + " - ");
        mes += (imnode.getDescriptorVMName() + " - ");
        mes += (imnode.getNodeName() + ".\n");
        return mes;
    }

    public void printTree(Object[] o) {
        String toto = "toto";
        if (toto.equals("toto")) {
            System.out.println("toto equals");
        }
        IMNode node = (IMNode) o[0];
        System.out.println("" + node.getPADName());
        System.out.println("\t+--" + node.getVNodeName());
        System.out.println("\t\t+--" + node.getHostName());
        System.out.println("\t\t\t+--" + node.getDescriptorVMName());
        System.out.println("\t\t\t\t+--" + node.getNodeName());
        for (int i = 1; i < o.length; i++) {
            IMNode imnode1 = (IMNode) o[i - 1];
            IMNode imnode2 = (IMNode) o[i];

            if (imnode1.getPADName().equals(imnode2.getPADName())) {
                if (imnode1.getVNodeName().equals(imnode2.getVNodeName())) {
                    if (imnode1.getHostName().equals(imnode2.getHostName())) {
                        if (imnode1.getDescriptorVMName()
                                       .equals(imnode2.getDescriptorVMName())) {
                            System.out.println("\t\t\t\t+--" +
                                imnode2.getNodeName());
                        } else {
                            System.out.println("\t\t\t+--" +
                                imnode2.getDescriptorVMName());
                            System.out.println("\t\t\t\t+--" +
                                imnode2.getNodeName());
                        }
                    } else {
                        System.out.println("\t\t+--" + imnode2.getHostName());
                        System.out.println("\t\t\t+--" +
                            imnode2.getDescriptorVMName());
                        System.out.println("\t\t\t\t+--" +
                            imnode2.getNodeName());
                    }
                } else {
                    System.out.println("\t+--" + imnode2.getVNodeName());
                    System.out.println("\t\t+--" + imnode2.getHostName());
                    System.out.println("\t\t\t+--" +
                        imnode2.getDescriptorVMName());
                    System.out.println("\t\t\t\t+--" + imnode2.getNodeName());
                }
            } else {
                System.out.println("" + imnode2.getPADName());
                System.out.println("\t+--" + imnode2.getVNodeName());
                System.out.println("\t\t+--" + imnode2.getHostName());
                System.out.println("\t\t\t+--" + imnode2.getDescriptorVMName());
                System.out.println("\t\t\t\t+--" + imnode2.getNodeName());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Simple test IMMonitoring");

        try {
            IMMonitoring imMonitoring = IMFactory.getMonitoring();
            System.out.println(imMonitoring.echo());

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
