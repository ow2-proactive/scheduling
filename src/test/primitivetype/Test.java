package test.primitivetype;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

public class Test implements Active {

  public Test() {
  }


  public void echo(int i) {
    System.out.println("I have received " + i);
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java  uniquenode.Test nodeName");
      System.exit(-1);
    }
    Test test = null;

    try {
      test = (Test)ProActive.newActive("test.primitivetype.Test", null, NodeFactory.getNode(args[0]));

    } catch (Exception e) {
      e.printStackTrace();
    }

    test.echo(5);

  }
}
