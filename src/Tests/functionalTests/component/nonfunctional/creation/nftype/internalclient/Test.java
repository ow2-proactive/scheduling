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
package functionalTests.component.nonfunctional.creation.nftype.internalclient;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

import org.objectweb.proactive.core.node.Node;

import functionalTests.ComponentTest;

import functionalTests.component.creation.ComponentInfo;


/**
 * @author Paul Naoumenko
 *
 *Creates a functional component, by defining among its parameters its non-functional type
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super("Creation of a composite functional component with internal non-functional client interface",
                "Creation of a composite functional component with internal non-functional client interface");
    }

    @org.junit.Test
    public void action() throws Exception {
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
                        type_factory
                                .createFcItfType(
                                        Constants.MULTICAST_CONTROLLER,
                                        /*MULTICAST CONTROLLER*/org.objectweb.proactive.core.component.controller.MulticastController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.GATHERCAST_CONTROLLER,
                                        /*GATHERCAST CONTROLLER*/org.objectweb.proactive.core.component.controller.GathercastController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),
                        type_factory
                                .createFcItfType(
                                        Constants.MIGRATION_CONTROLLER,
                                        /*MIGRATION CONTROLLER*/org.objectweb.proactive.core.component.controller.MigrationController.class
                                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                        TypeFactory.SINGLE),

                        ((ProActiveTypeFactory) type_factory)
                                .createFcItfType(
                                        "dummy-internal-client-controller",
                                        /*DUMMY CONTROLLER*/functionalTests.component.nonfunctional.creation.DummyControllerItf.class
                                                .getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                                        ProActiveTypeFactory.SINGLETON_CARDINALITY,
                                        ProActiveTypeFactory.INTERNAL) });

        componentA = cf.newFcInstance(fType, nfType, (ContentDescription) null,//The component is composite
                new ControllerDescription("componentA", Constants.COMPOSITE, !Constants.SYNCHRONOUS,
                    Constants.WITHOUT_CONFIG_FILE), (Node) null);

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

    }

    public boolean postConditions() throws Exception {
        return (componentA instanceof ProActiveComponentRepresentative);
    }
}
