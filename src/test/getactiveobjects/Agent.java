package test.getactiveobjects;



import org.objectweb.proactive.ProActive;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Agent implements java.io.Serializable {
  private String name;
  private String nodename;
  private String hostname;
  private String myname;

  public Agent() {
  }

  public Agent(String name) {
    this.name = name;
  }

  public String getName() {
    try {
      System.out.println("getName called");
      //return the name of the Host  
      this.hostname = java.net.InetAddress.getLocalHost().getHostName();
      //return java.net.InetAddress.getLocalHost().getHostName();
      return hostname;
    } catch (Exception e) {
      e.printStackTrace();
      return "getName failed";
    }

  }

  public String getNodeName() {
    try {
      System.out.println("getNodeName called");
      //return the name of the Node  
      this.nodename = ProActive.getBodyOnThis().getNodeURL();
      return nodename;
    } catch (Exception e) {
      e.printStackTrace();
      return "getNodeName failed";
    }
  }
  
  public void setMyName(String myName){
  	this.myname = myName;
  }
  
  public String getMyName(){
  	return myname;
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

}
