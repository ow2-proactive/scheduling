package test.migrationobject;

import org.objectweb.proactive.core.node.NodeFactory;
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


  public void migrateTo(String url) {
    try {

      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void migrateTo(Object o) {
    try {
      ProActive.migrateTo(o);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void runtimeException() {
    Integer s = null;
    s.toString();
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.migrationobject.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;
    Test test2 = null;

    try {
      System.out.println("Creating object on node " + args[0]);
      test = (Test)ProActive.newActive("test.migrationobject.Test", null, NodeFactory.getNode(args[0]));
      System.out.println("Creating object in local JVM ");
      test2 = (Test)ProActive.newActive("test.migrationobject.Test", null);
      System.out.println("Requesting migration");
      test2.migrateTo(test);
      Thread.sleep(5000);
      System.out.println("Calling echo");
      test2.echo();
      Thread.sleep(5000);
      System.out.println("Calling runtimeException");
      test.runtimeException();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
