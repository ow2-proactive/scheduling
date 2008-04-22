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
package org.objectweb.proactive.core.remoteobject.adapter;

import java.io.Serializable;

import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * @author The ProActive Team
 * Remote Object Adapter is a mechanism that allow to insert an interception object.
 * Thus it is possible to insert personalized mechanisms within the remote objects like a cache mechanism
 * @param <T>
 */
public abstract class Adapter<T> implements Serializable, StubObject {

    /**
     * the generated stub
     */
    protected T target;

    public Adapter() {
    }

    /**
     * @param target the generated stub
     */
    public Adapter(T target) {
        this.target = target;
        construct();
    }

    /**
     * a method that allows to change the default target of the adapter.
     * Setting a new adapter could invalid some of the treatment done when this adapter has been constructed,
     * that why construct() is called once again.
     * @param target the new target of this adapter
     */
    public void setAdapter(T target) {
        this.target = target;
        construct();
    }

    /**
     * @return return the current target of this adapter
     */
    public T getAdapter() {
        return target;
    }

    /**
     * a method called during the constructor call.
     * If some treatment has to be done during the constructor call, Adapters have to
     * override this method
     */
    protected abstract void construct();

    /**
     * set the proxy to the active object
     */
    public void setProxy(Proxy p) {
        ((StubObject) target).setProxy(p);
    }

    /**
     * return the proxy to the active object
     */
    public Proxy getProxy() {
        return ((StubObject) target).getProxy();
    }
}
