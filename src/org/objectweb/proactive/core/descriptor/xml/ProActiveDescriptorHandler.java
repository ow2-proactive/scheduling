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
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 *
 * Receives SAX event and pass them on
 *
 * @author       Lionel Mestre
 * @version      0.91
 *
 */
public class ProActiveDescriptorHandler extends AbstractUnmarshallerDecorator implements ProActiveDescriptorConstants {

  private ProActiveDescriptor proActiveDescriptor;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public ProActiveDescriptorHandler() {
    super();
    proActiveDescriptor = new ProActiveDescriptorImpl();
    addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor));
    addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(proActiveDescriptor));
    {
    PassiveCompositeUnmarshaller ch = new PassiveCompositeUnmarshaller();
    ch.addHandler(VIRTUAL_NODE_TAG, new VirtualNodeHandler());
    this.addHandler(VIRTUAL_NODES_TAG, ch);
    }
  }



  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  public static void main(String[] args) throws java.io.IOException {
    InitialHandler h = new InitialHandler();
    String uri = "file:///Z:/test/ProActive/descriptors/Runtime.xml";
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
        InitialHandler h = new InitialHandler();
        String uri = xmlDescriptorUrl;
        org.objectweb.proactive.core.xml.io.StreamReader sr =
          new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h);
        sr.read();
        return(ProActiveDescriptorHandler)h.getResultObject();
      }
        catch (org.xml.sax.SAXException e){
        e.printStackTrace();
        System.out.println("a problem occurs when getting the ProActiveDescriptorHandler");
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
    // line added to return a ProactiveDescriptorHandlr object
    private ProActiveDescriptorHandler proActiveDescriptorHandler;
    private InitialHandler() {
      super();
      proActiveDescriptorHandler = new ProActiveDescriptorHandler();
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
      VirtualNode vn = proActiveDescriptor.createVirtualNode(vnName);
      // cyclic
      String cyclic = attributes.getValue("cyclic");
      if (checkNonEmpty(cyclic)) {
        vn.setCyclic(cyclic.equals("true"));
      }
      // localbackup
      String localBackup = attributes.getValue("localBackup");
      if (checkNonEmpty(localBackup)) {
        vn.setLocalBackup(Boolean.getBoolean(localBackup));
      }
    }
  } // end inner class VirtualNodeHandler

}