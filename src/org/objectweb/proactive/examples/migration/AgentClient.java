package org.objectweb.proactive.examples.migration;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AgentClient {

  public static void main(String[] args) {
    Agent myServer;
    String nodeName, hostName;
    try {
        // Create an active server within this VM
        myServer = (Agent)org.objectweb.proactive.ProActive.newActive(Agent.class.getName(), new Object[]{"local"});
      // Invokes a remote method on this object to get the message
      hostName = myServer.getName();
      nodeName = "default";
      System.out.println(hostName);
      for(int i=0; i<args.length;i++){
      // Prints out the message
      myServer.moveTo(args[i]);
      nodeName=myServer.getNodeName();
      hostName=myServer.getName();
      System.out.println("The name is : " + hostName+" " + nodeName);
      }
      //myServer.endBodyActivity();
    } catch (Exception e) {
      System.err.println("Could not reach/create server object");
      e.printStackTrace();
      System.exit(1);
    }
  }
}


