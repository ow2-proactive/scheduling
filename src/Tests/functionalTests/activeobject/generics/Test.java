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
package functionalTests.activeobject.generics;

import java.util.Arrays;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


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
    @org.junit.Test
    public void action() throws Exception {
        //      new active with '_' in classname of a parametized type.
        //pa.stub.parameterized.nonregressiontest.activeobject.generics.Stub_Pair_Generics_[nonregressiontest_activeobject_generics_My_Type5org_objectweb_proactive_core_util_wrapper_IntWrapper]
        Pair<My_DType, IntWrapper> p_ = (Pair<My_DType, IntWrapper>) PAActiveObject.newActive(Pair.class
                .getName(), new Class[] { My_DType.class, IntWrapper.class }, new Object[] {
                new My_DType("toto"), new IntWrapper(12) });
        assertTrue(My_DType.class.isAssignableFrom(p_.getFirst().getClass()));
        assertTrue(IntWrapper.class.isAssignableFrom(p_.getSecond().getClass()));
        assertEquals("toto", p_.getFirst().toString());
        assertEquals(12, p_.getSecond().intValue());

        // new active with reifiable parameter types
        // test before non reifiable return types to verify caching of synchronous/asynchrous method calls 
        // works fine with parameterized types
        Pair<StringWrapper, IntWrapper> b = (Pair<StringWrapper, IntWrapper>) PAActiveObject.newActive(
                Pair.class.getName(), new Class[] { StringWrapper.class, IntWrapper.class }, new Object[] {
                        new StringWrapper("toto"), new IntWrapper(12) });
        assertTrue(StringWrapper.class.isAssignableFrom(b.getFirst().getClass()));
        assertTrue(IntWrapper.class.isAssignableFrom(b.getSecond().getClass()));
        assertEquals("toto", b.getFirst().stringValue());
        assertEquals(12, b.getSecond().intValue());

        // new active with non reifiable parameter types
        Pair<String, Integer> a = (Pair<String, Integer>) PAActiveObject.newActive(Pair.class.getName(),
                new Class[] { String.class, Integer.class }, new Object[] { "A", 42 });
        assertTrue(String.class.isAssignableFrom(a.getFirst().getClass()));
        assertTrue(Integer.class.isAssignableFrom(a.getSecond().getClass()));
        assertEquals("A", a.getFirst());
        assertTrue(42 == a.getSecond());

        // turn active
        Pair<Integer, String> pair = new Pair<Integer, String>(42, "X");
        assertTrue(42 == pair.getFirst());
        assertEquals("X", pair.getSecond());

        Pair<Integer, String> activePair = (Pair<Integer, String>) PAActiveObject.turnActive(pair,
                new Class[] { Integer.class, String.class });
        assertTrue(Integer.class.isAssignableFrom(activePair.getFirst().getClass()));
        assertTrue(String.class.isAssignableFrom(activePair.getSecond().getClass()));
        assertTrue(42 == activePair.getFirst());
        assertEquals("X", activePair.getSecond());

        // new active group with reifiable parameter types
        Pair<StringWrapper, IntWrapper> gb = (Pair<StringWrapper, IntWrapper>) PAGroup.newGroup(Pair.class
                .getName(), new Class[] { StringWrapper.class, IntWrapper.class },
                new Object[][] { { new StringWrapper("A"), new IntWrapper(1) },
                        { new StringWrapper("B"), new IntWrapper(2) } });
        assertTrue(StringWrapper.class.isAssignableFrom(gb.getFirst().getClass()));
        assertTrue(IntWrapper.class.isAssignableFrom(gb.getSecond().getClass()));

        StringWrapper stringWrapperResult = gb.getFirst();
        Group stringWrapperResultGroup = PAGroup.getGroup(stringWrapperResult);
        IntWrapper intWrapperResult = gb.getSecond();
        Group intWrapperResultGroup = PAGroup.getGroup(intWrapperResult);

        assertEquals(new StringWrapper("A"), stringWrapperResultGroup.get(0));
        assertEquals(new StringWrapper("B"), stringWrapperResultGroup.get(1));
        assertEquals(new IntWrapper(1), intWrapperResultGroup.get(0));
        assertEquals(new IntWrapper(2), intWrapperResultGroup.get(1));

        // new active group with non reifiable parameter types (which is not allowed with groups)
        boolean invocationTargetException = false;
        Pair<String, Integer> ga = (Pair<String, Integer>) PAGroup.newGroup(Pair.class.getName(),
                new Class[] { String.class, Integer.class }, new Object[][] { { "A", 1 }, { "B", 2 } });
        try {
            // verify this invocation is not possible
            ga.getFirst();
        } catch (Throwable e) {
            invocationTargetException = true;
        }
        assertTrue(invocationTargetException);

        // test name escaping with generics, main test are done in nonregressiontest.stub.stubgeneration
        assertEquals("pa.stub._StubMy__P__DType", Utils.convertClassNameToStubClassName("My_P_DType",
                new Class[] {}));
        String escape = Utils.convertClassNameToStubClassName(Pair.class.getName(), new Class[] {
                My_DType.class, IntWrapper.class });
        assertEquals(
                escape,
                "pa.stub.parameterized.functionalTests.activeobject.generics._StubPair_GenericsfunctionalTests_Pactiveobject_Pgenerics_PMy__DType_Dorg_Pobjectweb_Pproactive_Pcore_Putil_Pwrapper_PIntWrapper");

        String[] unescape = Utils.getNamesOfParameterizingTypesFromStubClassName(escape);
        String[] result = new String[] { "functionalTests.activeobject.generics.My_DType",
                "org.objectweb.proactive.core.util.wrapper.IntWrapper" };
        assertTrue(Arrays.equals(unescape, result));
    }
}
