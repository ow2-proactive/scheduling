package test.immediateexecution;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

import java.util.Vector;

public class Test implements org.objectweb.proactive.RunActive {

  public Test() {

  }


  public String toString() {
    System.out.println("Test: toString method called");
    return super.toString();
  }


  public void runActivity(Body body) {
    System.out.println("Live() method started");
    System.out.println("Live() won't do anything");
    while (body.isActive()) {
      try {
        Thread.currentThread().sleep(10000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  public static void main(String[] args) {
    Vector v = new Vector();

    Test test = null;
    Test test2 = null;
    try {
      test = (Test)ProActive.newActive("test.immediateexecution.Test", null);
      test2 = (Test)ProActive.newActive("test.immediateexecution.Test", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //System.out.println("The object is " +  test);
    //	System.out.println("Testing equals() ..." + test.equals(test));
    System.out.println("Inserting the active object in the vector");
    v.addElement(test);
    System.out.println("Testing for its presence ..." + v.contains(test));
    System.out.println("Inserting another object");
    v.addElement(test2);
    System.out.println("Comparing the two objects = " + test2.equals(test));
    System.out.println("Testing for its presence ..." + v.contains(test2));
  }
}
