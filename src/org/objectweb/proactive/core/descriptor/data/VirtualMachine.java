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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.core.process.ExternalProcess;

/**
 * This class represents a VirtualNode
 *
 * @author       Lionel Mestre
 * @version      1.0
 */
public class VirtualMachine {

  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  /** the name of this VirtualMachine */
  private String name;

  /** true if this VirtualMachine is cyclic */
  private boolean cyclic;
  
  /** the acquisition method to use to find the VirtualMachine once created */
  private String acquisitionMethod;

  /** the process to start in order to create the JVM */
  private ExternalProcess process;

  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of VirtualNode
  */
  VirtualMachine() {
  }


  //
  //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
  //
  
  public void setCyclic(boolean b) {
    cyclic = b;
  }
  
  public boolean getCyclic() {
    return cyclic;
  }


  public void setName(String s) {
    name = s;
  }
  
  public String getName() {
    return name;
  }
  
  
  public void setAcquisitionMethod(String s) {
    acquisitionMethod = s;
  }
  
  
  public String getAcquisitionMethod() {
    return acquisitionMethod;
  }
  
  
  public void setProcess(ExternalProcess p) {
    process = p;
  }
  
  
  public ExternalProcess getProcess() {
    return process;
  }
  
  
}