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

/**
 * This class represents a VirtualNode
 *
 * @author       Lionel Mestre
 * @date         2001/10
 * @version      1.0
 * @copyright    INRIA - Project Oasis
 */
public class VirtualNode {

  //
  //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
  //

  /** the name of this VirtualNode */
  private String name;

  /** true if this VirtualNode is cyclic */
  private boolean cyclic;

  /** true if this VirtualNode should be created localy in case the creation of the target jvm fails */
  private boolean localBackup;

  /** the list of virtual machines associated with this VirtualNode */
  private java.util.ArrayList virtualMachines;
  
  /** index of the last associated jvm used */
  private int lastVirtualMachineIndex;


  //
  //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
  //

 /**
  * Contructs a new intance of VirtualNode
  */
  VirtualNode() {
    virtualMachines = new java.util.ArrayList(5);
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


  public void setLocalBackup(boolean b) {
    localBackup = b;
  }
  
  public boolean getLocalBackup() {
    return localBackup;
  }


  public void setName(String s) {
    name = s;
  }
  
  public String getName() {
    return name;
  }
  
  
  public void addVirtualMachine(VirtualMachine virtualMachine) {
    virtualMachines.add(virtualMachine);
    System.out.println("mapped VirtualNode="+name+" with VirtualMachine="+virtualMachine.getName());
  }

  public VirtualMachine getVirtualMachine() {
    if (virtualMachines.isEmpty()) return null;
    VirtualMachine vm = (VirtualMachine) virtualMachines.get(lastVirtualMachineIndex);
    if (cyclic) {
      lastVirtualMachineIndex = (lastVirtualMachineIndex + 1) % virtualMachines.size();
    }
    return vm;
  }
  
}