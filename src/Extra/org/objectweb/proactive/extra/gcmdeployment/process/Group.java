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
package org.objectweb.proactive.extra.gcmdeployment.process;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;


public interface Group extends Serializable {
    public String getId();

    /**
     * Set environment variables for this cluster
     * @param env environment variables
     */
    public void setEnvironment(Map<String, String> env);

    /**
     * Set the command path to override the default one
     * @param commandPath path to the command
     */
    public void setCommandPath(String commandPath);

    /**
     * Set the HostInfo
     *
     * @param hostInfo
     */
    public void setHostInfo(HostInfo hostInfo);

    /**
     * Get the HostInfo
     *
     * @return if set the HostInfo is returned. null is returned otherwise
     */
    public HostInfo getHostInfo();

    /**
     * Check that this bridge is in a consistent state and is ready to be
     * used.
     *
     * @throws IllegalStateException thrown if anything is wrong
     */
    public void check() throws IllegalStateException;

    /**
     * Build the command to start the group
     *
     * @param commandBuilder The final command builder
     * @return The command to be used to start this group
     */
    public List<String> buildCommands(CommandBuilder commandBuilder,
        GCMApplicationDescriptor gcma);
}
