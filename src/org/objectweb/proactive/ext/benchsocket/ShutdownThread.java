package org.objectweb.proactive.ext.benchsocket;

/**
 * @author fabrice
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ShutdownThread extends Thread {

	BenchOutputStream bos;
	
	
	public ShutdownThread(BenchOutputStream bos) {
		this.bos = bos;
	}
	
	public void run() {
		
		this.bos.displayTotal();
	}

}
