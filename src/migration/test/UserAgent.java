package migration.test;

import org.objectweb.proactive.ProActive;

//import org.objectweb.proactive.ext.migration.Destination;
import org.objectweb.proactive.ext.migration.MigrationStrategyManager;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.body.migration.Migratable;

public class UserAgent implements org.objectweb.proactive.RunActive, java.io.Serializable {

  int etape = 0; // this is to count the jumps we have made so far
  MigrationStrategyManager migrationStrategyManager;

  public UserAgent() {
    //System.out.println("UserAgent constructor");
  }


  public UserAgent(Integer i) {
    //	System.out.println("UserAgent constructor with parameter");
  }


  public void titi() {
    System.out.println("UserAgent: migrate()");
    try {
      ProActive.migrateTo("zephir/Node1");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("UserAgent: migrate() Over");
  }


  public void titi2() {
    System.out.println("UserAgent: migrate() deuxieme etape");
    try {
      ProActive.migrateTo("arthur/Node1");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("UserAgent: migrate() deuxieme etape Over");
  }


  public void runActivity(org.objectweb.proactive.Body b) {
    if (etape == 0) {
      // Destination r;  
      //  b.migrationStrategy = new MigrationStrategyImpl();
      //   r = new NodeDestination("arthur/Node1", "getUser");
      //   b.migrationStrategy.add(r);       
      //   r = new NodeDestination("zephir/Node1", "getUser");
      //    b.migrationStrategy.add(r);
      //	r = new NodeDestination("oasis/Node1", "getUser");
      //     b.migrationStrategy.add(r);

      etape++;
      migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) b);
      try {
        migrationStrategyManager.startStrategy(b);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * Display on standard output the local hostname
   */
  public void echo() {
    System.out.println("UserAgent:Echo()");
  }


  /**
   * Get some informations about the load of the current host
   * Warning: this only work on Solaris since it calls some unix command
   */
  public void getLoad() {
    try {
      Process p = Runtime.getRuntime().exec("ps");
      p.waitFor();
      String s;
      java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
      while ((s = d.readLine()) != null) {
        System.out.println(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void getUser() {

    System.out.println("UserAgent: Here are the users on the system \n");
    try {

      Process p = Runtime.getRuntime().exec("date");
      p.waitFor();
      String s;
      java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
      while ((s = d.readLine()) != null) {
        System.out.println(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      Process p = Runtime.getRuntime().exec("who");
      p.waitFor();
      String s;
      java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
      while ((s = d.readLine()) != null) {
        System.out.println(s);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public int echoInt() {
    return (6);
  }


  /**
   * Send an echoInt() to the Agent t
   */
  public void echoIntOn(UserAgent t) {
    System.out.println("UserAgent: echoIntOn() Should be synchrone");
    echoInt();
  }
}
