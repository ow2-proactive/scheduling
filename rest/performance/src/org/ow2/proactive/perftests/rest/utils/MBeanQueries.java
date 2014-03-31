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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.perftests.rest.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MBeanQueries {

    // mbean
    public static final String MBEAN_MONITORING = "java.lang:type=Runtime";
    public static final String MBEAN_HOST_OVERVIEW = "java.lang:type=OperatingSystem";
    public static final String MBEAN_CPU_USAGE = "sigar:Type=CpuCoreUsage,Name=*";
    public static final String MBEAN_CPU = "sigar:Type=Cpu";
    public static final String MBEAN_MEM = "sigar:Type=Mem";
    public static final String MBEAN_SWAP = "sigar:Type=Swap";
    public static final String MBEAN_FS = "sigar:Type=FileSystem,Name=*";
    public static final String MBEAN_NETINF = "sigar:Type=NetInterface,Name=*";
    public static final String MBEAN_HEAP_MEM = "java.lang:type=Memory";
    public static final String MBEAN_THREADS = "java.lang:type=Threading";
    public static final String MBEAN_CLASSES = "java.lang:type=ClassLoading";
    public static final String MBEAN_JVM_CPU_USAGE = "java.lang:type=OperatingSystem";
    public static final String MBEAN_PROCESSES = "sigar:Type=Processes";

    private static final Map<String, String> MBEANS_RESOURCE_PATH = new HashMap<String, String>();
    private static final String MBEAN_DFLT_RESOURCE_PATH_S = "/rm/node/mbean";
    private static final String MBEAN_DFLT_RESOURCE_PATH_M = "/rm/node/mbeans";
    static {
        MBEANS_RESOURCE_PATH.put(MBEAN_HOST_OVERVIEW, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_CPU_USAGE, MBEAN_DFLT_RESOURCE_PATH_M);
        MBEANS_RESOURCE_PATH.put(MBEAN_MEM, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_NETINF, MBEAN_DFLT_RESOURCE_PATH_M);
        MBEANS_RESOURCE_PATH.put(MBEAN_FS, MBEAN_DFLT_RESOURCE_PATH_M);
        MBEANS_RESOURCE_PATH.put(MBEAN_CPU, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_SWAP, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_PROCESSES, MBEAN_DFLT_RESOURCE_PATH_S);
        
        MBEANS_RESOURCE_PATH.put(MBEAN_HEAP_MEM, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_THREADS, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_CLASSES, MBEAN_DFLT_RESOURCE_PATH_S);
        MBEANS_RESOURCE_PATH.put(MBEAN_JVM_CPU_USAGE, MBEAN_DFLT_RESOURCE_PATH_S);

    }

    // attributes
    private static final String[] ATTR_MONITORING = new String[] {
            "ManagementSpecVersion", "Name", "SpecName", "SpecVendor",
            "StartTime", "Uptime", "VmName", "VmVendor", "VmVersion",
            "BootClassPath", "ClassPath", "LibraryPath" };
    private static final String[] ATTR_HOST_OVERVIEW = new String[] { "Name",
            "Arch", "Version" };
    private static final String[] ATTR_CPU_USAGE = new String[] { "Combined" };
    private static final String[] ATTR_CPU = new String[] { "CacheSize",
            "CoresPerSocket", "Idle", "Irq", "Mhz", "Model", "Nice", "SoftIrq",
            "Sys", "Total", "TotalCores", "TotalSockets", "User", "Vendor",
            "Wait" };
    private static final String[] ATTR_MEM = new String[] { "ActualUsed",
            "ActualFree", "Total" };
    private static final String[] ATTR_SWAP = new String[] { "Used", "Free",
            "Total" };
    private static final String[] ATTR_FS = new String[] { "DevName",
            "DirName", "Files", "Options", "SysTypeName", "Free", "Used",
            "Total" };
    private static final String[] ATTR_FS_TOT = new String[] { "Total" };
    private static final String[] ATTR_NETWORK = new String[] {
            "DefaultGateway", "DomainName", "FQDN", "HostName", "PrimaryDns",
            "SecondaryDns" };
    private static final String[] ATTR_NETINF = new String[] { "RxBytes" };
    private static final String[] ATTR_HEAP_MEM = new String[] { "HeapMemoryUsage" };
    private static final String[] ATTR_THREADS = new String[] { "ThreadCount" };
    private static final String[] ATTR_CLASSES = new String[] { "LoadedClassCount" };
    private static final String[] ATTR_JVM_CPU_USAGE = new String[] { "ProcessCpuTime" };
    private static final String[] ATTR_PROCESSES = new String[] { "Processes" };

    // query
    public static final String QUERY_MONITORING = buildQuery(MBEAN_MONITORING,
            ATTR_MONITORING);
    public static final String QUERY_HOST_OVERVIEW = buildQuery(
            MBEAN_HOST_OVERVIEW, ATTR_HOST_OVERVIEW);
    public static final String QUERY_CPU_USAGE = buildQuery(MBEAN_CPU_USAGE,
            ATTR_CPU_USAGE);
    public static final String QUERY_CPU = buildQuery(MBEAN_CPU, ATTR_CPU);
    public static final String QUERY_MEM = buildQuery(MBEAN_MEM, ATTR_MEM);
    public static final String QUERY_SWAP = buildQuery(MBEAN_SWAP, ATTR_SWAP);
    public static final String QUERY_FS = buildQuery(MBEAN_FS, ATTR_FS);
    public static final String QUERY_NETWORK_INF = buildQuery(MBEAN_NETINF,
            ATTR_NETINF);
    public static final String QUERY_NETWORK = buildQuery(MBEAN_NETINF,
            ATTR_NETWORK);
    public static final String QUERY_HEAP_MEM = buildQuery(MBEAN_HEAP_MEM,
            ATTR_HEAP_MEM);
    public static final String QUERY_THREADS = buildQuery(MBEAN_THREADS,
            ATTR_THREADS);
    public static final String QUERY_CLASSES = buildQuery(MBEAN_CLASSES,
            ATTR_CLASSES);
    public static final String QUERY_JVM_CPU_USAGE = buildQuery(
            MBEAN_JVM_CPU_USAGE, ATTR_JVM_CPU_USAGE);
    public static final String QUERY_PROCESSES = buildQuery(MBEAN_PROCESSES,
            ATTR_PROCESSES);
    public static final String QUERY_FS_TOT = buildQuery(MBEAN_FS, ATTR_FS_TOT);

    public static String getResourcePath(String mbean) {
        return MBEANS_RESOURCE_PATH.get(mbean);
    }
    public static String getQueryUrl(String serverUrl, String resourcePath,
            String nodeJmxUrl, String attrQuery) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(serverUrl).append(resourcePath).append('?');
        try {
            buffer.append(encode("nodejmxurl")).append('=')
                    .append(encode(nodeJmxUrl));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        buffer.append(attrQuery);
        return buffer.toString();
    }

    private static String buildQuery(String mbean, String[] attributes) {
        StringBuilder buffer = new StringBuilder();
        try {
            buffer.append('&').append(encode("objectname")).append('=')
                    .append(encode(mbean));
            int index = 0;
            if (attributes != null) {
                while (index < attributes.length) {
                    buffer.append('&').append(encode("attr")).append('=')
                            .append(encode(attributes[index++]));
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return buffer.toString();
    }

    private static String encode(String unescaped)
            throws UnsupportedEncodingException {
        return HttpUtility.encode(unescaped);
    }

    private MBeanQueries() {
    }

}
