package modelisation.mixed;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.Vector;

import modelisation.server.AgentWithExponentialMigrationAndServer;
import modelisation.server.TimedLocationServerMetaObjectFactory;

import modelisation.statistics.ExponentialLaw;

import modelisation.util.NodeControler;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ext.locationserver.LocationServer;


public class Bench implements org.objectweb.proactive.RunActive {

    protected static NodeControler auto;

    public Bench() {
        super();
    }

    public static AgentWithExponentialMigrationMixed startExponentialAgent(double d, 
                                                                           Node[] nodes, 
                                                                           String nodeName, 
                                                                           long   lifeTime) {

        AgentWithExponentialMigrationMixed agent = null;
        Object[] args = new Object[3];
        args[0] = new Double(d);
        args[1] = nodes;
        args[2] = new Long(lifeTime);
        System.out.println("NODES SIZE = " + nodes.length);
        try {
            agent = (AgentWithExponentialMigrationMixed)ProActive.newActive(AgentWithExponentialMigrationMixed.class.getName(), 
                                                                            args, 
                                                                            NodeFactory.getNode(
                                                                                    nodeName), 
                                                                            null, 
                                                                            TimedMixedMetaObjectFactory.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agent;
    }

    public static void initialise(NodeControler a) {
        Bench.auto = a;
    }

    public static void stop() {
        System.out.println("Bench: stoping......");
        Bench.auto.killAllProcess();
    }

    public static Node[] readDestinationFile(String fileName) {

        FileReader f_in = null;
        Vector v = new Vector();
        String s;
        try {
            f_in = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not Found");
        }

        // on ouvre un "lecteur" sur ce fichier
        BufferedReader _in = new BufferedReader(f_in);
        // on lit a partir de ce fichier
        // NB : a priori on ne sait pas combien de lignes on va lire !!
        try {
            // tant qu'il y a quelque chose a lire
            while (_in.ready()) {
                // on le lit
                s = _in.readLine();
                //   StringTokenizer tokens = new StringTokenizer(s, " ");
                System.out.println("Adding " + s + " to destinationFile");
                v.addElement(NodeFactory.getNode(s));
                //   this.add(new NodeDestination(new String (tokens.nextToken()),tokens.nextToken()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Node[] result = new Node[v.size()];
        v.copyInto(result);
        return result;
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
            waittime = (int)(expo.next() * 1000);
            System.out.println(
                    "Bench: waiting " + waittime + 
                    " ms before calling the agent");
            startTimeSleep = System.currentTimeMillis();
            try {
                Thread.sleep(waittime);
            } catch (Exception e) {
                e.printStackTrace();
            }
            endTimeSleep = System.currentTimeMillis();
            System.out.println(
                    "Bench: calling the agent after " + 
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

//try {
//	NodeFactory.getNode(args[0]);
//} catch (NodeException e) {
//	e.printStackTrace();
//}
//  
//        
//        if (true) {
//        System.exit(0);	
//        }
        ExponentialLaw expo = new ExponentialLaw(Double.parseDouble(args[0]));
        LocationServer s = null;
        NodeControler auto = new NodeControler();
        System.out.println("Test: looking up for the server");
        System.out.println(
                "Test: using lambda = " + args[0] + " nu = " + args[1]);
        if (!auto.startAllNodes(auto.readDestinationFile(args[4]), "")) {
            auto.killAllProcess();
            System.err.println("Error creating nodes, aborting");
            System.exit(-1);
        }
        Bench.initialise(auto);

        //Reading the destination file
        Node[] nodes = Bench.readDestinationFile(args[4]);
        System.out.println("NODES IN MAIN = " + nodes.length);

        AgentWithExponentialMigrationMixed agent = Bench.startExponentialAgent(Double.parseDouble(
                                                                                       args[1]), 
                                                                               nodes, 
                                                                               args[5], 
                                                                               Long.parseLong(
                                                                                       args[6]));
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
            bench = (Bench)ProActive.newActive(Bench.class.getName(), param, 
                                               null, null, 
                                               TimedMixedMetaObjectFactory.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}