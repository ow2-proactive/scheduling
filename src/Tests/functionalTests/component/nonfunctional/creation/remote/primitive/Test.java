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
package functionalTests.component.nonfunctional.creation.remote.primitive;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentative;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerComponentImpl;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 * @author Paul Naoumenko
 *
 * creates a new non-functional component, marked as non-functional
 */
public class Test extends ComponentTest {
    Component dummyNFComponent;
    String name;
    String nodeUrl;

    public Test() {
        super("Creation of a primitive non functional-component on the local default node",
            "Test newActiveComponent method for a primitive component on the local default node");
    }

    @org.junit.Test
    public void action() throws Exception {
        Thread.sleep(2000);
        Component boot = Fractal.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        TypeFactory type_factory = Fractal.getTypeFactory(boot); /*Getting the Fractal-ProActive type factory*/
        ProActiveGenericFactory cf = Fractive.getGenericFactory(boot); /*Getting the Fractal-ProActive generic factory*/

        ProActiveDescriptor deploymentDescriptor = ProDeployment.getProactiveDescriptor(Test.class.getResource(
                    "/functionalTests/component/nonfunctional/creation/descriptor.xml")
                                                                                              .getPath());
        deploymentDescriptor.activateMappings();
        VirtualNode vn = deploymentDescriptor.getVirtualNode("computers-vn");
        dummyNFComponent = cf.newNFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType(
                            "fitness-controller-membrane",
                            DummyControllerItf.class.getName(),
                            TypeFactory.SERVER, TypeFactory.MANDATORY,
                            TypeFactory.SINGLE),
                    }),
                new ControllerDescription("fitnessController",
                    Constants.PRIMITIVE),
                new ContentDescription(DummyControllerComponentImpl.class.getName()),
                vn);
        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getLifeCycleController(dummyNFComponent).startFc();
        DummyControllerItf ref = (DummyControllerItf) dummyNFComponent.getFcInterface(
                "fitness-controller-membrane");
        name = ref.dummyMethodWithResult();
        System.out.println("Received result is : " + name);
        ref.dummyVoidMethod("Message");
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
        Fractal.getLifeCycleController(dummyNFComponent).stopFc();
    }

    public boolean postConditions() throws Exception {
        return (dummyNFComponent instanceof ProActiveNFComponentRepresentative);
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
            test.endTest();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
