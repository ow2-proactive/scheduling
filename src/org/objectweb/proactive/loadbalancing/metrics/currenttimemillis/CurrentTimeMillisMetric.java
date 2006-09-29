package org.objectweb.proactive.loadbalancing.metrics.currenttimemillis;

import org.objectweb.proactive.loadbalancing.LoadBalancer;
import org.objectweb.proactive.loadbalancing.metrics.Metric;

public class CurrentTimeMillisMetric implements Metric {

	private double rank = 0;
	
	public CurrentTimeMillisMetric(double creationTime){
		this.rank = (System.currentTimeMillis()- creationTime) * 1000;
	}
	
	public void takeDecision(LoadBalancer lb) {
		// TODO Auto-generated method stub
		lb.stealWork();
	}

	public double getRanking() {
		// TODO Auto-generated method stub
		return rank;
	}

	public double getLoad() {
		// TODO Auto-generated method stub
		return 0;
	}

}
