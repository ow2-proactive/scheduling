package migration.test;

import org.objectweb.proactive.core.body.future.FutureList;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public class AgentForFuture implements Serializable {

  int etape = 0; // this is to count the jumps we have made so far
  SimpleAgentForFuture agent;
  Vector waitingFutures = new Vector();
  FutureList futureList;


  public AgentForFuture() {
  }


  public void getFuture() {
    System.out.println("AgentForFuture: Now processing getFuture()");
    //we  create a future objet through this call since the callee is blocked
    waitingFutures.add(agent.createFuture());
  }


  public void getFutureAndAddToFutureList() {
    System.out.println("AgentForFuture: Now processing getFutureAndAddToFutureList()");
    EmptyFuture f = agent.createFuture();
    waitingFutures.add(f);
    this.addToFutureList(f);
  }


  public void displayAllFutures() {
    EmptyFuture temp;

    for (Enumeration e = waitingFutures.elements(); e.hasMoreElements();) {
      temp = (EmptyFuture)e.nextElement();
      System.out.println(temp.getName());
      //   temp.markForMigration();

    }
  }

  //call a method on the other agent to unblock it
  public void unblockOtherAgent() {
    agent.go();
  }


  public String synchroneRequest() {
    System.out.println("AgentForFuture: Now executing synchroneRequest()");
    return ("TOTO");
  }


  public void setSimpleAgent(SimpleAgentForFuture t) {
    agent = t;
  }


  public void moveTo(String t) {
    try {
      ProActive.migrateTo(t);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;
  }


  /**
   * Request its body futurePool and create an empty future list
   */
  public void createFutureList() {
    org.objectweb.proactive.core.body.ActiveBody b = (org.objectweb.proactive.core.body.ActiveBody) ProActive.getBodyOnThis();
    FuturePool futurePool = b.getFuturePool();
    this.futureList = new FutureList(futurePool);
  }


  public void addToFutureList(Object o) {
    this.futureList.add(o);
  }


  public void displayAwaited() {
    if (futureList != null) {
      System.out.println("AgentForFuture: displayAwaited() still waiting " + futureList.countAwaited() + " futures");
    }
  }


  public void displayAllAwaited() {
    if (futureList != null) {
      System.out.println("AgentForFuture: I am waiting for all my futures:  " + futureList.allAwaited());
    }
  }


  public void displayNoneAwaited() {
    if (futureList != null) {
      System.out.println("AgentForFuture: displayNoneAwaited() I don't have any pending future:  " + futureList.noneAwaited());
    }
  }


  public void waitAllFuture() {
    if (futureList != null) {
      System.out.println("AgentForFuture: waiting all futures  ");
      futureList.waitAll();
    }
  }


  public void waitOneFuture() {
    if (futureList != null) {
      System.out.println("AgentForFuture: waiting one future  ");
      futureList.waitOne();
    }
  }



  //call a method on the other agent to unblock it and wait for its replies
  public void unblockOtherAgentAndWaitAll() {
    agent.go();
    this.waitAllFuture();
  }


  public void unblockOtherAgentAndWaitOne() {
    agent.go();
    this.waitOneFuture();
  }
}