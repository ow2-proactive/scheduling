package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

//import MobileAgents.*;

public class TestFutureMigration {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  // BodyForAgent body;


  public TestFutureMigration() {
    super();
  }


  public static void main(String[] args) {
    SimpleAgentForFuture t = null;
    AgentForFuture tf = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");
	

    //	System.setSecurityManager(new RMISecurityManager());


    if (args.length < 2) {
      System.err.println("Usage: TestFutureMigration hostName1/nodeName1 hostName2/nodeName2");
      System.exit(-1);
    }

    System.out.println("This is designed to test the migration of an agent with its futures");
    System.out.println("****Creating Agent");

    try {
      t = (SimpleAgentForFuture)ProActive.newActive("migration.test.SimpleAgentForFuture", null);
      tf = (AgentForFuture)ProActive.newActive("migration.test.AgentForFuture", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");
    //we pass the reference to our simple agent
    tf.setSimpleAgent(t);

    //now we start the future request
    //thus creating futures un tf
    //	tf.getFuture();
    //	tf.getFuture();
    //tf.getFuture();
    tf.getFuture();

    try {
      Thread.sleep(2000);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("TestFutureMigration: Calling moveTo() " + args[0]);
    tf.moveTo(args[0]);
    try {
      Thread.sleep(2000);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("TestFutureMigration: Calling moveTo() " + args[1]);
    tf.moveTo(args[1]);
    try {
      Thread.sleep(5000);

    } catch (Exception e) {
      e.printStackTrace();
    }
    tf.unblockOtherAgent();
    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //now we ask for the display of the futures
    tf.displayAllFutures();
    System.out.println("Test over");
  }
}
