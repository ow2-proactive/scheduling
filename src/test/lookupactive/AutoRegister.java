package test.lookupactive;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ProActive;

public class AutoRegister implements Active {

  public AutoRegister() {

  }


  public void echo() {
    System.out.println("Echo()");

  }


  public void register(String r) {
    System.out.println("AutoRegister: registering to " + r);
    try {
      ProActive.register(ProActive.getStubOnThis(), r);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("AutoRegister: registering successful");
  }


  public static void main(String args[]) {
    if (args.length < 1) {
      System.out.println("usage: java test.lookupactive.AutoRegister <registerInfo>");
      System.exit(1);
    }
    try {
      AutoRegister auto = (AutoRegister)ProActive.newActive("test.lookupactive.AutoRegister", null);
      auto.register(args[0]);
      System.out.println("AutoRegister: object registered");
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
