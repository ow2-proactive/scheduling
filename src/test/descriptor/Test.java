/**
 * Created on 27 juin 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Window>Preferences>Java>Templates.
 */
package test.descriptor;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.descriptor.xml.ProActiveDescriptorHandler;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.examples.migration.Agent;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class Test implements ProActiveDescriptorConstants{
  
	private static String FS = System.getProperty("file.separator");
	private static String XML_LOCATION = System.getProperty("user.home")+FS+"ProActive"+FS+"descriptors"+FS+"TestBSub.xml";
  public static void main(String[] args) throws java.io.IOException{
    ProActiveDescriptor proActiveDescriptor=null;
    Agent myServer;
    String hostName;
    try{
    //proActiveDescriptor=ProActive.getProactiveDescriptor("file://Z:/test/ProActive/classes/C3D_Dispatcher_RendererTest.xml");
    proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
    }catch(Exception e){
    e.printStackTrace();
    System.out.println("Pb in main");
    }
    //}catch(org.xml.sax.SAXException e){
    //e.printStackTrace();
    //}
//    try
//		{
//			Node node5 =  NodeFactory.getDefaultNode();
//     System.out.println("------------------"+NodeFactory.isNodeLocal(node5));
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
     
//    proActiveDescriptor.activateMappings(); 
//    //System.out.println("renderer activated");
    VirtualNode vn1 = proActiveDescriptor.getVirtualNode("renderer");
    //System.out.println(vn1.getName());
    vn1.activate();
//    try{
//    Node node = vn1.getNode();
////    myServer = (Agent)ProActive.newActive(Agent.class.getName(), new Object[]{"local"},node);
////    hostName = myServer.getName();
////    System.out.println("hostname is "+hostName);
//    }catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//    try
//		{
//			System.out.println("-----------------"+vn1.getNode().getNodeInformation().getURL());
//			System.out.println("-----------------"+vn1.getNode().getNodeInformation().getVMID());
//			System.out.println("-----------------"+NodeFactory.isNodeLocal(vn1.getNode()));
//			Thread.sleep(15000);
//			String[] nodes = vn1.getNodesURL();
//			for (int i = 0; i < nodes.length; i++)
//			{
//				System.out.println("node :"+nodes[i]); 
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
    
   // VirtualNode vn2 = proActiveDescriptor.getVirtualNode("renderer");
    //System.out.println(vn2.getName());
    //vn2.activate();
//    VirtualNode vn3 = proActiveDescriptor.getVirtualNode("Renderer2");
////    //System.out.println(vn3.getName());
////    vn3.activate();
//    VirtualNode vn4 = proActiveDescriptor.getVirtualNode("Renderer3");
////    //System.out.println(vn4.getName());
////    vn4.activate();
//    VirtualNode vn5 = proActiveDescriptor.getVirtualNode("Renderer4");
////    //System.out.println(vn5.getName());
//    vn5.activate();
//    try
//		{
//			System.out.println("-----------------"+vn2.getNode().getNodeInformation().getURL());
//			System.out.println("-----------------"+vn2.getNode().getNodeInformation().getVMID());
//			System.out.println("-----------------"+NodeFactory.isNodeLocal(vn2.getNode()));
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
    
//    try{
//    node1 = vn1.getNode("//sea.inria.fr/bob");
//    node2 = vn2.getNode("//wapiti.inria.fr/bob");
//    }catch (Exception ne){
//    	ne.printStackTrace();
//    }
    //try{
    //node1 = NodeFactory.getNode("//sea.inria.fr/bob");
    //node2 = NodeFactory.getNode("//wapiti.inria.fr/bob");
    //}catch (Exception ne){
    //ne.printStackTrace();
    //} 
//    try{
//    System.out.println("---------------------"+vn5.getNode().getNodeInformation().getURL());
//    System.out.println("--------------------------"+vn3.getNode().getNodeInformation().getURL());
//    System.out.println("-----------------"+vn4.getNode().getNodeInformation().getURL());
//    }catch(Exception e){
//    e.printStackTrace();
//    }
  }
  
  
  
}
