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
import org.objectweb.proactive.examples.migration.Agent;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class Test implements ProActiveDescriptorConstants{
  
	private static String FS = System.getProperty("file.separator");
	private static String XML_LOCATION = System.getProperty("user.home")+FS+"ProActive"+FS+"descriptors"+FS+"examples"+FS+"TestBSub.xml";
  public static void main(String[] args) throws java.io.IOException{
    ProActiveDescriptor proActiveDescriptor=null;
    try{
    //proActiveDescriptor=ProActive.getProactiveDescriptor("file:///Z:/ProActive/descriptors/C3D_Dispatcher_RendererNew.xml");
    proActiveDescriptor=ProActive.getProactiveDescriptor("file:"+XML_LOCATION);
    }catch(Exception e){
    e.printStackTrace();
    System.out.println("Pb in main");
    }
    
     
    proActiveDescriptor.activateMappings(); 

  }
  
  
  
}
