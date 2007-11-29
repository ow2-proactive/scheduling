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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * References on an active object are indirect link to the active object. There
 * is some interposition objects between the caller and the targetted active
 * object like a StubObject and a Proxy object. It is possible to know if an
 * object is a reference onto an active object by checking if the object
 * implements StubObject. A reference can be either on a local (on the same
 * runtime) or on a distant (on a remote runtime) active object. if an object is
 * a reference onto an active object, it implements StubObject but also the
 * class of the active object allowing to perform method call as if the method
 * call was made on the active object
 */
@PublicAPI
public interface StubObject {

    /**
     * set the proxy to the active object
     */
    public void setProxy(Proxy p);

    /**
     * return the proxy to the active object
     */
    public Proxy getProxy();
}
