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


public class GroupPBS extends AbstractGroup {
    private String resources = null;
    private String wallTime;
    private int nodes;
    private int ppn;

    private String jobName;

    private String queueName;
    private String interactive;
    private String stdout;
    private String stderr;
    private String mailWhen;
    private String mailTo;
    private String joinOutput;
    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/pbs.sh",
        PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // ProActive script and parameters are read from STDIN
        // echo "oar2.sh paCommand bookedNodeAcces hostcapacity ppn" | qsub ...
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
        command.append(" ");

        command.append(ppn);

        command.append('"');

        command.append(" | ");

        command.append(buildQsub());

        // Script

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands() {
        return null;
    }

    private String buildQsub() {
        StringBuffer commandBuf = new StringBuffer();
        if (getCommandPath() != null) {
            commandBuf.append(getCommandPath());
        } else {
            commandBuf.append("qsub");
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
            commandBuf.append(" -N ");
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

        if (mailWhen != null) {
            commandBuf.append(" -m ");
            commandBuf.append(mailWhen);
            commandBuf.append(" ");
        }

        if (mailTo != null) {
            commandBuf.append(" -M ");
            commandBuf.append(mailTo);
            commandBuf.append(" ");
        }

        if (joinOutput != null) {
            commandBuf.append(" -j ");
            commandBuf.append(joinOutput);
            commandBuf.append(" ");
        }

        // Ressources
        commandBuf.append(" -l ");
        if (resources != null) {
            commandBuf.append(resources);
        } else {
            // build resources 
            if (wallTime != null) {
                commandBuf.append("walltime=");
                commandBuf.append(wallTime);
                commandBuf.append(",");
            }
            if (nodes != 0) {
                commandBuf.append("nodes=");
                commandBuf.append(nodes);
                if (ppn != 0) {
                    commandBuf.append(":ppn=");
                    commandBuf.append(ppn);
                }
                commandBuf.append(",");
            }
            // remove the extra ','
            commandBuf.setCharAt(commandBuf.length() - 1, ' ');
        }

        // argument - must be last append
        commandBuf.append(" ");
        return commandBuf.toString();
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public void setPPN(int processorPerNode) {
        this.ppn = processorPerNode;
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

    public void setMailWhen(String mailWhen) {
        this.mailWhen = mailWhen;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public void setJoinOutput(String nodeValue) {
        if ((nodeValue != null) && nodeValue.equals("true")) {
            this.joinOutput = "oe";
        } else {
            this.joinOutput = null;
        }
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
