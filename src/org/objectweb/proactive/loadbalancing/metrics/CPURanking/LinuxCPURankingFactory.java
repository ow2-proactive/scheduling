package org.objectweb.proactive.loadbalancing.metrics.CPURanking;

import org.objectweb.proactive.loadbalancing.metrics.Metric;
import org.objectweb.proactive.loadbalancing.metrics.MetricFactory;

public class LinuxCPURankingFactory implements MetricFactory {

	public Metric getNewMetric() {
		return new LinuxCPURanking();
	}

}
