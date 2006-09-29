package org.objectweb.proactive.loadbalancing.metrics;


import org.objectweb.proactive.loadbalancing.LoadBalancer;


public interface Metric{

	/**
	 * This method has to call the methods :
	 * 	- stealWork 
	 * 	or
	 *  - startBalancing
	 *  according to the load 
	 * @param lb the LoadBalancer
	 */
	public void takeDecision(LoadBalancer lb);
	
	public double getRanking();
	
	public double getLoad();
}
