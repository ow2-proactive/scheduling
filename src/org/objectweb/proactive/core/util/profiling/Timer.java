package org.objectweb.proactive.core.util.profiling;

/**
 * @author fabrice
 *
 */
public interface Timer {
	
	public void start();
	
	public void resume();
	
	public void pause();
	
	public void stop();

	public long getCumulatedTime();
	
	public int getNumberOfValues();
	
	public long getAverage(); 
	
	public void dump();
	
}
