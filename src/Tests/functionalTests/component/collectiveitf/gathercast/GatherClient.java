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
package functionalTests.component.collectiveitf.gathercast;

import org.junit.Assert;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.exceptions.GathercastTimeoutException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntMutableWrapper;


public class GatherClient implements GatherClientAttributes, TotoItf, BindingController {
    DummyItf client2primitive;
    DummyItf client2composite;
    String id;

    /*
     * @see functionalTests.component.collectiveitf.gather.TotoItf#test()
     */
    public BooleanWrapper test() {
        client2primitive.foo(new IntMutableWrapper(new Integer(id)));

        try {
            A a = new A(new Integer(id));
            B b1 = client2primitive.bar(a);
            B b2 = client2composite.bar(a);
            Assert.assertTrue(b1.getValue() == a.getValue());
            Assert.assertTrue(b2.getValue() == a.getValue());
        } catch (Throwable t) {
            System.out.println("client side");
            t.printStackTrace();
        }

        boolean timedout = false;
        try {
            B b = client2primitive.timeout();
            b.getValue(); // only case where timedout exception is thrown: need to access a result!
        } catch (GathercastTimeoutException e) {
            timedout = true;
            //            System.out.println("timeout worked fine with primitive in client " +id);
        }
        Assert.assertTrue(timedout);

        timedout = false;
        try {
            B b = client2composite.timeout();
            b.getValue(); // only case where timedout exception is thrown: need to access a result!
        } catch (GathercastTimeoutException e) {
            timedout = true;
            //            System.out.println("timeout worked fine for composite in client " +id);
        }
        Assert.assertTrue(timedout);

        // no assertion failed => return ok
        return new BooleanWrapper(true);
    }

    /*
     * @see functionalTests.component.collectiveitf.Identifiable#getID()
     */
    public String getId() {
        return id;
    }

    /*
     * @see functionalTests.component.collectiveitf.Identifiable#setID(java.lang.String)
     */
    public void setId(String id) {
        this.id = id;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if ("client2composite".equals(clientItfName)) {
            client2composite = (DummyItf) serverItf;
        } else if ("client2primitive".equals(clientItfName)) {
            client2primitive = (DummyItf) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] { "client2composite", "client2primitive" };
    }

    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("client2composite".equals(clientItfName)) {
            return client2composite;
        } else if ("client2primitive".equals(clientItfName)) {
            return client2primitive;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String arg0) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        throw new ProActiveRuntimeException("not implemented!");
    }
}
