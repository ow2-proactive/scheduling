package org.objectweb.proactive.examples.userguide.cmagent.synch;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.examples.userguide.cmagent.simple.State;


public class CMAgentChained extends CMAgentInitialized implements Serializable {
	private CMAgentChained previousNeighbour=null;
	private CMAgentChained nextNeighbour=null;
	public void setPreviousNeighbour(CMAgentChained neighbour){
		this.previousNeighbour = neighbour;
		if (neighbour.getNextNeigbour() == null) neighbour.setNextNeighbour((CMAgentChained)PAActiveObject.getStubOnThis());
	}
	public void setNextNeighbour(CMAgentChained neighbour) {
		this.nextNeighbour = neighbour;
		if (neighbour.getPreviousNeigbour() == null) neighbour.setPreviousNeighbour((CMAgentChained)PAActiveObject.getStubOnThis());
	}
	public CMAgentChained getPreviousNeigbour(){
		return previousNeighbour;
	}
	public CMAgentChained getNextNeigbour(){
		return nextNeighbour;
	}
	public Vector<State> getAllPreviousStates(){
		System.out.println(PAActiveObject.getStubOnThis());

		if (this.previousNeighbour != null) {
			System.out.println("Passing the call to the previous neighbour...");
			// wait-by-necessity
			Vector<State> states = this.previousNeighbour.getAllPreviousStates();
			states.add(this.getCurrentState());
			return states;
		}
		else{
			System.out.println("No more previous neighbours..");
			Vector<State> states = new Vector<State>();
			states.add(this.getCurrentState());
			return states;
		}
	}
	public Vector<State> getAllNextStates(){
		System.out.println(PAActiveObject.getStubOnThis());
		if (this.nextNeighbour != null) {
			// wait-by-necessity
			System.out.println("Passing the call to the next neighbour..");
			Vector<State> states = this.nextNeighbour.getAllNextStates();
			states.add(this.getCurrentState());
			return states;
		}
		else{
			System.out.println("No more next neighbours");
			Vector<State> states = new Vector<State>();
			states.add(this.getCurrentState());
			return states;
		}
	}

}
