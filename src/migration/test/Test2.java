//package org.objectweb.proactive.examples.talkAgents;
package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;

public class Test2 {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  // BodyForAgent body;


  public Test2() {
    super();
  }


  public static void main(String[] args) throws java.lang.reflect.InvocationTargetException, FileNotFoundException, IOException, NotBoundException, NoSuchMethodException {
    TalkativeAgent t = null;
    TalkativeAgent t2 = null;
    TalkativeAgent t3 = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");

    System.setSecurityManager(new RMISecurityManager());

    System.out.println("Testing migration of an Agent");
    System.out.println("Should migrate to zephir.inria.fr on Node1");
    System.out.println("****Creating Agent");
    //	TalkativeAgent t = (TalkativeAgent) Javall.newActiveAgentWithoutExceptions("migration.test.TalkativeAgent",null,null);
    try {
      t = (TalkativeAgent)ProActive.newActive("migration.test.TalkativeAgent", null);
      t2 = (TalkativeAgent)ProActive.newActive("migration.test.TalkativeAgent", null);
      t3 = (TalkativeAgent)ProActive.newActive("migration.test.TalkativeAgent", null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");
    //	t.echo();

    try {

      t.titi();
      // 	    t.titi2();
      // 	    t.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("FIRST SYNCHRONOUS CALL OVER");
    //Now we send two asynchronous call
    t2.echoIntOn(t);
    t3.echoIntOn(t);

    System.out.println("SECOND SYNCHRONOUS CALL OVER");
    //t.echoInt();
    // t.echoInt();
  }
}
