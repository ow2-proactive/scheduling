package test.nodefactory;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;

public class TestFactory {

  public static void main(String args[]) throws NodeException {

if (args.length < 1) 
    {
	System.err.println("Usage: java " + TestFactory.class.getName() + " node" );
	System.exit(-1);
    }

    try {
      NodeFactory.createNode("rmi://localhost/Node1");
    } catch (Exception e) {
    }
    System.out.println("Trying to find the node");

    Node node = NodeFactory.getNode("//localhost/Node1");
    //System.out.println( "The node is " + NodeFactory.getNode("//tuba/Node1"));

    System.out.println("TestFactory: Creating agent");
    // 	try {
    // 	    SimpleAgent agent = (SimpleAgent) ProActive.newActive("test.nodefactory.SimpleAgent", null);
    // 	    System.out.println("TestFactory: trying migration");
    // 	    agent.migrateTo("//tuba/Node2");
    // 	    agent.echo();
    // 	} catch (Exception e) {e.printStackTrace();}
    System.out.println("Trying remote creation on node //localhost/Node2");

    try {
      SimpleAgent agent = (SimpleAgent)ProActive.newActive("test.nodefactory.SimpleAgent", null, NodeFactory.getNode(args[0]));
      agent.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Waiting 5 seconds, then try to create a new Node");
    System.out.println("Should throw a NodeException");
    try {
      NodeFactory.createNode(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
