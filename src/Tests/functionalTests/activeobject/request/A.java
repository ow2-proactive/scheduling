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
package functionalTests.activeobject.request;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


/**
 * @author rquilici
 */
public class A implements Serializable, RunActive {

    /**
         *
         */
    private static final long serialVersionUID = 7313731330586627985L;
    int counter = 0;

    // for ACs after termination test
    private A delegate;

    public A() {
    }

    public void initDeleguate() {
        try {
            this.delegate = (A) ProActiveObject.newActive(A.class.getName(),
                    new Object[0]);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    public void runActivity(Body body) {
        body.blockCommunication();
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        body.acceptCommunication();
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    public void method1() {
        counter++;
    }

    public A method2() {
        counter++;
        return new A();
    }

    public int method3() {
        counter++;
        return counter;
    }

    public StringWrapper getValue() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new StringWrapper("Returned value");
    }

    public StringWrapper getDelegateValue() {
        return this.delegate.getValue();
    }

    public void exit() throws Exception {
        ProActiveObject.terminateActiveObject(true);
    }
}
