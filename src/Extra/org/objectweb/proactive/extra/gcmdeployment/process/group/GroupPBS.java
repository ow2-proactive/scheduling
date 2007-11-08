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
package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupPBS extends AbstractGroup {
    private String hostList;
    private String hostNumber;
    private String processorPerNode;
    private String wallTime;
    private String queueName;
    private String interactive;
    private String stdout;
    private String stderr;
    private String mailWhen;
    private String mailTo;
    private String joinOutput;

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

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    public void setNodes(String nodeNumber) {
        this.hostNumber = nodeNumber;
    }

    public void setProcessorPerNodeNumber(String processorPerNode) {
        this.processorPerNode = processorPerNode;
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
}
