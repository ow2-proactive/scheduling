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
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

/**
 * This class reads a infrastructure element from XML
 *
 * @author       Lionel Mestre
 * @date         2001/10
 * @version      1.0
 * @copyright    INRIA - Project Oasis
 */
public class JVMProcessHandler extends ProcessHandler {

  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of VirtualNode
  */
  public JVMProcessHandler(ProActiveDescriptor proActiveDescriptor) {
    super(proActiveDescriptor);
    UnmarshallerHandler pathHandler = new PathHandler();
    {
    CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
    cu.addHandler(PATH_TAG, pathHandler);
    this.addHandler(CLASSPATH_TAG, cu);
    }
    BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
    bch.addHandler(PATH_TAG, pathHandler);
    this.addHandler(JAVA_PATH_TAG, bch);
    this.addHandler(POLICY_FILE_TAG, bch);
    this.addHandler(CLASSNAME_TAG, new SingleValueUnmarshaller());
    this.addHandler(PARAMETERS_TAG, new SingleValueUnmarshaller());
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //

  public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    super.startContextElement(name, attributes);
    if (refid == null && ! (targetProcess instanceof JVMProcess)) {
      throw new org.xml.sax.SAXException("JVMProcess defined with a class that do not implements the interface JVMProcess");
    }
  }


  //
  //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
  //

  protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    if (refid != null) return;
    // the fact targetProcess is a JVMProcess is checked in startContextElement
    JVMProcess jvmProcess = (JVMProcess) targetProcess;
    if (name.equals(CLASSPATH_TAG)) {
      String[] paths = (String[]) activeHandler.getResultObject();
      if (paths.length > 0) {
        StringBuffer sb = new StringBuffer();
        String pathSeparator = System.getProperty("path.separator");
        sb.append(paths[0]);
        for (int i=1; i<paths.length; i++) {
          sb.append(pathSeparator);
          sb.append(paths[i]);
        }
        jvmProcess.setClasspath(sb.toString());
      }
    } else if (name.equals(JAVA_PATH_TAG)) {
      String jp = (String) activeHandler.getResultObject();
      jvmProcess.setJavaPath(jp);
    } else if (name.equals(POLICY_FILE_TAG)) {
      jvmProcess.setPolicyFile((String) activeHandler.getResultObject());
    } else if (name.equals(CLASSNAME_TAG)) {
      jvmProcess.setClassname((String) activeHandler.getResultObject());
    } else if (name.equals(PARAMETERS_TAG)) {
      jvmProcess.setParameters((String) activeHandler.getResultObject());
    } else {
      super.notifyEndActiveHandler(name, activeHandler);
    }
  }


  //
  //  ----- INNER CLASSES -----------------------------------------------------------------------------------
  //

  public class SingleValueUnmarshaller extends BasicUnmarshaller {
    public void readValue(String value) throws org.xml.sax.SAXException {
      setResultObject(value);
    }
  }

}