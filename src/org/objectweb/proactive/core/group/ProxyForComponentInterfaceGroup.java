/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.StubObject;


public class ProxyForComponentInterfaceGroup extends ProxyForGroup {
    protected InterfaceType interfaceType;
    protected Component owner;

    public ProxyForComponentInterfaceGroup()
        throws ConstructionOfReifiedObjectFailedException {
        super();
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(ConstructorCall c, Object[] p)
        throws ConstructionOfReifiedObjectFailedException {
        super(c, p);
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(String nameOfClass)
        throws ConstructionOfReifiedObjectFailedException {
        this();
        className = Interface.class.getName();
    }

    /*
     * @see org.objectweb.proactive.core.group.Group#getGroupByType()
     */
    public Object getGroupByType() {
        try {
            Interface result = ProActiveComponentGroup.newComponentInterfaceGroup(interfaceType,
                    owner);

            ProxyForComponentInterfaceGroup proxy = (ProxyForComponentInterfaceGroup) ((StubObject) result).getProxy();
            proxy.memberList = this.memberList;
            proxy.className = this.className;
            proxy.interfaceType = this.interfaceType;
            proxy.owner = this.owner;
            proxy.proxyForGroupID = this.proxyForGroupID;
            proxy.waited = this.waited;
            return result;
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
