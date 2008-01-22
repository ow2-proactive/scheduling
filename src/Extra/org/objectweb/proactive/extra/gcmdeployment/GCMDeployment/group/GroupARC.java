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

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group.GroupARCParser.FileTransfer;



public class GroupARC extends AbstractGroup {
    private String jobName;
    private String count;
    private List<String> args;
    private String stdout;
    private String stderr;
    private String stdin;
    private String maxTime;
    private String notify;
    private List<FileTransfer> inputFiles;
    private List<FileTransfer> outputFiles;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setArguments(List<String> args) {
        this.args = args;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public void setInputFiles(List<FileTransfer> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public void setOutputFiles(List<FileTransfer> outputFiles) {
        this.outputFiles = outputFiles;
    }
}
