package test.itinerary;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.ext.migration.MigrationStrategyImpl;
import org.objectweb.proactive.ext.migration.MigrationStrategy;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.ext.migration.MigrationStrategyManager;
import org.objectweb.proactive.ProActive;

import java.io.Serializable;

public class Test implements org.objectweb.proactive.RunActive, Serializable {

  private MigrationStrategyManager migrationStrategyManager;
  private MigrationStrategy migrationStrategy;
  private int etape = 0;


  public Test() {
    System.out.println("Test constructor");
  }


  public Test(String[] nodes) {
    System.out.println("Test constructor with " + nodes.length + " destinations");
    migrationStrategy = new MigrationStrategyImpl();
    int i;
    for (i = 0; i < nodes.length; i++) {
      migrationStrategy.add(nodes[i], "echo");
    }
  }
  

  public void echo() {
    System.out.println("Hello, I am here");
  }


/*
  public void migrateTo(String url) {
    try {
      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
*/

  public void runActivity(Body body) {
    if (etape == 0) {
      try {
        migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) body);
        migrationStrategyManager.setMigrationStrategy(this.migrationStrategy);
        migrationStrategyManager.startStrategy(body);
      } catch (Exception e) {
        e.printStackTrace();
      }
      etape++;
    }
    body.fifoPolicy();
  }


  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java test.migrationStrategy.Test <nodeName> ... <nodeName>");
      System.exit(1);
    }

    Test test = null;
    Object[] arg = new Object[1];
    arg[0] = args;
    try {
      System.out.println("Creating object");
      test = (Test)ProActive.newActive("test.migrationStrategy.Test", arg);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
