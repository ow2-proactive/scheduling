package migration.test;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;

import java.net.InetAddress;

public class PingPong {

  public static void main(String[] args) {

    Node node1 = null;
    Node node2 = null;
    SimpleAgent agent = null;
    //PingPong p = new PingPong();
    try {
      InetAddress localhost = InetAddress.getLocalHost();
      System.out.println("creating node1");
      node1 = NodeFactory.createNode("//localhost/Node1");
      System.out.println("creating node2");
      node2 = NodeFactory.createNode("//localhost/Node2");
      System.out.println("Now creating the agent");

      agent = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);

      System.out.println("Now starting test");

      for (int i = 0; i < 10; i++) {
        agent.moveTo("//localhost/Node1");
        //Thread.currentThread().sleep(100);
        agent.moveTo("//localhost/Node2");
        //Thread.currentThread().sleep(100);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
