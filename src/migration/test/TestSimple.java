package migration.test;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

public class TestSimple {

  public static void main(String[] args) {
    if (!(args.length > 0)) {
      System.out.println("Usage: java migration.test.TestSimple hostname/NodeName ");
      System.exit(-1);
    }
    SimpleAgent t = null;
    SimpleAgent t2 = null;
    try {
      t = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
      t2 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null, NodeFactory.getNode(args[0]));
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;
  
    //t.MoveTo("http://zephir.inria.fr/Node1");
    t.moveTo(args[0]);
    //	t.echoInt();

    System.out.println("The Active Object is now on host " + t.whereAreYou());
    //t.echo();
  }
}
