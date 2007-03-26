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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.ssh;

import org.objectweb.proactive.core.util.HostsInfos;


/**
 * This class contains all the parameters used by the ssh code.
 * This code documents what default values are used for each
 * of these parameters.
 */
public class SshParameters {
    static private int _connectTimeout = -1;
    static private String _tryNormalFirst = null;

    static public boolean getTryNormalFirst() {
        if (_tryNormalFirst == null) {
            _tryNormalFirst = System.getProperty(
                    "proactive.tunneling.try_normal_first");
        }
        if ((_tryNormalFirst != null) && _tryNormalFirst.equals("yes")) {
            return true;
        } else {
            return false;
        }
    }

    static public int getConnectTimeout() {
        if (_connectTimeout == -1) {
            String timeout = System.getProperty(
                    "proactive.tunneling.connect_timeout");
            if (timeout != null) {
                _connectTimeout = Integer.parseInt(timeout);
            } else {
                _connectTimeout = 2000;
            }
        }
        return _connectTimeout;
    }

    static public boolean getUseTunnelGC() {
        String useTunnelGC = System.getProperty("proactive.tunneling.use_gc");
        if ((useTunnelGC != null) && useTunnelGC.equals("yes")) {
            return true;
        } else {
            return false;
        }
    }

    static public int getTunnelGCPeriod() {
        String gcPeriod = System.getProperty("proactive.tunneling.gc_period");
        if (gcPeriod != null) {
            return Integer.parseInt(gcPeriod);
        } else {
            // 10s
            return 10000;
        }
    }

    static public boolean getSshTunneling() {
        String tunneling = System.getProperty(
                "proactive.communication.protocol");
        if ((tunneling != null) && tunneling.equals("rmissh")) {
            return true;
        } else {
            return false;
        }
    }

    static public String getSshUsername(String hostname) {
        return HostsInfos.getUserName(hostname);
    }

    static public String getSshPort() {
        String sshPort = System.getProperty("proactive.ssh.port");
        if (sshPort == null) {
            sshPort = "22";
        }
        return sshPort;
    }

    static public String getSshKnownHostsFile() {
        String hostfile = System.getProperty("proactive.ssh.known_hosts");
        if (hostfile == null) {
            hostfile = System.getProperty("user.home") + "/.ssh/known_hosts";
        }
        return hostfile;
    }

    static public String getSshKeyDirectory() {
        String keydir = System.getProperty("proactive.ssh.key_directory");
        if (keydir == null) {
            keydir = SSHKeys.SSH_DIR;
        }
        return keydir;
    }
}
