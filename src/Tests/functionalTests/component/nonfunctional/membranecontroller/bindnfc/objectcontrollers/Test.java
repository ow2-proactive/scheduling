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
package functionalTests.component.nonfunctional.membranecontroller.bindnfc.objectcontrollers;

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
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;

import org.objectweb.proactive.core.node.Node;

import functionalTests.ComponentTest;

import functionalTests.component.creation.ComponentA;
import functionalTests.component.creation.ComponentInfo;


/**
 * @author Paul Naoumenko
 *
 * Experimenting with non-functional type and controller objects
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super("Setting object controllers as an implementation of non-functional interfaces",
                "Test setControllerObject method of the MembraneController");
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

                });

        componentA = cf.newFcInstance(fType, nfType, (ContentDescription) null,//The component is composite        
                new ControllerDescription("myComposite", Constants.COMPOSITE, !Constants.SYNCHRONOUS,
                    Constants.WITHOUT_CONFIG_FILE), (Node) null);

        MembraneController memController = Fractive.getMembraneController(componentA);
        //Setting the controllers by hand

        memController.setControllerObject(Constants.BINDING_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveBindingControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.CONTENT_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveContentControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveNameController.class.getName());

        memController.startMembrane();//Starting the membrane, non-functional calls can be emitted on controllers
        //Emmiting calls on non-functional interfaces

        System.out.println("Name of the composite is :" + Fractal.getNameController(componentA).getFcName());

        Component componentB = cf.newFcInstance(fType, nfType, new ContentDescription(ComponentA.class
                .getName(), new Object[] { "tata" }), new ControllerDescription("componentB",
            Constants.PRIMITIVE, !Constants.SYNCHRONOUS, Constants.WITHOUT_CONFIG_FILE), (Node) null);

        MembraneController componentBMembraneController = Fractive.getMembraneController(componentB);

        componentBMembraneController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl.class
                        .getName());
        componentBMembraneController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveNameController.class.getName());
        componentBMembraneController.startMembrane();//Need to do this, otherwise, when adding this component to the composite one, there will be a suspension, because the addFcSubComponent method is calling the SuperController of the primitive component

        Fractal.getContentController(componentA).addFcSubComponent(componentB);
        Fractal.getBindingController(componentA).bindFc("componentInfo",
                componentB.getFcInterface("componentInfo"));

        System.out.println("Parameters are : " +
            ((ProActiveComponent) componentA).getComponentParameters().getHierarchicalType());
        System.out.println("Lifecycle state is : " + Fractal.getLifeCycleController(componentA).getFcState());

        System.out.println("Name is :" + Fractal.getNameController(componentA).getFcName());
        Component[] tabComp = Fractal.getSuperController(componentB).getFcSuperComponents();
        System.out.println("Super components of primitive: " + tabComp);
        //tabComp=Fractal.getContentController(componentA).getFcSubComponents();
        //Component[] tabComp2=Fractal.getSuperController(tabComp[0]).getFcSuperComponents();
        //System.err.println("Super components of primitive: "+tabComp2);

        memController.stopMembrane();
        memController.setControllerObject(Constants.BINDING_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveBindingControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.CONTENT_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveContentControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl.class
                        .getName());
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveNameController.class.getName());

        memController.startMembrane();
        Fractal.getBindingController(componentA).unbindFc("componentInfo");
        Fractal.getContentController(componentA).removeFcSubComponent(componentB);
        Fractal.getContentController(componentA).addFcSubComponent(componentB);
        Fractal.getBindingController(componentA).bindFc("componentInfo",
                componentB.getFcInterface("componentInfo"));

        System.out.println("Parameters are : " +
            ((ProActiveComponent) componentA).getComponentParameters().getHierarchicalType());
        System.out.println("Lifecycle state is : " + Fractal.getLifeCycleController(componentA).getFcState());
        System.out.println("Name of the composte is :" + Fractal.getNameController(componentA).getFcName());

        componentBMembraneController.stopMembrane();
        componentBMembraneController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.controller.ProActiveSuperControllerImpl.class
                        .getName());
        componentBMembraneController.startMembrane();
        tabComp = Fractal.getSuperController(componentB).getFcSuperComponents();

        System.out.println("Super components of composite: " + tabComp);

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
