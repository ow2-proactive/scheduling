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
package functionalTests.component.nonfunctional.creation.remote.composite;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
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

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerComponentImpl;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 * @author The ProActive Team
 *
 * creates a new non-functional component, marked as non-functional
 */
public class Test extends ComponentTest {
    Component dummyNFComposite;
    Component dummyNFPrimitive;
    String name;
    String nodeUrl;

    public Test() {
        super("Creation of a primitive non functional-component on the local default node",
                "Test newActiveComponent method for a primitive component on the local default node");
    }

    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent(); /*
         * Getting the Fractal-Proactive
         * bootstrap component
         */
        TypeFactory type_factory = Fractal.getTypeFactory(boot); /*
         * Getting the Fractal-ProActive
         * type factory
         */
        ProActiveGenericFactory cf = Fractive.getGenericFactory(boot); /*
         * Getting the
         * Fractal-ProActive generic
         * factory
         */

        System.out.println("Remote composite");
        ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Test.class
                .getResource("/functionalTests/component/nonfunctional/creation/descriptor.xml").getPath());
        deploymentDescriptor.activateMappings();
        VirtualNode vn = deploymentDescriptor.getVirtualNode("computers-vn");

        dummyNFComposite = cf.newNFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("fitness-controller-membrane", DummyControllerItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), }),
                new ControllerDescription("fitnessController", Constants.COMPOSITE), /*
                 * new
                 * ContentDescription(DummyControllerComponentImpl.class.getName())
                 */
                null, vn);
        //logger.debug("OK, instantiated the component");
        // start the component!
        dummyNFPrimitive = cf.newNFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("fitness-controller-membrane", DummyControllerItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), }),
                new ControllerDescription("fitnessController", Constants.PRIMITIVE), new ContentDescription(
                    DummyControllerComponentImpl.class.getName()));

        Fractal.getContentController(dummyNFComposite).addFcSubComponent(dummyNFPrimitive);
        Fractal.getBindingController(dummyNFComposite).bindFc("fitness-controller-membrane",
                dummyNFPrimitive.getFcInterface("fitness-controller-membrane"));

        Fractal.getLifeCycleController(dummyNFComposite).startFc();
        DummyControllerItf ref = (DummyControllerItf) dummyNFComposite
                .getFcInterface("fitness-controller-membrane");
        name = ref.dummyMethodWithResult();
        ref.dummyVoidMethod("Message to a composite");
        Assert.assertTrue(dummyNFComposite instanceof ProActiveNFComponentRepresentative);
        Fractal.getLifeCycleController(dummyNFComposite).stopFc();
    }
}
