/*
 * Created on Mar 19, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * This one is specialy dedicaced to Fabrice  ;)
 *
 * @author Laurent Baduel
 */
public class Tetrahedron extends TopologyGroup {
    public Tetrahedron(Group g, int size)
        throws ConstructionOfReifiedObjectFailedException {
        super(g, size);
        for (int i = 0; i < size; i++) {
            this.add(g.get(i));
        }
    }
}
