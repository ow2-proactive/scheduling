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

import java.rmi.dgc.VMID;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.event.CommunicationEventListener;
import org.objectweb.proactive.ic2d.event.SpyEventListener;
import org.objectweb.proactive.ic2d.event.VMObjectListener;
import org.objectweb.proactive.ic2d.spy.Spy;
import org.objectweb.proactive.ic2d.spy.SpyEvent;
import org.objectweb.proactive.ic2d.spy.SpyMessageEvent;

/**
 * Holder class for the host data representation
 */
public class VMObject extends AbstractDataObject {
	
	static Logger log4jlogger = Logger.getLogger(VMObject.class.getName());

  private static String SPY_LISTENER_NODE_NAME = "SpyListenerNode";
  private static Node SPY_LISTENER_NODE;
  static {
    String currentHost;
    try {
      currentHost = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException e) {
      currentHost = "localhost";
    }
    //System.out.println("current host: "+currentHost);
    try {
    	// TODO add security here
      SPY_LISTENER_NODE = NodeFactory.createNode("//"+currentHost+"/"+SPY_LISTENER_NODE_NAME, true,null,null);
    } catch (NodeException e) {
      SPY_LISTENER_NODE = null;
    }
  }
  
  protected Spy spy;
  protected VMID vmid;
  protected String protocolId;
  protected java.util.HashMap objectNodeMap;

  protected SpyListenerImpl activeSpyListener;

  protected VMObjectListener listener;
  
  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  public VMObject(HostObject host, VMID vmid, Node node, String protocolId) throws ActiveObjectCreationException, NodeException {
    super(host);
    //System.out.println("nodeURL : "+node.getNodeInformation().getURL());
    if (log4jlogger.isDebugEnabled()){
    log4jlogger.debug ("VMObject.<init>");
    }
    this.vmid = vmid;
    this.protocolId = protocolId;
    this.objectNodeMap = new java.util.HashMap();
    SpyListenerImpl spyListener = new SpyListenerImpl(new MySpyEventListener());
		if (log4jlogger.isDebugEnabled()){
    	log4jlogger.debug("VMObject.<init> creating activeSpyListener");
		}
    this.activeSpyListener = (SpyListenerImpl) ProActive.turnActive(spyListener, SPY_LISTENER_NODE);
		if (log4jlogger.isDebugEnabled()){
    log4jlogger.debug("VMObject.<init> creating spy");
		}
    this.spy = (Spy) ProActive.newActive(Spy.class.getName(), new Object[] {activeSpyListener} , node);
    addNodeObject(node);
    controller.log("VMObject id="+vmid+" created based on node "+node.getNodeInformation().getURL());
  }


  //
  // -- PUBLIC METHOD -----------------------------------------------
  //


  public String toString() {
    return "VM id=" + vmid + "\n" + super.toString();
  }
  
  

  //
  // Event Listener
  //
    
  public void registerListener(VMObjectListener listener) {
    this.messageMonitoringListener = listener;
    this.listener = listener;
    // notify existing childs
    notifyListenerOfExistingChilds();
    sendEventsForAllActiveObjects();
  }
  
  
  //
  // Accessor methods
  //
  
  public void migrateTo(UniqueID objectID, String nodeTargetURL) throws MigrationException {
    try {
      spy.migrateTo(objectID, nodeTargetURL);
    } catch (MigrationException e) {
      throw e;                         
    } catch (Exception e) {
      recoverExceptionInSpy(e);
      throw new MigrationException("Problem contacting the Spy", e);
    }
  }

  
  public VMID getID() {
    return vmid;
  }
  
  public String getProtocolId(){
  	return this.protocolId;
  } 
  
  public int getActiveObjectsCount() {
    return objectNodeMap.size();
  }

  
  public String getSystemProperty(String key) {
    try {
      return spy.getSystemProperty(key);
    } catch (Exception e) {
      recoverExceptionInSpy(e);
      return "! Error occured";
    }
  }


  public long getUpdateFrequence() {
    try {
      return spy.getUpdateFrequence();
    } catch (Exception e) {
      recoverExceptionInSpy(e);
      return 0;
    }
  }


