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
public class Test {
	
	private static String FS = System.getProperty("file.separator");
	private static String XML_LOCATION_UNIX = System.getProperty("user.home")+FS+"ProActive"+FS+"descriptors"+FS+"examples"+FS+"AgentClient.xml";
	private static String XML_LOCATION_WIN = "///Z:/ProActive/descriptors/examples/AgentClient.xml";

  public static void main(String[] args) {
    Agent agent;
    String nodeName, hostName;
    
        
        ProActiveDescriptor proActiveDescriptor=null;
		try{
    proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION_UNIX);
   // proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
    
    proActiveDescriptor.activateMappings();
    VirtualNode vn = proActiveDescriptor.getVirtualNode("Agent");
    agent = (Agent)vn.getUniqueAO();
    //System.out.println("name of the agent "+agent.getName());
    //agent.moveTo("//sea.inria.fr/bob");
    System.out.println("name of the agent "+agent.getName());
    System.out.println("name of the node "+agent.getNodeName());
    } catch (Exception e) {
      System.err.println("Could not reach/create server object");
      e.printStackTrace();
      System.exit(1);
    }
  }
}


