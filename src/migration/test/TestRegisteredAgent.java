package migration.test;

import org.objectweb.proactive.ProActive;

public class TestRegisteredAgent {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;
   
  public TestRegisteredAgent() {
    super();
  }

  public static void main(String[] args) {
    RegisteredAgent a2 = null;
    RegisteredAgent a1 = null;

    try {
      a2 = (RegisteredAgent)ProActive.newActive("migration.test.RegisteredAgent", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }

    a2.lookupRegistered("//oasis/Agent");
    // 	try {
    // 	    Thread.currentThread().sleep(2000);
    // 	} catch (Exception e) {}
    a1 = a2.getFriend();
    System.out.println("Found friend " + a1 + " of class " + a1.getClass().getName());
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    a2.joinFriend(a1);

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    a2.echo();
  }
}
