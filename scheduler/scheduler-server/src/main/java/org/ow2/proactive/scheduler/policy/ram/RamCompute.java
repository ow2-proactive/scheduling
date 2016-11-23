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

        double totRAMAvailable = bean.getTotalPhysicalMemorySize() / GIGABYTE;

        double totalRAMUsed = 0;
        for (LocalNode localNode : ProActiveRuntimeImpl.getProActiveRuntime().getLocalNodes()) {
            if (localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME) != null) {
                totalRAMUsed += Double
                        .parseDouble(localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME));
            }
        }

        double ramInBytes = totRAMAvailable - totalRAMUsed;

        return ramInBytes;

    }

}