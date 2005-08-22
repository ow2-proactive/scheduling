package modelisation.mixed;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.locationserver.LocationServer;

import modelisation.ModelisationBench;
import modelisation.statistics.ExponentialLaw;
import modelisation.statistics.RandomNumberFactory;
import modelisation.statistics.RandomNumberGenerator;


public class Bench extends ModelisationBench
    implements org.objectweb.proactive.RunActive {
    public Bench() {
        super();
    }

    //    public static AgentWithExponentialMigrationMixed startExponentialAgent(
    //        double d, Node[] nodes, String nodeName, long lifeTime) {
    //        AgentWithExponentialMigrationMixed agent = null;
    //        Object[] args = new Object[3];
    //        args[0] = new Double(d);
    //        args[1] = nodes;
    //        args[2] = new Long(lifeTime);
    //        System.out.println("NODES SIZE = " + nodes.length);
    //        try {
    //            agent = (AgentWithExponentialMigrationMixed) ProActive.newActive(AgentWithExponentialMigrationMixed.class.getName(),
    //                    args, NodeFactory.getNode(nodeName), null,
    //                    TimedMixedMetaObjectFactory.newInstance());
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return agent;
    //    }
    public static AgentWithExponentialMigrationMixed startExponentialAgent(
        double d, Node[] nodes, long lifeTime) {
        AgentWithExponentialMigrationMixed agent = null;
        Object[] args = new Object[3];
        args[0] = new Double(d);
        args[1] = nodes;
        args[2] = new Long(lifeTime);
        System.out.println("NODES SIZE = " + nodes.length);
        try {
            agent = (AgentWithExponentialMigrationMixed) ProActive.newActive(AgentWithExponentialMigrationMixed.class.getName(),
                    args, Bench.StartNode, null,
                    TimedMixedMetaObjectFactory.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agent;
    }

    //******** ACTIVE PART OF THE TEST -  UGLY BUT NECESSARY BECAUSE OF HALF-BODIES*** //
    AgentWithExponentialMigrationMixed agent;
    ExponentialLaw expo;
    long benchTime;

    public Bench(AgentWithExponentialMigrationMixed a, ExponentialLaw e,
        Long time) {
        this.agent = a;
        this.expo = e;
        this.benchTime = time.longValue();
    }

    public void runActivity(Body b) {
        System.out.println("Bench: runActivity <<<<<<<<<<<<<<<<<<<<");

        long startTimeSleep = 0;
        long endTimeSleep = 0;
        int waittime;
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < benchTime) {
            waittime = (int) (expo.next() * 1000);
            System.out.println("Bench: waiting " + waittime +
                " ms before calling the agent");
            startTimeSleep = System.currentTimeMillis();
            try {
                Thread.sleep(waittime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            endTimeSleep = System.currentTimeMillis();
            System.out.println("Bench: calling the agent after " +
                (endTimeSleep - startTimeSleep));
            try {
                agent.echoObject();
            } catch (Exception e) {
                e.printStackTrace();
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

    public static void main(String[] args) {
        if (args.length < 7) {
            System.out.println(
                "Usage: java modelisation.Main  <lambda> <nu> <ttl> <maxMigrations> <destinationFile> <creationNode> <benchLength>");
            System.exit(-1);
        }

        ProActiveConfiguration.load();

        RandomNumberGenerator expo = RandomNumberFactory.getGenerator("lambda");
        expo.initialize(Double.parseDouble(args[0]));

        LocationServer s = null;
        System.out.println("Test: looking up for the server");
        System.out.println("Test: using lambda = " + args[0] + " nu = " +
            args[1]);

        //        Node[] nodes = Bench.readDestinationFile(args[4],"" + args[0] + "_" + args[1]);
        Node[] nodes = Bench.readMapingFile(args[4]);

        System.out.println("NODES IN MAIN = " + nodes.length);

        AgentWithExponentialMigrationMixed agent = Bench.startExponentialAgent(Double.parseDouble(
                    args[1]), nodes, Long.parseLong(args[6]));

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Calling agent.start()");

        agent.start();
        Long benchTime = Long.valueOf(args[6]);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Creating Active Bench");

        Bench bench = null;
        Object[] param = new Object[] { agent, expo, benchTime };
        try {
            bench = (Bench) ProActive.newActive(Bench.class.getName(), param,
                    (Node) null, null, TimedMixedMetaObjectFactory.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
