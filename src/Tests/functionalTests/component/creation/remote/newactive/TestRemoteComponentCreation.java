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
package functionalTests.component.creation.remote.newactive;

import org.junit.Assert;
import org.junit.Ignore;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.node.Node;

import functionalTests.ComponentTestDefaultNodes;
import functionalTests.component.creation.ComponentA;
import functionalTests.component.creation.ComponentInfo;


/**
 * @author Matthieu Morel
 *
 * creates a primitive component on a remote node with ACs
 */
public class TestRemoteComponentCreation extends ComponentTestDefaultNodes {

    public TestRemoteComponentCreation() {

        super(DeploymentType._1x1);
        //        super("Creation of a primitive component on a remote node",
        //                "Test newActiveComponent method for a primitive component on a remote node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void primitiveCreation() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);

        Node remoteNode = super.getANode();
        String remoteHost = remoteNode.getVMInformation().getHostName();
        Assert.assertTrue(remoteHost != null);

        Component componentA = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("componentInfo", ComponentInfo.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) }), new ControllerDescription(
            "componentA", Constants.PRIMITIVE), new ContentDescription(ComponentA.class.getName(),
            new Object[] { "toto" }), remoteNode);
        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getLifeCycleController(componentA).startFc();
        ComponentInfo ref = (ComponentInfo) componentA.getFcInterface("componentInfo");
        String name = ref.getName();
        String nodeUrl = ref.getNodeUrl();

        Assert.assertEquals(name, "toto");
        Assert.assertTrue(nodeUrl.indexOf(remoteHost) != -1);
    }

    @org.junit.Test
    @Ignore
    public void compositeCreation() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);

        Node remoteNode = super.getANode();
        String remoteHost = remoteNode.getVMInformation().getHostName();
        Assert.assertTrue(remoteHost != null);

        Component primitiveA = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("componentInfo", ComponentInfo.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) }), new ControllerDescription(
            "componentA", Constants.PRIMITIVE), new ContentDescription(ComponentA.class.getName(),
            new Object[] { "toto" }));

        Component compositetA = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("componentInfo", ComponentInfo.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) }), new ControllerDescription(
            "compositetA", Constants.COMPOSITE), null, remoteNode);
        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getContentController(compositetA).addFcSubComponent(primitiveA);

        Fractal.getLifeCycleController(compositetA).startFc();
        ComponentInfo ref = (ComponentInfo) compositetA.getFcInterface("componentInfo");
        String name = ref.getName();
        String nodeUrl = ((ComponentInfo) compositetA.getFcInterface("componentInfo")).getNodeUrl();

        Assert.assertEquals(name, "toto");
        Assert.assertTrue(nodeUrl.indexOf(remoteHost) != -1);
    }
}
