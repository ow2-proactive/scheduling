/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeLookup;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 * This class receives deployment events
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 */
class DeploymentHandler extends PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {

  private ProActiveDescriptor proActiveDescriptor;
	
  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

  public DeploymentHandler(ProActiveDescriptor proActiveDescriptor) {
  	super(false);
    this.proActiveDescriptor = proActiveDescriptor;
		this.addHandler(REGISTER_TAG,new RegisterHandler());
		this.addHandler(LOOKUP_TAG, new LookupHandler());
    {
    PassiveCompositeUnmarshaller ch = new PassiveCompositeUnmarshaller();
    ch.addHandler(MAP_TAG, new MapHandler());
    this.addHandler(MAPPING_TAG, ch);
    }
    {
    PassiveCompositeUnmarshaller ch = new PassiveCompositeUnmarshaller();
    ch.addHandler(JVM_TAG, new JVMHandler());
    this.addHandler(JVMS_TAG, ch);
    }
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //


  //
  // -- implements UnmarshallerHandler ------------------------------------------------------
  //


  //
  //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
  //

  //
  //  ----- INNER CLASSES -----------------------------------------------------------------------------------
  //

	private class RegisterHandler extends BasicUnmarshaller {
		
		private RegisterHandler(){
		}
			public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
				String vn = attributes.getValue("virtualNode");
				if (! checkNonEmpty(vn)) throw new org.xml.sax.SAXException("register Tag without any virtualnode defined");
				String protocol = attributes.getValue("protocol");
    	if (! checkNonEmpty(protocol)) throw new org.xml.sax.SAXException("lookup Tag without any protocol defined");
    	protocol = UrlBuilder.checkProtocol(protocol);
			VirtualNodeImpl vnImpl = (VirtualNodeImpl)proActiveDescriptor.createVirtualNode(vn,false);
			//VirtualNodeImpl vnImpl= (VirtualNodeImpl)vnStrat.getVirtualNode();
			//vnImpl.setRegistrationValue(true);
			vnImpl.setRegistrationProtocol(protocol);
				
			}
		
		
	}
	
	private class LookupHandler extends BasicUnmarshaller {
		
		private LookupHandler(){
		}
		
