package migration.test;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class RegisteredAgent implements Active, Serializable {

  int etape = 0; // this is to count the jumps we have made so far
  public RegisteredAgent friend;


  public RegisteredAgent() {
    System.out.println("RegisteredAgent constructor");
  }


  public RegisteredAgent(Integer i) {
    System.out.println("RegisteredAgent constructor with parameter");

  }


  //To test Garbage collecting
  protected void finalize() throws Throwable {
    System.out.println("RegisteredAgent: finalize() ");
    super.finalize();
  }


  public void register(String s) {
    try {
      ProActive.register(ProActive.getStubOnThis(), s);
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }


  public void lookupRegistered(String s) {
    try {
      System.out.println("Friend is " + friend);
      this.friend = (RegisteredAgent)ProActive.lookupActive("migration.test.RegisteredAgent", s);
      System.out.println("Friend after is " + friend);
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("Friend after2 is " + friend);
  }


  public void joinFriend(Object a) {
    try {
      ProActive.migrateTo(a);
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
    }
  }


  public RegisteredAgent getFriend() {
    return friend;
  }


  /**
   * Display on standard output the local hostname
   */
  public void echo() {
    System.out.println("RegisteredAgent:Echo()");
  }


  public int echoInt() {
    return (6);
  }


  public static void main(String[] argv) {
    System.out.println("Ready to test lookupActive and register");
    RegisteredAgent a1 = null;
    RegisteredAgent a2 = null;
    try {
      a1 = (RegisteredAgent)ProActive.newActive("migration.test.RegisteredAgent", null);
      a2 = (RegisteredAgent)ProActive.newActive("migration.test.RegisteredAgent", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Now trying register");
    a1.register("rmi://oasis/Agent");

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    a2.lookupRegistered("//oasis/Agent");

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }

    a1 = a2.getFriend();
    System.out.println("Found friend " + a1 + " of class " + a1.getClass().getName());
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    a2.joinFriend(a1);

    // 	a2.lookupRegistered("//zephir/Agent");
    // 	try {
    // 	    Thread.currentThread().sleep(2000);
    // 	} catch (Exception e) {}
    // 	System.out.println("Found friend "+ a2.getFriend());

  }
}
