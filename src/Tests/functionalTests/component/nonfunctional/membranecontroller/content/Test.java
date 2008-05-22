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
 * 
 * A PAS COMMITER
 */
package functionalTests.component.nonfunctional.membranecontroller.content;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerComponentImpl;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;
import functionalTests.component.nonfunctional.membranecontroller.DummyFunctionalComponentImpl;
import functionalTests.component.nonfunctional.membranecontroller.DummyFunctionalItf;


/**
 * @author The ProActive Team
 *
 * Testing adding and getting a reference on a non-functional component
 */
public class Test extends ComponentTest {
    Component dummyNFComponent;
    Component dummyFComponent;
    String name;
    String nodeUrl;

    public Test() {
        super("Adding non-functional component inside the membrane,then getting a reference",
                "Test Adding non-functional component inside the membrane, then getting a reference");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        // Thread.sleep(2000);
        Component boot = Fractal.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        TypeFactory type_factory = Fractal.getTypeFactory(boot); /*Getting the Fractal-ProActive type factory*/
        ProActiveGenericFactory cf = Fractive.getGenericFactory(boot); /*Getting the Fractal-ProActive generic factory*/

        dummyNFComponent = cf.newNFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("dummy-controller-membrane", DummyControllerItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), }),
                new ControllerDescription("dummyController", Constants.PRIMITIVE), new ContentDescription(
                    DummyControllerComponentImpl.class.getName()));

        logger.debug("NF component created");
        dummyFComponent = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("dummy-functional", DummyFunctionalItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE), }), new ControllerDescription(
            "dummyFunctional", Constants.PRIMITIVE, getClass().getResource(
                    "/functionalTests/component/nonfunctional/membranecontroller/content/config.xml")
                    .getPath()), new ContentDescription(DummyFunctionalComponentImpl.class.getName()));

        Fractive.getMembraneController(dummyFComponent).addNFSubComponent(dummyNFComponent);
        Component[] components = Fractive.getMembraneController(dummyFComponent).getNFcSubComponents();
        System.out.println("Name : " + Fractal.getNameController(components[0]).getFcName());
        Fractive.getMembraneController(dummyFComponent).startNFc("dummyController");
        Fractive.getMembraneController(dummyFComponent).stopNFc("dummyController");
        System.out.println("Lifecycle state :" +
            Fractive.getMembraneController(dummyFComponent).getNFcState("dummyController"));
        Fractive.getMembraneController(dummyFComponent).removeNFSubComponent(dummyNFComponent);
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
        //Fractal.getLifeCycleController(dummyNFComponent).stopFc();
    }

    public boolean postConditions() throws Exception {
        return /*(dummyNFComponent  instanceof ProActiveNFComponentRepresentative)*/true;
    }

}
