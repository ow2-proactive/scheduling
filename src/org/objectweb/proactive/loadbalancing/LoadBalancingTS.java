package org.objectweb.proactive.loadbalancing;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.loadbalancing.metrics.MetricFactory;

public class LoadBalancingTS implements TechnicalService, Serializable {

	public void init(Map argValues) {
		// TODO Auto-generated method stub
		String metricFactoryName = (String) argValues.get("MetricFactory");
		try {
			MetricFactory mf = (MetricFactory) Class.forName(metricFactoryName)
					.newInstance();
			LoadBalancing.activate(mf);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void apply(Node node) {
		LoadBalancing.addNode(node);

	}

}
