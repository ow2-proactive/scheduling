package test.localnode;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.Node;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Test {

  public static void main(String[] args) {
    Node node1 = null;
    Node node2 = null;
    Node remoteNode = null;
    if (args.length < 3) {
      System.err.println("Usage: java  localnode.Test nodeName1 nodeName2 remoteNode");
      System.exit(-1);
    }

    try {
      LocateRegistry.createRegistry(1099);
    } catch (RemoteException e) {
      System.err.println("Warning, cannot create registry");
    }

    System.out.println("Creating first node");
    try {
      node1 = NodeFactory.createNode(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Creating second node");
    try {
      node2 = NodeFactory.createNode(args[1]);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Is node 1 local? " + NodeFactory.isNodeLocal(node1));
    System.out.println("Is node 2 local? " + NodeFactory.isNodeLocal(node2));
    System.out.println("Getting ref on remoteNode");
    try {
      remoteNode = NodeFactory.getNode(args[2]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Is remote node local? " + NodeFactory.isNodeLocal(remoteNode));

  }
}
