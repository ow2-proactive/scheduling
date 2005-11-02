/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.representative;

import org.objectweb.proactive.core.UniqueID;


/**
 * Identifies the functional interface of a component by its name and the id of the body of the component
 * it belongs to.
 *
 * @author Matthieu Morel
 */
public class FunctionalInterfaceID {
    private String functionalInterfaceName;
    private UniqueID componentBodyID;

    public FunctionalInterfaceID(String functionalInterfaceName,
        UniqueID componentBodyID) {
        this.functionalInterfaceName = functionalInterfaceName;
        this.componentBodyID = componentBodyID;
    }

    public String getFunctionalInterfaceName() {
        return functionalInterfaceName;
    }

    public UniqueID getComponentBodyID() {
        return componentBodyID;
    }

    public int hashCode() {
        return componentBodyID.hashCode() + functionalInterfaceName.hashCode();
    }

    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof FunctionalInterfaceID) {
            return (functionalInterfaceName.equals(((FunctionalInterfaceID) o).functionalInterfaceName) &&
            (componentBodyID.equals(((FunctionalInterfaceID) o).componentBodyID)));
        } else {
            return false;
        }
    }
}
