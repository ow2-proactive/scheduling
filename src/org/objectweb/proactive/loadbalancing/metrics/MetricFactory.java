package org.objectweb.proactive.loadbalancing.metrics;

import java.io.Serializable;

public interface MetricFactory  extends Serializable {
	public Metric getNewMetric();
}
