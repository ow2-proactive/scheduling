//@snippet-start state_full
package org.objectweb.proactive.examples.userguide.cmagent.simple;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;


public class State implements Serializable {
    private long commitedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
    private long initMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
    private long maxMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
    private long usedMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    private String osArch = ManagementFactory.getOperatingSystemMXBean().getArch();
    private String osName = ManagementFactory.getOperatingSystemMXBean().getName();
    private String osVersion = ManagementFactory.getOperatingSystemMXBean().getVersion();
    private int osProcs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    private int liveThreads = ManagementFactory.getThreadMXBean().getThreadCount();
    private long startedThreads = ManagementFactory.getThreadMXBean().getTotalStartedThreadCount();
    private int peakThreads = ManagementFactory.getThreadMXBean().getPeakThreadCount();
    private int deamonThreads = ManagementFactory.getThreadMXBean().getDaemonThreadCount();
    private Date timePoint = new Date();
    private String hostname;
    {
        try {
            hostname = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public State() {
    }

    public String toString() {

        return new String("======= [" + "State at " + timePoint + " on " + hostname + "] =======" +
            "\nCommited memory: " + commitedMemory + " bytes\nInitial memory requested: " + initMemory +
            " bytes\nMaximum memory available: " + maxMemory + " bytes\nUsed memory: " + usedMemory +
            " bytes\nOperating System: " + osName + " " + osVersion + " " + osArch + "\nProcessors: " +
            osProcs + "\nCurrent live threads: " + liveThreads + "\nTotal started threads: " +
            startedThreads + "\nPeak number of live threads: " + peakThreads + "\nCurrent daemon threads: " +
            deamonThreads +
            "\n===============================================================================\n");

    }
}
//@snippet-end state_full