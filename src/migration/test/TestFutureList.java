package migration.test;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class TestFutureList {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  // BodyForAgent body;


  public TestFutureList() {
    super();
  }


  public static void main(String[] args) {
    SimpleAgentForFuture t = null;
    AgentForFuture tf = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");


    //	System.setSecurityManager(new RMISecurityManager());


    if (args.length < 2) {
      System.err.println("Usage: TestFutureList hostName1/nodeName1 hostName2/nodeName2");
      System.exit(-1);
    }

    System.out.println("This is designed to test the use of the future list");

    try {
      t = (SimpleAgentForFuture)ProActive.newActive("migration.test.SimpleAgentForFuture", null, NodeFactory.getNode(args[0]));
      tf = (AgentForFuture)ProActive.newActive("migration.test.AgentForFuture", null, NodeFactory.getNode(args[1]));
    } catch (Exception e) {
      e.printStackTrace();
    }


    //we pass the reference to our simple agent
    tf.setSimpleAgent(t);

    tf.createFutureList();

    //now we start the future request
    //thus creating futures un tf
    tf.getFutureAndAddToFutureList();
    tf.getFutureAndAddToFutureList();
    tf.getFutureAndAddToFutureList();
    tf.getFutureAndAddToFutureList();

    System.out.println("*** Sleeping for 5 seconds");

    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    tf.displayAwaited();
    tf.displayAllAwaited();
    tf.displayNoneAwaited();

    System.out.println("*** Sleeping for 1 seconds");
    try {
      Thread.currentThread().sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 	tf.moveTo(args[0]);
    // 	tf.moveTo(args[1]);
    System.out.println("*** Asking the reply to the futures");
    //	tf.unblockOtherAgentAndWaitAll();	

    //tf.unblockOtherAgentAndWaitOne();	
    tf.waitAllFuture();
    System.out.println("*** Sleeping for 5 seconds");
    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    tf.displayAwaited();
    tf.displayAllAwaited();
    tf.displayNoneAwaited();
    // 	//now we ask for the display of the futures
    // 	tf.displayAllFutures();	
    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("*** Test over");
  }
}
