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
package nonregressiontest.component.migration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import testsuite.test.Assertions;


// we need this active object to perform the test, because futures updates are involved (managed by future pool)
// therefore, future pool must be serialized, which poses a problem if there is a reference on a HalfBody (from main)
// solution : we run the test from an active object (no HalfBody involved)
public class TestAO implements Serializable {
    public boolean go() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        ProActiveDescriptor deploymentDescriptor = ProActive.getProactiveDescriptor(Test.class.getResource(
                    "/nonregressiontest/component/descriptor/deploymentDescriptor.xml")
                                                                                              .getPath());
        context.put("deployment-descriptor", deploymentDescriptor);

        Component x = (Component) f.newComponent("nonregressiontest.component.migration.x",
                context);
        deploymentDescriptor.activateMappings();
        Fractal.getLifeCycleController(x).startFc();
        Fractive.getMigrationController(x)
                .migrateTo(deploymentDescriptor.getVirtualNode("VN3").getNode());
        Assertions.assertEquals("hello",
            ((E) x.getFcInterface("e")).gee(new StringWrapper("hello"))
             .stringValue());

        Component y = (Component) f.newComponent("nonregressiontest.component.migration.y",
                context);
        Fractive.getMigrationController(y)
                .migrateTo(deploymentDescriptor.getVirtualNode("VN1").getNode());
        Fractal.getLifeCycleController(y).startFc();

        Component toto = (Component) f.newComponent("nonregressiontest.component.migration.toto",
                context);
        Fractive.getMigrationController(toto)
                .migrateTo(deploymentDescriptor.getVirtualNode("VN2").getNode());
        Fractal.getLifeCycleController(toto).startFc();
        Assertions.assertEquals("toto",
            ((E) toto.getFcInterface("e01")).gee(new StringWrapper("toto"))
             .stringValue());
        //        
        Component test = (Component) f.newComponent("nonregressiontest.component.migration.test",
                context);

        Fractal.getLifeCycleController(test).startFc();
        StringWrapper result = new StringWrapper("");
        for (int i = 0; i < 2; i++) {
            result = ((A) test.getFcInterface("a")).foo(new StringWrapper(
                        "hello world !"));
        }

        Component[] subComponents = Fractal.getContentController(test)
                                           .getFcSubComponents();
        for (int i = 0; i < subComponents.length; i++) {
            NameController nc = Fractal.getNameController(subComponents[i]);
            if (nc.getFcName().equals("y")) {
                Fractive.getMigrationController(subComponents[i])
                        .migrateTo(deploymentDescriptor.getVirtualNode("VN3")
                                                       .getNode());
                break;
            }
        }

        //		}
        //	}

        //        Fractive.getMigrationController(test).migrateTo("rmi://gaudi/toto");
        //        Fractive.getMigrationController(test).migrateTo(ProActiveRuntimeImpl.getProActiveRuntime().getURL());

        // check singleton - gathercast - multicast interfaces
        for (int i = 0; i < 100; i++) {
            result = ((A) test.getFcInterface("a")).foo(new StringWrapper(
                        "hello world !"));
        }
        Assertions.assertEquals("hello world !", result.stringValue());

        // check collection interfaces
        result = ((E) test.getFcInterface("e01")).gee(new StringWrapper(
                    "hello world !"));
        Assertions.assertEquals("hello world !", result.stringValue());

        deploymentDescriptor.killall(false);

        return true;
    }
}
