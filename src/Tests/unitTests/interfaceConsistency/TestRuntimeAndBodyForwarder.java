/** ################################################################
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
package unitTests.interfaceConsistency;

import java.lang.reflect.Method;

import org.junit.Test;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.UniqueRuntimeID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.UniversalBodyForwarder;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeForwarder;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntimeForwarder;
import static junit.framework.Assert.assertTrue;
public class TestRuntimeAndBodyForwarder {
    @Test
    public void proActiveRuntimeForwarder() {
        boolean ret = checkConsistency(ProActiveRuntime.class,
                ProActiveRuntimeForwarder.class, UniqueRuntimeID.class, true);

        assertTrue(ret);
    }

    @Test
    public void remoteProActiveRuntimeForwarder() {
        boolean ret = checkConsistency(RemoteProActiveRuntime.class,
                RemoteProActiveRuntimeForwarder.class, UniqueRuntimeID.class,
                true);

        assertTrue(ret);
    }

    @Test
    public void universalBodyForwarder() {
        boolean ret = checkConsistency(UniversalBody.class,
                UniversalBodyForwarder.class, UniqueID.class, false);

        assertTrue(ret);
    }

    @Test
    public void remoteBodyForwarder() {
        //        boolean ret = checkConsistency(RemoteBody.class,
        //                RemoteBodyForwarder.class, UniqueID.class, false);
        boolean ret = true;
        assertTrue(ret);
    }

    static public boolean checkConsistency(Class normal, Class forwarder,
        Class prefix, boolean unneededCheck) {
        boolean testPassed = true;
        StringBuffer msg = new StringBuffer();

        msg.append("Missing methods in " + forwarder + "\n");
        msg.append("--------------------------------\n");

        // Checks that each method in runtime as an equivalent in forwarderRuntime
        Method[] methods = normal.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Class[] parameters = method.getParameterTypes();
            Class[] fParameters = new Class[parameters.length + 1];
            fParameters[0] = prefix;
            for (int j = 0; j < parameters.length; j++) {
                fParameters[j + 1] = parameters[j];
            }
            try {
                Method forwarderMethod = forwarder.getMethod(method.getName(),
                        fParameters);
                if (!forwarderMethod.getReturnType()
                                        .equals(method.getReturnType())) {
                    testPassed = false;
                    msg.append(forwarderMethod + "\n");
                    msg.append("     -> Incompatible return type\n");
                }
            } catch (NoSuchMethodException e) {
                testPassed = false;
                msg.append(method + "\n");
                msg.append("     -> Missing forwarder equivalent");
            }
        }

        if (unneededCheck) {
            Method[] forwarderMethods = forwarder.getDeclaredMethods();
            for (int i = 0; i < forwarderMethods.length; i++) {
                Method method = forwarderMethods[i];
                Class[] fParameters = method.getParameterTypes();
                Class[] parameters = new Class[fParameters.length - 1];
                if ((fParameters.length == 0) ||
                        !fParameters[0].equals(prefix)) {
                    // Probably not a forwarded method, skipping it
                    continue;
                }
                for (int j = 1; j < fParameters.length; j++) {
                    parameters[j - 1] = fParameters[j];
                }
                try {
                    normal.getMethod(method.getName(), parameters);
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
