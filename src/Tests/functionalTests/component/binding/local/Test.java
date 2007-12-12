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
package functionalTests.component.binding.local;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.core.component.Binding;
import org.objectweb.proactive.core.component.Bindings;

import functionalTests.ComponentTest;
import functionalTests.component.Setup;


/**
 * @author Matthieu Morel
 *
 * a test for bindings / rebindings on client collective interfaces between remote components
 */
public class Test extends ComponentTest {

    /**
         *
         */
    Component compA;
    Component compB1;
    Component compB2;
    Component compD;
    Component compDbis;
    public static String P1_NAME = "p1";
    public static String P2_NAME = "p2";
    Interface compA_i1_server;
    Interface compA_i2_client;
    Interface compB1_i2_server;
    Interface compB2_i2_server;
    Interface compD_i2_client_collective;
    Bindings bindings1; // single binding
    Bindings bindings2; // collective binding
    Bindings bindings3; // collective bindings, each interface has different names
    Binding b1;
    Binding b2;
    Binding b3;
    Binding b4;
    Binding b5;

    public Test() {
        super("Components : Binding / rebinding / access to bound elements ",
            "Components : Binding / rebinding / access to bound elements ");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        testAdd();
        testContainsBindingOn();
        testGetExternalClientBindings();
        testGet();
        testRemove();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Before
    public void initTest() throws Exception {
        System.setProperty("fractal.provider",
            "org.objectweb.proactive.core.component.Fractive");
        compA = Setup.createCompositeA();
        compB1 = Setup.createCompositeB1();
        compB2 = Setup.createPrimitiveB2();
        compD = Setup.createPrimitiveD();
        compDbis = Setup.createPrimitiveDbis();
        compA_i1_server = (Interface) compA.getFcInterface("i1");
        compA_i2_client = (Interface) compA.getFcInterface("i2");
        compB1_i2_server = (Interface) compB1.getFcInterface("i2");
        compB2_i2_server = (Interface) compB2.getFcInterface("i2");
        compD_i2_client_collective = (Interface) compD.getFcInterface("i2");

        b1 = new Binding(compA_i2_client, "i2", compB1_i2_server);
        b2 = new Binding(compD_i2_client_collective, "i2", compB1_i2_server);
        b3 = new Binding(compD_i2_client_collective, "i2", compB2_i2_server);
        b4 = new Binding((Interface) compDbis.getFcInterface("i2"), "i201",
                compB2_i2_server);
        b5 = new Binding((Interface) compDbis.getFcInterface("i2"), "i202",
                compB2_i2_server);
        // TODO test whether multiple bindings on the same server interface is allowed?
        bindings1 = new Bindings();
        bindings2 = new Bindings();
        bindings3 = new Bindings();
        bindings1.add(b1);
        bindings2.add(b2); // collective with single name
        bindings2.add(b3); // collective with single name
        bindings3.add(b4); // collective with specific name
        bindings3.add(b5);
    }

    private void testAdd() {
        Assert.assertTrue(bindings1.containsBindingOn("i2"));
        Assert.assertTrue(bindings2.containsBindingOn("i2"));
        Assert.assertTrue(bindings3.containsBindingOn("i201"));
        Assert.assertTrue(bindings3.containsBindingOn("i202"));
    }

    private void testRemove() {
        bindings1.remove("i2");
        bindings2.remove("i2");
        bindings3.remove("i201");
        Assert.assertFalse(bindings1.containsBindingOn("i2"));
        Assert.assertFalse(bindings2.containsBindingOn("i2"));
        Assert.assertFalse(bindings3.containsBindingOn("i201"));
    }

    private void testGet() {
        Binding retreived1 = (Binding) bindings1.get("i2");
        Object retreived2 = bindings2.get("i2");
        Object retreived3 = bindings3.get("i202");
        Binding dummy = (Binding) bindings1.get("dummy");
        Assert.assertEquals(dummy, null);

        //        // check we get a vector of bindings for a collective binding with same names
        // removed because of new way of managing collection interfaces
        //        Assert.assertEquals(retreived1, b1);
        //        Vector v = new Vector();
        //        v.addElement(b2);
        //        v.addElement(b3);
        //        Assert.assertTrue(v.equals(retreived2));

        //check we get a single binding for a collective binding with different names
        Assert.assertEquals(retreived3, b5);
    }

    private void testContainsBindingOn() {
        Assert.assertTrue(bindings1.containsBindingOn("i2"));
        Assert.assertTrue(bindings2.containsBindingOn("i2"));
        Assert.assertTrue(bindings3.containsBindingOn("i201"));
        Assert.assertTrue(bindings3.containsBindingOn("i202"));
        Assert.assertFalse(bindings1.containsBindingOn("dummy"));
        Assert.assertFalse(bindings2.containsBindingOn("dummy"));
        Assert.assertFalse(bindings3.containsBindingOn("i2"));
    }

    private void testGetExternalClientBindings() {
        Assert.assertTrue(Arrays.equals(bindings1.getExternalClientBindings(),
                new String[] { "i2" }));
        Assert.assertTrue(Arrays.equals(bindings2.getExternalClientBindings(),
                new String[] { "i2" }));
        // cannot do equality between tables, as the ordering from getExternalsBindings is not predictable
        List l = Arrays.asList(bindings3.getExternalClientBindings());
        String[] t = new String[] { "i202", "i201" };
        Assert.assertTrue((l.size() == t.length) &&
            (l.containsAll(Arrays.asList(t))));
    }
}
