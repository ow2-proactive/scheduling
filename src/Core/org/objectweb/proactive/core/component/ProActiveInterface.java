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
package org.objectweb.proactive.core.component;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.Type;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * Abstract implementation of the {@link Interface} interface of the Fractal api.
 * As functional interfaces are specified for each component, they are generated at
 * instantiation time (bytecode generation), by subclassing this class.
 *
 * @author Matthieu Morel
 *
 */
public interface ProActiveInterface extends Interface, StubObject {

    /**
     * Sets the isInternal.
     * @param isInternal The isInternal to set
     */
    public abstract void setFcIsInternal(boolean isInternal);

    /**
     * Sets the name.
     * @param name The name to set
     */
    public abstract void setFcItfName(String name);

    /**
     * Sets the owner.
     * @param owner The owner to set
     */
    public abstract void setFcItfOwner(Component owner);

    /**
     * Sets the type.
     * @param type The type to set
     */
    public abstract void setFcType(Type type);

    /**
     * getter
     * @return the delegatee
     */
    public abstract Object getFcItfImpl();

    /**
     * Sets the object to which this interface reference object should delegate
     * method calls.
     *
     * @param impl the object to which this interface reference object should
     *      delegate method calls.
     * @see #getFcItfImpl getFcItfImpl
     */
    public abstract void setFcItfImpl(final Object impl);

    //    this method comes from the merge with the java5 branch but does not seem useful,
    //    uncommenting this method will break some components tests, not sure what to do with
    //    public abstract boolean isFcCollective();
}
