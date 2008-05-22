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

import org.objectweb.fractal.api.Interface;


/**
 * Extended binding, with additionnal information about a binding
 * @author The ProActive Team 
 *
 */
public class NFBinding extends Binding {
    private final String clientComponent;
    private final String serverComponent;
    private final String serverInterfaceName;

    public NFBinding(Interface clientInterface, String clientItfName, Interface serverInterface,
            String clientComponent, String serverComponent) {
        super(clientInterface, clientItfName, serverInterface);
        this.clientComponent = clientComponent;
        this.serverComponent = serverComponent;
        this.serverInterfaceName = serverInterface.getFcItfName();
    }

    public String getClientComponentName() {
        return clientComponent;
    }

    public String getServerComponentName() {
        return clientComponent;
    }

    public String getServerInterfaceName() {
        return serverInterfaceName;
    }

}
