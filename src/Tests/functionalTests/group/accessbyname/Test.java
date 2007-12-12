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
package functionalTests.group.accessbyname;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;

import functionalTests.FunctionalTest;
import functionalTests.group.A;
import static junit.framework.Assert.assertTrue;

/**
 * This class tests the access to named elements of a group.
 *
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
    A typedGroup;

    private A createGroup() throws Exception {
        typedGroup = (A) PAGroup.newGroup(A.class.getName());

        Group group = PAGroup.getGroup(typedGroup);
        group.addNamedElement("number0", new A("Agent0"));
        group.add(new A("Agent1"));
        group.addNamedElement("number2", new A("Agent2"));

        return this.typedGroup;
    }

    @org.junit.Test
    public void action() throws Exception {
        this.createGroup();

        // was the group created ?
        assertTrue(typedGroup != null);
        Group group = PAGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        assertTrue(group.size() == 3);

        //		tests for named elements
        A agent0_indexed = (A) group.get(0);
        A agent0_named = (A) group.getNamedElement("number0");
        A agent1_indexed = (A) group.get(1);
        A agent2_named = (A) group.getNamedElement("number2");
        A agent2_indexed = (A) group.get(2);

        // tests correct ordering and access to named elements
        assertTrue(((agent0_indexed == agent0_named)));
        assertTrue(agent2_indexed == agent2_named);
        assertTrue(agent1_indexed.getName().equals("Agent1"));

        group.removeNamedElement("number0");
        // tests removal and re-ordering
        assertTrue(group.size() == 2);
        assertTrue(group.get(0) == agent1_indexed);
        assertTrue(!group.containsKey("number0"));
    }
}
