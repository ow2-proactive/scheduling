/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;


/**
 * <p>
 * Event sent when a proActiveRuntime is registered in a Hashtable.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/08/06
 * @since   ProActive 0.9.4
 *
 */
public class RuntimeRegistrationEvent extends ProActiveEvent {

    /** constant for the registration */
    public static final int RUNTIME_REGISTERED = 10;
    protected String creatorID;
    protected String registeredRuntimeName;
    protected String protocol;
    protected String vmName;

    /**
     * Creates a new <code>RuntimeRegistrationEvent</code>
     * @param <code>proActiveRuntime</code> the local runtime on which the registration occurs
     * @param <code>messageType</code> the type of the event RUNTIME_REGISTERED
     * @param <code>registeredRuntimeName</code> the name of the registered ProActiveRuntime
     * @param <code>creatorID</code> The name of the creator of the registered ProActiveRuntime
     * @param <code>protocol</code> The protocol used to register the registered ProActiveRuntime when created
     */
    public RuntimeRegistrationEvent(ProActiveRuntime ProActiveRuntime,
        int messagetype, String registeredRuntimeName, String creatorID,
        String protocol, String vmName) {
        super(ProActiveRuntime, messagetype);
        this.creatorID = creatorID;
        this.registeredRuntimeName = registeredRuntimeName;
        this.protocol = protocol;
        this.vmName = vmName;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public String getRegisteredRuntimeName() {
        return registeredRuntimeName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVmName() {
        return vmName;
    }
}
