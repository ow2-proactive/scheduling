package test.descriptor.lookupregister;



import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Agent implements java.io.Serializable {
  private static String FS = System.getProperty("file.separator");
	private static String XML_LOCATION_UNIX = System.getProperty("user.home")+FS+"ProActive"+FS+"descriptors"+FS+"examples"+FS+"Agent.xml";
	private static String XML_LOCATION_WIN = "///Z:/ProActive/descriptors/examples/Agent.xml";
	
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
      System.out.println("getName called");
      //return the name of the Host  
      System.out.println("My name is "+this.name);
      return this.name;
      //return java.net.InetAddress.getLocalHost().getHostName().toUpperCase();
    } catch (Exception e) {
      e.printStackTrace();
      return "getName failed";
    }

  }

  public String getNodeName() {
    try {
      System.out.println("getNodeName called");
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

  
	public static void main(String[] args) {
		ProActiveDescriptor proActiveDescriptor=null;
		try{
    proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION_UNIX);
   // proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
    
    proActiveDescriptor.activateMappings();
    VirtualNode vn = proActiveDescriptor.getVirtualNode("Agent");
    ProActive.newActive(Agent.class.getName(), new Object[]{"local"},vn.getNode());
    }catch(Exception e){
    e.printStackTrace();
    System.out.println("Pb in main");
    }
	}
}
