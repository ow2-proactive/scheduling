package org.objectweb.proactive.examples.migration;

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
public class AgentClient {

  public static void main(String[] args) {
    Agent myServer;
    String nodeName, hostName;
    ProActiveDescriptor proActiveDescriptor;
    try {
    	
		  proActiveDescriptor = ProActive.getProactiveDescriptor("file:"+args[0]);
			proActiveDescriptor.activateMappings();
			
 
    VirtualNode agent = proActiveDescriptor.getVirtualNode("Agent");
    String[] nodeList = agent.getNodesURL();
     // Create an active server within this VM
     myServer = (Agent)org.objectweb.proactive.ProActive.newActive(Agent.class.getName(), new Object[]{"local"});
      // Invokes a remote method on this object to get the message
     hostName = myServer.getName();
   	 nodeName=myServer.getNodeName();
     System.out.println("Agent is on: host " + hostName+" Node " + nodeName);
      
      for(int i=0; i<nodeList.length;i++){
      // Prints out the message
      myServer.moveTo(nodeList[i]);
      nodeName=myServer.getNodeName();
      hostName=myServer.getName();
      System.out.println("Agent is on: host " + hostName+" Node " + nodeName);
      }
      myServer.endBodyActivity();
    }
      
      catch (Exception e) {
      System.err.println("Could not reach/create server object");
      e.printStackTrace();
      System.exit(1);
    	}
  }
}