		public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
			String vnLookup = attributes.getValue("virtualNode");
    	if (! checkNonEmpty(vnLookup)) throw new org.xml.sax.SAXException("lookup Tag without any virtualnode defined");
    	String protocol = attributes.getValue("protocol");
    	if (! checkNonEmpty(protocol)) throw new org.xml.sax.SAXException("lookup Tag without any protocol defined");
    	String host = attributes.getValue("host");
    	if (! checkNonEmpty(host) && protocol.equals("rmi")) throw new org.xml.sax.SAXException("within a lookup tag attribute host must be defined for rmi protocol");		
    	protocol = UrlBuilder.checkProtocol(protocol);
    	String url = UrlBuilder.buildUrl(host,vnLookup,protocol);
    	VirtualNodeLookup vn  = (VirtualNodeLookup)proActiveDescriptor.createVirtualNode(vnLookup,true);
    	vn.setLookupInformations(url,protocol);
    	}
	}
  /**
   * This class receives map events
   */
  private class MapHandler extends PassiveCompositeUnmarshaller {
  	
  	VirtualNode vn;
  	
  	
    private MapHandler() {
//    	CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
//   		cu.addHandler(VMNAME_TAG, new VmNameHandler());
//    	this.addHandler(JVMSET_TAG, cu);
			this.addHandler(JVMSET_TAG, new JvmSetHandler());
    }
    
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      // create and register a VirtualNode
      String vnName = attributes.getValue("virtualNode");
      if (! checkNonEmpty(vnName)) throw new org.xml.sax.SAXException("mapping defined without specifying virtual node");
      vn = proActiveDescriptor.createVirtualNode(vnName,false);
      
    }
    
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    	if(name.equals(JVMSET_TAG)){
    		String[] vmNames = (String[]) activeHandler.getResultObject();
    		//throws an exception if vn has property unique or unique_singleAO and more than one vm are defined
    		if(vmNames.length > 1 && vn.getProperty()!=null && (vn.getProperty().equals("unique")||vn.getProperty().equals("unique_singleAO"))) throw new org.xml.sax.SAXException("a set of virtual machine is defined for a virtualNode that is unique");
    		if (vmNames.length > 0) {
    			for (int i=0; i<vmNames.length; i++) {
    				VirtualMachine vm = proActiveDescriptor.createVirtualMachine(vmNames[i]);
    				
    				if (vm.getCreatorId() == null)
      			{
      				vm.setCreatorId(vn.getName());
      		
      			}
      			vn.addVirtualMachine(vm);
    			}
    			
    		}
    		
    	}
  }
  
    
  //
  // -- INNER CLASSES ------------------------------------------------------
  //
  	
  	private class SingleValueUnmarshaller extends BasicUnmarshaller {
    	public void readValue(String value) throws org.xml.sax.SAXException {
      setResultObject(value);
    	} 	
  	} //end of inner class SingleValueUnmarshaller
  	
  	private class JvmSetHandler extends CollectionUnmarshaller{
  		
  		protected JvmSetHandler(){
  			super(String.class);
  			this.addHandler(VMNAME_TAG, new VmNameHandler());	
  			this.addHandler(CURRENTJVM_TAG, new CurrentJvmHandler());
  		}
  		
  		protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
  			if(name.equals(CURRENTJVM_TAG)){
  				String protocol = (String)activeHandler.getResultObject();
  				vn.createNodeOnCurrentJvm(protocol);
  			}
  			else{
  				super.notifyEndActiveHandler(name,activeHandler);
  			}
  		}
  		
  	}//end of inner class JvmSetHandler
  	
  	private class VmNameHandler extends BasicUnmarshaller{
			
			private VmNameHandler(){
			}
			
			public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
				String vmName = attributes.getValue("value");
				if (checkNonEmpty(vmName)) {
      	setResultObject(vmName);
   			}else throw new org.xml.sax.SAXException("The name of the Jvm cannot be set to an empty string");
  		}
		}	//end of inner class VmNameHandler
		
		
		private class CurrentJvmHandler extends BasicUnmarshaller{
			private CurrentJvmHandler(){
			}
			public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
				String protocol = attributes.getValue("protocol");
				setResultObject(protocol);
			}
		}// end of inner class CurrentJvmHandler
  } // end inner class MapHandler
		
		
		

  /**
   * This class receives jvm events
   */
  private class JVMHandler extends PassiveCompositeUnmarshaller {

    private VirtualMachine currentVM;

    private JVMHandler() {
      this.addHandler(ACQUISITION_TAG, new AcquisitionHandler());
      this.addHandler(CREATION_PROCESS_TAG, new CreationHandler());
    }

    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      // create and register a VirtualNode
      
      String vmName = attributes.getValue("name");
      if (! checkNonEmpty(vmName)) throw new org.xml.sax.SAXException("VirtualMachine defined without name");
      currentVM = proActiveDescriptor.createVirtualMachine(vmName);
      String nodeNumber = attributes.getValue("nodeNumber");
      try{
      	if (checkNonEmpty(nodeNumber)) {
        	currentVM.setHostsNumber(nodeNumber);
      	}
      }catch(java.io.IOException e){
      	throw new org.xml.sax.SAXException(e);
      }
    }


    /**
     * This class receives acquisition events
     */
    private class AcquisitionHandler extends BasicUnmarshaller {

      private AcquisitionHandler() {
      }

      public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
        String acquisitionMethod = attributes.getValue("method");
        if (acquisitionMethod != null) {
          currentVM.setAcquisitionMethod(acquisitionMethod);
        }
      }

    } // end inner class AcquisitionHandler



    /**
     * This class receives acquisition events
     */
    private class CreationHandler extends PassiveCompositeUnmarshaller {
      

      private CreationHandler() {
					this.addHandler(PROCESS_REFERENCE_TAG,new ProcessReferenceHandler());
      }

      protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        Object o = activeHandler.getResultObject();
        if (o == null) return;
        if (o instanceof String) {
          // its an id
          proActiveDescriptor.registerProcess(currentVM, (String) o);
        }

      }
      
      
    } // end inner class CreationHandler

  } // end inner class JVMHandler

}
