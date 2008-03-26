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
package org.objectweb.proactive.core.component.controller;

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.ComponentRequest;


/**
 * A controller for managing gathercast interfaces, notably bindings and invocations on gathercast interfaces
 *
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface GathercastController extends CollectiveInterfaceController {

    /**
     * Notifies this component that a binding has been performed to one of its gathercast interfaces
     * @param serverItfName the name of the gathercast interface
     * @param sender a reference on the component connecting to the gathercast interface
     * @param clientItfName the name of the interface connecting to the gathercast interface
     */
    public void addedBindingOnServerItf(String serverItfName, ProActiveComponent sender, String clientItfName);

    /**
     * Notifies this component that a binding has been removed from one of its gathercast interfaces
     * @param serverItfName the name of the gathercast interface
     * @param owner a reference on the component connected to the gathercast interface
     * @param clientItfName the name of the interface connected to the gathercast interface
     */
    public void removedBindingOnServerItf(String serverItfName, ProActiveComponent owner, String clientItfName);

    /**
     * Returns a list of references to the interfaces connected to a given gathercast interface of this component
     * @param serverItfName name of a gathercast interface
     * @return the list of interfaces connected to this gathercast interface
     */
    public List<ItfID> getConnectedClientItfs(String serverItfName);

    /**
     * Delegates a request to be processed by the gathercast controller
     * @param r a request on a gathercast interface
     * @return the result of the gathercast invocation
     * @throws ServeException if the request handling failed
     */
    public Object handleRequestOnGatherItf(ComponentRequest r) throws ServeException;
}
