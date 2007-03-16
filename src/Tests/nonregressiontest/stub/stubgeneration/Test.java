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
package nonregressiontest.stub.stubgeneration;

import java.util.Arrays;

import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.Utils;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


/**
 * @author rquilici
 */
public class Test extends FunctionalTest {
    String stubClassName;
    byte[] data;

    public Test() {
        super("Stub Generation",
            "Testing on-the-fly generation of stub classes in bytecode form");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        //empty String
        Assertions.assertEquals(Utils.convertClassNameToStubClassName("", null),
            "");

        //not a stub
        String notAStubClassName = "nonregressiontest.stub.stubgeneration.A";
        Assertions.assertEquals(Utils.convertStubClassNameToClassName(
                notAStubClassName), notAStubClassName);

        // Class not in a package 
        Assertions.assertEquals(Utils.convertStubClassNameToClassName(
                "pa.stub._StubA"), "A");

        //tests with a simple name
        String baseclassName = "nonregressiontest.stub.stubgeneration.A";
        data = JavassistByteCodeStubBuilder.create(baseclassName, null);
        Assertions.assertNotNull(data);
        Class stubClass = org.objectweb.proactive.core.component.gen.Utils.defineClass("pa.stub.nonregressiontest.stub.stubgeneration._StubA",
                data);
        Assertions.assertTrue("A isn't parent of its Stub!",
            A.class.isAssignableFrom(stubClass));
        stubClassName = Utils.convertClassNameToStubClassName(baseclassName,
                null);
        Assertions.assertEquals(stubClassName +
            " not equals pa.stub.nonregressiontest.stub.stubgeneration._StubA",
            stubClassName,
            "pa.stub.nonregressiontest.stub.stubgeneration._StubA");
        Assertions.assertEquals(Utils.convertStubClassNameToClassName(
                stubClassName), baseclassName);
        Assertions.assertTrue(Arrays.equals(
                Utils.getNamesOfParameterizingTypesFromStubClassName(
                    stubClassName), new String[0]));

        //tests with a more complicated name, test char escaping
        baseclassName = "nonregressiontest.stub.stubgeneration._StubA_PTy_Dpe_Generics";
        data = JavassistByteCodeStubBuilder.create(baseclassName,
                new Class[] { My_PFirst_PType.class, My_DSecond_PType.class });
        Assertions.assertNotNull(data);
        stubClass = org.objectweb.proactive.core.component.gen.Utils.defineClass("pa.stub.parameterized.nonregressiontest.stub.stubgeneration._Stub__StubA__PTy__Dpe__Generics_Genericsnonregressiontest_Pstub_Pstubgeneration_PMy__PFirst__PType_Dnonregressiontest_Pstub_Pstubgeneration_PMy__DSecond__PType",
                data);
        Assertions.assertTrue("_StubA_PTy_Dpe_Generics isn't parent of its Stub!",
            _StubA_PTy_Dpe_Generics.class.isAssignableFrom(stubClass));
        stubClassName = Utils.convertClassNameToStubClassName(baseclassName,
                new Class[] { My_PFirst_PType.class, My_DSecond_PType.class });
        Assertions.assertEquals(stubClassName +
            " not equals pa.stub.nonregressiontest.stub.stubgeneration._StubA",
            stubClassName,
            "pa.stub.parameterized.nonregressiontest.stub.stubgeneration._Stub__StubA__PTy__Dpe__Generics_Genericsnonregressiontest_Pstub_Pstubgeneration_PMy__PFirst__PType_Dnonregressiontest_Pstub_Pstubgeneration_PMy__DSecond__PType");
        Assertions.assertEquals(Utils.convertStubClassNameToClassName(
                stubClassName), baseclassName);
        Assertions.assertTrue(Arrays.equals(
                Utils.getNamesOfParameterizingTypesFromStubClassName(
                    stubClassName),
                new String[] {
                    My_PFirst_PType.class.getName(),
                    My_DSecond_PType.class.getName()
                }));

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
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        return (data != null);
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.initTest();
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                test.endTest();
                System.exit(0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
