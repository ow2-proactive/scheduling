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
package nonregressiontest.component.activity;

import nonregressiontest.component.ComponentTest;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;


/**
 * @author Matthieu Morel
 *
 * creates a new component
 */
public class Test extends ComponentTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4191411789650566643L;

	public Test() {
        super("Encapsulation of functional activity within component activity",
            "Encapsulation of functional activity within component activity");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        Component comp = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {  }),
                new ControllerDescription("component", Constants.PRIMITIVE),
                new ContentDescription(A.class.getName(), new Object[] {  }));

        Fractal.getLifeCycleController(comp).startFc();
        Fractal.getLifeCycleController(comp).stopFc();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        String expectedResult = A.INIT_COMPONENT_ACTIVITY +
            A.RUN_COMPONENT_ACTIVITY + A.INIT_FUNCTIONAL_ACTIVITY +
            A.RUN_FUNCTIONAL_ACTIVITY + A.END_FUNCTIONAL_ACTIVITY +
            A.END_COMPONENT_ACTIVITY;
        A.getLock().waitForRelease(); // wait until component activity is finished
        return expectedResult.equals(A.message);
    }
    
    public static void main(String[] args) {
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
