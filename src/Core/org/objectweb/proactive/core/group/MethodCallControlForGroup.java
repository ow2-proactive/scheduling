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
package org.objectweb.proactive.core.group;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.MethodCallInfo;


/**
 * @author Laurent Baduel
 */
public abstract class MethodCallControlForGroup extends MethodCall {
    public MethodCallControlForGroup() {
    }

    @Override
    public Method getReifiedMethod() {
        return null;
    }

    /**
     * Returns the number of parmeters (0 for most of method call for group)
     * @return 0
     * @see org.objectweb.proactive.core.mop.MethodCall#getNumberOfParameter()
     */
    @Override
    public int getNumberOfParameter() {
        return 0;
    }

    //
    // --- PRIVATE METHODS FOR SERIALIZATION --------------------------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        this.writeTheObject(out);
    }

    @Override
    protected void writeTheObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        this.readTheObject(in);
    }

    @Override
    protected void readTheObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    // Overloaded to avoid this MethodCallControlForGroup object
    // go inside the recycling pool of MethodCall.
    @Override
    protected void finalize() {
    }

    // return null
    @Override
    public Object execute(Object targetObject)
        throws InvocationTargetException, MethodCallExecutionFailedException {
        return null;
    }

    @Override
    public MethodCallInfo getMethodCallInfo() {
        return new MethodCallInfo(MethodCallInfo.CallType.OneWay,
            MethodCallInfo.SynchronousReason.NotApplicable, null);
    }

    /**
     * ControlCall for group never are asynchronous
     * @return <code>false</code>
     * @see org.objectweb.proactive.core.mop.MethodCall#isAsynchronousWayCall()
     */
    @Override
    public boolean isAsynchronousWayCall() {
        return false;
    }

    /**
     * ControlCall for group always are oneway
     * @return <code>true</code>
     * @see org.objectweb.proactive.core.mop.MethodCall#isOneWayCall()
     */
    @Override
    public boolean isOneWayCall() {
        return true;
    }

    /**
     * This method does nothing, because control messages are not subject to be bloqued by barriers
     * @param barrierTags unsed parameter
     */
    @Override
    public void setBarrierTags(LinkedList barrierTags) {
    }

    /**
     * Control messages are never tagged
     * @return null
     */
    @Override
    public LinkedList getBarrierTags() {
        return null;
    }
}
