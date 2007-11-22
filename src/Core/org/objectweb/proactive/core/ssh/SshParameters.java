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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.ssh;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.HostsInfos;


/**
 * This class contains all the parameters used by the ssh code.
 * This code documents what default values are used for each
 * of these parameters.
 */
public class SshParameters {
    static private int _connectTimeout = -1;

    static public int getConnectTimeout() {
        if (_connectTimeout == -1) {
            String timeout = PAProperties.PA_SSH_TUNNELING_CONNECT_TIMEOUT.getValue();
            if (timeout != null) {
                _connectTimeout = Integer.parseInt(timeout);
            } else {
                _connectTimeout = 2000;
            }
        }
        return _connectTimeout;
    }

    static public int getTunnelGCPeriod() {
        String gcPeriod = PAProperties.PA_SSH_TUNNELING_GC_PERIOD.getValue();
        if (gcPeriod != null) {
            return Integer.parseInt(gcPeriod);
        } else {
            // 10s
            return 10000;
        }
    }

    static public boolean getSshTunneling() {
        String tunneling = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        if ((tunneling != null) &&
                tunneling.equals(Constants.RMISSH_PROTOCOL_IDENTIFIER)) {
            return true;
        } else {
            return false;
        }
    }

    static public String getSshUsername(String hostname) {
        return HostsInfos.getUserName(hostname);
    }

    static public String getSshPort() {
        String sshPort = PAProperties.PA_SSH_PORT.getValue();
        if (sshPort == null) {
            sshPort = "22";
        }
        return sshPort;
    }

    static public String getSshKnownHostsFile() {
        if (PAProperties.PA_SSH_KNOWN_HOST.getValue() != null) {
            return PAProperties.PA_SSH_TUNNELING_KNOW_HOSTS.getValue();
        }
        return System.getProperty("user.home") +
        PAProperties.PA_SSH_TUNNELING_KNOW_HOSTS.getValue();
    }

    static public String getSshKeyDirectory() {
        String keydir = PAProperties.PA_SSH_KEY_DIR.getValue();
        if (keydir == null) {
            keydir = SSHKeys.SSH_DIR;
        }
        return keydir;
    }
}
