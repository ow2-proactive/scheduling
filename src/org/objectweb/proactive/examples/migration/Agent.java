package org.objectweb.proactive.examples.migration;



import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Agent implements InitActive, RunActive, EndActive, java.io.Serializable {
  private String name;
  private String nodename;
  private String hostname;

  public Agent() {
  }

  public Agent(String name) {
    this.name = name;
  }

  public String getName() {
    try {
      //System.out.println("getName called");
      //return the name of the Host  
      return java.net.InetAddress.getLocalHost().getHostName().toUpperCase();
    } catch (Exception e) {
      e.printStackTrace();
      return "getName failed";
    }

  }

  public String getNodeName() {
    try {
      //System.out.println("getNodeName called");
      //return the name of the Node  
      return ProActive.getBodyOnThis().getNodeURL().toUpperCase();
    } catch (Exception e) {
      e.printStackTrace();
      return "getNodeName failed";
    }
  }

  public void moveTo(String nodeURL) {
    try {
      System.out.println(" I am going to migate");
      ProActive.migrateTo(nodeURL);
      System.out.println("migration done");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void endBodyActivity() {
    ProActive.getBodyOnThis().terminate();
  }

  public void initActivity(Body body) {
    System.out.println("Initialization of the Activity");
  }

  public void runActivity(Body body) {
    org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
    while (body.isActive()) {
      // The synchro policy is FIFO
      service.blockingServeOldest();
      // The synchro policy is LIFO uncomment the following lines
      //service.waitForRequest();
      //System.out.println(" I am going to serve " + service.getYoungest().getMethodName());
      //service.serveYoungest();
      //System.out.println("served");
    }
  }

  public void endActivity(Body body) {
    System.out.println("End of the activity of this Active Object");
  }
}
