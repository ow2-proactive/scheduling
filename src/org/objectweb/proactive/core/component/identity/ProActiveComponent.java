/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.core.component.identity;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.UniqueID;

import java.io.Serializable;
import java.util.List;


/**
 * This class extends Component, in order to provide access to some ProActive
 * functionalities (the parameters of the component, the request queue, the reified object)
 *
 * @author Matthieu Morel
 */
public interface ProActiveComponent extends Component, Serializable {

    /**
     * accessor to the base object : either a direct reference or a stub
     * @return a reference on the base object. If called from the meta-objects,
     * it returns a direct reference on the base object. If called from the representative,
     * it returns a stub on the base object (standard ProActive stub, same type than
     * the base object)
     */
    public Object getReferenceOnBaseObject();

    /**
     * provides a reference to the current component
     * @return a component representative on the current component
     * - If called from the representative, it returns this representative
     * - if called from the meta-object, it returns a representative on itself
     */
    public Component getRepresentativeOnThis();

    /**
     * comparison between components
     * @param object another component to compare to
     * @return true if both components are equals
     */
    public boolean equals(Object object);

    /**
     * getter for a unique identifier
     * @return a unique identifier of the component (of the active object) accross virtual machines
     */
    public UniqueID getID();
    
    
    public List getInterceptors();
}
