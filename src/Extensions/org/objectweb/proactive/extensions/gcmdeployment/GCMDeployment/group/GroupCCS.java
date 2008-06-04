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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderProActive;


public class GroupCCS extends AbstractGroup {

    private String runTime = null;
    private int cpus = 0;
    private String stdout = null;
    private String stderr = null;

    private PathElement scriptLocation = new PathElement("dist\\scripts\\gcmdeployment\\ccs.vbs",
        PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {

        StringBuilder command = new StringBuilder();
        command.append("cscript");
        command.append(" ");
        command.append(scriptLocation.getFullPath(hostInfo, commandBuilder));

        command.append(" ");
        command.append("/tasks:" + cpus);
        command.append(" ");

        String cbCommand = ((CommandBuilderProActive) commandBuilder).buildCommand(hostInfo, gcma);
        cbCommand = Helpers.escapeWindowsCommand(cbCommand);
        cbCommand += " -c 1 ";
        command.append(" ");
        command.append("/application:\"" + cbCommand + "\"");
        command.append(" ");

        String classpath = ((CommandBuilderProActive) commandBuilder).getClasspath(hostInfo);
        command.append(" ");
        command.append("/classpath:" + classpath);
        command.append(" ");

        if (getStdout() != null) {
            command.append("/stdout:" + getStdout());
            command.append(" ");
        }

        if (getStderr() != null) {
            command.append("/stderr:" + getStderr());
            command.append(" ");
        }

        if (getRunTime() != null) {
            command.append("/runtime:" + getRunTime());
            command.append(" ");
        }

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands() {
        return null;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
