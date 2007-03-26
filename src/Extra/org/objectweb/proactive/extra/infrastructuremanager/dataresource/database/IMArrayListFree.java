package org.objectweb.proactive.extra.infrastructuremanager.dataresource.database;

import java.util.ArrayList;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extra.infrastructuremanager.dataresource.IMNode;


public class IMArrayListFree extends ArrayList<IMNode> {
    public boolean add(IMNode imNode) {
        try {
            imNode.setFree();
        } catch (NodeException e) {
        }
        return super.add(imNode);
    }
}
