package migration.test;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.migration.Destination;
import org.objectweb.proactive.ext.migration.MigrationStrategyManager;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.body.migration.Migratable;

public class TravelingAgent implements org.objectweb.proactive.RunActive, java.io.Serializable {

  int etape = 0; // this is to count the jumps we have made so far
  int counter = 0;
  MigrationStrategyManager migrationStrategyManager;


  public TravelingAgent() {
    System.out.println("TravelingAgent constructor");
  }


  public TravelingAgent(Integer i) {
    System.out.println("TravelingAgent constructor with parameter");
  }


  public void titi() {
    System.out.println("TravelingAgent: migrate()");
    try {
      ProActive.migrateTo("zephir/Node1");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("TravelingAgent: migrate() Over");
  }


  public void titi2() {
    System.out.println("TravelingAgent: migrate() deuxieme etape");
    try {
      ProActive.migrateTo("arthur/Node1");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("TravelingAgent: migrate() deuxieme etape Over");
  }


  //To test Garbage collecting
  protected void finalize() throws Throwable {
    System.out.println("TravelingAgent: finalize() ");
    super.finalize();
  }


  public void runActivity(org.objectweb.proactive.Body b) {
    if (etape == 0) {
      //Destination r;  
      //                b.migrationStrategy = new MigrationStrategyImpl();
      // r = new NodeDestination("//tuba/Node1", "count");
      //  b.migrationStrategy.add(r);       
      //	r = new NodeDestination("//tuba/Node2", "count");
      //	b.migrationStrategy.add(r);
      //	r = new NodeDestination("//tuba/Node1", "count");
      //	b.migrationStrategy.add(r);
      //	r = new NodeDestination("//tuba/Node2", "count");
      //	b.migrationStrategy.add(r);
      //	r = new NodeDestination("oasis/Node1", "count");
      //	b.migrationStrategy.add(r);
      //	r = new NodeDestination("oasis/Node3", "count");
      //	b.migrationStrategy.add(r);
      //	r = new NodeDestination("oasis/Node2", "count");
      //	b.migrationStrategy.add(r);
      etape++;
      try {
        migrationStrategyManager = new MigrationStrategyManagerImpl((Migratable) b);
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
    System.out.println("TravelingAgent: echo()");
    //	String location;
    //	location = this.getLocation();
    //System.out.println("TravelingAgent: I'm now on host " + location);
  }


  public void count() {
    System.out.println("TravelingAgent: count() number " + counter++);
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


  public int echoInt() {
    return (6);
  }


  public EmptyFuture getFuture() {
    System.out.println("TravelingAgent: getFuture()");
    return new EmptyFuture();

  }
}
