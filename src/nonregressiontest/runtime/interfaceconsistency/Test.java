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
package nonregressiontest.runtime.interfaceconsistency;

import java.lang.reflect.Method;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.UniqueRuntimeID;
import org.objectweb.proactive.core.body.RemoteBody;
import org.objectweb.proactive.core.body.RemoteBodyForwarder;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.UniversalBodyForwarder;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarder;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntimeForwarder;

import testsuite.test.FunctionalTest;


/**

 * @author Clement MATHIEU
 */
public class Test extends FunctionalTest {
    private boolean testPassed = true;

    public Test() {
        super("runtime interface consistency",
            "Check interface consistency between ProActiveRuntime and RemoteProActiveRuntime");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
    	testPassed = true;
    	
        testPassed &= checkConsistency(ProActiveRuntime.class,
            RemoteProActiveRuntime.class, true);
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
        return testPassed;
    }

    static public boolean checkConsistency(Class normal, Class remote,
         boolean unneededCheck) {
       
    	boolean testPassed = true;
        StringBuffer msg = new StringBuffer();

        msg.append("Missing methods in " + remote + "\n");
        msg.append("--------------------------------\n");

        // Checks that each method in runtime as an equivalent in forwarderRuntime
        Method[] methods = normal.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class[] parameters = method.getParameterTypes();
            try {
                Method remoteMethod = remote.getMethod(method.getName(),
                        parameters);
                if (!remoteMethod.getReturnType()
                                        .equals(method.getReturnType())) {
                    testPassed = false;
                    msg.append(remoteMethod + "\n");
                    msg.append("     -> Incompatible return type\n");
                }
            } catch (NoSuchMethodException e) {
                if (!(method.getName().equals("getJobID") && parameters.length == 0)) {
                	testPassed = false;
                	msg.append(method + "\n");
                	msg.append("     -> Missing in remote class");
                }
            }
        }

        if (unneededCheck) {
            Method[] remoteMethods = remote.getDeclaredMethods();
            for (int i = 0; i < remoteMethods.length; i++) {
                Method method = remoteMethods[i];
                Class[] rParameters = method.getParameterTypes();
                              try {
                    normal.getMethod(method.getName(), rParameters);
                } catch (NoSuchMethodException e) {
                    msg.append(method + "\n");
                    msg.append("     -> Probably unneeded");
                }
            }
        }

        if (!testPassed) {
            System.err.println(msg);
        }

        return testPassed;
    }
}