  public void setUpdateFrequence(long updateFrequence) {
    try {
      spy.setUpdateFrequence(updateFrequence);
    } catch (Exception e) {
      recoverExceptionInSpy(e);
    }
  }
  
 
  public void sendEventsForAllActiveObjects() {
  	if (log4jlogger.isDebugEnabled()){
    log4jlogger.debug("VMObject.sendEventForAllActiveObjects()");
  	}
    try {
      spy.sendEventsForAllActiveObjects();
    } catch (Exception e) {
      recoverExceptionInSpy(e);
    }
  }



  //
  // Node related methods
  //
  
  public NodeObject addNodeObject(Node node) {
  	if (log4jlogger.isDebugEnabled()){
    log4jlogger.debug("VMObject: addNodeObject()");
  	}
    String nodeName = node.getNodeInformation().getName();
    NodeObject nodeObject = (NodeObject) getChild(nodeName);
    if (nodeObject == null) {
      nodeObject = new NodeObject(this, node);
      putChild(nodeName, nodeObject);
      if (listener != null) listener.nodeObjectAdded(nodeObject);
      sendEventsForAllActiveObjects();
    }
    return nodeObject;
  }


  public NodeObject getNodeObject(String nodeName) {
    return (NodeObject) getChild(nodeName);
  }


  public NodeObject getNodeObject(UniqueID bodyID) {
    return (NodeObject) objectNodeMap.get(bodyID);
  }


  public void removeNodeObject(String nodeName) {
    // remove the node
    NodeObject nodeObject = (NodeObject) removeChild(nodeName);
    if (nodeObject == null) {
      controller.log("The node "+nodeName+" does not exist. Cannot remove it");
    } else {
      if (listener != null) listener.nodeObjectRemoved(nodeObject);
    }
  }
  
  
  public void destroyObject() {
    getTypedParent().removeVMObject(vmid);
  }
 

  //
  // -- PROTECTED METHOD -----------------------------------------------
  //
  
  protected void registerActiveObject(UniqueID id, NodeObject nodeObject) {
    objectNodeMap.put(id, nodeObject);
  }
  
  protected void unregisterActiveObject(UniqueID id) {
    objectNodeMap.remove(id);
  }


  protected synchronized boolean destroy() {
    if (super.destroy()) {
      try {
        spy.terminate();
      } catch (Exception e) {}
      activeSpyListener.terminate();
      objectNodeMap.clear();
      spy = null;
      activeSpyListener = null;
      listener = null;
      return true;
    } else {
     return false;
    }
  }


  protected void monitoringMessageEventChanged(ActiveObject object, boolean value) {
    try {
      if (value) {
        spy.addMessageEventListener(object.getID());
      } else {
        spy.removeMessageEventListener(object.getID());
      }
      super.monitoringMessageEventChanged(object, value);
    } catch (Exception e) {
      recoverExceptionInSpy(e);
    }
  }
  
  
  protected HostObject getTypedParent() {
    return (HostObject)parent;
  }
  
  

  //
  // -- PRIVATE METHOD -----------------------------------------------
  //
    
  private void recoverExceptionInSpy(Exception e) {
    controller.log("Exception occured while contacting Spy for VM "+vmid+". Now removing the VM from IC2D.",e);
    destroyObject();
  }
  
  
  private ActiveObject findActiveObject(UniqueID id) {
    NodeObject nodeObject = getNodeObject(id);
    if (nodeObject == null) {
      controller.log("!! Event received for an unknown node, id="+id);
      return null; // unknown node
    }
    ActiveObject ao = nodeObject.getActiveObject(id);
    if (ao == null) {
      controller.log("!! Event received for an unknown active object, id="+id);
    }
    return ao;
  }
  

  private synchronized void notifyListenerOfExistingChilds() {
    if (getChildObjectsCount() == 0) return;
    java.util.Iterator iterator = childsIterator();
    while (iterator.hasNext()) {
      NodeObject nodeObject = (NodeObject) iterator.next();
      listener.nodeObjectAdded(nodeObject);
    }
  }
  

//  private String getNodeNameFromURL(String nodeURL) {
//    int n = nodeURL.indexOf('/', 2); // looking for the end of the host
//    if (n < 3) return nodeURL;
//    return nodeURL.substring(n+1);
//  }
  
  
  //
  // -- INNER CLASSES -----------------------------------------------
  //
  

