package test.interf;

import org.objectweb.proactive.ProActive;

public class TestInterface implements MyInt {

  public TestInterface() {

  }


  public MyInt toto() {
    System.out.println("toto called");
    return null;
  }


  public static void main(String[] args) {
    TestInterface test = null;
    try {
      test = (TestInterface)ProActive.newActive("test.interf.TestInterface", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    test.toto();

  }
}
