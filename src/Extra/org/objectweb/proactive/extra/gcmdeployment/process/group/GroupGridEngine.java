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

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupGridEngine extends AbstractGroup {
    private String queue;
    private String hostNumber;
    private String wallTime;
    private String parallelEnvironment;
    private String stdout;
    private String stderr;
    private String directory;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setWallTime(String d) {
        this.wallTime = d;
    }

    /**
     * Returns the number of nodes requested when running the job
     * @return the number of nodes requested when running the job
     */
    public String getHostsNumber() {
        return this.hostNumber;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param hostNumber the number of nodes requested when running the job
     */
    public void setHostsNumber(String hostNumber) {
        this.hostNumber = hostNumber;
    }

    /**
     * Sets the parallel environment for this GridEngineSubProcess
     * @param p the parallel environment to use
     */
    public void setParallelEnvironment(String p) {
        this.parallelEnvironment = p;
    }

    /**
     * Returns the parallel environment for this GridEngineSubProcess
     * @return the parallel environment for this GridEngineSubProcess
     */
    public String getParallelEnvironment() {
        return this.parallelEnvironment;
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
}
