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
package functionalTests.component.interceptor;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class A implements FooItf, BindingController {
    FooItf b;

    public A() {
    }

    public void foo() {
        b.foo();
    }

    public void bindFc(String clientItfName, Object serverItf)
        throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (clientItfName.equals(FooItf.CLIENT_ITF_NAME)) {
            b = (FooItf) serverItf;
            //logger.debug("MotorImpl : added binding on a wheel");
        } else {
            throw new IllegalBindingException(
                "no such binding is possible : client interface name does not match");
        }
    }

    public String[] listFc() {
        return null;
    }

    public Object lookupFc(String clientItfName)
        throws NoSuchInterfaceException {
        if ("foo-client".equals(clientItfName)) {
            return b;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    public void unbindFc(String clientItfName)
        throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
    }
}
