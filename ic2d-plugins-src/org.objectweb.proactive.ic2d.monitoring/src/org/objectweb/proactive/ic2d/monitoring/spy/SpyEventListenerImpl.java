/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.ic2d.monitoring.spy;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.data.State;

public class SpyEventListenerImpl implements SpyEventListener, Serializable{

	public final static boolean DEFAULT_IS_MONITORING = true;
	private static boolean isMonitoring = DEFAULT_IS_MONITORING;
	
	private NodeObject nodeObject;

	public SpyEventListenerImpl(){

	}

	public SpyEventListenerImpl(NodeObject nodeObject){
		this.nodeObject = nodeObject;
	}

	public static void SetMonitoring(boolean monitoring){
		isMonitoring = monitoring;
	}
	
	public static boolean isMonitoring(){
		return isMonitoring;
	}

	public void activeObjectAdded(UniqueID id, String jobID, String nodeURL, String className, boolean isActive) {
		AOObject ao = getActiveObject(id);
		if(ao==null){
			ao = new AOObject(nodeObject, className.substring(className.lastIndexOf(".")+1), id, jobID);
			nodeObject.exploreChild(ao);
		}
	}

	public void activeObjectChanged(UniqueID id, boolean isActive, boolean isAlive) {
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;

		if(!isActive){
			ao.resetCommunications();
			nodeObject.removeChild(ao);
		}
	}

	public void objectWaitingForRequest(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;
		ao.setState(State.WAITING_FOR_REQUEST);
		ao.setRequestQueueLength(0);
	}

	public void objectWaitingByNecessity(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;

		//TODO is correct?
		State state = ao.getState();
		if(state == State.MIGRATING)
			return;

		ao.setState((state == State.SERVING_REQUEST)
				?State.WAITING_BY_NECESSITY_WHILE_SERVING
						:State.WAITING_BY_NECESSITY_WHILE_ACTIVE);
	}

	public void objectReceivedFutureResult(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;
		switch (ao.getState()) {
		case WAITING_BY_NECESSITY_WHILE_SERVING:
			ao.setState(State.SERVING_REQUEST);
			break;
		case WAITING_BY_NECESSITY_WHILE_ACTIVE:
			ao.setState(State.ACTIVE);
			break;
		}
	}

	public void requestMessageSent(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
	}

	public void replyMessageSent(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;
		ao.setState(State.ACTIVE);
		ao.setRequestQueueLength(((SpyMessageEvent) spyEvent).getRequestQueueLength());
		
	}

	public void requestMessageReceived(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject target = nodeObject.findActiveObjectById(id);
		if(target == null)
			return;

		//TODO is correct?
		if(target.getState()==State.MIGRATING)
			return;

		target.setState(State.SERVING_REQUEST);
		target.setRequestQueueLength(((SpyMessageEvent) spyEvent).getRequestQueueLength());

		UniqueID sourceId = ((SpyMessageEvent) spyEvent).getSourceBodyID();
		
		
		AOObject source = target.getWorld().findActiveObjectById(sourceId);
	
		
		// We didn't find the source
		if(source == null)
			return;

		// We didn't find the destination
		if(target == null)
			return;

		//long s = System.currentTimeMillis();
		source.addCommunication(target);
		//System.out.println("SpyEventListenerImpl.requestMessageReceived() = "+(System.currentTimeMillis()-s));		
	}

	public void replyMessageReceived(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
	}

	public void voidRequestServed(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;
		ao.setState(State.ACTIVE);
		ao.setRequestQueueLength(((SpyMessageEvent) spyEvent).getRequestQueueLength());
		
	}

	public void allEventsProcessed() {
		if(!isMonitoring)
			return;
	}

	public void servingStarted(UniqueID id, SpyEvent spyEvent) {
		if(!isMonitoring)
			return;
		AOObject ao = getActiveObject(id);
		if(ao == null)
			return;

		//TODO is correct?
		if(ao.getState()==State.MIGRATING)
			return;

		ao.setState(State.SERVING_REQUEST);
		ao.setRequestQueueLength(((SpyMessageEvent) spyEvent).getRequestQueueLength());
		
	}

	public String getName(UniqueID id){
		AOObject ao = null;
		if(nodeObject != null)
			ao = (AOObject) nodeObject.getChild(id.toString());
		if(ao == null)
			return id.toString();
		return ao.getFullName();
	}

	public AOObject getActiveObject(UniqueID id){
		return (AOObject) nodeObject.getChildInAllChildren(id.toString());
	}
}
