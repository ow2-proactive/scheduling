package migration.test;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.mop.StubObject;

import java.io.IOException;
import java.io.Serializable;

public class SimpleAgentForFuture implements org.objectweb.proactive.RunActive, Serializable {

  AgentForThread agent = null; //to test the equality between stubs

  public SimpleAgentForFuture() {
  }


  public void runActivity(org.objectweb.proactive.Body b) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(b);
    System.out.println("SimpleAgentForFuture: Now calling custom live");
    //first we wait to allow the caller to migrate with its futures
    service.blockingRemoveOldest("go");
    System.out.println("SimpleAgentForFuture: Now in service");
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    service.fifoServing();
  }


  /**
   * We send a synchrone request to agent a
   */

  public void synchroneRequestOnAgent(AgentForThread a) {
    String t;
    //	System.out.println("SimpleAgentForFuture: now calling synchrone request");
    t = a.synchroneRequest();
    // System.out.println("THE RESULT OF THE SYNCHRONE CALL IS " +a.synchroneRequest());
    if (t == null)
      System.out.println("SIMPLEAGENT ERREUR RETURN NULL@!!!!!!");
    else
      System.out.println("SimpleAgentForFuture: We got the following result: " + t);
  }

  //unblock the live method
  public void go() {
    System.out.println("SimpleAgentForFuture: go()");
  }


  public int echoInt() {
    System.out.println("TOTO");
    return 1;
  }


  public void MoveTo(String t) {
    try {
      ProActive.migrateTo(t);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;
  }


  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    //System.out.println("OOOOOOOOOOOOOOOOOOOOOOOO Serializing");
    out.defaultWriteObject();
    //	System.out.println(">>>ExplicitBody: Serialization ");
  }


  public void setThreadAgent(AgentForThread a) {
    agent = a;
  }


  public EmptyFuture createFuture() {
    System.out.println("SimpleAgentForFuture: createFuture()");

    return new EmptyFuture();

  }


  public boolean compareReferences(AgentForThread a) {
    boolean b;
    BodyProxy a1 = ((BodyProxy)((StubObject)a).getProxy());
    BodyProxy a2 = ((BodyProxy)((StubObject)agent).getProxy());

    System.out.println("A1 = " + a1);
    System.out.println("A2 = " + a2);
    b = a1.equals(a2);
    //return((agent.equals(a)));
    return b;
  }
}
