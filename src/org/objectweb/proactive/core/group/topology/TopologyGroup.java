/*
 * Created on Mar 16, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * Topologies are groups. They just give special acces to their members or (sub)groups members.
 *
 * @author Laurent Baduel
 */
public abstract class TopologyGroup extends ProxyForGroup {

    /**
     * Constructor : a Topology is build with a group with the specified size
     * @param g - the group used a base for the new group (topology)
     * @param size - the number of member of g used to build the topology
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public TopologyGroup(Group g, int size)
        throws ConstructionOfReifiedObjectFailedException {
        super(g.getTypeName());
        for (int i = 0; i < size; i++) {
            this.add(g.get(i));
        }
    }
}
