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

import org.objectweb.proactive.core.event.RuntimeRegistrationEventListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

/**
 * A <code>VirtualNode</code> is a conceptual entity that represents one or several nodes. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 *
 * @author  ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptor
 * @see VirtualMachine
 */
public interface VirtualNode extends RuntimeRegistrationEventListener
{

 
	/**
	 * Method setCyclic.
	 * @param b the value of cyclic attribute, the default value is false
	 */
  public void setCyclic(boolean b);
  
  
	/**
	 * Returns the value of cyclic attribute.
	 * @return boolean
	 */
  public boolean getCyclic();


	/**
	 * Method setLocalBackup.
	 * @param b the value of localbackup attribute, the default value is false
	 */
  public void setLocalBackup(boolean b);
  
  
	/**
	 * Returns the value of localbackup attribute.
	 * @return boolean
	 */
  public boolean getLocalBackup();


	/**
	 * Method setName.
	 * @param s
	 */
  public void setName(String s);
  
  
	/**
	 * Returns the name of this VirtualNode
	 * @return String
	 */
  public String getName();
  
  
	/**
	 * Adds a VirtualMachine entity to this VirtualNode
	 * @param virtualMachine
	 */
  public void addVirtualMachine(VirtualMachine virtualMachine);
  
  
	/**
	 * Returns the virtualMachine entity linked to this VirtualNode or if cyclic, returns
	 * one of the VirtualMachines linked to this VirtualNode with a cyclic manner(internal count incremented each time this method is called). 
	 * @return VirtualMachine
	 */
  public VirtualMachine getVirtualMachine() ;
  
  
  /**
   * Activates all the Nodes mapped to this VirtualNode in the XML Descriptor
   */
  public void activate();
  
  
//  /**
//   * Desactivate all the Nodes mapped to This VirtualNode in the XML Descriptor
//   */
//  public void desactivate();
  
  
  /**
   * Returns the number of Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int getNodeCount();
  
  
  /**
   * Returns the number of Nodes already created among the Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int createdNodeCount();
  
  
  
  /**
   * Returns the first Node created among Nodes mapped to this VirtualNode in the XML Descriptor 
   * Another call to this method will return the following created node if any. Note that the order
   * in which Nodes are created has nothing to do with the order defined in the XML descriptor.
   * @return Node
   */
  public Node getNode() throws NodeException;

  
  
  /**
   * Returns the Node mapped to this VirtualNode with the specified index. There is no relationship between,
   * the order in the xml descriptor and the order in the array.
   * @param index
   * @return Node the node at the specified index in the array of nodes mapped to this VirtualNode
   */
  public Node getNode(int index);
  
  
	/**
	 * Returns all nodes url mapped to this VirualNode
	 * @return String[]. An array of string containing the url of all nodes mapped to
	 * this VirtualNode in the XML descriptor.
	 */
  public String[] getNodesURL() throws NodeException;
  
  
	/**
	 * Returns all nodes mapped to this VirtualNode
	 * @return Node[] An array of Node conataining all the nodes mapped to this
	 * VirtualNode in the XML descriptor
	 */
  public Node[] getNodes() throws NodeException;
  
  
	/**
	 * Returns the node of the given url among nodes mapped to this VirtualNode in the xml
	 * descriptor or null if such node does not exist.
	 * @param name
	 * @return Node the node of the given url or null if such node does not exist
	 */
  public Node getNode(String url) throws NodeException;
  	
 
}