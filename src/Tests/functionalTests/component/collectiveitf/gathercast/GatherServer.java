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
package functionalTests.component.collectiveitf.gathercast;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class GatherServer implements GatherDummyItf {
    private int cpt = 0;
    private static String[] listMsg = { "10", "20" };
    private int nbCalls = 0;

    /*
     * @see functionalTests.component.collectiveitf.gather.GatherDummyItf#foo(java.util.List)
     */
    public void foo(List<IntMutableWrapper> l) {
        // verify values are transmitted correctly
        Assert.assertTrue(l.contains(new IntMutableWrapper(new Integer(Test.VALUE_1))));
        Assert.assertTrue(l.contains(new IntMutableWrapper(new Integer(Test.VALUE_2))));
    }

    /*
     * @see functionalTests.component.collectiveitf.gather.GatherDummyItf#bar(java.util.List)
     */
    public List<B> bar(List<A> l) {
        List<B> result = new ArrayList<B>(l.size());
        for (int i = 0; i < l.size(); i++) {
            result.add(i, new B(l.get(i).getValue()));
        }
        return result;
    }

    public List<B> timeout() {
        List<B> l = new ArrayList<B>();
        l.add(new B());
        l.add(new B());
        return l;
    }

    public void executeAlone(List<String> msg) {
        Assert.assertTrue(msg.size() == 1);
        Assert.assertEquals(listMsg[nbCalls], msg.get(0));
        nbCalls = (nbCalls + 1) % listMsg.length;
    }

    public List<StringWrapper> executeAlone2() {
        List<StringWrapper> l = new ArrayList<StringWrapper>();
        l.add(new StringWrapper("Hello" + cpt++));
        return l;
    }
}
