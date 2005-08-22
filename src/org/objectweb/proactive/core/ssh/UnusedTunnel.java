package org.objectweb.proactive.core.ssh;

import org.objectweb.proactive.core.ssh.SshParameters;
import org.objectweb.proactive.core.ssh.SshTunnel;


/**
 * @author mlacage
 * A trivial placeholder for SshTunnels and their "unused" time.
 * Used by the SshTunnel GC.
 */
public class UnusedTunnel {
    private SshTunnel _tunnel;
    private long _time;

    public UnusedTunnel(SshTunnel tunnel) {
        _tunnel = tunnel;
        _time = System.currentTimeMillis();
    }

    public SshTunnel getTunnel() {
        return _tunnel;
    }

    public boolean isOldEnough() {
        long current = System.currentTimeMillis();
        if ((current - _time) >= SshParameters.getTunnelGCPeriod()) {
            return true;
        } else {
            return false;
        }
    }
}
