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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.core.StartRuntime;


public class GroupPrun extends AbstractGroup {

    private String resources = null;
    private String wallTime = null;
    private int nodes = 0;
    private int ppn = 0;
    private String stdout = null;

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        if (getCommandPath() != null) {
            command.append(getCommandPath());
        } else {
            command.append("prun");
        }
        command.append(" ");

        int hostCapacity = -1;
        if (getResources() != null) {
            command.append(getResources());
            hostCapacity = 1;
        } else {
            command.append(" -v "); // report host allocation

            if (getStdout() != null)
                command.append(" -o " + getStdout());
            command.append(" ");

            if (getWallTime() != null)
                command.append(" -t " + getWallTime());
            command.append(" ");

            // Always ask for one and only one CPU per node
            // prun is not able to manage CPUs but only nodes
            // Since exclusive access is granted we can start ppn PA nodes
            // remaining CPUs are wasted
            command.append(" -1 "); // one process per node

            command.append(" -np ");
            command.append(nodes); // number of nodes to be allocated 

            hostCapacity = ppn;
        }
        command.append(" ");

        //1st option ProActive command
        String cbCommand = commandBuilder.buildCommand(hostInfo, gcma);
        if (hostInfo.getHostCapacity() == 0) {
            // if user put his own command in deployment file, he should also define hostCapacity
            cbCommand += " -" + StartRuntime.Params.capacity.shortOpt() + " " + hostCapacity;
        }
        //    	cbCommand = Helpers.escapeCommand(cbCommand);
        command.append(cbCommand);
        command.append(" ");

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands() {
        return null;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getWallTime() {
        return wallTime;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getPpn() {
        return ppn;
    }

    public void setPpn(int ppn) {
        this.ppn = ppn;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
}
