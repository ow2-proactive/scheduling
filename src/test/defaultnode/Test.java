package test.defaultnode;

import org.objectweb.proactive.ProActive;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Test implements Serializable {

  public static void main(String[] args) {
    // 	if (args.length<2)
    // 	    {
    // 		System.err.println("Usage: java  defaultnode.Test nodeName1 nodeName2");
    // 		System.exit(-1);
    // 	    }	

    try {
      LocateRegistry.createRegistry(1099);
    } catch (RemoteException e) {
      System.err.println("Warning, cannot create registry");
    }

    System.out.println("Creating Object");
    try {
      Test t = (Test)ProActive.newActive("test.defaultnode.Test", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
