package modelisation.forwarder;

import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class Agent implements org.objectweb.proactive.RunActive,
    java.io.Serializable {
    	
    protected int migrationCounter;
	protected RandomNumberGenerator expo;
    protected Node[] nodes;
    protected int index;
    protected boolean start = false;
    protected long lifeTime;
    protected long startTime;
    private int count;
    private final int MAX = 20;

protected boolean simpleBench;

    public Agent() {
    }

    public Agent(Double nu, Node[] array, Long lifeTime)
        throws IllegalArgumentException {
        //  this.expo = new ExponentialLaw(nu.doubleValue());
        this.expo = RandomNumberFactory.getGenerator("nu");
        this.expo.initialize(nu.doubleValue());
        nodes = array;

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
    
    /**
     * Called to indicate that this agent is on a bench
     * without forwarders 
     */
    public void simpleBench() {
    	this.simpleBench = true;
    }
    
    public void runActivity(Body body) {
        System.out.println("live started");
        if (checkTerminate(body)) {
            body.terminate();
        }

        while (body.isActive()) {
            while (!start) {
                body.serve(body.getRequestQueue().blockingRemoveOldest());
            }
            if (++index > nodes.length) {
                index = 1;
            }

if (this.simpleBench) {
	System.out.println("XXXXXXXX Threads count = " + Thread.activeCount());
}

            double time = expo.next();

            // this.checkTerminate(body);
            System.out.println(System.currentTimeMillis() +
                " AgentWithExponentialMigrationAndForwarder: waiting " +
                (time * 1000) + " before migration");
            try {
                Thread.sleep((int) (time * 1000));
                //we empty the requestQueue
                count += body.getRequestQueue().size();
                body.getRequestQueue().clear();
                //	this.checkTerminate(body);
                System.out.println(System.currentTimeMillis() +
                    " AgentWithExponentialMigrationAndForwarder: migrating to " +
                    nodes[index - 1].getNodeInformation());
                //	count++;
                //   System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				if (this.simpleBench) {
					//System.out.println("XXXXXXXX Threads count = " + Thread.activeCount());
					if (this.migrationCounter++ > 24) {
					  System.exit(0);	
					}
				}
                
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

    public static void main(String[] arguments) {
        Node[] nodes = new Node[arguments.length]; //Bench.readDestinationFile(args[2]);
        for (int i = 0; i < arguments.length; i++) {
            try {
                nodes[i] = NodeFactory.getNode(arguments[i]);
            } catch (NodeException e1) {
                e1.printStackTrace();
            }
        }

        Object[] args = new Object[3];
        args[0] = new Double(1);
        args[1] = nodes;
        args[2] = new Long(50000);

        try {
            Agent agent = null;

            if ("ibis".equals(System.getProperty("proactive.rmi"))) {
                System.out.println(" USING IBIS");
                agent = (Agent) ProActive.newActive(Agent.class.getName(),
                        args, null, null, new NoForwarderIbisMetaObjectFactory());
            } else {
                System.out.println(" USING RMI");
                agent = (Agent) ProActive.newActive(Agent.class.getName(),
                        args, null, null, new NoForwarderMetaObjectFactory());
            }

            agent = (Agent) ProActive.newActive("modelisation.forwarder.Agent",
                    args, null, null, new NoForwarderMetaObjectFactory());
                    agent.simpleBench();
            agent.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
