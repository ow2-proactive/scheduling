//package org.objectweb.proactive.examples.talkAgents;
package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class Test1 {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  // BodyForAgent body;


  public Test1() {
    super();
  }


  public static void main(String[] args) throws java.lang.reflect.InvocationTargetException, FileNotFoundException, IOException, NotBoundException, NoSuchMethodException {

    if (args.length < 1) {
      System.out.println("usage: java migration.test.Test1 <node>");
      System.exit(-1);
    }
    TalkativeAgent t = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");


    //	System.setSecurityManager(new RMISecurityManager());

    System.out.println("Testing migration of an Agent");
    System.out.println("Should migrate to " + args[0]);
    System.out.println("****Creating Agent");
    //	TalkativeAgent t = (TalkativeAgent) Javall.newActiveAgentWithoutExceptions("MobileAgents.examples2.TalkativeAgent",null,null);
    try {
      t = (TalkativeAgent)ProActive.newActive("migration.test.TalkativeAgent", null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");
    //	t.echo();

    try {

      t.moveTo(args[0]);
      //	     t.titi();
      //	     t.titi2();
      // 	    t.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

    //System.out.println("FIRST SYNCHRONOUS CALL OVER");
    //Now we try a synchronous call
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    t.echo();
    //System.out.println("SECOND SYNCHRONOUS CALL OVER");
    //t.echoInt();
    // t.echoInt();

  }
}
