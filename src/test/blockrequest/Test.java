package test.blockrequest;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

public class Test implements org.objectweb.proactive.RunActive {

  private int counter = 0;


  public Test() {

  }


  public void method1() {
    System.out.println("void method1() called");
  }


  public int method2() {
    System.out.println("int method2() called");
    return counter++;
  }


  public void runActivity(Body body) {
    System.out.println("----- Test: blocking requests");
    body.blockCommunication();
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("----- Test: accepting requests");
    body.acceptCommunication();
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    service.fifoServing();
  }


  public static void main(String[] args) {
    Test test = null;
    try {
      test = (Test)ProActive.newActive("test.blockrequest.Test", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("****** Testing method1");
    for (int i = 0; i < 10; i++) {
      test.method1();
    }

    System.out.println("****** Testing method2");
    for (int i = 0; i < 10; i++) {
      System.out.println("Result is " + test.method2());
    }
  }
}
