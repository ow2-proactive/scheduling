package test.lookupactive;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ProActive;

public class Test implements Active {

  public Test() {

  }


  public void echo() {
    System.out.println("Echo()");

  }


  public static void main(String args[]) {

    if (args.length < 2) {
      System.out.println("usage: java test.lookupactive.Test <hostname> <objectToLookup>");
      System.exit(1);
    }
    try {
      Test t = (Test)ProActive.newActive("test.lookupactive.Test", null);
      System.out.println("Test: registering the object");
      ProActive.register(t, "//localhost/test");
      System.out.println("Test: trying to lookup");
      Test t2 = (Test)ProActive.lookupActive("test.lookupactive.Test", "//" + args[0] + "/test");
      t2.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Test: looking for an object already registered");
    try {
      AutoRegister auto = (AutoRegister)ProActive.lookupActive("test.lookupactive.AutoRegister", args[1]);
      auto.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
