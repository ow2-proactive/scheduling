package org.objectweb.proactive.core.ssh;

/**
 * @author mlacage
 * A trivial classic Semaphore (in the Dijkstra sense).
 */
public class Semaphore {
	private int _value;

	public Semaphore( int value ) {
		this._value = value; 
	}

	public synchronized void down() {
		_value--;
		if (_value < 0) {
			try { wait(); } catch( Exception e ) {e.printStackTrace();}
		}
	}

	public synchronized void up() {
		_value++;
		notify();
	}
}
