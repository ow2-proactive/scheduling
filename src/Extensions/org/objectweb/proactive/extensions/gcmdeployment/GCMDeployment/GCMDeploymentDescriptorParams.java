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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import java.io.File;
import java.net.URL;

import org.objectweb.proactive.core.xml.VariableContractImpl;


public class GCMDeploymentDescriptorParams {

    /** The GCM Descriptor describing the resources to be used */
    private URL GCMDescriptor;

    /** The resource provider ID */
    private String id;

    private VariableContractImpl vContract;

    public VariableContractImpl getVariableContract() {
        return vContract;
    }

    public void setVContract(VariableContractImpl contract) {
        vContract = contract;
    }

    public URL getGCMDescriptor() {
        return GCMDescriptor;
    }

    public String getId() {
        return id;
    }

    public void setGCMDescriptor(URL descriptor) {
        GCMDescriptor = descriptor;
    }

    public void setId(String id) {
        if (id == null) {
            GCMD_LOGGER.warn(this.getClass().getName() + ".setId called with id==null", new Exception());
            return;
        }

        if (id.equals("")) {
            GCMD_LOGGER.warn(this.getClass().getName() + ".setId called with id==\"\"", new Exception());
            return;
        }

        this.id = id;
    }
}
