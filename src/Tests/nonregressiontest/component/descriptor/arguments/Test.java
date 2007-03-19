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
package nonregressiontest.component.descriptor.arguments;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import nonregressiontest.component.ComponentTest;

import testsuite.test.Assertions;


/**
 * This test instantiates a component from the "dummy.fractal" definition, which is parameterized
 * with the "message" argument.
 * The "message" argument is then used to set the "info" attribute in the dummy component.
 *
 * @author Matthieu Morel
 */
public class Test extends ComponentTest {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7744558732415529004L;
	Component dummy;

    public Test() {
        super("Configuration with ADL arguments and AttributeController",
            "Configuration with ADL arguments and AttributeController");
    }

    /* (non-Javadoc)
     * @see testsuite.test.FunctionalTest#action()
     */
    @Override
	public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        context.put("message", "hello world");
        dummy = (Component) f.newComponent("nonregressiontest.component.descriptor.arguments.dummy",
                context);
        Fractal.getLifeCycleController(dummy).startFc();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    @Override
	public void initTest() throws Exception {
    }

    /* (non-Javadoc)
     * @see testsuite.test.AbstractTest#endTest()
     */
    @Override
	public void endTest() throws Exception {
    }

    @Override
	public boolean postConditions() throws Exception {
        Assertions.assertEquals("This component is storing the info : hello world",
                ((Action) dummy.getFcInterface("action")).doSomething());
        return true;
    }

    private Component getDummy() {
        return dummy;
    }
    
    
    public static void main(String[] args) throws Throwable {
    	Test t = new Test();
    	t.action();
    }
}
