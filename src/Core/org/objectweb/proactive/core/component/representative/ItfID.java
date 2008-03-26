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
package org.objectweb.proactive.core.component.representative;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * Identifies the functional interface of a component by its name and the id of the body of the component
 * it belongs to.
 *
 * @author The ProActive Team
 */
public class ItfID implements Serializable {
    private String itfName;
    private UniqueID componentBodyID;
    boolean isClientItf = false;

    public ItfID(String itfName, UniqueID componentBodyID) {
        this.itfName = itfName;
        this.componentBodyID = componentBodyID;
    }

    public String getItfName() {
        return itfName;
    }

    public UniqueID getComponentBodyID() {
        return componentBodyID;
    }

    @Override
    public int hashCode() {
        return componentBodyID.hashCode() + itfName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof ItfID) {
            return (itfName.equals(((ItfID) o).itfName) && (componentBodyID
                    .equals(((ItfID) o).componentBodyID)));
        } else {
            return false;
        }
    }
}
