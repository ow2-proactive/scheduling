package test.looplocationserver;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;

public class Test {

  public Test() {}

  public void callOther(ObjectWithLocationServer o) {
    o.echo();
  }

  public static void main(String args[]) {
    if (args.length < 2) {
      System.out.println("Usage: " + Test.class.getName() + "<locationServer> <destination1> ... <destinationN>");
      System.exit(-1);
    }

    System.out.println("== Test: creating an ObjectWithLocationServer");
    ObjectWithLocationServer obj1 = null;
    Object[] param = new Object[1];
    String[] stringParam = new String[args.length - 1];
    System.arraycopy(args, 1, stringParam, 0, args.length - 1);
    System.out.println("stringParam= " + stringParam);

    param[0] = stringParam;

    try {
      obj1 = (ObjectWithLocationServer) ProActive.newActive(Test.class.getName(), param, null, null, LocationServerMetaObjectFactory.newInstance());
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("== Test: creating active object Test");

    Test test = null;
    try {
      test = (Test) ProActive.newActive(Test.class.getName(), null, null, null, LocationServerMetaObjectFactory.newInstance());
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