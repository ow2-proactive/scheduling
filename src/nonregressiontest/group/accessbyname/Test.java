/*
 * Created on Jul 7, 2004
 * author : Matthieu Morel
 */
package nonregressiontest.group.accessbyname;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import nonregressiontest.group.A;

import testsuite.test.FunctionalTest;


/**
 * This class tests the access to named elements of a group.
 *
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
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

    public void action() throws Exception {
        this.createGroup();
    }

    public A action(Object o) throws Exception {
        return this.createGroup();
    }

    public void initTest() throws Exception {
        // nothing to do
    }

    public void endTest() throws Exception {
        // nothing to do
    }

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
