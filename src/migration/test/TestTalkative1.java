//package org.objectweb.proactive.examples.talkAgents;
package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class TestTalkative1 {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  // BodyForAgent body;


  public TestTalkative1() {
    super();
  }


  public static void main(String[] args) throws java.lang.reflect.InvocationTargetException, FileNotFoundException, IOException, NotBoundException, NoSuchMethodException {
    TalkativeAgent t = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");

    if (args.length < 1) {
      System.err.println("Usage: TestFriends hostName/nodeName");
      System.exit(-1);
    }

    System.out.println("Testing migration of an Agent");
    System.out.println("****Creating Agent");
    try {
      t = (TalkativeAgent)ProActive.newActive("migration.test.TalkativeAgent", null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");

    t.moveTo(args[0]);
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    t.echo();

  }
}
