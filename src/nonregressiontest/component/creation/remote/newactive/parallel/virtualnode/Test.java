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

import nonregressiontest.component.I1;
import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentA;
import nonregressiontest.component.PrimitiveComponentB;

import nonregressiontest.descriptor.defaultnodes.TestNodes;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.type.ParallelComposite;

import testsuite.test.FunctionalTest;


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
public class Test extends FunctionalTest {
    private static final String P1_NAME = "primitive-component-1";

    //private static final String P2_NAME = "primitive-component-2";
    private static final String P3_NAME = "primitive-component-3";
    private static final String PARALLEL_NAME = "parallel-component-1";
    public static final String MESSAGE = "-->Main";
    Component primitiveComponentA;
    String name;
    String nodeUrl;
    Message message;
    Component[] parallelizedComponents;
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
        System.setProperty("proactive.future.ac", "enable");
        // start a new thread so that automatic continuations are enabled for components
        ACThread acthread = new ACThread();
        acthread.start();
        acthread.join();
        System.setProperty("proactive.future.ac", "disable");
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        parallelizedComponents = new Component[2];
    }

    private class ACThread extends Thread {
        public void run() {
            try {
                TypeFactory type_factory = Fractal.getTypeFactory();
                ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                            type_factory.createFcItfType("i1",
                                I1.class.getName(), TypeFactory.SERVER,
                                TypeFactory.MANDATORY, TypeFactory.SINGLE),
                            type_factory.createFcItfType("i2",
                                I2.class.getName(), TypeFactory.CLIENT,
                                TypeFactory.MANDATORY, TypeFactory.SINGLE)
                        });

                ComponentType i2_type = type_factory.createFcType(new InterfaceType[] {
                            type_factory.createFcItfType("i2",
                                I2.class.getName(), TypeFactory.SERVER,
                                TypeFactory.MANDATORY, TypeFactory.SINGLE)
                        });

                ComponentParameters parallelized_components_parameters = new ComponentParameters(P1_NAME,
                        ComponentParameters.PRIMITIVE, i1_i2_type);
                ComponentParameters p3_parameters = new ComponentParameters(P3_NAME,
                        ComponentParameters.PRIMITIVE, i2_type);
                ComponentParameters parallel_component_parameters = new ComponentParameters(PARALLEL_NAME,
                        ComponentParameters.PARALLEL, i1_i2_type);

                parallelizedComponents = ProActive.newActiveComponent(PrimitiveComponentA.class.getName(),
                        new Object[] {  },
                        TestNodes.getVirtualNode("Cyclic-AC"),
                        parallelized_components_parameters);
                p3 = ProActive.newActiveComponent(PrimitiveComponentB.class.getName(),
                        new Object[] {  }, null, null, null, p3_parameters);
                parallelComponent = ProActive.newActiveComponent(ParallelComposite.class.getName(),
                        new Object[] {  }, TestNodes.getRemoteACVMNode(), null,
                        null, parallel_component_parameters);
                
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        String parallelizedComponent_1_name = Fractal.getComponentParametersController(parallelizedComponents[0])
                                                     .getComponentParameters()
                                                     .getName();
        String parallelizedComponent_2_name = Fractal.getComponentParametersController(parallelizedComponents[1])
                                                     .getComponentParameters()
                                                     .getName();
        String p3_name = Fractal.getComponentParametersController(p3)
                                .getComponentParameters().getName();
        String pr1_name = Fractal.getComponentParametersController(parallelComponent)
                                 .getComponentParameters().getName();
        return (parallelizedComponent_1_name.equals(P1_NAME +
            ComponentParameters.CYCLIC_NODE_APPENDIX + 0) &&
        parallelizedComponent_2_name.equals(P1_NAME +
            ComponentParameters.CYCLIC_NODE_APPENDIX + 1) &&
        p3_name.equals(P3_NAME) && pr1_name.equals(PARALLEL_NAME));
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
