package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;

public class TestItinary {

  //Main suivant1;
  static org.objectweb.proactive.core.mop.Proxy proxy;

  public TestItinary() {
    super();
  }


  public static void main(String[] args) {
    TravelingAgent t = null;
    Object[] parametres = new Object[1];

    parametres[0] = (Object)new Integer("10");

    System.setSecurityManager(new RMISecurityManager());

    System.out.println("Testing the itinary in the live method of TravelingAgent");
    System.out.println("Should migrate to arthur.inria.fr/Node1 then to arthur.inria.fr/Node2 both on Node1");
    System.out.println("****Creating Agent");

    try {
      t = (TravelingAgent)ProActive.newActive("migration.test.TravelingAgent", null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");

    try {
      Thread.currentThread().sleep(5000);
      // t.getFuture();
    } catch (Exception e) {
    }
    t.getFuture();
    System.out.println("Now sending another call to test the update of location");
    try {
      Thread.currentThread().sleep(5000);
      //t.echo();

    } catch (Exception e) {
    }
    t.getFuture();
    System.out.println("Test over");
    t = null;
    System.gc();
  }
}
