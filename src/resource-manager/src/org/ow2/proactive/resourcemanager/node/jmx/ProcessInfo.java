/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.node.jmx;

import java.util.Iterator;
import java.util.List;


public class ProcessInfo {

    private int pid;
    private String owner;
    private String startTime;
    private String memSize;
    private String memRss;
    private String memShare;
    private String state;
    private String cpuTime;
    private String description;
    private String[] commandline;
    private String cpuPerc;

    public ProcessInfo() {
    }

    public ProcessInfo(List process) {
        Iterator<Object> it = process.iterator();
        pid = Integer.parseInt(it.next().toString());
        owner = it.next().toString();
        startTime = it.next().toString();
        memSize = it.next().toString();
        memRss = it.next().toString();
        memShare = it.next().toString();
        state = it.next().toString();
        cpuTime = it.next().toString();
        description = it.next().toString();
        commandline = (String[]) it.next();
        cpuPerc = it.next().toString();
    }

    public int getPid() {
        return pid;
    }

    public String getOwner() {
        return owner;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getMemSize() {
        return memSize;
    }

    public String getMemRss() {
        return memRss;
    }

    public String getMemShare() {
        return memShare;
    }

    public String getState() {
        return state;
    }

    public String getCpuTime() {
        return cpuTime;
    }

    public String getDescription() {
        return description;
    }

    public String[] getCommandline() {
        return commandline;
    }

    public String getCpuPerc() {
        return cpuPerc;
    }

}
