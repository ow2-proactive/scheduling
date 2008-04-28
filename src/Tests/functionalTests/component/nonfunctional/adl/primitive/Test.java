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
package functionalTests.component.nonfunctional.adl.primitive;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentative;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 *
 *
 * @author The ProActive Team
 */
public class Test extends ComponentTest {
    Component root;

    public Test() {
        super("Basic creation of non-functional components", "Basic creation of non-functional components");
    }

    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getNFFactory();
        @SuppressWarnings("unchecked")
        Map context = new HashMap();

        root = (Component) f.newComponent("functionalTests.component.nonfunctional.adl.dummyPrimitive",
                context);

        Fractal.getLifeCycleController(root).startFc();

        DummyControllerItf ref = (DummyControllerItf) root.getFcInterface("dummy-membrane");
        String name = ref.dummyMethodWithResult();
        ref.dummyVoidMethod("Message");
        Assert.assertTrue(root instanceof ProActiveNFComponentRepresentative);
    }
}
