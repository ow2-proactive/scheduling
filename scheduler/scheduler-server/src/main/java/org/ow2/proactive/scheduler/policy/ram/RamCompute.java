package org.ow2.proactive.scheduler.policy.ram;

import java.io.Serializable;
import java.lang.management.ManagementFactory;

import org.objectweb.proactive.core.runtime.LocalNode;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import com.sun.management.OperatingSystemMXBean;


public class RamCompute implements Serializable {

    private static final long GIGABYTE = (1024L * 1024L * 1024L);

    public RamCompute() {
    }

    public long getAvailableRAM() {

        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totRAMAvailable = bean.getTotalPhysicalMemorySize() / GIGABYTE;

        int totalRAMUsed = 0;
        for (LocalNode localNode : ProActiveRuntimeImpl.getProActiveRuntime().getLocalNodes()) {
            if (localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME) != null) {
                totalRAMUsed += Integer
                        .parseInt(localNode.getProperty(RamSchedulingPolicy.RAM_VARIABLE_NAME));
            }
        }

        long ramInBytes = totRAMAvailable - totalRAMUsed;

        return ramInBytes;

    }

}