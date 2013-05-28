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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.cmd.Ps;


public class SigarProcesses implements SigarProcessesMXBean {

    /** Log4J logger */
    private final static Logger logger = Logger.getLogger(SigarProcesses.class);
    
    @SuppressWarnings("unchecked")
    @Override
    public ProcessInfo[] getProcesses() throws SigarException {
        Sigar sigar = new Sigar();
        long[] pids = sigar.getProcList();

        List<ProcessInfo> result = new ArrayList<ProcessInfo>(pids.length);

        for (int i = 0; i < pids.length; i++) {
            long pid = pids[i];
            try {
                @SuppressWarnings("rawtypes")
                List info = Ps.getInfo(sigar, pid);             // Add standard info. 
                info.add(sigar.getProcArgs(pid));               // Add also arguments of each process. 
                info.add(sigar.getProcCpu(pid).getPercent());   // Add cpu usage (perc.).
                
                result.add(new ProcessInfo(info));
            } catch (SigarException e) {
                // Ignore it, probably the process does not exist anymore.
                logger.warn("Could not get information for PID: " + pid, e);
            }

            // TODO see why sigar.getProcCpu(pid).getPercent()
            // returns '0.0' always.

        }

        return result.toArray(new ProcessInfo[] {});
    }
}
