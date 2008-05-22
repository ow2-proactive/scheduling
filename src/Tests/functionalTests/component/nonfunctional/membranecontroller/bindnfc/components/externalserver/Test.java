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
package functionalTests.component.nonfunctional.membranecontroller.bindnfc.components.externalserver;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.controller.MembraneController;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import functionalTests.ComponentTest;
import functionalTests.component.creation.ComponentInfo;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 * @author Paul Naoumenko
 *
 *Testing non-functional bindings
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super(
                "Binds the non-functional external server interface of composite component to a component inside its membrane",
                "Binds the non-functional external server interface of composite component to a component inside its membrane");
    }

    @org.junit.Test
    public void action() throws Exception {
        //Thread.sleep(2000);
        Component boot = Fractal.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        TypeFactory type_factory = Fractal.getTypeFactory(boot); /*Getting the Fractal-ProActive type factory*/
        ProActiveGenericFactory cf = Fractive.getGenericFactory(boot); /*Getting the Fractal-ProActive generic factory*/

        Type fType = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "componentInfo", ComponentInfo.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                TypeFactory.SINGLE), });

        Type nfType = type_factory
                .createFcType(new InterfaceType[] {
                        type_factory
                                .createFcItfType(
                                        Constants.BINDING_CONTROLLER,
                                        /*BINDING CONTROLLER*/org.objectweb.proactive.core.component.controller.ProActiveBindingController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.COMPONENT_PARAMETERS_CONTROLLER,
                                        /*COMPONENT PARAMETERS CONTROLLER*/org.objectweb.proactive.core.component.controller.ComponentParametersController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.CONTENT_CONTROLLER,
                                        /*CONTENT CONTROLLER*/org.objectweb.proactive.core.component.controller.ProActiveContentController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.LIFECYCLE_CONTROLLER,
                                        /*LIFECYCLE CONTROLLER*/org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.SUPER_CONTROLLER,
                                        /*SUPER CONTROLLER*/org.objectweb.proactive.core.component.controller.ProActiveSuperController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory.createFcItfType(Constants.NAME_CONTROLLER,
                        /*NAME CONTROLLER*/org.objectweb.fractal.api.control.NameController.class.getName(),
                                TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                        type_factory.createFcItfType(Constants.MEMBRANE_CONTROLLER,
                        /*MEMBRANE CONTROLLER*/MembraneController.class.getName(), TypeFactory.SERVER,
                                TypeFactory.MANDATORY, TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        "dummy-controller",
                                        /*DUMMY CONTROLLER*/functionalTests.component.nonfunctional.creation.DummyControllerItf.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),

                });

        componentA = cf.newFcInstance(fType, nfType, (ContentDescription) null,//The component is composite
                new ControllerDescription("componentA", Constants.COMPOSITE, !Constants.SYNCHRONOUS,
                    Constants.WITHOUT_CONFIG_FILE), (Node) null);

        //Filling the membrane with object  controllers
        MembraneController memController = Fractive.getMembraneController(componentA);

        memController.setControllerObject(Constants.BINDING_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveBindingControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.COMPONENT_PARAMETERS_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ComponentParametersControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.CONTENT_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveContentControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveNameController.class.getName());

        //Creation of the non-functional name controller component
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getNFFactory();
        Map context = new HashMap();
        Component nameController = null;
        nameController = (Component) f.newComponent(
                "org.objectweb.proactive.core.component.componentcontroller.adl.nameControllerComponent",
                context);

        System.out.println("The name of the nameController component is : " +
            Fractal.getNameController(nameController).getFcName());

        Fractal.getNameController(nameController).setFcName("nameController");//Mandatory to manipulate NF components inside the membrane
        //Fractal.getLifeCycleController(parametersController).startFc();
        memController.addNFSubComponent(nameController);
        memController.bindNFc(Constants.NAME_CONTROLLER, "nameController.name");

        //Adding a non-functional GCM component into the membrane.
        Component dummyMaster = (Component) f.newComponent(
                "functionalTests.component.nonfunctional.adl.dummyMaster", context);
        Fractal.getNameController(dummyMaster).setFcName("dummyMaster");

        memController.addNFSubComponent(dummyMaster);
        memController.bindNFc("dummy-controller", "dummyMaster.dummy-master");

        Component dummyController = (Component) f.newComponent(
                "functionalTests.component.nonfunctional.adl.dummyPrimitive", context);

        Fractal.getNameController(dummyController).setFcName("dummyPrimitive");

        memController.addNFSubComponent(dummyController);
        memController.bindNFc("dummyMaster.dummy-client", "dummyPrimitive.dummy-membrane");

        memController.startMembrane();// Before starting the mmebrane, make sure that all mandatory NF interfaces are bound
        System.err.println("Name is : " + Fractal.getNameController(componentA).getFcName());
        memController.stopMembrane();//The membrane must be in a stopped state for reconfiguration

        memController.removeNFSubComponent(nameController);
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveNameController.class.getName());
        memController.startMembrane();//Restart the membrane, to be able to serve non-functional calls
        System.err.println("Object replaces component : Name is : " +
            Fractal.getNameController(componentA).getFcName());
        memController.stopMembrane();

        memController.addNFSubComponent(nameController);
        memController.bindNFc(Constants.NAME_CONTROLLER, "nameController.name");

        memController.startMembrane();
        System.err.println("Name is : component replaces object : " +
            Fractal.getNameController(componentA).getFcName());
        memController.stopMembrane();

        memController.startMembrane();
        Object lookUp = memController.lookupNFc("dummyMaster.dummy-client");
        System.out.println("The returned interface is : " + lookUp);
        DummyControllerItf dummyControl = (DummyControllerItf) componentA.getFcInterface("dummy-controller");
        System.out.println("Dummy void method : " + dummyControl.dummyMethodWithResult());
        IntWrapper res = dummyControl.result(new IntWrapper(4));
        System.out.println(" Message with return value : " + res.intValue());

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
        Fractal.getLifeCycleController(componentA).stopFc();
    }

    public boolean postConditions() throws Exception {
        return (componentA instanceof ProActiveComponentRepresentative);
    }
}
