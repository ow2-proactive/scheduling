package test.remotecreation;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Test implements Serializable {

  public Test() {

  }


  public void echo() {
    System.out.println("Echo()");

  }


  public static void main(String args[]) {

    if (args.length < 1) {
      System.out.println("usage: java test.lookupactive.Test <nodename>");
      System.exit(1);
    }

    try {
      System.out.println("Test: creating object on the remote node");
      Test t = (Test)ProActive.newActive("test.remotecreation.Test", null, NodeFactory.getNode(args[0]));
      t.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
