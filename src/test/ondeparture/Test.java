package test.ondeparture;

import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Test implements Serializable {

  private org.objectweb.proactive.ext.migration.MigrationStrategyManager myStrategyManager;

  public Test() {
    System.out.println("Test constructor");
  }


  public Test(Integer i) {
    System.out.println("Test constructor with parameter");
  }


  public void echo() {
    System.out.println("----> Hello, I am here");
  }


  public void leaving() {
    System.out.println("---> I am leaving");
  }


  public void arrived() {
    System.out.println("---> I have arrived");
  }


  public void migrateTo(String url) {
    try {
      org.objectweb.proactive.core.body.migration.Migratable migratable = (org.objectweb.proactive.core.body.migration.Migratable) ProActive.getBodyOnThis();
      myStrategyManager = new org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl(migratable);
      myStrategyManager.onArrival("arrived");
      myStrategyManager.onDeparture("leaving");
      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.ondeparture.Test <nodeName>");
      System.exit(1);
    }

    Test test = null;

    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive("test.ondeparture.Test", null);
      System.out.println("Requesting migration");
      test.migrateTo(args[0]);
      Thread.sleep(5000);
      System.out.println("Calling echo");
      test.echo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
