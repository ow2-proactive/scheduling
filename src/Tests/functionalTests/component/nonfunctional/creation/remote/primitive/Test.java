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
package functionalTests.component.nonfunctional.creation.remote.primitive;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentative;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerComponentImpl;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 * @author The ProActive Team
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
        /*
         * Getting the Fractal-Proactive bootstrap component
         */
        Component boot = Fractal.getBootstrapComponent();

        /*
         * Getting the Fractal-ProActive type factory
         */
        TypeFactory type_factory = Fractal.getTypeFactory(boot);

        /*
         * Getting the
         * Fractal-ProActive generic
         * factory
         */
        ProActiveGenericFactory cf = Fractive.getGenericFactory(boot);

        ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Test.class
                .getResource("/functionalTests/component/nonfunctional/creation/descriptor.xml").getPath());
        deploymentDescriptor.activateMappings();

        VirtualNode vn = deploymentDescriptor.getVirtualNode("computers-vn");

        ComponentType fcType = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "fitness-controller-membrane", DummyControllerItf.class.getName(), TypeFactory.SERVER,
                TypeFactory.MANDATORY, TypeFactory.SINGLE), });

        ControllerDescription controllerDescription = new ControllerDescription("fitnessController",
            Constants.PRIMITIVE);

        ContentDescription contentDescription = new ContentDescription(DummyControllerComponentImpl.class
                .getName());

        dummyNFComponent = newNFcInstance(cf, fcType, controllerDescription, contentDescription, vn);

        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getLifeCycleController(dummyNFComponent).startFc();
        DummyControllerItf ref = (DummyControllerItf) dummyNFComponent
                .getFcInterface("fitness-controller-membrane");
        name = ref.dummyMethodWithResult();
        System.out.println("Received result is : " + name);
        ref.dummyVoidMethod("Message");
    }

    public Component newFcInstance(ProActiveGenericFactory cf, Type type,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode virtualNode)
            throws Exception {
        if (virtualNode == null) {
            return cf.newFcInstance(type, controllerDesc, contentDesc, (Node) null);
        }

        virtualNode.activate();

        if (virtualNode.getNodes().length == 0) {
            throw new InstantiationException(
                "Cannot create component on virtual node as no node is associated with this virtual node");
        }
        return cf.newFcInstance(type, controllerDesc, contentDesc, virtualNode.getNode());
    }

    static public Component newNFcInstance(ProActiveGenericFactory cf, Type type,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode virtualNode)
            throws Exception {
        if (virtualNode == null) {
            return cf.newNFcInstance(type, controllerDesc, contentDesc, (Node) null);
        }
        virtualNode.activate();
        if (virtualNode.getNodes().length == 0) {
            throw new InstantiationException(
                "Cannot create component on virtual node as no node is associated with this virtual node");
        }
        return cf.newNFcInstance(type, controllerDesc, contentDesc, virtualNode.getNode());
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
