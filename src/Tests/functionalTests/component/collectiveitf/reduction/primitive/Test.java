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
package functionalTests.component.collectiveitf.reduction.primitive;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.exceptions.ReductionException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;

    public Test() {
        super("Multicast reduction for primitive components", "Multicast reduction for primitive components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        try {
            // test selection of unique value
            List<IntWrapper> l = new ArrayList<IntWrapper>();
            l.add(new IntWrapper(12));
            Object result = null;
            try {
                result = org.objectweb.proactive.core.component.type.annotations.multicast.ReduceMode.SELECT_UNIQUE_VALUE
                        .reduce(l);
            } catch (ReductionException e) {
                e.printStackTrace();
            }
            Assert.assertEquals(new IntWrapper(12), result);

            // test case: simple invocation on component with unicast - annotated interface
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();
            Component root = (Component) f.newComponent(
                    "functionalTests.component.collectiveitf.reduction.primitive.adl.Testcase", context);
            Fractal.getLifeCycleController(root).startFc();
            boolean result2 = ((RunnerItf) root.getFcInterface("runTestItf")).runTest();
            Assert.assertTrue(result2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
