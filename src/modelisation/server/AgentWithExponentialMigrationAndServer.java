package modelisation.server;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import modelisation.statistics.ExponentialLaw;

public class AgentWithExponentialMigrationAndServer implements org.objectweb.proactive.RunActive, java.io.Serializable {

    protected ExponentialLaw expo;
    protected Node[] nodes;
    protected int index;
    protected boolean start = false;
    protected long lifeTime;
    protected long startTime;
    //   protected boolean migratedOnce =  false;

    public AgentWithExponentialMigrationAndServer() {

    }

    public AgentWithExponentialMigrationAndServer(Double nu, Node[] array, Long lifeTime)
            throws IllegalArgumentException {
        this.expo = new ExponentialLaw(nu.doubleValue());
        nodes = array;
        System.out.println("AgentWithExponentialMigrationAndServer: array contains "
                           + array.length + " destinations");
        for (int i = 0; i < array.length; i++)
            System.out.println("destination " + i + " = " + array[i]);
        index = 0;
        this.lifeTime = lifeTime.longValue();
    }


    public void start() {
        System.out.println(" -------------------------- Start");
        this.start = true;
        this.startTime = System.currentTimeMillis();
    }


    public boolean stop() {
        if (startTime == 0)
            return false;
        return ((System.currentTimeMillis() - startTime) > lifeTime);
    }


    public void migrateTo(String url) {
        try {
            ProActive.migrateTo(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void runActivity(Body body) {
        long startTime = 0;
        long endTime = 0;
        while (body.isActive() && (!this.stop())) {
            while (!start) {
                body.serve(body.getRequestQueue().blockingRemoveOldest());
            }
            if (++index > nodes.length)
                index = 1;

            double time = expo.next();
            System.out.println(System.currentTimeMillis() +
                               " AgentWithExponentialMigrationAndServer: waiting " + (time * 1000) + " before migration");
            startTime = System.currentTimeMillis();
            try {
                Thread.sleep((int) (time * 1000));
                endTime = System.currentTimeMillis();
                System.out.println("AgentWithExponentialMigrationAndServer: waited " + (endTime - startTime));
                //we empty the requestQueue
                body.getRequestQueue().clear();

                //FOR TEST
                //  if (!migratedOnce)
                // 	      {
                //		System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndServer: migrating to " + nodes[index - 1].getName());
                //		  this.migratedOnce=true;
                ProActive.migrateTo(nodes[index - 1]);
                //	      }
            } catch (Exception e) {
                e.printStackTrace();
                this.lifeTime = 0;
            }
        }
        if (this.stop()) {
            System.out.println(System.currentTimeMillis() + " AgentWithExponentialMigrationAndServer: stoping...");
            System.exit(0);
        }
    }


    //  public DummyObject echo() {
    //        //    System.out.println("I am here");
    //      return null;
    //    }

    public void echo() {
    }

    public static void main(String args[]) {
        AgentWithExponentialMigrationAndServer a = new AgentWithExponentialMigrationAndServer(new Double(2), null, null);
    }

}
