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
package functionalTests.component.creation.local.newactive.primitive;

import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.ComponentTest;
import functionalTests.component.creation.ComponentA;
import functionalTests.component.creation.ComponentInfo;


/**
 * @author Matthieu Morel
 *
 * creates a new component
 */
public class Test extends ComponentTest {

    /**
         *
         */
    private static final long serialVersionUID = 8246601088194840413L;
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super("Creation of a primitive component on the local default node",
            "Test newActiveComponent method for a primitive component on the local default node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        componentA = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("componentInfo",
                            ComponentInfo.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    }),
                new ControllerDescription("componentA", Constants.PRIMITIVE),
                new ContentDescription(ComponentA.class.getName(),
                    new Object[] { "toto" }));
        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getLifeCycleController(componentA).startFc();
        ComponentInfo ref = (ComponentInfo) componentA.getFcInterface(
                "componentInfo");
        name = ref.getName();
        nodeUrl = ((ComponentInfo) componentA.getFcInterface("componentInfo")).getNodeUrl();

        Assert.assertEquals(name, "toto");
        Assert.assertTrue(nodeUrl.indexOf(URIBuilder.getHostNameorIP(
                    ProActiveInet.getInstance().getInetAddress())) != -1);
    }
}
