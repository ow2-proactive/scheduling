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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;


public interface GCMDeploymentDescriptor {

    /** A magic number to indicate that the maximum capacity of
    * this GCM Deployment Descriptor is not known.
    */
    public static final long UNKNOWN_CAPACITY = -1;

    /**
     * Start the deployment
     *
     * The first step is to perform all required file transfers. Then
     * Use the CommandBuilder to build the command to be launched.
     */
    public void start(CommandBuilder commandBuilder);

    /**
     * Returns the maximum capacity this GCM Deployment Descriptor can provide
     *
     * @return the maximum capacity as declared in the descriptor file. If the
     * max capacity cannot be computed UNKNOWN_CAPACITY is returned.
     */
    public long getMaxCapacity();

    public String getDescriptorFilePath();
}
