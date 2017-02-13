/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
        Iterator it = process.iterator();
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
