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
package functionalTests.component.descriptor.arguments;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;

import functionalTests.ComponentTest;


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
    Component dummy;

    public Test() {
        super("Configuration with ADL arguments and AttributeController",
                "Configuration with ADL arguments and AttributeController");
    }

    /*
     * (non-Javadoc)
     * 
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        context.put("message", "hello world");
        dummy = (Component) f.newComponent("functionalTests.component.descriptor.arguments.dummy", context);
        Fractal.getLifeCycleController(dummy).startFc();

        Assert.assertEquals("This component is storing the info : hello world", ((Action) dummy
                .getFcInterface("action")).doSomething());
    }
}
