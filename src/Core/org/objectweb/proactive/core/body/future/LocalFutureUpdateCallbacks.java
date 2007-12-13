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
package org.objectweb.proactive.core.body.future;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * An implementation of java.util.concurrent.Future wrapping a MethodCallResult.
 * Passed as parameter to the user defined callback on future update.
 */
class ProActiveFuture implements java.util.concurrent.Future<Object> {
    private MethodCallResult result;

    public ProActiveFuture(MethodCallResult result) {
        this.result = result;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Cannot cancel an already arrived ProActive future");
    }

    public Object get() throws ExecutionException {
        try {
            return this.result.getResult();
        } catch (Throwable t) {
            throw new ExecutionException(t);
        }
    }

    public Object get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }
}

/**
 * A callback is method declared as 'void myCallback(java.util.concurrent.Future fr)'
 * It is added using addFutureCallback(myFuture, "myCallback"), and will be
 * queued when the future is updated on ProActive.getBodyOnThis()
 * Callbacks are local, so are not copied when a future is serialized.
 */
public class LocalFutureUpdateCallbacks {
    private BodyImpl body;
    private Collection<Method> methods;
    private FutureProxy future;

    LocalFutureUpdateCallbacks(FutureProxy future) {
        try {
            this.body = (BodyImpl) PAActiveObject.getBodyOnThis();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Can only be called in a body");
        }
        this.methods = new LinkedList<Method>();
        this.future = future;
    }

    void add(String methodName) {
        if (PAActiveObject.getBodyOnThis() != this.body) {
            throw new IllegalStateException("Callbacks added by different "
                + "bodies on the same future, this cannot be possible"
                + "without breaking the no-sharing property");
        }
        Object target = this.body.getReifiedObject();
        Class<?> c = target.getClass();
        Method m;
        try {
            m = c.getMethod(methodName, java.util.concurrent.Future.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot find method: " + c.getName() + "." + methodName +
                "(Future)", e);
        }
        this.methods.add(m);
    }

    void run() {
        ProActiveFuture[] args = new ProActiveFuture[] { new ProActiveFuture(this.future
                .getMethodCallResult()) };

        for (Method m : this.methods) {
            MethodCall mc = MethodCall.getMethodCall(m, args, null);

            try {
                this.body.sendRequest(mc, null, this.body);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                e.printStackTrace();
            }
        }
    }
}
