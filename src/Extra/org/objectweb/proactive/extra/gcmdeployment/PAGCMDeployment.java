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
package org.objectweb.proactive.extra.gcmdeployment;

import java.io.File;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplication;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationImpl;


@PublicAPI
public class PAGCMDeployment {

    /**
     * Returns a {@link GCMApplication} to manage the GCM Application described by the GCM
     * Application Descriptor XML file
     * 
     * @param file
     *            The GCM Application Descriptor file
     * @return A GCM Application
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(File file) throws ProActiveException {
        return loadApplicationDescriptor(file, null);
    }

    /**
     * Returns a {@link GCMApplication} to manage the GCM Application described by the GCM
     * Application Descriptor XML file
     * 
     * @param file
     *            The GCM Application Descriptor file
     * @param vContract
     *            A Variable Contract between the deployment descriptor and the application program
     * @return A GCM Application
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(File file, VariableContractImpl vContract)
            throws ProActiveException {
        return new GCMApplicationImpl(file, vContract);
    }

}
