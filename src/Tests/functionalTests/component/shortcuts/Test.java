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
package functionalTests.component.shortcuts;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.ProFuture;

import functionalTests.ComponentTest;
import functionalTests.component.I1;
import functionalTests.component.Message;
import functionalTests.component.PrimitiveComponentA;
import functionalTests.component.PrimitiveComponentB;
import functionalTests.component.Setup;


/**
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {

    /**
         *
         */
    private static final long serialVersionUID = 8211123887252319212L;
    private static final int NB_WRAPPERS = 5;
    private Message result1;
    private Message result2;
    private Message result3;
    private Message result4;
    private final String expectedResult = "foo" + PrimitiveComponentA.MESSAGE +
        PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE;
    private Component systemWithWrappingWithShortcuts;
    private Component systemWithWrappingWithoutShortcuts;
    private Component systemWithoutWrapping;

    public Test() {
        super("Shortcut communications through composite components",
            "Shortcut communications through composite components");
    }

    @org.junit.Test
    public void action() throws Exception {
        initializeComponentSystems();

        //System.out.println("testing unwrapped system");
        Fractal.getLifeCycleController(systemWithoutWrapping).stopFc();
        Fractal.getLifeCycleController(systemWithoutWrapping).startFc();
        result1 = ((I1) systemWithoutWrapping.getFcInterface("i1")).processInputMessage(new Message(
                    "foo"));
        // waiting for the future is only for having an ordered logging output
        ProFuture.waitFor(result1);
        Thread.sleep(2000);

        //System.out.println("testing wrapped system without shortcuts");
        Fractal.getLifeCycleController(systemWithWrappingWithoutShortcuts)
               .stopFc();
        Fractal.getLifeCycleController(systemWithWrappingWithoutShortcuts)
               .startFc();

        result2 = ((I1) systemWithWrappingWithoutShortcuts.getFcInterface("i1")).processInputMessage(new Message(
                    "foo"));
        ProFuture.waitFor(result2);
        Thread.sleep(2000);

        //System.out.println("testing wrapped system with shortcuts -- fist invocation");
        // first call, which performs tensioning
        result3 = ((I1) systemWithWrappingWithShortcuts.getFcInterface("i1")).processInputMessage(new Message(
                    "foo"));
        ProFuture.waitFor(result3);
        Thread.sleep(2000);

        Fractal.getLifeCycleController(systemWithWrappingWithShortcuts).stopFc();
        Fractal.getLifeCycleController(systemWithWrappingWithShortcuts).startFc();

        // second call, which goes directly through the shortcut
        //System.out.println("testing wrapped system with shortcuts -- second invocation");
        result4 = ((I1) systemWithWrappingWithShortcuts.getFcInterface("i1")).processInputMessage(new Message(
                    "foo"));
        ProFuture.waitFor(result4);
        Thread.sleep(2000);

        // TODO_M manage shortcuts with reconfigurations
        // reset while shortcut exists
        //        resetComponentSystem();
        //        initializeComponentSystems();
        //      first call, which performs tensioning
        //        result5 = ((I1) systemWithWrappingWithShortcuts.getFcInterface("i1")).processInputMessage(new Message("foo"));
        // a shortcut is now realized. Compare with previous result
        //        result6 = ((I1) systemWithWrappingWithShortcuts.getFcInterface("i1")).processInputMessage(new Message("foo"));
        Assert.assertEquals(expectedResult,
            ((Message) ProFuture.getFutureValue(result4)).getMessage());
        Assert.assertEquals(expectedResult,
            ((Message) ProFuture.getFutureValue(result3)).getMessage());
        Assert.assertEquals(expectedResult,
            ((Message) ProFuture.getFutureValue(result2)).getMessage());
        Assert.assertEquals(expectedResult,
            ((Message) ProFuture.getFutureValue(result1)).getMessage());
    }

    private void initializeComponentSystems() throws Exception {
        // system without wrapped components
        Component unwrappedA = Setup.createPrimitiveA();
        Component unwrappedB = Setup.createPrimitiveB1();
        Fractal.getBindingController(unwrappedA)
               .bindFc("i2", unwrappedB.getFcInterface("i2"));
        Fractal.getLifeCycleController(unwrappedA).startFc();
        Fractal.getLifeCycleController(unwrappedB).startFc();
        systemWithoutWrapping = unwrappedA;

        // system with wrapping but without shortcuts
        Component wrappedAWithoutShortcuts = Setup.createPrimitiveA();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedAWithoutShortcuts = wrapWithCompositeOfTypeA(NB_WRAPPERS -
                    i, wrappedAWithoutShortcuts);
        }

        Component wrappedBWithoutShortcuts = Setup.createPrimitiveB1();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedBWithoutShortcuts = wrapWithCompositeOfTypeB(NB_WRAPPERS -
                    i, wrappedBWithoutShortcuts);
        }

        Fractal.getBindingController(wrappedAWithoutShortcuts)
               .bindFc("i2", wrappedBWithoutShortcuts.getFcInterface("i2"));
        Fractal.getLifeCycleController(wrappedAWithoutShortcuts).startFc();
        Fractal.getLifeCycleController(wrappedBWithoutShortcuts).startFc();

        systemWithWrappingWithoutShortcuts = wrappedAWithoutShortcuts;

        // system with wrapping and with shortcuts
        Component wrappedAWithShortcuts = Setup.createPrimitiveA();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedAWithShortcuts = wrapWithSynchronousCompositeOfTypeA(NB_WRAPPERS -
                    i, wrappedAWithShortcuts);
        }

        Component wrappedBWithShortcuts = Setup.createPrimitiveB1();
        for (int i = 0; i < NB_WRAPPERS; i++) {
            wrappedBWithShortcuts = wrapWithSynchronousCompositeOfTypeB(NB_WRAPPERS -
                    i, wrappedBWithShortcuts);
        }

        Fractal.getBindingController(wrappedAWithShortcuts)
               .bindFc("i2", wrappedBWithShortcuts.getFcInterface("i2"));
        Fractal.getLifeCycleController(wrappedAWithShortcuts).startFc();
        Fractal.getLifeCycleController(wrappedBWithShortcuts).startFc();

        systemWithWrappingWithShortcuts = wrappedAWithShortcuts;
    }

    private void resetComponentSystem()
        throws IllegalContentException, IllegalLifeCycleException,
            NoSuchInterfaceException, IllegalBindingException {
        // TODO_M change the inner wrapped components and check the shortcut is aware of the reconfiguration
    }

    private Component wrapWithSynchronousCompositeOfTypeB(int index,
        Component wrappee) throws Exception {
        Component wrapper = Setup.createSynchronousCompositeOfTypeB(
                "sync_composite_b" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper)
               .bindFc("i2", wrappee.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithCompositeOfTypeB(int index, Component wrappee)
        throws Exception {
        Component wrapper = Setup.createCompositeOfTypeB("composite_b" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper)
               .bindFc("i2", wrappee.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithSynchronousCompositeOfTypeA(int index,
        Component wrappee) throws Exception {
        Component wrapper = Setup.createSynchronousCompositeOfTypeA(
                "sync_composite_a" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper)
               .bindFc("i1", wrappee.getFcInterface("i1"));
        Fractal.getBindingController(wrappee)
               .bindFc("i2", wrapper.getFcInterface("i2"));
        return wrapper;
    }

    private Component wrapWithCompositeOfTypeA(int index, Component wrappee)
        throws Exception {
        Component wrapper = Setup.createCompositeOfTypeA("composite_a" + index);
        Fractal.getContentController(wrapper).addFcSubComponent(wrappee);
        Fractal.getBindingController(wrapper)
               .bindFc("i1", wrappee.getFcInterface("i1"));
        Fractal.getBindingController(wrappee)
               .bindFc("i2", wrapper.getFcInterface("i2"));
        return wrapper;
    }

    @Before
    public void initTest() throws Exception {
        System.setProperty("proactive.components.use_shortcuts", "true");
    }

    @After
    public void endTest() throws Exception {
        System.setProperty("proactive.components.use_shortcuts", "false");
    }
}
