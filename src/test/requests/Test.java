package test.requests;

import org.objectweb.proactive.ProActive;

public class Test {

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


  public Object method3() {

    System.out.println("Object method3() called");
    return new Object();
  }


  public static void main(String[] args) {
    Test test = null;
    try {
      test = (Test)ProActive.newActive("test.requests.Test", null);
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
    System.out.println("****** Testing method3");
    for (int i = 0; i < 10; i++) {
      test.method3();
    }
  }
}
