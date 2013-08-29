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
package org.ow2.proactive.tests.performance.jmeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log.Logger;


/**
 * Utility class used to equally divide set of available hosts
 * between multiple threads. 
 * 
 * @author ProActive team
 *
 */
public class TestHosts {

    public static class Host implements Comparable<Host> {

        private final String hostName;

        private List<Thread> threads = new ArrayList<Thread>();

        Host(String hostName) {
            this.hostName = hostName;
        }

        public int getThreadsNumber() {
            return threads.size();
        }

        @Override
        public int compareTo(Host o) {
            return Integer.valueOf(threads.size()).compareTo(Integer.valueOf(o.threads.size()));
        }

        public String getHostName() {
            return hostName;
        }

        public String toString() {
            return hostName;
        }

    }

    private String hostsList;

    private List<Host> hosts = new ArrayList<TestHosts.Host>(0);

    private final Logger logger;

    public TestHosts(Logger logger) {
        this.logger = logger;
    }

    private void initHosts(String hostsList) {
        this.hostsList = hostsList;
        this.hosts = new ArrayList<TestHosts.Host>();
        for (String host : hostsList.split(",")) {
            host = host.trim();
            logger.info("Adding host in the free hosts list: " + host);
            hosts.add(new Host(host));
        }
        if (hosts.isEmpty()) {
            throw new IllegalArgumentException("Invalid hosts list: " + hostsList);
        }
    }

    public synchronized Set<String> getAllHostsNames() {
        Set<String> result = new LinkedHashSet<String>();
        for (Host host : hosts) {
            result.add(host.getHostName());
        }
        return result;
    }

    public synchronized Host getHost() {
        if (hosts.isEmpty()) {
            throw new IllegalStateException("Hosts lists isn't initialized");
        }
        return getHost(Thread.currentThread());
    }

    private Host getHost(Thread thread) {
        for (Host host : hosts) {
            if (host.threads.contains(thread)) {
                throw new IllegalStateException("Thread " + thread + " has already got host " + host);
            }
        }
        Collections.sort(hosts);
        Host host = hosts.get(0);
        host.threads.add(thread);

        if (host.threads.size() > 1) {
            logger.warn("Warning: more than 1 thread use host " + host.toString() + " (threads: " +
                host.threads.size() + ")");
        }
        return host;
    }

    public synchronized void releaseHost(Host host) {
        releaseHost(Thread.currentThread(), host);
    }

    private void releaseHost(Thread thread, Host host) {
        if (!host.threads.contains(thread)) {
            throw new IllegalArgumentException("Thread " + thread + " doesn't use host " + host);
        }
        host.threads.remove(thread);
    }

    public boolean isDifferentHostsList(String hostsList) {
        return this.hostsList.equals(hostsList);
    }

    public synchronized void initializeHostsIfChanged(String hostsList) {
        if (this.hostsList == null || !this.hostsList.equals(hostsList)) {
            if (hostsList == null) {
                throw new IllegalArgumentException("Hosts list is null");
            }
            logger.info("Hosts list changed, creating new hostslist (" + hostsList + ")");
            initHosts(hostsList);
        }
    }

    /*
    private static void sanityTest() {
        TestHosts hosts = new TestHosts(Hierarchy.getDefaultHierarchy().getRootLogger());

        hosts.initializeHostsIfChanged("host1,host2");

        Thread thread1 = new Thread();
        Thread thread2 = new Thread();
        Thread thread3 = new Thread();

        Host host1 = hosts.getHost(thread1);
        Host host2 = hosts.getHost(thread2);
        hosts.releaseHost(thread1, host1);
        Host host3 = hosts.getHost(thread3);

        junit.framework.Assert.assertEquals(host1.getHostName(), host3.getHostName());

        junit.framework.Assert.assertEquals(1, host1.getThreadsNumber());
        junit.framework.Assert.assertEquals(1, host2.getThreadsNumber());
        Host tmpHost1 = hosts.getHost(new Thread());
        Host tmpHost2 = hosts.getHost(new Thread());
        junit.framework.Assert.assertEquals(2, tmpHost1.getThreadsNumber());
        junit.framework.Assert.assertEquals(2, tmpHost2.getThreadsNumber());

        hosts.initializeHostsIfChanged("host1,host2,host3");

        host1 = hosts.getHost(thread1);
        host2 = hosts.getHost(thread2);
        host3 = hosts.getHost(thread3);

        Thread thread4 = new Thread();
        hosts.releaseHost(thread2, host2);
        Host host4 = hosts.getHost(thread4);

        junit.framework.Assert.assertEquals(host2.getHostName(), host4.getHostName());

        hosts.releaseHost(thread4, host4);
        host2 = hosts.getHost(thread2);

        junit.framework.Assert.assertEquals(host4.getHostName(), host2.getHostName());

        System.out.println("Test passed");
    }

    public static void main(String[] args) throws Exception {
        sanityTest();
    }
     */
}
