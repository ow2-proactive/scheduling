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
package org.objectweb.proactive.core.group;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides multithreading for the (a)synchronous methodcall on a group.
 *
 * @author The ProActive Team
 */
public class ProcessForAsyncCall extends AbstractProcessForGroup implements Runnable {
    //    private int index;
    private MethodCall mc;
    private Body body;
    CountDownLatch doneSignal;
    DispatchMonitor dispatchMonitor;

    public ProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList, Vector memberListOfResultGroup,
            int groupIndex, MethodCall mc, int resultIndex, Body body, CountDownLatch doneSignal) {
        this.proxyGroup = proxyGroup;
        this.memberList = memberList;
        this.memberListOfResultGroup = memberListOfResultGroup;
        this.groupIndex = groupIndex;
        this.resultIndex = resultIndex;
        this.mc = mc;
        this.body = body;
        this.doneSignal = doneSignal;
    }

    public void setDispatchMonitor(DispatchMonitor dispatchMonitor) {
        this.dispatchMonitor = dispatchMonitor;
    }

    public void run() {
        Object object = this.memberList.get(this.groupIndex);
        // push an initial context for this thread
        LocalBodyStore.getInstance().pushContext(new Context(body, null));

        /* only do the communication (reify) if the object is not an error nor an exception */
        if (!(object instanceof Throwable)) {
            try {
                executeMC(this.mc, object);
            } catch (Throwable e) {
                /*
                 * when an exception occurs, put it in the result group instead of the (unreturned)
                 * value
                 */
                ProxyForGroup.addToListOfResult(this.memberListOfResultGroup, new ExceptionInGroup(
                    this.memberList.get(this.groupIndex), this.resultIndex, e.fillInStackTrace()),
                        this.resultIndex, dispatchMonitor, groupIndex);
            }
        } else {
            /*
             * when there is a Throwable instead of an Object, a method call is impossible, add null
             * to the result group
             */
            // FIXME weird semantics : should add an exception instead
            ProxyForGroup.addToListOfResult(this.memberListOfResultGroup, null, this.resultIndex,
                    dispatchMonitor, groupIndex);
        }

        doneSignal.countDown();
    }

    public void executeMC(MethodCall mc, Object object) throws Throwable {

        boolean objectIsLocal = false;

        Proxy lastProxy = AbstractProcessForGroup.findLastProxy(object);
        if (lastProxy instanceof UniversalBodyProxy) {
            objectIsLocal = ((UniversalBodyProxy) lastProxy).isLocal();
        }

        if (lastProxy == null) {
            // means we are dealing with a standard Java Object 
            this.addToListOfResult(mc.execute(object));
        } else if (!objectIsLocal) {
            /* add the return value into the result group */
            this.addToListOfResult(((StubObject) object).getProxy().reify(mc));
        } else {
            /* add the return value into the result group */
            this.addToListOfResult(((StubObject) object).getProxy().reify(mc.getShallowCopy()));
        }
    }

    protected void addToListOfResult(Object result) {
        ProxyForGroup.addToListOfResult(memberListOfResultGroup, result, this.resultIndex, dispatchMonitor,
                groupIndex);
    }

}
