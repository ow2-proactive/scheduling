package org.objectweb.proactive.extra.infrastructuremanager.dataresource.database;

import java.util.Vector;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;

public class IMVectorFree  extends Vector<IMNode> {
	
	public boolean add(IMNode imNode) {
		try {
			imNode.setFree();
		} catch (NodeException e) {}
		return super.add(imNode);
	}

}
