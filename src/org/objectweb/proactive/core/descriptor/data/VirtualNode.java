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
 * A <code>VirtualNode</code> represents a conceptual entity. After activation
 * a <code>VirtualNode</code> represents one or several nodes.
 *
 * @author  ProActive Team
 * @version 1.0,  2002/06/20
 * @since   ProActive 0.9.3
 */
public interface VirtualNode extends RuntimeRegistrationEventListener
{

 

 
  public void setCyclic(boolean b);
  
  
  public boolean getCyclic();


  public void setLocalBackup(boolean b);
  
  public boolean getLocalBackup();


  public void setName(String s);
  
  
  public String getName();
  
  
  public void addVirtualMachine(VirtualMachine virtualMachine);
  
  
  public VirtualMachine getVirtualMachine() ;
  
  
  /**
   * Activates all the Nodes mapped to this VirtualNode in the XML Descriptor
   */
  public void activate();
  
  
  /**
   * Desactivate all the Nodes mapped to This VirtualNode in the XML Descriptor
   */
  public void desactivate();
  
  
  /**
   * Returns the number of Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int getNodeCount();
  
  
  /**
   * Returns the number of Nodes already created among the Nodes mapped to this VirtualNode in the XML Descriptor
   * @return int
   */
  public int CreatedNodeCount();
  
  
//  /**
//   * Waits until at least one Node mapped to this VirtualNode in the XML Descriptor is created
//   */
//  public void waitForNodeCreation() throws NodeException;
  
  
  /**
   * Returns the first Node available among Nodes mapped to this VirtualNode in the XML Descriptor 
   * @return Node
   */
  public Node getNode() throws NodeException;

  
  
  /**
   * Returns the Node mapped to this VirtualNode with the specified index(in the XML Descriptor
   * @param index
   * @return Node
   */
  public Node getNode(int index);
  
  
	/**
	 * Returns all nodes name mapped to this VirualNode
	 * @return String[]. An array of string containing the name of all nodes mapped to
	 * this VirtualNode in the XML descriptor.
	 */
  public String[] getNodesURL() throws NodeException;
  
  
	/**
	 * Returns all nodes mapped to this VirtualNode
	 * @return Node[] An array of Node conataining all the nodes mapped to this
	 * VirtualNode in the XML descriptor
	 */
  public Node[] getNodes() throws NodeException;
  
  
//  /**
//   * Returns the Node mapped to this VirtualNode in the XML Descriptor with the specified name
//   * @param name
//   * @return Node
//   */
//  public Node getNode(String name) throws NodeException{
//    Node node = null;
//    try{
//    node = NodeFactory.getNode(name);
//    }catch (Exception ne){
//    ne.printStackTrace();
//    throw new NodeException("Cannot get the node of the name"+name);
//    } 
//    createdNodes.add(node);
//    return node;
//		return null;
 // }
  
  
  
   
}