package test.migration;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Test implements Active, Serializable {

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


  public void runtimeException() {
    Integer s = null;
    s.toString();
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.migration.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;

    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive("test.migration.Test", null);
      System.out.println("Requesting migration");
      test.migrateTo(args[0]);
      Thread.sleep(5000);
      System.out.println("Calling echo");
      test.echo();
      Thread.sleep(5000);
      System.out.println("Calling runtimeException");
      test.runtimeException();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
