package essai.proplayer;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import java.util.Enumeration;


/**
 * Client generating a ProStream
 * @see StreamClient
 */
public class Client extends StreamClient implements org.objectweb.proactive.RunActive {


  /**no arg constructor -specific to ProActive*/
  public Client() {
  }


  /**
   * @param n number of servers
   * @param servers name of the server(s)
   */
  public Client(Integer n, String servers) {
    super(n);

    java.util.StringTokenizer st = new java.util.StringTokenizer(servers);
    String name = null;
    Server s = null;
    int i = 1;

    /**lookup servers*/
    while (st.hasMoreTokens()) {
      try {
        name = st.nextToken();
        System.out.println("//" + name);
        s = (Server) ProActive.lookupActive(Server.class.getName(), "//" + name);
        System.out.println("s=Server");
        addPlayer(new Player(s));
        System.out.println("addPlayer");
        i++;
        System.out.println("i = " + i);
      } catch (Exception e) {
        System.out.println("\nFailed to lookup server on host //" + name);
        System.exit(1);
      }
    }
  }


  /**
   * method called by the Client to send a reference on itself
   * in order to achieve side-effect on own attributes
   * MUST be declared PUBLIC !
   */
  public void registerSelf() {
    int i = 0;
    Client myself = (Client) ProActive.getStubOnThis();
    if (myself != null) {
      for (Enumeration e = playerArray.elements(); e.hasMoreElements(); i++)
        ((StreamServer) ((Player) e.nextElement()).server).registerClient(myself, i);
    } else {
      System.out.println("Failed to get a stub on the Client - exiting.");
      System.exit(1);
    }
  }


  /**The live method*/
  public void runActivity(Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    service.blockingServeOldest("registerSelf");
    while (true) {
      service.blockingServeOldest("next");
    }
  }


  /**Main method of Client*/
  public static void main(String[] args) {
    int n = args.length;
    String servers = null;
    if (n < 1) {
      System.out.println("Syntax: java Client <serverhostname> [serverhostname2] [...]");
      return;
    }

    /**define all the servers*/
    servers = new String(args[0]);
    for (int i = 1; i < n; i++) {
      servers = servers.concat(" " + args[i]);
      System.out.println(servers);
    }

    /*create an active object from this client*/
    try {
      Object[] params = new Object[2];
      params[0] = new Integer(n);
      params[1] = servers;
      //Client theClient = (Client)ProActive.newActive("essai.proplayer.Client", params,null);
      Client theClient = (Client) ProActive.newActive("essai.proplayer.Client", params, NodeFactory.getNode("//localhost:7777/Node1"));
      theClient.registerSelf();  /*theClient.lookupActive*/
    } catch (ActiveObjectCreationException e) {
      System.err.println("Failed to create the active client: " + e);
      e.printStackTrace();
      System.exit(1);
    } catch (NodeException e) {
      System.out.println("Node Exception" + e);
    }
  }
}
