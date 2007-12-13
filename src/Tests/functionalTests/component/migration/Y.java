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
package functionalTests.component.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class Y implements B, BindingController, Serializable {

    /**
     *
     */
    int fooCounter = 0;
    C c = null;

    public List<StringWrapper> foo(List<StringWrapper> l) {
        System.out.println("y processing foo method " + fooCounter);
        fooCounter++;
        List<StringWrapper> result = c.bar(l);
        return result;
    }

    public List<StringWrapper> gee() {
        List<StringWrapper> l = new ArrayList<StringWrapper>();
        l.add(new StringWrapper("hello gee"));
        return l;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("c".equals(clientItfName)) {
            c = (C) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { "c" };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("c".equals(clientItfName)) {
            return c;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if ("c".equals(clientItfName)) {
            c = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }
}
