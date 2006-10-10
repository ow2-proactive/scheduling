/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package nonregressiontest.activeobject.generics;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


/**
 * Checks that ProActive correctly handles generics.
 *
 * 1. Stubs on generic return types are correctly typed with the parameterizing type.
 * 2. Groups correctly handle generics, including for reifiable parameterizing return types.
 *
 *
 * @author Matthieu Morel
 */
public class Test extends FunctionalTest {
    ProActiveDescriptor descriptor;

    public Test() {
        super("Generic types", "Generic types");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
    	
    	// new active with non reifiable parameter types
        Pair<String, Integer> a = (Pair<String, Integer>) ProActive.newActive(Pair.class.getName(),
                new Class[] { String.class, Integer.class },
                new Object[] { "A", 42 });
        Assertions.assertTrue(String.class.isAssignableFrom(
                a.getFirst().getClass()));
        Assertions.assertTrue(Integer.class.isAssignableFrom(
                a.getSecond().getClass()));
        Assertions.assertEquals("A", a.getFirst());
        Assertions.assertEquals(42, a.getSecond());

        
        // turn active
        Pair<Integer, String> pair = new Pair<Integer, String>(42, "X");
        Assertions.assertEquals(42, pair.getFirst());
        Assertions.assertEquals("X", pair.getSecond());

        Pair<Integer, String> activePair = (Pair<Integer, String>)ProActive.turnActive(pair, new Class[] {Integer.class, String.class});
        Assertions.assertTrue(Integer.class.isAssignableFrom(
        		activePair.getFirst().getClass()));
        Assertions.assertTrue(String.class.isAssignableFrom(
        		activePair.getSecond().getClass()));
        Assertions.assertEquals(42, activePair.getFirst());
        Assertions.assertEquals("X", activePair.getSecond());
        

        
//      new active with reifiable parameter types
        Pair<StringWrapper, IntWrapper> b = (Pair<StringWrapper, IntWrapper>) ProActive.newActive(Pair.class.getName(),
                new Class[] { StringWrapper.class, IntWrapper.class },
                new Object[] { new StringWrapper("toto"), new IntWrapper(12) });
        Assertions.assertTrue(StringWrapper.class.isAssignableFrom(
                b.getFirst().getClass()));
        Assertions.assertTrue(IntWrapper.class.isAssignableFrom(
                b.getSecond().getClass()));
        Assertions.assertEquals("toto", b.getFirst().stringValue());
        Assertions.assertEquals(12, b.getSecond().intValue());

        
        // new active group with reifiable parameter types
        Pair<StringWrapper, IntWrapper> gb = (Pair<StringWrapper, IntWrapper>) ProActiveGroup.newGroup(Pair.class.getName(),
                new Class[] { StringWrapper.class, IntWrapper.class },
                new Object[][] {
                    { new StringWrapper("A"), new IntWrapper(1) },
                    { new StringWrapper("B"), new IntWrapper(2) }
                });
        Assertions.assertTrue(StringWrapper.class.isAssignableFrom(
                gb.getFirst().getClass()));
        Assertions.assertTrue(IntWrapper.class.isAssignableFrom(
                gb.getSecond().getClass()));

        StringWrapper stringWrapperResult = gb.getFirst();
        Group stringWrapperResultGroup = ProActiveGroup.getGroup(stringWrapperResult);
        IntWrapper intWrapperResult = gb.getSecond();
        Group intWrapperResultGroup = ProActiveGroup.getGroup(intWrapperResult);

        Assertions.assertEquals(new StringWrapper("A"),
            stringWrapperResultGroup.get(0));
        Assertions.assertEquals(new StringWrapper("B"),
            stringWrapperResultGroup.get(1));
        Assertions.assertEquals(new IntWrapper(1), intWrapperResultGroup.get(0));
        Assertions.assertEquals(new IntWrapper(2), intWrapperResultGroup.get(1));

        
        // new active group with non reifiable parameter types (which is not allowed with groups)
        boolean invocationTargetException = false;
        Pair<String, Integer> ga = (Pair<String, Integer>) ProActiveGroup.newGroup(Pair.class.getName(),
                new Class[] { String.class, Integer.class },
                new Object[][] {
                    { "A", 1 },
                    { "B", 2 }
                });
        try {
            // verify this invocation is not possible
            ga.getFirst();
        } catch (Throwable e) {
            invocationTargetException = true;
        }
        Assertions.assertTrue(invocationTargetException);
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        return true;
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
