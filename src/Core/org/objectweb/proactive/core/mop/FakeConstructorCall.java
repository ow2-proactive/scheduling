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
package org.objectweb.proactive.core.mop;

import java.lang.reflect.InvocationTargetException;


/**
 * Fakes a constructor call by returning an already-existing object as if
 * it were the result of the reflection of this ConstructorCall object
 */
class FakeConstructorCall implements ConstructorCall, java.io.Serializable {
    private Object target;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     *
     */
    public FakeConstructorCall(Object target) {
        this.target = target;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements ConstructorCall -----------------------------------------------
    //

    /**
     * Make a deep copy of all arguments of the constructor
     * Do nothing as we don't want to copy the object if used locally
     */
    public void makeDeepCopyOfArguments() throws java.io.IOException {
    }

    /**
     * Return the name of the target class that constructor is for
     */
    public String getTargetClassName() {
        return target.getClass().getName();
    }

    /**
     * Performs the object construction that is reified vy this object
     * @throws InvocationTargetException
     * @throws ConstructorCallExecutionFailedException
     */
    public Object execute() throws InvocationTargetException, ConstructorCallExecutionFailedException {
        return target;
    }
}
