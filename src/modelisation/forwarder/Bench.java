package modelisation.forwarder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.Vector;

import modelisation.statistics.ExponentialLaw;
import modelisation.util.NodeControler;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

public class Bench {

  protected static NodeControler auto;

  public static AgentWithExponentialMigrationAndForwarder startExponentialAgent(double d, Node[] nodes, String nodeName, long lifeTime) {
    AgentWithExponentialMigrationAndForwarder agent = null;

    Object args[] = new Object[3];
    args[0] = new Double(d);
    args[1] = nodes;
    args[2] = new Long(lifeTime);

    try {
      agent =
        (AgentWithExponentialMigrationAndForwarder) ProActive.newActive(
          "modelisation.forwarder.AgentWithExponentialMigrationAndForwarder",
          args,
          FowarderMetaObjectFactory.newInstance(),
          NodeFactory.getNode(nodeName));
    } catch (Exception e) {
      e.printStackTrace();
    }
    //	agent.initialize(s);
    //	agent.start();
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
        //	StringTokenizer tokens = new StringTokenizer(s, " ");
        System.out.println("Adding " + s + " to destinationFile");
        v.addElement(NodeFactory.getNode(s));
        //	this.add(new NodeDestination(new String (tokens.nextToken()),tokens.nextToken()));

      }
    } // catch (IOException e) {}
    catch (Exception e) {
      e.printStackTrace();
    }

    Node[] result = new Node[v.size()];
    v.copyInto(result);
    return result;
  }

  //******** ACTIVE PART OF THE TEST -  UGLY BUT NECESSARY BECAUSE OF HALF-BODIES*** //

  AgentWithExponentialMigrationAndForwarder agent;
  ExponentialLaw expo;

  public Bench() {}

  public Bench(AgentWithExponentialMigrationAndForwarder a, ExponentialLaw e) {
    this.agent = a;
    this.expo = e;
  }

  public void live(Body b) {
    int waittime;
    for (int i = 0; i < 20; i++) {
      waittime = (int) (expo.next() * 1000);
      System.out.println(System.currentTimeMillis() + " Bench: waiting " + waittime + " ms before calling the agent");
      try {
        Thread.sleep(waittime);
      } catch (Exception e) {
        e.printStackTrace();
      }

      System.out.println(System.currentTimeMillis() + " Bench: calling the agent for round " + i);
      try {
        agent.echo();
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

  // *************************************//

  public static void main(String args[]) {
    if (args.length < 5) {
      System.out.println("Usage: java modelisation.Bench  <lambda> <nu> <destinationFile> <creationNode> <benchLength>");
      System.exit(-1);
    }

    ExponentialLaw expo = new ExponentialLaw(Double.parseDouble(args[0]));
    NodeControler auto = new NodeControler();

    if (!auto.startAllNodes(auto.readDestinationFile(args[2]), "" + args[0] + "_" + args[1])) {
      auto.killAllProcess();
      System.err.println("Error creating nodes, aborting");
      System.exit(-1);
    }

    Bench.initialise(auto);

    //Reading the destination file
    Node[] nodes = Bench.readDestinationFile(args[2]);

    //	System.exit(-1);

    //	obj1.initialize(nodes);

    //Bench.mesureRequestCost(obj1, 1000);
    AgentWithExponentialMigrationAndForwarder agent = Bench.startExponentialAgent(Double.parseDouble(args[1]), nodes, args[3], Long.parseLong(args[4]));
    try {
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("");
    System.out.println("Bench started at " + new Date());
    System.out.println("");
    System.out.println("Calling agent.start()");

    agent.start();

    //	long benchTime = Long.parseLong(args[4]);

    try {
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //	long startTime = System.currentTimeMillis();
    //int waittime;
    //  while (System.currentTimeMillis() - startTime < benchTime) {

    //creation of the active bench for the test
    Bench bench = null;
    Object[] param = new Object[] { agent, expo };
    try {
      bench = (Bench) ProActive.newActive(Bench.class.getName(), param, FowarderMetaObjectFactory.newInstance(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}