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
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
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

public class ProActiveDescriptorHandler extends AbstractUnmarshallerDecorator implements ProActiveDescriptorConstants {

  private ProActiveDescriptor proActiveDescriptor;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public ProActiveDescriptorHandler(String xmlDescriptorUrl) {
    super(false);
    proActiveDescriptor = new ProActiveDescriptorImpl(xmlDescriptorUrl);
    addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor));
    addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(proActiveDescriptor));
	addHandler(SECURITY_TAG, new SecurityHandler(proActiveDescriptor));
    {
    PassiveCompositeUnmarshaller compDefHandler = new PassiveCompositeUnmarshaller();
    PassiveCompositeUnmarshaller vNodesDefHandler = new PassiveCompositeUnmarshaller();
    PassiveCompositeUnmarshaller vNodesAcqHandler = new PassiveCompositeUnmarshaller();
    vNodesDefHandler.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeHandler());
    vNodesAcqHandler.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeLookupHandler());
    compDefHandler.addHandler(VIRTUAL_NODES_DEFINITION_TAG, vNodesDefHandler);
    compDefHandler.addHandler(VIRTUAL_NODES_ACQUISITION_TAG, vNodesAcqHandler);
    this.addHandler(COMPONENT_DEFINITION_TAG, compDefHandler);
    }
  }



  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  public static void main(String[] args) throws java.io.IOException {
    
    String uri = "Z:\\ProActive\\descriptors\\C3D_Dispatcher_Renderer.xml";
	InitialHandler h = new InitialHandler(uri);
    //String uri = "file:/net/home/rquilici/ProActive/descriptors/C3D_Dispatcher_Renderer.xml";
    
    org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h);
    sr.read();
  }
  
  /**
   * Creates ProActiveDescriptor object from XML Descriptor
   * @param xmlDescriptorUrl the URL of XML Descriptor
   */
  public static ProActiveDescriptorHandler createProActiveDescriptor(String xmlDescriptorUrl) throws java.io.IOException,org.xml.sax.SAXException {
      //static method added to replace main method
      try {
        InitialHandler h = new InitialHandler(xmlDescriptorUrl);
        String uri = xmlDescriptorUrl;
        org.objectweb.proactive.core.xml.io.StreamReader sr =
          new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h);
        sr.read();
        return(ProActiveDescriptorHandler)h.getResultObject();
      }
        catch (org.xml.sax.SAXException e){
        e.printStackTrace();
        logger.fatal("a problem occurs when getting the ProActiveDescriptorHandler");
        throw e;
      }
    }


  //
  // -- implements XMLUnmarshaller ------------------------------------------------------
  //


  public Object getResultObject() throws org.xml.sax.SAXException {
    return proActiveDescriptor;
  }

  public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
  }


  //
  // -- PROTECTED METHODS ------------------------------------------------------
  //

  protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
  }


  //
  // -- PRIVATE METHODS ------------------------------------------------------
  //


  //
  // -- INNER CLASSES ------------------------------------------------------
  //


  /**
   * Receives deployment events
   */
  private static class InitialHandler extends AbstractUnmarshallerDecorator {
    // line added to return a ProactiveDescriptorHandler object
    private ProActiveDescriptorHandler proActiveDescriptorHandler;
    private InitialHandler(String xmlDescriptorUrl) {
      super();
      proActiveDescriptorHandler = new ProActiveDescriptorHandler(xmlDescriptorUrl);
      this.addHandler(PROACTIVE_DESCRIPTOR_TAG, proActiveDescriptorHandler);
    }
    public Object getResultObject() throws org.xml.sax.SAXException {
      return proActiveDescriptorHandler;
    }
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    }
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }
  }
  
  /**
   * This class receives virtualNode events
   */
  private class VirtualNodeHandler extends BasicUnmarshaller {
    private VirtualNodeHandler() {
    }
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      // create and register a VirtualNode
      String vnName = attributes.getValue("name");
      if (! checkNonEmpty(vnName)) throw new org.xml.sax.SAXException("VirtualNode defined without name");
      // underneath, we know that it is a VirtualNodeImpl, since the bollean in the method is false 
      VirtualNodeImpl vn = (VirtualNodeImpl)proActiveDescriptor.createVirtualNode(vnName,false);
      // property
      String property = attributes.getValue("property");
      if (checkNonEmpty(property)) {
       vn.setProperty(property);
      }
      String timeout = attributes.getValue("timeout");
	  String waitForTimeoutAsString = attributes.getValue("waitForTimeout");
	  boolean waitForTimeout = false;
	  if (checkNonEmpty(waitForTimeoutAsString)){
		waitForTimeout = new Boolean(waitForTimeoutAsString).booleanValue();
	  }
      if (checkNonEmpty(timeout)) {
       vn.setTimeout(new Integer(timeout).longValue(), waitForTimeout);
      }
      String minNodeNumber = attributes.getValue("minNodeNumber");
      if (checkNonEmpty(minNodeNumber)){
          vn.setMinNumberOfNodes((new Integer(minNodeNumber).intValue()));
      }

    }
  } // end inner class VirtualNodeHandler
	
	/**
   * This class receives virtualNode events
   */
  private class VirtualNodeLookupHandler extends BasicUnmarshaller {
    private VirtualNodeLookupHandler() {
    }
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
      // create and register a VirtualNode
      String vnName = attributes.getValue("name");
      if (! checkNonEmpty(vnName)) throw new org.xml.sax.SAXException("VirtualNode defined without name");
      VirtualNode vn = proActiveDescriptor.createVirtualNode(vnName,true);
    }
  } // end inner class VirtualNodeLookupHandler

	// SECURITY
	/**
			* This class receives Security events
			*/
	  private class SecurityHandler extends BasicUnmarshaller {
		  private ProActiveDescriptor proActiveDescriptor;

		  public SecurityHandler(ProActiveDescriptor proActiveDescriptor) {
			  super();
			  this.proActiveDescriptor = proActiveDescriptor;
		  }

		  public void startContextElement(String name, Attributes attributes)
			  throws org.xml.sax.SAXException {
			  // create and register a VirtualNode
			  String file = attributes.getValue("file");

        
			  if (!checkNonEmpty(file)) {
				  throw new org.xml.sax.SAXException("Empty security file");
			  }
			  logger.debug("creating PolicyServer : " + file);
			  proActiveDescriptor.createPolicyServer(file);
		  }
	  }
	   // end inner class SecurityHandler
}
