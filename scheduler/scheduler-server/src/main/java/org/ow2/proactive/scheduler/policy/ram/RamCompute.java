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
package org.ow2.proactive.scheduler.policy.ram;

import java.io.Serializable;
import java.lang.management.ManagementFactory;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.runtime.LocalNode;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import com.sun.management.OperatingSystemMXBean;


/**
 * Utility class that returns the total RAM available in a machine minus the RAM preallocated by a task.
 * A task preallocate RAM by setting in the node property its generic information called ALLOC_RAM_GIGABYTES
 * 
 * 
 * it works only for SingleJVM deployments on a given machine
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 7.18.0
 */
@PublicAPI
public class RamCompute implements Serializable {

    private static final long GIGABYTE = (1024L * 1024L * 1024L);

    public RamCompute() {
    }

    /**
     * 
     * @return the total GIGA of RAM available in a machine minus the RAM preallocated by a task.
     */
    public double getAvailableRAMInGB() {

        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double totRAMAvailable = ((double) bean.getTotalPhysicalMemorySize()) / GIGABYTE;

        double totalRAMUsed = 0;
        for (LocalNode localNode : ProActiveRuntimeImpl.getProActiveRuntime().getLocalNodes()) {
            if (localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME) != null) {
                totalRAMUsed += Double.parseDouble(localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME));
            }
        }

        double ramInBytes = totRAMAvailable - totalRAMUsed;

        return ramInBytes;

    }

}
