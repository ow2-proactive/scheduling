package test.samevmmigration;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Test {

  public static void main(String[] args) {
    Node node1 = null;
    Node node2 = null;
    Node remoteNode = null;
    MobileObject mo = null;
    if (args.length < 2) {
      System.err.println("Usage: java  localnode.Test nodeName1 nodeName2");
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
    System.out.println("Creating mobile object");
    System.out.println(args[0] + " is local " + NodeFactory.isNodeLocal(node1));
    try {
      mo = (MobileObject)ProActive.newActive("test.samevmmigration.MobileObject", null, node1);
      mo.migrateTo(node2);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
