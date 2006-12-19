/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class JSchSingleton {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SSH);

    private JSchSingleton() {
    }

    static private Semaphore _sem = null;

    static private Semaphore getSem() {
        if (_sem == null) {
            _sem = new Semaphore(1);
        }
        return _sem;
    }

    /**
     * Everytime you want to access one of the methods exported
     * by the JSch library, you need to acquire this global lock.
     */
    static public void acquireLock() {
        Semaphore sem = getSem();
        sem.down();
    }

    static public void releaseLock() {
        Semaphore sem = getSem();
        sem.up();
    }

    static private JSch getJSch() {
        if (_jsch == null) {
            Hashtable<String, String> config = new Hashtable<String, String>();
            config.put("StrictHostKeyChecking", "no");
            JSch.setConfig(config);
            _jsch = new JSch();
            try {
                String hostfile = SshParameters.getSshKnownHostsFile();
                InputStream is = new FileInputStream(hostfile);
                _jsch.setKnownHosts(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                String keydir = SshParameters.getSshKeyDirectory();
                logger.debug("read key dir: " + keydir);
                File parent = new File(keydir);
                String[] children = parent.list();
                for (int i = 0; i < children.length; i++) {
                    String filename = children[i];
                    if (filename.endsWith(".pub")) {
                        String privKeyFilename = filename.substring(0,
                                filename.length() - 4);
                        File privKeyFile = new File(parent, privKeyFilename);
                        if (privKeyFile.canRead()) {
                            try {
                                logger.debug("adding identity " +
                                    privKeyFile.getPath());
                                _jsch.addIdentity(privKeyFile.getPath(), (String)null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return _jsch;
    }

    static private JSch _jsch = null;

    static public Session getSession(String username, String hostname,
        String sshPort) throws IOException {
        if (_hash == null) {
            _hash = new Hashtable<String, Session>();
        }
        String key = sshPort + username + hostname;
        Session val = _hash.get(key);
        Session session;
        if (val == null) {
            int port = Integer.parseInt(sshPort);
            try {
                session = getJSch().getSession(username, hostname, port);
                session.setUserInfo(new UserInfoNone());
            } catch (JSchException e) {
                throw new IOException(e.getMessage());
            }
            _hash.put(key, session);
        } else {
            session = val;
        }
        if (!session.isConnected()) {
            try {
                session.connect();
            } catch (JSchException e) {
                if (e.getMessage().indexOf("java.net.NoRouteToHostException") != -1) {
                    logger.info("No route to host from " +
                        InetAddress.getLocalHost().getHostName() + " to " +
                        hostname + ":" + sshPort +
                        "; please check your descriptor file.");
                    if (ProActiveConfiguration.isForwarder()) {
                        logger.info("Forwarding enabled." +
                            " An internal IP is probably returned by a forwarder," +
                            " you can fix this using internal_ip attribute");
                    }
                } else {
                    logger.info(e.getMessage() + " when connecting from " +
                        InetAddress.getLocalHost().getHostName() + "to " +
                        hostname + ":" + sshPort);
                    throw new IOException(e.getMessage());
                }
            }
        }
        return session;
    }

    static private Hashtable<String, Session> _hash = null;

    static public void flushMaybe(String username, String hostname,
        String sshPort, int localPort) {
        String key = sshPort + username + hostname;
        Session val = _hash.get(key);
        if (val == null) {
            return;
        }
        Session session = val;
        try {
            session.delPortForwardingL(localPort);
            int nForward = session.getPortForwardingL().length;
            if (nForward == 0) {
                _hash.remove(key);
                session.disconnect();
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
}
