package test.samevmmigration;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;

public class MobileObject {

  public MobileObject() {

  }


  public void migrateTo(Node node) {
    System.out.println("MobileObject: migrating");
    try {
      ProActive.migrateTo(node);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
