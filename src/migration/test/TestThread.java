package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class TestThread {

  public static void main(String[] args) throws java.lang.reflect.InvocationTargetException, FileNotFoundException, IOException, NotBoundException, NoSuchMethodException {
    AgentForThread t = null;
    SimpleAgent t2 = null;
    SimpleAgent t3 = null;
    SimpleAgent t4 = null;
    SimpleAgent t5 = null;
    SimpleAgent t6 = null;
    //	System.setSecurityManager(new RMISecurityManager());


    if (args.length < 1) {
      System.err.println("Usage: TestThread hostName/nodeName");
      System.exit(-1);
    }

    //This test works as follow
    //we create an AgentForThread which will accept requests
    //but won't serve them until it migrates.
    // To send the requests to this agent, we use SimpleAgents.

    System.out.println("Testing AgentForThread");
    System.out.println("****Creating Agents");
    //	AgentForThread t = (AgentForThread) Javall.newActiveAgentWithoutExceptions("MobileAgents.examples2.AgentForThread",null,null);
    try {
      t = (AgentForThread)ProActive.newActive("migration.test.AgentForThread", null);
      //t2 = (SimpleAgent) ProActive.newActive("migration.test.SimpleAgent",null, new NodeLocator("rmi://oasis/Node2"));
      t2 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
      t3 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
      t4 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
      t5 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
      t6 = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");
    System.out.println("######Now sending synchronous calls");
    // ExplicitBody.DEBUG=  true;
    //	ExplicitBody.TRACING=  true;
    // 	ExplicitBody.EXECUTE=  true;
    //ExplicitBody.EXECUTE=true;
//	RequestQueue.DEBUG=true;

    //	Request.DEBUG = true;
    t2.synchroneRequestOnAgent(t);
    //t2.echoInt();
    t3.synchroneRequestOnAgent(t);
    t4.synchroneRequestOnAgent(t);
    t5.synchroneRequestOnAgent(t);
    t6.synchroneRequestOnAgent(t);
    System.out.println("####### Synchronous calls sent");
    System.out.println("******Now starting migration");
    t.moveTo(args[0]);
    //	t.moveTo("http://tuba.inria.fr/Node2");

    //	t.moveTo("http://oasis.inria.fr/Node1");
    //t.moveTo("http://oasis.inria.fr/Node2");
    try {
      Thread.currentThread().sleep(500);
    } catch (InterruptedException e) {

    }

    //	t2.synchroneRequestOnAgent(t);
    //t2.setThreadAgent(t);
    //	System.out.println("The agent has been set");
    //	System.out.println("The result of the comparison is " + t2.compareReferences(t));
    //t.moveTo("http://tuba.inria.fr/Node1");
    //t.moveTo("http://arthur.inria.fr/Node1");
  }
}
