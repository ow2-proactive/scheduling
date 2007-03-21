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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.group.accessbyname;

import nonregressiontest.group.A;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import testsuite.test.FunctionalTest;


/**
 * This class tests the access to named elements of a group.
 *
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3756826632109245192L;
	A typedGroup;

    public Test() {
        super("access to group elements by their name",
            "access to group elements by their name");
    }

    private A createGroup() throws Exception {
        typedGroup = (A) ProActiveGroup.newGroup(A.class.getName());

        Group group = ProActiveGroup.getGroup(typedGroup);
        group.addNamedElement("number0", new A("Agent0"));
        group.add(new A("Agent1"));
        group.addNamedElement("number2", new A("Agent2"));

        return this.typedGroup;
    }

    @Override
	public void action() throws Exception {
        this.createGroup();
    }

    public A action(Object o) throws Exception {
        return this.createGroup();
    }

    @Override
	public void initTest() throws Exception {
        // nothing to do
    }

    @Override
	public void endTest() throws Exception {
        // nothing to do
    }

    @Override
	public boolean postConditions() throws Exception {
        // was the group created ?
        if (this.typedGroup == null) {
            return false;
        }
        Group group = ProActiveGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        if (group.size() != 3) {
            return false;
        }

        //		tests for named elements
        A agent0_indexed = (A) group.get(0);
        A agent0_named = (A) group.getNamedElement("number0");
        A agent1_indexed = (A) group.get(1);
        A agent2_named = (A) group.getNamedElement("number2");
        A agent2_indexed = (A) group.get(2);

        // tests correct ordering and access to named elements
        if (!((agent0_indexed == agent0_named) &&
                (agent2_indexed == agent2_named) &&
                agent1_indexed.getName().equals("Agent1"))) {
            return false;
        }

        group.removeNamedElement("number0");
        // tests removal and re-ordering
        if ((group.size() != 2) || (group.get(0) != agent1_indexed) ||
                group.containsKey("number0")) {
            return false;
        }

        // end of tests for named elements
        return true;
    }
}
