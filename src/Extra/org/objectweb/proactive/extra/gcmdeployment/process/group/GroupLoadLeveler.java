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


public class GroupLoadLeveler extends AbstractGroup {
    private String jobName;
    private String stdout;
    private String stderr;
    private String directory;
    private String resources;
    private String maxTime;
    private List<String> argumentList;
    private String nbTasks;
    private String cpusPerTask;
    private String tasksPerHost;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public void setArgumentList(List<String> argumentList) {
        this.argumentList = argumentList;
    }

    public void setNbTasks(String nbTasks) {
        this.nbTasks = nbTasks;
    }

    public void setCpusPerTask(String cpusPerTask) {
        this.cpusPerTask = cpusPerTask;
    }

    public void setTasksPerHost(String tasksPerHost) {
        this.tasksPerHost = tasksPerHost;
    }
}
