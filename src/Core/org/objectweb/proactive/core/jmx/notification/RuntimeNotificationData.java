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
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.jmx.naming.FactoryName;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class RuntimeNotificationData implements Serializable {

    /** The name of the creator of the registered ProActiveRuntime */
    private String creatorID;

    /** The url of the ProActiveRuntime */
    private String runtimeUrl;

    /** The protocol used to register the registered ProActiveRuntime when created */
    private String creationProtocol;

    /** The name of the registered ProActiveRuntime */
    private String vmName;

    /**
     * Empty constructor
     */
    public RuntimeNotificationData() {
        // No args constructor
    }

    /** Creates a new RuntimeNotificationData
     * @param creatorID The name of the creator of the registered ProActiveRuntime
     * @param runtimeUrl The url of the ProActiveRuntime
     * @param creationProtocol The protocol used to register the registered ProActiveRuntime when created
     * @param vmName The name of the registered ProActiveRuntime
     */
    public RuntimeNotificationData(String creatorID, String runtimeUrl, String creationProtocol, String vmName) {
        this.creatorID = creatorID;
        this.creationProtocol = creationProtocol;
        this.vmName = vmName;

        this.runtimeUrl = FactoryName.getCompleteUrl(runtimeUrl);
    }

    /**
     * Returns The protocol used to register the registered ProActiveRuntime when created
     * @return The protocol used to register the registered ProActiveRuntime when created
     */
    public String getCreationProtocol() {
        return this.creationProtocol;
    }

    /**
     * Returns The name of the creator of the registered ProActiveRuntime
     * @return The name of the creator of the registered ProActiveRuntime
     */
    public String getCreatorID() {
        return this.creatorID;
    }

    /**
     * Returns The name of the registered ProActiveRuntime
     * @return The name of the registered ProActiveRuntime
     */
    public String getVmName() {
        return this.vmName;
    }

    /**
     * Returns The url of the ProActiveRuntime
     * @return The url of the ProActiveRuntime
     */
    public String getRuntimeUrl() {
        return this.runtimeUrl;
    }
}
