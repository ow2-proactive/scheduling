package org.objectweb.proactive.ic2d.monitoring.data;

import org.objectweb.proactive.ic2d.monitoring.spy.SpyEvent;

/**
 * This class is used to slow down the events sent by the spies.
 */
public class EventRetarder {

	/** The buffer of all events */
	//private buffer;
	
	/** Thread which send the events */
	private Thread sender;
	
	/** Indique si le thread doit continuer a tourner */
	private boolean run;
	
	/** Time To Send (in seconds) */
	private int tts;
	
	/** temps entre 2 envoies d'evenements a ic2d */
	private int time;
	
	/** Nombre max d'evenement pendant un tour de boucle */
	private int max = 400;
	
	public EventRetarder(){
		// Initialisation du buffer
		//buffer = new 
		sender = new Thread(new Sender());
	}
	
	/**
	 * Adds some events to the buffer
	 * @param spyEvents
	 */
	public void addEvents(SpyEvent[] spyEvents){
		
	}
	
	
	private void dispatchEvents(){
		
	}
	
	//
	//-- INER CLASS ----------------
	//
	
	private class Sender implements Runnable {
		
		public Sender(){
		}
		
		public void run() {
			while(run) {
				
				try {
					Thread.sleep(tts * 1000);
				} catch (InterruptedException e) {/* Do nothing */}
			}
		}
	}
}
