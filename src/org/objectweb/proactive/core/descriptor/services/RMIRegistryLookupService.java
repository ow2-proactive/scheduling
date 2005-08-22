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
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * This class represents a service to acquire with an RMI lookup a ProActiveRuntime(JVM) previously registered
 * in a RMIRegistry on a local or remote host.
 * This service can be defined and used transparently when using XML Deployment descriptor
 * @author  ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class RMIRegistryLookupService implements UniversalService {

    /** lookup url */
    protected String url;
    protected static String serviceName = "RMIRegistryLookup";
    protected ProActiveRuntime[] runtimeArray;

    public RMIRegistryLookupService(String url) {
        this.url = url;
        this.runtimeArray = new ProActiveRuntime[1];
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        ProActiveRuntime part = RuntimeFactory.getRuntime(UrlBuilder.removeProtocol(
                    url, "rmi:"), "rmi:");
        runtimeArray[0] = part;
        return runtimeArray;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }
}
