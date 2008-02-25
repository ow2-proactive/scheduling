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


public class GroupLSF extends AbstractGroup {
    private String resources = null;
    private String wallTime;
    private int processorNumber;

    private String stdout;
    private String stderr;

    private String interactive;
    private String jobName;
    private String queueName;

    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/lsf.sh",
        PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // BSUB parameters
        command.append("echo ");
        command.append('"');
        command.append(scriptLocation.getFullPath(hostInfo, commandBuilder));
        command.append(" ");

        String cbCommand = commandBuilder.buildCommand(hostInfo, gcma);
        cbCommand = Helpers.escapeCommand(cbCommand);
        command.append(cbCommand);
        command.append(" ");

        command.append(getBookedNodesAccess());
        command.append(" ");

        command.append(hostInfo.getHostCapacity());

        command.append('"');

        command.append(" | ");

        command.append(buildBsub());

        // Script

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    private String buildBsub() {
        StringBuffer commandBuf = new StringBuffer();
        if (getCommandPath() != null) {
            commandBuf.append(getCommandPath());
        } else {
            commandBuf.append("bsub");
        }
        commandBuf.append(" ");

        if (queueName != null) {
            commandBuf.append(" -q ");
            commandBuf.append(queueName);
            commandBuf.append(" ");
        }

        if (interactive != null) {
            commandBuf.append(" -I ");
        }

        if (jobName != null) {
            commandBuf.append(" -J ");
            commandBuf.append(jobName);
            commandBuf.append(" ");
        }

        if (stdout != null) {
            commandBuf.append(" -o ");
            commandBuf.append(stdout);
            commandBuf.append(" ");
        }

        if (stderr != null) {
            commandBuf.append(" -e ");
            commandBuf.append(stderr);
            commandBuf.append(" ");
        }

        // Ressources
        if (resources != null) {
            commandBuf.append(resources);
        } else {
            if (processorNumber != 0) {
                commandBuf.append(" -n ");
                commandBuf.append(processorNumber);
                commandBuf.append(" ");
            }

            // build resources 
            if (wallTime != null) {
                commandBuf.append(" -c ");
                commandBuf.append(wallTime);
                commandBuf.append(" ");
            }
        }

        return commandBuf.toString();
    }

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setProcessorNumber(int processorNumber) {
        this.processorNumber = processorNumber;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public void setStdout(String outputFile) {
        this.stdout = outputFile;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
