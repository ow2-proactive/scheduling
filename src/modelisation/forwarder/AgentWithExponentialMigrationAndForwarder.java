package modelisation.forwarder;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import modelisation.statistics.ExponentialLaw;

public class AgentWithExponentialMigrationAndForwarder implements org.objectweb.proactive.RunActive, java.io.Serializable {

  protected ExponentialLaw expo;
  protected Node[] nodes;
  protected int index;
  protected boolean start = false;
  protected long lifeTime;
  protected long startTime;

  private int count;
  private final int MAX = 20;

  public AgentWithExponentialMigrationAndForwarder() {}

  public AgentWithExponentialMigrationAndForwarder(Double nu, Node[] array, Long lifeTime) throws IllegalArgumentException {
    this.expo = new ExponentialLaw(nu.doubleValue());
    nodes = array;
    //   System.out.println("AgentWithExponentialMigrationAndForwarder: array contains " + array.length);
    //this.initialize(s);
    index = 0;
    this.lifeTime = lifeTime.longValue();
  }

  public void start() {
    this.start = true;
    this.startTime = System.currentTimeMillis();
  }

  //   public boolean stop() {
  //     if (startTime == 0)
  //       return false;
  //     return ((System.currentTimeMillis() - startTime) > lifeTime);
  //   }

  //   public void migrateTo(String url) {
  //     try {
  //       ProActive.migrateTo(url);
  //     } catch (Exception e) {
  //       e.printStackTrace();
  //     }
  //   }

  public void runActivity(Body body) {
    System.out.println("live started");
    if (checkTerminate(body)) {
      body.terminate();
    }

    while (body.isActive()) {
      while (!start) {
        body.serve(body.getRequestQueue().blockingRemoveOldest());
      }

      if (++index > nodes.length)
        index = 1;

      double time = expo.next();
      // this.checkTerminate(body);
      System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndForwarder: waiting " + (time * 1000) + " before migration");
      try {
        Thread.sleep((int) (time * 1000));
        //we empty the requestQueue

        count += body.getRequestQueue().size();
        body.getRequestQueue().clear();
        //	this.checkTerminate(body);
        //		System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndForwarder: migrating to "
        //	   + nodes[index - 1].getName());
        //	count++;
        ProActive.migrateTo(nodes[index - 1]);
        //System.out.println("Migration done");

      } catch (Exception e) {
        e.printStackTrace();
        this.count = MAX + 1;
      }
    }
    //    if (this.stop()) {
    //       System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndForwarder: stoping...");
    //       System.exit(0);
    //     }

  }

  protected boolean checkTerminate(Body body) {
    return (count > MAX);
  }

  //    if (this.stop()) {
  //       System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndForwarder: stoping...");
  //       System.exit(0);
  //     }

  public DummyObject echo() {
    // System.out.println("I am here");
    return null;
  }
}