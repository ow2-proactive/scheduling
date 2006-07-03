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
package nonregressiontest.component.creation.remote.newactive.parallel.virtualnode;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.type.ParallelComposite;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

import testsuite.test.Assertions;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1;
import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;
import nonregressiontest.descriptor.defaultnodes.TestNodes;


/**
 * @author Matthieu Morel
 *
 * Step 1. Creation of the components
 *
 * Creates the following components :
 *
 *                 __________________
 *                 |                                  |
 *                 |                                        |                            ________
 *         i1     |                                  |i2                        |              |
 *                 |                                  |                i2        |(p3)       |
 *                 |                                  |                           |_______|
 *                 |_(pr1)____________|
 *
 *                                          ________                                ________
 *                                         |              |                              |              |
 *                                  i1    |   (p1)    |i2                        i1| (p2)      |i2
 *                                         |_______|                              |_______|
 *
 *
 *         where :
 *                 (p1), (p2) and (p3) are primitive components,
 *                  pr1 is a parallel component
 *                 i1 represents an interface of type I1
 *                 i2 represents an interface of type I2
 *                 i3 represents an interface of type I3
 *                                 pr1 and p2 are remote components
 *
 */
public class Test extends ComponentTest {
    private static final String P1_NAME = "primitive-component-1";

    //private static final String P2_NAME = "primitive-component-2";
    private static final String P3_NAME = "primitive-component-3";
    private static final String PARALLEL_NAME = "parallel-component-1";
    public static final String MESSAGE = "-->Main";
    Component primitiveComponentA;
    String name;
    String nodeUrl;
    Message message;
    Component parallelizedComponentsOnVirtualNode; // typed group
    Component parallelizedComponentsOnArrayOfNodes; // typed group created from Node[]
    Component p3;
    Component parallelComponent;

    public Test() {
        super("Creation of a parallel system on a cyclic virtual node",
            "Test creation of a parallel system on a cyclic virtual node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        ProActiveGenericFactory cf = (ProActiveGenericFactory)Fractal.getGenericFactory(boot);
        ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                    type_factory.createFcItfType("i1", I1.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                    type_factory.createFcItfType("i2", I2.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                });

        ComponentType i2_type = type_factory.createFcType(new InterfaceType[] {
                    type_factory.createFcItfType("i2", I2.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                });

        // those are created on a VirtualNode (multiple)
        parallelizedComponentsOnVirtualNode = (Component)((Group)cf.newFcInstanceAsList(i1_i2_type,
                new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(),
                    new Object[] {  }),  TestNodes.getVirtualNode("Cyclic-AC"))).getGroupByType();

        // those are created on Node[]
        
        parallelizedComponentsOnArrayOfNodes = (Component)((Group)cf.newFcInstanceAsList(i1_i2_type,
                new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(),
                    new Object[] {  }), TestNodes.getVirtualNode("Cyclic-AC").getNodes())).getGroupByType();

        
        p3 = cf.newFcInstance(i2_type,
                new ControllerDescription(P3_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(),
                    new Object[] {  }));
        parallelComponent = cf.newFcInstance(i1_i2_type,
                new ControllerDescription(PARALLEL_NAME, Constants.PARALLEL),
                new ContentDescription(ParallelComposite.class.getName(),
                    new Object[] {  }), TestNodes.getRemoteACVMNode());
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
        String parallelizedComponentOnVirtualNode_1_name = Fractal.getNameController((Component) ProActiveGroup.getGroup(
                    parallelizedComponentsOnVirtualNode).get(0)).getFcName();
        String parallelizedComponentOnVirtualNode_2_name = Fractal.getNameController((Component) ProActiveGroup.getGroup(
                    parallelizedComponentsOnVirtualNode).get(1)).getFcName();

        String parallelizedComponentOnArrayOfNodes_1_name = Fractal.getNameController((Component) ProActiveGroup.getGroup(
                parallelizedComponentsOnVirtualNode).get(0)).getFcName();
    String parallelizedComponentOnArrayOfNodes_2_name = Fractal.getNameController((Component) ProActiveGroup.getGroup(
                parallelizedComponentsOnVirtualNode).get(1)).getFcName();

        
        String p3_name = Fractal.getNameController(p3).getFcName();
        String pr1_name = Fractal.getNameController(parallelComponent)
                                 .getFcName();

        Assertions.assertEquals(parallelizedComponentOnVirtualNode_1_name, (P1_NAME +
            Constants.CYCLIC_NODE_SUFFIX + 0));
        Assertions.assertEquals(parallelizedComponentOnVirtualNode_2_name, (P1_NAME +
            Constants.CYCLIC_NODE_SUFFIX + 1));
        Assertions.assertEquals(parallelizedComponentOnArrayOfNodes_1_name, (P1_NAME +
                Constants.CYCLIC_NODE_SUFFIX + 0));
        Assertions.assertEquals(parallelizedComponentOnArrayOfNodes_2_name, (P1_NAME +
                Constants.CYCLIC_NODE_SUFFIX + 1));
        
        Assertions.assertEquals(p3_name, P3_NAME);
        Assertions.assertEquals(pr1_name, PARALLEL_NAME);
        
        
        return true;
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
            test.postConditions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
