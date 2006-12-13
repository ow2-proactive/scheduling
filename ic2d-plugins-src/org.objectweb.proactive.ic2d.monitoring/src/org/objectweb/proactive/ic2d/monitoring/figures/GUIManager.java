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
package org.objectweb.proactive.ic2d.monitoring.figures;


public class GUIManager{

	/** To know if we should repaint */
	private boolean dirty = true;

	private boolean isAlive = true;

	/** The manager */
	private Thread thread;


	/** Time To Sleep (in seconds) */
	private float tts = 0.1f;

	public GUIManager(AbstractFigure figure){
		//this.thread = new Thread(new Painter(figure));
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public void repaint(){
		dirty = true;
		switch (thread.getState()) {
		case NEW:
			//thread.start();
			break;
		default:
			break;
		}
	}


//	-- INNER CLASS -----------------------------------------------


	private class Painter implements Runnable {

		/** The figure to repaint */
		private AbstractFigure figure;

		public Painter(AbstractFigure figure){
			this.figure = figure;
		}

		public void run() {
			if(figure==null)
				return;
			//while(isAlive) {
				if(dirty){
					if(figure==null)
						return;
					/*Display.getDefault().asyncExec(new Runnable() {
						public void run () {
							//figure.repaint();//refresh();
						}});*/
					}
					try {
						Thread.sleep((long) (tts * 1000));
					} catch (InterruptedException e) {/* Do nothing */}
				}
			//}
		}
	}
