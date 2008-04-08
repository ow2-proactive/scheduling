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

import java.util.List;


public class GroupGlobus extends AbstractGroup {
    private String count;
    private String queue;
    private String maxTime;
    private String stderr;
    private String stdout;
    private String stdin;
    private String directory;
    private String hostname;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the count.
     * @return String
     */
    public String getCount() {
        return count;
    }

    /**
     * Sets the count.
     * @param count The count to set
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     * @return Returns the queue.
     */
    public String getQueue() {
        return queue;
    }

    /**
     * @param queue The queue to set.
     */
    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * @return Returns the stderr.
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderr The stderr to set.
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * @return Returns the stdout.
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @param stdout The stdout to set.
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
