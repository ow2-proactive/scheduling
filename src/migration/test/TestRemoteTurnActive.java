package migration.test;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

//This is to test the method ProActive.turnActive() 
//when we want the active object on a remote node

public class TestRemoteTurnActive {

  public static void main(String[] args) {
    Friends f1 = null;
    Friends f2 = null;

    //first create a "normal" object
    f1 = new Friends();
    try {
      f2 = (Friends)ProActive.turnActive(f1, NodeFactory.getNode("oasis/Node1")); 
      //f2 = (Friends) ProActive.turnActive(f1);
    } catch (Exception e) {
      e.printStackTrace();

    }

    System.out.println("Creation of remote Active finished");
    f2.echo();
  }
}
