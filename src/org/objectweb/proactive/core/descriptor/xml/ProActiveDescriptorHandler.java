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
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 *
 * Receives SAX event and pass them on
 *
 * @author       Lionel Mestre
 * @date         2002/02
 * @version      0.91
 * @copyright    INRIA - Project Oasis
 *
 */
public class ProActiveDescriptorHandler extends AbstractUnmarshallerDecorator implements ProActiveDescriptorConstants {

  private ProActiveDescriptor proActiveDescriptor;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public ProActiveDescriptorHandler() {
    super();
    proActiveDescriptor = new ProActiveDescriptor();
    addHandler(DEPLOYMENT_TAG, new DeploymentHandler(proActiveDescriptor));
    addHandler(INFRASTRUCTURE_TAG, new InfrastructureHandler(proActiveDescriptor));
  }



  //
  // -- PUBLIC METHODS -----------------------------------------------
  //


  public static void main(String[] args) throws java.io.IOException {
    InitialHandler h = new InitialHandler();
    String uri = "file:///D:/cygwin/home/lmestre/ProActive/ProActiveDescriptor2.xml";
    org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(uri), h);
    sr.read();
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
    private InitialHandler() {
      super();
      this.addHandler(PROACTIVE_DESCRIPTOR_TAG, new ProActiveDescriptorHandler());
    }
    public Object getResultObject() throws org.xml.sax.SAXException {
      return null;
    }
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    }
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }
  }

}