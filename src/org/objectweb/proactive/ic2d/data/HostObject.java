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
package org.objectweb.proactive.ic2d.data;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ic2d.event.HostObjectListener;
import org.objectweb.proactive.ic2d.util.HostNodeFinder;
import org.objectweb.proactive.ic2d.util.IbisHostNodeFinder;
import org.objectweb.proactive.ic2d.util.RMIHostNodeFinder;
import org.objectweb.proactive.ic2d.util.RunnableProcessor;

/**
 * Holder class for the host data representation
 */
public class HostObject extends AbstractDataObject {

  /** Name of this Host  */
  protected String hostname;
  
  /** OS */
  protected String os;
  
  protected HostNodeFinder nodeFinder;

  protected HostObjectListener listener;

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public HostObject(WorldObject parent, String hostname, String protocol) {
    super(parent);
    try {
      this.hostname = java.net.InetAddress.getByName(hostname).getHostName();
      //controller.log("HostObject "+this.hostname+ " created");
    } catch (java.net.UnknownHostException e) {
      this.hostname = hostname;
      controller.warn("Hostname "+hostname+ " failed reverse lookup.");
    }
    if ("ibis".equals(protocol)) {
		this.nodeFinder = new IbisHostNodeFinder(controller);
    }
     else {
		this.nodeFinder = new RMIHostNodeFinder(controller);
     }
  }


  //
  // -- PUBLIC METHODS -----------------------------------------------
  //

  public String toString() {
    return "Host: " + hostname + "\n" + super.toString();
  }
  

  public void createAllNodes() {
    RunnableProcessor.getInstance().processRunnable("Create nodes for "+hostname, new CreateNodeTask(), controller);
  }  
  
  
  public void createOneNode(String nodeName) {
    RunnableProcessor.getInstance().processRunnable("Create one node for "+hostname, new CreateNodeTask(nodeName), controller);
  }  
  
    
  //
  // accessor methods
  //
  
  public String getHostName() {
    return hostname;
  }
  
  
  public String getOperatingSystem() {
    return os;
  }


  public boolean isGlobusEnabled() {
  	if (this.hostname.indexOf("globus") != -1) return true;
    return false;
  }
  

  //
  // Event Listener
  //
    
  public void registerListener(HostObjectListener listener) {
    this.messageMonitoringListener = listener;
    this.listener = listener;
  }


  //
  // VM related methods
  //
    
  /**
   * Register the node
   */
  public VMObject addVMObject(Node node) {
    java.rmi.dgc.VMID vmid = node.getNodeInformation().getVMID();
    VMObject vmObject = getVMObject(vmid);
    if (vmObject != null) {
      controller.log("The node "+node.getNodeInformation().getURL()+" belongs to an already existing vm id="+vmid);
      // add the node to the existing vm in case it doesn't exist
      vmObject.addNodeObject(node);
      // refresh ActiveObject for this vm
      vmObject.sendEventsForAllActiveObjects();
      return vmObject;
    }
    try {
      vmObject = new VMObject(this, vmid, node);
      putChild(vmid, vmObject);
      controller.log("The node "+node.getNodeInformation().getURL()+" has been found on vm id="+vmid);
      if (listener != null) listener.vmObjectAdded(vmObject);
      if (os == null) {
        os = vmObject.getSystemProperty("os.name");
        if (listener != null) listener.operatingSystemFound(os);
      }
      return vmObject;
    } catch (ActiveObjectCreationException e) {
      controller.log("Cannot create the spy on host "+hostname+" on node "+node.getNodeInformation().getURL(),e);
      return null;
    } catch (NodeException e) {
      controller.log("Problem with the node "+node.getNodeInformation().getURL(),e);
      return null;
    }
  }
    
  
  public void removeVMObject(java.rmi.dgc.VMID id) {
    VMObject vmObject = (VMObject) removeChild(id);
    if (vmObject != null && listener != null) listener.vmObjectRemoved(vmObject);
  }


  public VMObject getVMObject(java.rmi.dgc.VMID id) {
    return (VMObject) getChild(id);
  }


  public void destroyObject() {
    getTypedParent().removeHostObject(hostname);
  }
 
  
  public synchronized VMObject findVMObjectHavingExistingNode(String nodeName) {
    if (getChildObjectsCount() == 0) return null;
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      VMObject vmObject = (VMObject) iterator.next();
      if (vmObject.getNodeObject(nodeName) != null) {
        controller.log("Found that vm id="+vmObject.getID()+" own the node "+nodeName);
        return vmObject;
      }
    }
    return null;
  }


  //
  // -- PROTECTED METHOD -----------------------------------------------
  //
  
  protected WorldObject getTypedParent() {
    return (WorldObject)parent;
  }


  protected synchronized boolean destroy() {
    // destroy all childs
    if (super.destroy()) {
      // remove ref on other object
      listener = null;
      nodeFinder = null;
      return true;
    } else {
     return false;
    }
  }





  //
  // -- INNER CLASSES -----------------------------------------------
  //
  
  private class CreateNodeTask implements Runnable {
    private String targetNodeName;
    
    public CreateNodeTask() {
    }
    
    public CreateNodeTask(String targetNodeName) {
      this.targetNodeName = targetNodeName;
    }
    
    public void run() {
      Node[] nodes;
      try {
        nodes = nodeFinder.findNodes(hostname);
      //  System.out.println("XXXXXXX");
      } catch (java.io.IOException e) {
        controller.log("There is no RMI Registry on host "+hostname, e);
        return;
      }
      if (nodes.length == 0) {
        controller.warn("A RMIRegistry has been found on host "+hostname+" but no Node object are bound !");
      }
      for (int i = 0; i < nodes.length; i++) {
        Node node = nodes[i];
        //System.out.println("nodeURL "+node.getNodeInformation().getURL());
        String nodeName = node.getNodeInformation().getName();
        if ((targetNodeName == null) || targetNodeName.equals(nodeName)) {
          VMObject vmObject = findVMObjectHavingExistingNode(nodeName);
          if (vmObject == null) {
            // new NodeObject
            addVMObject(node);
          } else {
            controller.log("The node "+nodeName+" is already known by host "+hostname+" look for new objects");
            vmObject.sendEventsForAllActiveObjects();
          }
        }
      }
      if ((targetNodeName != null) && (findVMObjectHavingExistingNode(targetNodeName) == null)) {
        controller.warn("The node "+targetNodeName+" was not found on host "+hostname+". Check the name of the node");
      }
    }
  } // end inner class CreateNodeTask


}
