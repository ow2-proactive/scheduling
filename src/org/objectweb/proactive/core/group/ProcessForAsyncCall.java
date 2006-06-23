/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.group;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.representative.ProActiveComponentRepresentative;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides multithreading for the (a)synchronous methodcall on a group.
 *
 * @author Laurent Baduel
 */
public class ProcessForAsyncCall extends AbstractProcessForGroup
    implements Runnable {
    private Vector memberListOfResultGroup;
    private int index;
    private MethodCall mc;
    private Body body;

    public ProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList,
        Vector memberListOfResultGroup, int index, MethodCall mc, Body body) {
        this.proxyGroup = proxyGroup;
        this.memberList = memberList;
        this.memberListOfResultGroup = memberListOfResultGroup;
        this.index = index;
        this.mc = mc;
        this.body = body;
    }

    public void run() {
        Object object = this.memberList.get(this.index);
        LocalBodyStore.getInstance().setCurrentThreadBody(body);
        boolean objectIsLocal = false;

        /* only do the communication (reify) if the object is not an error nor an exception */
        if (!(object instanceof Throwable)) {
            try {
                if (object instanceof ProActiveComponentRepresentative) {
                    // delegate to the corresponding interface
                    Object target;
                    if (mc.getComponentInterfaceName() == null) {
                        // a call on the Component interface
                        target = object;
                    } else {
                        target = ((ProActiveComponentRepresentative) object).getFcInterface(mc.getComponentInterfaceName());
                    }
                    this.proxyGroup.addToListOfResult(memberListOfResultGroup,
                        this.mc.execute(target), this.index);
                } else if (object instanceof ProActiveInterface) {
                    this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,
                        this.mc.execute(object), this.index);
                } else {
                    Proxy lastProxy = AbstractProcessForGroup.findLastProxy(object);
                    if (lastProxy instanceof UniversalBodyProxy) {
                        objectIsLocal = ((UniversalBodyProxy) lastProxy).isLocal();
                    }
                    if (lastProxy == null) {
                        // means we are dealing with a standard Java Object 
                        this.proxyGroup.addToListOfResult(memberListOfResultGroup,
                            this.mc.execute(object), this.index);
                    } else if (!objectIsLocal) {

                        /* add the return value into the result group */
                        this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,
                            ((StubObject) object).getProxy().reify(this.mc),
                            this.index);
                    } else {

                        /* add the return value into the result group */
                        this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,
                            ((StubObject) object).getProxy().reify(new MethodCall(
                                    this.mc)), this.index);
                    }
                }
            } catch (Throwable e) {

                /* when an exception occurs, put it in the result group instead of the (unreturned) value */
                this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,
                    new ExceptionInGroup(this.memberList.get(this.index),
                        this.index, e), this.index);
            }
        } else {

            /* when there is a Throwable instead of an Object, a method call is impossible, add null to the result group */
            this.proxyGroup.addToListOfResult(this.memberListOfResultGroup,
                null, this.index);
        }
    }
}
