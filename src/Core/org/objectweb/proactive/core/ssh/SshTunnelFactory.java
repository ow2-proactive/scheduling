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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *
 * This factory class performs tunnel caching: if a tunnel is not
 * used anymore, it is put in the unused list and whenever a client
 * requests a similar tunnel, this tunnel is reused. unused tunnels
 * are closed after a timeout determined by proactive.tunneling.gc_timeout
 * (the default value is 10000ms)
 * Caching is performed only if proactive.tunneling.use_gc is set to "yes".
 * Otherwise, tunnels are created and destroyed purely on a need-to basis.
 */
public class SshTunnelFactory {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SSH);
    private java.util.Hashtable<String, UnusedTunnel> _unused;
    static private SshTunnelFactory _factory = null;

    static private SshTunnelFactory getFactory() {
        if (_factory == null) {
            _factory = new SshTunnelFactory();
        }
        return _factory;
    }

    static public SshTunnel createTunnel(String host, int port) throws java.io.IOException {
        return getFactory().create(host, port);
    }

    static public void reportUnusedTunnel(SshTunnel tunnel) throws Exception {
        getFactory().reportUnused(tunnel);
    }

    private SshTunnelFactory() {
        _unused = new java.util.Hashtable<String, UnusedTunnel>();
        if (PAProperties.PA_SSH_TUNNELING_USE_GC.isTrue()) {
            Thread gcThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        getFactory().GC();
                    }
                }
            };
            gcThread.start();
        }
    }

    private String getKey(String host, int port) {
        return host + port;
    }

    private synchronized SshTunnel create(String host, int port) throws java.io.IOException {
        if (PAProperties.PA_SSH_TUNNELING_USE_GC.isTrue()) {
            UnusedTunnel unused = _unused.get(getKey(host, port));
            SshTunnel tunnel;
            if (unused == null) {
                logger.debug("create tunnel " + host + ":" + port);
                tunnel = new SshTunnel(host, port);
            } else {
                logger.debug("reuse tunnel " + host + ":" + port);
                _unused.remove(getKey(host, port));
                tunnel = unused.getTunnel();
            }
            return tunnel;
        } else {
            return new SshTunnel(host, port);
        }
    }

    private synchronized void reportUnused(SshTunnel tunnel) throws Exception {
        if (tunnel != null) {
            String host = tunnel.getDistantHost();
            int port = tunnel.getDistantPort();
            if (PAProperties.PA_SSH_TUNNELING_USE_GC.isTrue()) {
                UnusedTunnel prev = _unused.get(getKey(host, port));
                if (prev != null) {
                    prev.getTunnel().realClose();
                    _unused.remove(getKey(host, port));
                }
                logger.debug("return unused tunnel " + host + ":" + port);
                _unused.put(getKey(host, port), new UnusedTunnel(tunnel));
            } else {
                logger.debug("kill unused tunnel " + host + ":" + port);
                tunnel.realClose();
            }
        }
    }

    private synchronized void GC() {
        java.util.Enumeration<String> keys = _unused.keys();
        for (; keys.hasMoreElements();) {
            String key = keys.nextElement();
            UnusedTunnel tunnel = _unused.get(key);
            if (tunnel.isOldEnough()) {
                try {
                    SshTunnel sshTunnel = tunnel.getTunnel();
                    logger.debug("gc kill unused tunnel " + sshTunnel.getDistantHost() + ":" +
                        sshTunnel.getDistantPort());
                    sshTunnel.realClose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                _unused.remove(key);
            }
        }
    }
}
