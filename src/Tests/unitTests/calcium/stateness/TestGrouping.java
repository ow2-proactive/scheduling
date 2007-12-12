/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package unitTests.calcium.stateness;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import static org.junit.Assert.*;
import org.junit.Test;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;


public class TestGrouping {
    @SuppressWarnings("unchecked")
    @Test
    /**
     * muscles: a, b, c, d ,e
     *
     * Group 1: a->x, b->{x,y}, c->y
     * Group 2: d->p, p->o, e->q, q->o
     *
     */
    public void groupTest() throws Exception {
        //building the graph
        Leaf x = new Leaf();
        Leaf y = new Leaf();
        NodeMono a = new NodeMono(x);
        NodeBi b = new NodeBi(x, y);
        NodeMono c = new NodeMono(y);

        Leaf o = new Leaf();
        NodeMono p = new NodeMono(o);
        NodeMono q = new NodeMono(o);
        NodeMono d = new NodeMono(p);
        NodeMono e = new NodeMono(q);

        //check simple state sharings
        //assertFalse(Stateness.shareState(x, y));
        assertFalse(Stateness.isStateFul(x));
        assertFalse(Stateness.shareState(x, x));
        assertTrue(Stateness.shareState(a, a));
        assertTrue(Stateness.shareState(a, b));
        assertFalse(Stateness.shareState(a, c));

        assertTrue(Stateness.shareState(d, e));

        //put all the graphs entrypoints inside a list
        ArrayList list = new ArrayList();
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);
        list.add(e);
        list.add(a); //duplicates should be handled independantely

        //group the entrypoints by graph "islands"
        Collection<Collection<Object>> groups = Stateness.getReferenceGroups(list);
        assertTrue(groups.size() == 2);

        int total = list.size();
        for (Collection g : groups) {
            total -= g.size();

            assertTrue(g.size() > 0); //check each island has at least one element
            for (Object object : g) {
                assert (list.contains(object)); //check this element was in the original list
                list.remove(object);
            }
        }

        assertTrue(total == 0); //check all elements where put in some group
    }

    static class NodeMono {
        Object o;

        public NodeMono(Object o) {
            this.o = o;
        }
    }

    static class NodeBi {
        Object o1;
        Object o2;

        public NodeBi(Object o1, Object o2) {
            this.o1 = o1;
            this.o2 = o2;
        }
    }

    static class Leaf implements Serializable {
    }
}
