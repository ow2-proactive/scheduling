package org.objectweb.proactive.ic2d.monitoring.figures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.editparts.AbstractMonitoringEditPart;

public class GUIRefresher {

	Set<AbstractMonitoringEditPart> editParts = Collections.synchronizedSet(new HashSet<AbstractMonitoringEditPart>());

	private boolean isAlive = true;

	/** The manager */
	private Thread thread;

	/** Time To Sleep (in seconds) */
	private float tts = 0.1f;

	public GUIRefresher(){
		//this.thread = new Thread(new Refresher());
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public void refresh(AbstractMonitoringEditPart editPart){
		System.out.println("GUIRefresher.refresh() -- "+editPart.getClass().getName());
		//editParts.add(editPart);

	/*	if(thread.getState() == Thread.State.NEW)
			thread.start();*/
	}


//	-- INNER CLASS -----------------------------------------------


	private class Refresher implements Runnable {

		public void run() {
			if(editParts == null)
				return;
			//while(isAlive) {
				try {
					Thread.sleep((long) (tts * 1000));
					System.out.println("Refresher.run() REVEIL -- "+editParts.size());
				} catch (InterruptedException e) {/* Do nothing */}


				/*Display.getDefault().asyncExec(new Runnable() {
					public void run () {
						synchronized(editParts) {
							Iterator i = editParts.iterator(); // Must be in the synchronized block
							while (i.hasNext()){
								AbstractMonitoringEditPart editPart = (AbstractMonitoringEditPart) i.next();
								//editPart.refresh();
							}
							editParts.clear();
						}
					}});*/
			//}
		}
	}
}