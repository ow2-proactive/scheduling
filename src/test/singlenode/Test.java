package test.singlenode;

import org.objectweb.proactive.core.node.NodeFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Test {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java  uniquenode.Test nodeName1 nodeName2");
      System.exit(-1);
    }

    try {
      LocateRegistry.createRegistry(1099);
    } catch (RemoteException e) {
      System.err.println("Warning, cannot create registry");
    }

    System.out.println("Creating first node");
    try {
      NodeFactory.createNode(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Creating second node");
    try {
      NodeFactory.createNode(args[1]);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
