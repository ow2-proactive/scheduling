package test.listener;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

public class Test2 {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java test.listener.Test2 <nodeName>");
      System.exit(-1);
    }

    Test t = null;
    DummyObject t2 = null;
    try {
      t = (Test)ProActive.newActive("test.listener.Test", null, NodeFactory.getNode(args[0]));
      t2 = (DummyObject)ProActive.newActive("test.listener.DummyObject", null);
      t.setOther(t2);
      //t.toto();
      //t.getObject();

      t.callOther();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
