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
package functionalTests.stub.stubgeneration;

import java.util.Arrays;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.Utils;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


/**
 * Testing on-the-fly generation of stub classes in bytecode form
 * @author rquilici
 */
public class Test extends FunctionalTest {
    String stubClassName;
    byte[] data;

    @org.junit.Test
    public void action() throws Exception {
        //empty String
        assertEquals(Utils.convertClassNameToStubClassName("", null), "");

        //not a stub
        String notAStubClassName = "functionalTests.stub.stubgeneration.A";
        assertEquals(Utils.convertStubClassNameToClassName(notAStubClassName), notAStubClassName);

        // Class not in a package 
        assertEquals(Utils.convertStubClassNameToClassName("pa.stub._StubA"), "A");

        //tests with a simple name
        String baseclassName = "functionalTests.stub.stubgeneration.A";
        data = JavassistByteCodeStubBuilder.create(baseclassName, null);
        assertNotNull(data);
        Class<?> stubClass = org.objectweb.proactive.core.component.gen.Utils.defineClass(
                "pa.stub.functionalTests.stub.stubgeneration._StubA", data);
        assertTrue("A isn't parent of its Stub!", A.class.isAssignableFrom(stubClass));
        stubClassName = Utils.convertClassNameToStubClassName(baseclassName, null);
        assertEquals(stubClassName + " not equals pa.stub.functionalTests.stub.stubgeneration._StubA",
                stubClassName, "pa.stub.functionalTests.stub.stubgeneration._StubA");
        assertEquals(Utils.convertStubClassNameToClassName(stubClassName), baseclassName);
        assertTrue(Arrays.equals(Utils.getNamesOfParameterizingTypesFromStubClassName(stubClassName),
                new String[0]));

        //tests with a more complicated name, test char escaping
        baseclassName = "functionalTests.stub.stubgeneration._StubA_PTy_Dpe_Generics";
        data = JavassistByteCodeStubBuilder.create(baseclassName, new Class[] { My_PFirst_PType.class,
                My_DSecond_PType.class });
        assertNotNull(data);
        stubClass = org.objectweb.proactive.core.component.gen.Utils
                .defineClass(
                        "pa.stub.parameterized.functionalTests.stub.stubgeneration._Stub__StubA__PTy__Dpe__Generics_GenericsfunctionalTests_Pstub_Pstubgeneration_PMy__PFirst__PType_DfunctionalTests_Pstub_Pstubgeneration_PMy__DSecond__PType",
                        data);
        assertTrue("_StubA_PTy_Dpe_Generics isn't parent of its Stub!", _StubA_PTy_Dpe_Generics.class
                .isAssignableFrom(stubClass));
        stubClassName = Utils.convertClassNameToStubClassName(baseclassName, new Class[] {
                My_PFirst_PType.class, My_DSecond_PType.class });
        assertEquals(
                stubClassName + " not equals pa.stub.functionalTests.stub.stubgeneration._StubA",
                stubClassName,
                "pa.stub.parameterized.functionalTests.stub.stubgeneration._Stub__StubA__PTy__Dpe__Generics_GenericsfunctionalTests_Pstub_Pstubgeneration_PMy__PFirst__PType_DfunctionalTests_Pstub_Pstubgeneration_PMy__DSecond__PType");
        assertEquals(Utils.convertStubClassNameToClassName(stubClassName), baseclassName);
        assertTrue(Arrays.equals(Utils.getNamesOfParameterizingTypesFromStubClassName(stubClassName),
                new String[] { My_PFirst_PType.class.getName(), My_DSecond_PType.class.getName() }));

        // test Serializable return type
        A a = (A) PAActiveObject.newActive(A.class.getName(), new Object[] {});
        assertEquals(A.RESULT, a.foo().toString());

        //BENCH
        //        long begin = System.currentTimeMillis();
        //        for (int i = 0; i < 1000000; i++) {
        //            Utils.convertClassNameToStubClassName(baseclassName, null);
        //        }
        //        System.out.println("convertClassNameToStubClassName " +
        //            (System.currentTimeMillis() - begin));
        //        begin = System.currentTimeMillis();
        //        for (int i = 0; i < 1000000; i++) {
        //            Utils.convertStubClassNameToClassName(stubClassName);
        //        }
        //        System.out.println("convertStubClassNameToClassName " +
        //            (System.currentTimeMillis() - begin));
        assertTrue(data != null);
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            System.out.println("successful");
        } catch (Throwable t) {
            System.out.println("failed");
            t.printStackTrace();
        } finally {
            try {
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
