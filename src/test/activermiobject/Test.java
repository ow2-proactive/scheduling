package test.activermiobject;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Test extends UnicastRemoteObject implements RemoteTest, Active {

  public Test() throws RemoteException {

  }


  public void echo() throws RemoteException {
    System.out.println("Echo()");
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.activermiobject.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;

    try {
      System.out.println("Creating object");
      test = new Test();
      System.out.println("Turning it active");
      ProActive.turnActive(test, NodeFactory.getNode(args[0]));
      Thread.sleep(5000);
      //     System.out.println("Calling echo");
      //     test.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }


  public String getAttachUrl() {
    return "";
  }
}
