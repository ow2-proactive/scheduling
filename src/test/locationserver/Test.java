package test.locationserver;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;

public class Test {

  public Test() {
  }


  public void callOther(ObjectWithLocationServer o) {
    o.echo();
  }

  public static void main(String args[]) {
    if (args.length < 1) {
      System.out.println("Usage: java test.locationserver.Test <destination>");
      System.exit(-1);
    }

    System.out.println("== Test: creating an ObjectWithLocationServer");
    ObjectWithLocationServer obj1 = null;
    try {
      obj1 = (ObjectWithLocationServer)ProActive.newActive("test.locationserver.ObjectWithLocationServer", null, 
                                                           LocationServerMetaObjectFactory.newInstance(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }
   
    System.out.println("== Test: looking up for the server");
    LocationServer s = LocationServerFactory.getLocationServer();

    System.out.println("== Test: creating active object Test");
    Test test = null;
    try {
      test = (Test)ProActive.newActive("test.locationserver.Test", null, LocationServerMetaObjectFactory.newInstance(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }
 
    System.out.println("== Test: waiting 5s and calling echo()");
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    test.callOther(obj1);

    System.out.println("== Test: calling migrateTo() " + args[0]);
    obj1.migrateTo(args[0]);
    System.out.println("== Test:waiting 5s");
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("== Test: calling echo()");
    test.callOther(obj1);
  }
}
