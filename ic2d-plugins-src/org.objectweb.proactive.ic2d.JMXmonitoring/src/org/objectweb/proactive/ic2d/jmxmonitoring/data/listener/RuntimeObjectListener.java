package org.objectweb.proactive.ic2d.jmxmonitoring.data.listener;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.BodyNotificationData;
import org.objectweb.proactive.core.jmx.notification.FutureNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RuntimeNotificationData;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.State;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NamesFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;

public class RuntimeObjectListener implements NotificationListener{

	private RuntimeObject runtimeObject;

	public RuntimeObjectListener(RuntimeObject runtimeObject){
		this.runtimeObject = runtimeObject;
	}

	public void handleNotification(Notification notification, Object handback) {
		String type = notification.getType();
		// --- FutureEvent ----------------------
		if(type.equals(NotificationType.receivedFutureResult)){
			//System.out.println("...............................Received Future Result");
			FutureNotificationData notificationData = (FutureNotificationData) notification.getUserData();
			/*UniqueID id = event.getId();*/
			UniqueID id = notificationData.getBodyID();
			//ObjectName oname = event.getObjectName();
			ActiveObject waitingAO = runtimeObject.getWorldObject().findActiveObject(id);
			if(waitingAO==null){
				String nd = NamesFactory.getInstance().getName(id);
				System.err.println("RuntimeObjectListener "+nd+" id not found: "+id);
				return;
			}	
			State aoState = waitingAO.getState();
			switch (aoState) {
			case WAITING_BY_NECESSITY_WHILE_ACTIVE:
				waitingAO.setState(State.ACTIVE);
				break;
			case WAITING_BY_NECESSITY_WHILE_SERVING:
				waitingAO.setState(State.SERVING_REQUEST);
				break;
			default:
				waitingAO.setState(State.UNKNOWN);
				break;
			}
		}
		else if(type.equals(NotificationType.waitByNecessity)){
			FutureNotificationData notificationData = (FutureNotificationData) notification.getUserData();
			/*UniqueID id = event.getId();*/
			//ObjectName oname = notificationDada.getObjectName();
			UniqueID id = notificationData.getBodyID();
			ActiveObject waitingAO = runtimeObject.getWorldObject().findActiveObject(id);
			if(waitingAO==null){
				String nd = NamesFactory.getInstance().getName(id);
				System.err.println("RuntimeObjectListener "+nd+" id not found: "+id);
				return;
			}	
			State aoState = waitingAO.getState();
			if(aoState==State.SERVING_REQUEST)
				waitingAO.setState(State.WAITING_BY_NECESSITY_WHILE_SERVING);
			else
				waitingAO.setState(State.WAITING_BY_NECESSITY_WHILE_ACTIVE);
		}
		// --- BodyEvent ------------------------
		else if(type.equals(NotificationType.bodyChanged)){
			System.out.println("...............................Body Changed");
			/*IC2DEvent ic2dEvent = (IC2DEvent) notification.getUserData();
			if(!ic2dEvent.isAlive()){
				UniqueID id = ic2dEvent.getId();
				ActiveObject ao = runtimeObject.getWorldObject().findActiveObject(id);
				if(ao==null){
					System.err.println("RuntimeObjectListener id not found: "+id);
					return;
				}	
				ao.destroy();
			}*/
		}
		else if(type.equals(NotificationType.bodyCreated)){
			BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
			UniqueID id = notificationData.getId();
			String nodeUrl = notificationData.getNodeUrl();
			String name = notificationData.getClassName();
			ObjectName oname = FactoryName.createActiveObjectName(id);
			System.out.println("...............................Body Created "+notification.getSource());
			NodeObject node = (NodeObject)runtimeObject.getChild(nodeUrl);
			if(node!=null){
				node.addChild(new ActiveObject(node,id, name, oname));
			}
			else{
				System.out.println("RuntimeObjectListener.handleNotification() node pas trouve nodeUrl="+nodeUrl);
			}
			
			/*BodyCreationEvent bodyCreationEvent = (BodyCreationEvent) notification.getUserData();
			UniqueID id = bodyCreationEvent.getId();
			ObjectName oname = bodyCreationEvent.getObjectName();
			System.out.println("...............................Body Created id:"+id);
			NodeObject node = (NodeObject)runtimeObject.getChild(bodyCreationEvent.getNodeUrl());
			if(node!=null){
				node.addChild(new ActiveObject((NodeObject)node, id, bodyCreationEvent.getClassName(), oname));
			}
			else
				System.out
						.println("RuntimeObjectListener.handleNotification() node pas trouve");*/
		}
		else if(type.equals(NotificationType.bodyDestroyed)){
			BodyNotificationData notificationData = (BodyNotificationData) notification.getUserData();
			UniqueID id = notificationData.getId();
			// ObjectName oname = Name.createActiveObjectName(id);
			System.out.println("...............................Body Destroyed "+notification.getSource());
			runtimeObject.getWorldObject().removeActiveObject(id);
			
//			IC2DEvent event = (IC2DEvent) notification.getUserData();
//			/*UniqueID id = event.getId();*/
//			ObjectName oname = event.getObjectName();
//			UniqueID id = event.getId();
//			System.out.println("...............................Body Destroyed oname:"+/*id*/oname);
//			
//			runtimeObject.getWorldObject().removeActiveObject(id);
		}
		else if(type.equals(NotificationType.runtimeRegistered)){
			System.out.println("...............................Runtime Registered "+notification.getSource());
			RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
			runtimeObject.getParent().proposeChild();
		}
		else if(type.equals(NotificationType.runtimeUnregistered)){
			System.out.println("...............................Runtime Unregistered "+notification.getSource());
			RuntimeNotificationData userData = (RuntimeNotificationData) notification.getUserData();
		}
		// --- NodeEvent ----------------
		else if(type.equals(NotificationType.nodeCreated)){
			System.out.println("...............................Node Created");
			Node node = (Node)notification.getUserData();
			String nodeUrl = node.getNodeInformation().getURL();
			ObjectName oname = FactoryName.createNodeObjectName(runtimeObject.getUrl(), node.getNodeInformation().getName());
			NodeObject child = new NodeObject(runtimeObject,nodeUrl,oname);
			runtimeObject.addChild(child);
		}
		else if(type.equals(NotificationType.nodeDestroyed)){
			String nodeUrl = (String) notification.getUserData();
			System.out.println("...............................Node Destroyed : " + nodeUrl);
			NodeObject node = (NodeObject) runtimeObject.getChild(nodeUrl);
			if(node!=null){
				node.destroy();
			}			
		}
		else
			System.out.println(runtimeObject+" *=> "+type);
	}

}
