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
package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.HashMap;
import java.util.Map;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.I1;
import nonregressiontest.component.I2;
import nonregressiontest.component.Message;
import nonregressiontest.component.PrimitiveComponentB;
import nonregressiontest.component.collectiveitf.multicast.ClientParamLevelDispatchAnnotationTester;
import nonregressiontest.component.collectiveitf.multicast.MulticastTestItf;
import nonregressiontest.component.collectiveitf.multicast.Identifiable;
import nonregressiontest.component.collectiveitf.multicast.NonAnnotatedClientItf;
import nonregressiontest.component.collectiveitf.multicast.ServerImpl;
import nonregressiontest.component.collectiveitf.multicast.ServerItfTestItf;
import nonregressiontest.component.collectiveitf.multicast.ServerTestItf;
import nonregressiontest.component.collectiveitf.multicast.TesterImpl;
import nonregressiontest.component.collectiveitf.multicast.Tester;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;


/**
 * @author Matthieu Morel
 * a test for bindings on client collective interfaces between remote components
 *
 * Tests are performed using assertions in related classes
 */
public class Test extends ComponentTest {
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;


    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
    public void action() throws Exception {

        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        Component testcase = (Component) f.newComponent("nonregressiontest.component.collectiveitf.multicast.classbased.testcase",context);
        
        Fractal.getLifeCycleController(testcase).startFc();
        ((Tester)testcase.getFcInterface("runTestItf")).testConnectedServerMulticastItf();
        ((Tester)testcase.getFcInterface("runTestItf")).testOwnClientMulticastItf();
        
    }

    /*
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
    public void endTest() throws Exception {

        // TODO Auto-generated method stub
        
    }

    /*
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
    public void initTest() throws Exception {

        // TODO Auto-generated method stub
        
    }
    
    public static void main(String[] args) {
        System.setProperty("fractal.provider", "org.objectweb.proactive.core.component.Fractive");
        System.setProperty("java.security.policy", System.getProperty("user.dir")+"/proactive.java.policy");
        System.setProperty("log4j.configuration", System.getProperty("user.dir")+"/proactive-log4j");
        System.setProperty("log4j.configuration", "file:" + System.getProperty("user.dir")+"/proactive-log4j");
        System.setProperty("nonregressiontest.descriptor.defaultnodes.file", "/nonregressiontest/descriptor/defaultnodes/NodesLocal.xml");
        Test test = new Test();
        try {
            test.action();
            if (test.postConditions()) {
                System.out.println("TEST SUCCEEDED");
            } else {
                System.out.println("TEST FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
