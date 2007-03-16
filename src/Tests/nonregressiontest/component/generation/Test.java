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
package nonregressiontest.component.generation;

import org.objectweb.proactive.core.component.gen.Utils;

import nonregressiontest.component.ComponentTest;

import testsuite.test.Assertions;


/**
 * @author cdalmass
 *
 */
public class Test extends ComponentTest {

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {
        //        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        //        Map context_s = new HashMap();
        //        Component testcasesimple = (Component) f.newComponent("nonregressiontest.component.generation.simplename.testcase",context_s);
        //        
        //        Fractal.getLifeCycleController(testcasesimple).startFc();
        //
        //        // several iterations for thoroughly testing concurrency issues
        //        TestItfItfTest compItf = (TestItfItfTest)testcasesimple.getFcInterface("testitfitftest");
        //        StringWrapper result1 = compItf.testitfitftest();
        //        result1 = (StringWrapper)((TestItfItfTest)testcasesimple.getFcInterface("testitfitftest")).testitfitftest();
        //
        //        Map context_h = new HashMap();
        //        Component testcasehard = (Component) f.newComponent("nonregressiontest.component.generation.hardname.testcase",context_h);
        //        
        //        Fractal.getLifeCycleController(testcasehard).startFc();
        //
        //        // several iterations for thoroughly testing concurrency issues
        //        TestItf_P__I__O_ItfTest compHardItf = (TestItf_P__I__O_ItfTest)testcasehard.getFcInterface("testitf_P__I__O_itftest");
        //        StringWrapper result2 = compHardItf.testitf_P__I__O_itftest();
        //        result2 = (StringWrapper)compHardItf.testitf_P__I__O_itftest();
        //   //     System.out.println(result2);
        Assertions.assertEquals("CgeneratednonregressiontestCPcomponentCPgenerationCPItfCCOTypeCOitfCIunCrepresentative",
            Utils.getMetaObjectComponentRepresentativeClassName("itf-un",
                "nonregressiontest.component.generation.ItfCOType"));
        //Utils.getGatherProxyItfClassName(TODO_C);
        Assertions.assertEquals("nonregressiontest.component.generation.ItfCOType",
            Utils.getInterfaceSignatureFromRepresentativeClassName(
                "CgeneratednonregressiontestCPcomponentCPgenerationCPItfCCOTypeCOitfCIunCrepresentative"));
        Assertions.assertEquals("itf-un",
            Utils.getInterfaceNameFromRepresentativeClassName(
                "CgeneratednonregressiontestCPcomponentCPgenerationCPItfCCOTypeCOitfCIunCrepresentative"));
        Assertions.assertEquals("nonregressiontest.component.generation.GatherCODummyItf",
            Utils.getInterfaceSignatureFromGathercastProxyClassName(
                "CgeneratednonregressiontestCPcomponentCPgenerationCPGatherCCODummyItfCOgatherCCOServerItfCgathercastItfProxy"));
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
    public void endTest() throws Exception {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
    public void initTest() throws Exception {
        // TODO Auto-generated method stub
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("SUCCESS!");
            } else {
                System.out.println("FAILED!");
            }
        } catch (Exception e) {
            System.out.println("FAILED!");
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
