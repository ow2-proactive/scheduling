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
 * A <code>VirtualMachine</code> is a conceptual entity that represents
 * a JVM running a ProActiveRuntime
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualNode
 */
public interface VirtualMachine {

    
	/**
	 * Sets the number of nodes that will be created on this VirtualMachine.
	 * @param nodeNumber
	 * @throws IOException
	 */
  public void setNodeNumber(String nodeNumber) throws java.io.IOException;
  
  
	/**
	 * Returns the number of nodes that will be created on this VirtualMachine
	 * @return String
	 */
  public String getNodeNumber();


	/**
	 * Method setName.
	 * @param s
	 */
  public void setName(String s);
  
  
	/**
	 * Returns the name of this VirtualMachine
	 * @return String
	 */
  public String getName();
  
  
	/**
	 * Sets the acquisitionMethod field to the given value
	 * @param s
	 */
  public void setAcquisitionMethod(String s) ;
  
  
	/**
	 * Returns the AcquisitionMethod value
	 * @return String
	 */
  public String getAcquisitionMethod() ;
  
  
	/**
	 * Sets the process mapped to this VirtualMachine to the given process
	 * @param p
	 */
  public void setProcess(ExternalProcess p) ;
  
  
	/**
	 * Returns the process mapped to this VirtualMachine
	 * @return ExternalProcess
	 */
  public ExternalProcess getProcess() ;
  
  
  /**
   * Returns the name of the machine where the process mapped to this virtual machine 
   * was launched.
   * @return String
   */

  public String getHostName();
  
  
	/**
	 * Sets the creatorId field to the given value
	 * @param value The Id of the VirtualNode that created this VirtualMachine
	 */
  public void setCreatorId(String creatorId);
  
  
	/**
	 * Returns the value of creatorId field. 
	 * @return String The Id of the VirtualNode that created this VirtualMachine
	 */
  public String getCreatorId();

}
