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
import org.objectweb.fractal.api.Type;


/**
 * Abstract implementation of the Interface interface of the Fractal api
 * <p>
 * As functional interfaces are specified for each component, they are generated
 * at instantiation time (bytecode generation), by subclassing this class.
 *
 * @author Matthieu Morel
 */
public abstract class ProActiveInterfaceImpl implements java.io.Serializable, ProActiveInterface {
    private Component owner;
    private String name;
    private Type type;
    private boolean isInternal;

    public ProActiveInterfaceImpl() {
    }

    /*
     *
     * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
     */
    public Component getFcItfOwner() {
        return owner;
    }

    /*
     *
     * @see org.objectweb.fractal.api.Interface#getFcItfName()
     */
    public String getFcItfName() {
        return name;
    }

    /*
     *
     * @see org.objectweb.fractal.api.Interface#getFcItfType()
     */
    public Type getFcItfType() {
        return type;
    }

    /*
     *
     * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
     */
    public boolean isFcInternalItf() {
        return isInternal;
    }

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#setFcIsInternal(boolean)
     */
    public void setFcIsInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#setFcItfName(java.lang.String)
     */
    public void setFcItfName(String name) {
        this.name = name;
    }

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#setFcItfOwner(org.objectweb.fractal.api.Component)
     */
    public void setFcItfOwner(Component owner) {
        this.owner = owner;
    }

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#setFcType(org.objectweb.fractal.api.Type)
     */
    public void setFcType(Type type) {
        this.type = type;
    }

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#getFcItfImpl()
     */
    public abstract Object getFcItfImpl();

    /*
     *
     * @see org.objectweb.proactive.core.component.ProActiveInterface#setFcItfImpl(java.lang.Object)
     */
    public abstract void setFcItfImpl(final Object impl);

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + //            "componentIdentity : " + getFcItfOwner() + "\n" + "type : " +
            getFcItfType() + "\n" + "isInternal : " + isFcInternalItf() + "\n";
        return string;
    }
}
