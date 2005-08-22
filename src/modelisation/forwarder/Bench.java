package modelisation.forwarder;

import java.util.Date;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;

import modelisation.ModelisationBench;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;
import modelisation.util.NodeControler;


public class Bench extends ModelisationBench
    implements org.objectweb.proactive.RunActive {
    protected static NodeControler auto;
    protected static final int MAX = 50;

    // protected static final int MAX = 5;
    //    public static Agent startAgent(double d, Node[] nodes, String nodeName,
    //        long lifeTime) {
    //        Agent agent = null;
    //        Object[] args = new Object[3];
    //        args[0] = new Double(d);
    //        args[1] = nodes;
    //        args[2] = new Long(lifeTime);
    //        try {
    //            if ("ibis".equals(System.getProperty("proactive.communication.protocol"))) {
    //                System.out.println(" USING IBIS");
    //                agent = (Agent) ProActive.newActive(Agent.class.getName(),
    //                        args, NodeFactory.getNode(nodeName), null,
    //                        new ForwarderIbisMetaObjectFactory());
    //            } else {
    //                System.out.println(" USING RMI");
    //                agent = (Agent) ProActive.newActive(Agent.class.getName(),
    //                        args, NodeFactory.getNode(nodeName), null,
    //                        new ForwarderMetaObjectFactory());
    //            }
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return agent;
    //    }
    public static Agent startAgent(double d, Node[] nodes, long lifeTime) {
        Agent agent = null;
        Object[] args = new Object[3];
        args[0] = new Double(d);
        args[1] = nodes;
        args[2] = new Long(lifeTime);
        System.out.println("NODES SIZE = " + nodes.length);
        try {
            if ("ibis".equals(System.getProperty(
                            "proactive.communication.protocol"))) {
                System.out.println(" USING IBIS");
                agent = (Agent) ProActive.newActive(Agent.class.getName(),
                        args, Bench.StartNode, null,
                        new ForwarderIbisMetaObjectFactory());
            } else {
                System.out.println(" USING RMI");
                agent = (Agent) ProActive.newActive(Agent.class.getName(),
                        args, Bench.StartNode, null,
                        new ForwarderMetaObjectFactory());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agent;
    }

    //******** ACTIVE PART OF THE TEST -  UGLY BUT NECESSARY BECAUSE OF HALF-BODIES*** //
    Agent agent;
    RandomNumberGenerator expo;

    public Bench() {
    }

    public Bench(Agent a, RandomNumberGenerator e) {
        this.agent = a;
        this.expo = e;
    }

    public void runActivity(Body b) {
        int waittime;
        for (int i = 0; i < Bench.MAX; i++) {
            waittime = (int) (expo.next() * 1000);
            System.out.println(System.currentTimeMillis() + " Bench: waiting " +
                waittime + " ms before calling the agent");
            try {
                Thread.sleep(waittime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis() +
                " Bench: calling the agent for round " + i);
            try {
                agent.echo();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        //System.out.flush();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bench.stop();
        System.exit(0);
    }

    // *************************************//
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                "Usage: java modelisation.Bench  <lambda> <nu> <destinationFile>  <benchLength> <output>");
            System.err.println(
                "-Dnodecontroler.startnode=false to use already created nodes");
            System.exit(-1);
        }

        ProActiveConfiguration.load();

        //ExponentialLaw expo = new ExponentialLaw(Double.parseDouble(args[0]));
        RandomNumberGenerator expo = RandomNumberFactory.getGenerator("lambda");
        expo.initialize(Double.parseDouble(args[0]));

        //    Node[] nodes = Bench.readDestinationFile(args[2], "" + args[0] + "_" + args[1]);
        Node[] nodes = Bench.readMapingFile(args[2]);

        Agent agent = Bench.startAgent(Double.parseDouble(args[1]), nodes,
                Long.parseLong(args[3]));
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("");
        System.out.println("Bench started at " + new Date() + " with MAX  " +
            Bench.MAX);
        System.out.println("");
        System.out.println("Calling agent.start()");
        agent.start();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bench bench = null;
        Object[] param = new Object[] { agent, expo };
        try {
            if ("ibis".equals(System.getProperty(
                            "proactive.communication.protocol"))) {
                System.out.println(" USING IBIS");
                bench = (Bench) ProActive.newActive(Bench.class.getName(),
                        param, (Node) null, null,
                        new ForwarderIbisMetaObjectFactory());
                //   ProActiveMetaObjectFactory.instance = new ProActiveIbisMetaObjectFactory();
            } else {
                System.out.println(" USING RMI");
                bench = (Bench) ProActive.newActive(Bench.class.getName(),
                        param, (Node) null, null,
                        new ForwarderMetaObjectFactory());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
