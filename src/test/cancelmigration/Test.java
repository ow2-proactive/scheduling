package test.cancelmigration;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Test implements Serializable {

  public Test() {
    System.out.println("Test constructor");
  }


  public Test(Integer i) {
    System.out.println("Test constructor with parameter");
  }


  public void echo() {
    System.out.println("Hello, I am here");
  }


  public void migrateTo(Node node) {
    try {
      ProActive.migrateTo(node);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.cancelmigration.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;

    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive("test.cancelmigration.Test", null);
      System.out.println("Locating node");
      Node node = null;
      System.out.println("Requesting migration");
      test.migrateTo(null);
      System.out.println("Calling echo");
      test.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
