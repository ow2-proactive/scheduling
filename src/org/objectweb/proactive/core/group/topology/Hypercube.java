/*
 * Created on Mar 19, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;

/**
 * @author Laurent Baduel
 */
public class Hypercube extends TopologyGroup {

	public Hypercube (Group g, int size) throws ConstructionOfReifiedObjectFailedException {
	super(g, size);
	for (int i = 0 ; i < size ; i++) {
		this.add(g.get(i));
	}
}
}
