package test.constructorexception;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ProActive;

public class Test implements Active {

  public Test() throws Exception, CloneNotSupportedException {
    System.out.println("In constructor of Test");
  }


  public static void main(String[] args) {
    try {
      // Creates an active instance of class Test
      Test t = (Test)ProActive.newActive("test.constructorexception.Test", null);
      System.out.println("Creation of active objects succeeded.");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return;
  }
}