  private class MySpyEventListener implements SpyEventListener {

    private CommunicationEventListener communicationEventListener;
    
    public MySpyEventListener() {
      communicationEventListener = ((IC2DObject)getTopLevelParent()).getCommunicationEventListener();
    }

    //
    // -- Implement SpyEventListener -----------------------------------------------
    //
  
    public void activeObjectAdded(UniqueID id, String nodeURL, String classname, boolean isActive) {
      //String nodeName = getNodeNameFromURL(nodeURL);
      String nodeName = UrlBuilder.getNameFromUrl(nodeURL);
      //System.out.println("NodeName "+nodeName+" AO id "+id);
      NodeObject nodeObject = getNodeObject(nodeName);
      if (nodeObject != null) {
        nodeObject.addActiveObject(classname, id, isActive);
      }
    }
  
    public void activeObjectChanged(UniqueID id, boolean isActive, boolean isAlive) {
      ActiveObject object = findActiveObject(id);
      //System.out.println("activeObjectChanged object="+object.getName()+" isActive="+isActive);
      if (object == null) return;
      if (! isAlive) {
        object.destroyObject();
      } 
    }
    
    public void objectWaitingForRequest(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setServingStatus(ActiveObject.STATUS_WAITING_FOR_REQUEST);
      object.setRequestQueueLength(0);
      communicationEventListener.objectWaitingForRequest(object, spyEvent);
    }
  
    public void objectWaitingByNecessity(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setServingStatus(object.getServingStatus() == ActiveObject.STATUS_SERVING_REQUEST ?
                         ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_SERVING :
                         ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_ACTIVE);
      communicationEventListener.objectWaitingByNecessity(object, spyEvent);
    }
  
    public void objectReceivedFutureResult(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      switch (object.getServingStatus()) {
        case ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_SERVING : 
          object.setServingStatus(ActiveObject.STATUS_SERVING_REQUEST);
          break;
        case ActiveObject.STATUS_WAITING_BY_NECESSITY_WHILE_ACTIVE : 
          object.setServingStatus(ActiveObject.STATUS_ACTIVE);
          break;
      }
    }
  
    public void requestMessageSent(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) {  return; }
      if (! object.isMonitoringRequestSender()) return;
      communicationEventListener.requestMessageSent(object, spyEvent);
    }
  
    public void replyMessageSent(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setRequestQueueLength(((SpyMessageEvent)spyEvent).getRequestQueueLength());
      object.setServingStatus(ActiveObject.STATUS_ACTIVE);
      if (! object.isMonitoringReplySender()) return;
      communicationEventListener.replyMessageSent(object, spyEvent);
    }
    
    public void requestMessageReceived(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setRequestQueueLength(((SpyMessageEvent)spyEvent).getRequestQueueLength());
      if (! object.isMonitoringRequestReceiver()) return;
      communicationEventListener.requestMessageReceived(object, spyEvent);
    }
  
    public void replyMessageReceived(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      if (! object.isMonitoringReplySender()) return;
      communicationEventListener.replyMessageReceived(object, spyEvent);
    }
    
    public void voidRequestServed(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setRequestQueueLength(((SpyMessageEvent)spyEvent).getRequestQueueLength());
      object.setServingStatus(ActiveObject.STATUS_ACTIVE);
      if (! object.isMonitoringReplySender()) return;
      communicationEventListener.voidRequestServed(object, spyEvent);
    }
    
    public void servingStarted(UniqueID id, SpyEvent spyEvent) {
      if (! controller.isMonitoring()) return;
      ActiveObject object = findActiveObject(id);
      if (object == null) { return; }
      object.setRequestQueueLength(((SpyMessageEvent)spyEvent).getRequestQueueLength());
      object.setServingStatus(ActiveObject.STATUS_SERVING_REQUEST);
    }
    
    public void allEventsProcessed() {
      if (! controller.isMonitoring()) return;
      communicationEventListener.allEventsProcessed();
    }

  } // end inner class MySpyEventListener
  


}
