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
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 * This class receives deployment events
 *
 * @author       Lionel Mestre
 * @version      1.0
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
    this.proActiveDescriptor = proActiveDescriptor;
//    {
//    PassiveCompositeUnmarshaller ch = new PassiveCompositeUnmarshaller();
//    ch.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeHandler());
//    this.addHandler(VIRTUAL_NODES_TAG, ch);
//    }
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


//  /**
//   * This class receives virtualNode events
//   */
//  private class VirtualNodeHandler extends BasicUnmarshaller {
//    private VirtualNodeHandler() {
//    }
//    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
//      // create and register a VirtualNode
//      String vnName = attributes.getValue("name");
//      if (! checkNonEmpty(vnName)) throw new org.xml.sax.SAXException("VirtualNode defined without name");
//      VirtualNode vn = proActiveDescriptor.createVirtualNode(vnName);
//      // cyclic
//      String cyclic = attributes.getValue("cyclic");
//      if (checkNonEmpty(cyclic)) {
//        vn.setCyclic(cyclic.equals("true"));
//      }
//      // localbackup
//      String localBackup = attributes.getValue("localBackup");
//      if (checkNonEmpty(localBackup)) {
//        vn.setLocalBackup(Boolean.getBoolean(localBackup));
//      }
//    }
//  } // end inner class VirtualNodeHandler


  /**
   * This class receives map events
   */
  private class MapHandler extends PassiveCompositeUnmarshaller {
  	
  	VirtualNode vn;
  	
    private MapHandler() {
    	CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
   		cu.addHandler(VMNAME_TAG, new SingleValueUnmarshaller());
    	this.addHandler(JVMSET_TAG, cu);
    }
    
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      // create and register a VirtualNode
      String vnName = attributes.getValue("virtualNode");
      if (! checkNonEmpty(vnName)) throw new org.xml.sax.SAXException("mapping defined without specifying virtual node");
      vn = proActiveDescriptor.createVirtualNode(vnName);
      String vmName = attributes.getValue("jvm");
      if (! checkNonEmpty(vmName) && !vn.getCyclic()) throw new org.xml.sax.SAXException("non cyclic virtualNode mapping defined without specifying jvm");
      if ( checkNonEmpty(vmName) && vn.getCyclic()) throw new org.xml.sax.SAXException("for cyclic virtualNode, set of jvms must be specified in JVMSET_TAG");
      if (checkNonEmpty(vmName) && !vn.getCyclic()){
      	VirtualMachine vm = proActiveDescriptor.createVirtualMachine(vmName);
      	vn.addVirtualMachine(vm);
      }
			
    }
    
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    	if(name.equals(JVMSET_TAG)){
    		if(!vn.getCyclic()) throw new org.xml.sax.SAXException("a set of virtual machine is defined for a virtualNode that is not cyclic"); 
    		String [] vmNames = (String[]) activeHandler.getResultObject();
    		if (vmNames.length > 0) {
    			for (int i=0; i<vmNames.length; i++) {
    				VirtualMachine vm = proActiveDescriptor.createVirtualMachine(vmNames[i]);
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
      //System.out.println("startContextElement name="+name);
      String vmName = attributes.getValue("name");
      if (! checkNonEmpty(vmName)) throw new org.xml.sax.SAXException("VirtualMachine defined without name");
      currentVM = proActiveDescriptor.createVirtualMachine(vmName);
      String cyclic = attributes.getValue("cyclic");
      if (checkNonEmpty(cyclic)) {
        currentVM.setCyclic(cyclic.equals("true"));
      }
      String nodeNumber = attributes.getValue("nodeNumber");
      try{
      	if (checkNonEmpty(nodeNumber)) {
        	currentVM.setNodeNumber(nodeNumber);
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
        //System.out.println("found acquisitionMethod="+acquisitionMethod+" for currentVM="+currentVM.getName());
        if (acquisitionMethod != null) {
          currentVM.setAcquisitionMethod(acquisitionMethod);
        }
      }

    } // end inner class AcquisitionHandler



    /**
     * This class receives acquisition events
     */
    private class CreationHandler extends PassiveCompositeUnmarshaller {
      private Object result;

      private CreationHandler() {
        this.addHandler(PROCESS_TAG, new ProcessHandler(proActiveDescriptor));
        this.addHandler(JVM_PROCESS_TAG, new JVMProcessHandler(proActiveDescriptor));
      }

      protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        Object o = activeHandler.getResultObject();
        if (o == null) return;
        if (o instanceof String) {
          // its an id
          proActiveDescriptor.registerProcess(currentVM, (String) o);
        } else if (o instanceof ExternalProcess) {
          // its a process
          currentVM.setProcess((ExternalProcess) o);
        }
      }
    } // end inner class CreationHandler

  } // end inner class JVMHandler

}