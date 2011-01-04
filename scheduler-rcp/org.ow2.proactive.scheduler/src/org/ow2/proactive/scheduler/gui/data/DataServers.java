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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.gui.views.ServersView;


/**
 * Statically stores the information about currently available DataServers
 * <p>
 * 
 *
 */
public class DataServers {

    private static DataServers instance = null;

    private static final File serversHistoryFile = new File(System.getProperty("user.home") +
        "/.ProActive_Scheduler/dataservers.history");

    private Map<String, Server> servers = null;

    /**
     * Holds DataServer data
     */
    public static class Server {
        private FileSystemServerDeployer deployer = null;
        private String url = null;
        private String rootDir = null;
        private String name = null;
        private String proto = null;
        private boolean started = false;

        public FileSystemServerDeployer getDeployer() {
            return deployer;
        }

        public String getUrl() {
            return url;
        }

        public String getRootDir() {
            return rootDir;
        }

        public String getName() {
            return name;
        }

        public boolean isStarted() {
            return started;
        }

        public String getProtocol() {
            return this.proto;
        }

        /**
         * Start this DataServer
         * 
         * @param rebind rebind an existing object
         * @throws DataSpacesException deployment failed, or DS already started
         */
        public void start(boolean rebind) throws DataSpacesException {
            if (this.isStarted())
                throw new DataSpacesException("Server " + name + " is already running at " + url);

            try {
                if (this.proto == null) {
                    this.deployer = new FileSystemServerDeployer(this.name, this.rootDir, true, rebind);
                } else {
                    this.deployer = new FileSystemServerDeployer(this.name, this.rootDir, true, rebind,
                        this.proto);
                }
            } catch (Throwable e) {
                String msg = "";
                if (e.getMessage() != null && !e.getMessage().trim().equals("")) {
                    msg += ":\n" + e.getMessage();
                } else {
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        if (cause.getMessage() != null) {
                            msg += ":\n" + cause.getMessage();
                            cause = cause.getCause();
                        }
                    }
                }
                throw new DataSpacesException("Failed to deploy DataServer " + name + " at " + rootDir + msg,
                    e);
            }

            this.url = this.deployer.getVFSRootURL();
            this.started = true;
        }

        /**
         * Stops this server
         * 
         * @throws DataSpacesException termination failed, or DS is not running
         */
        public void stop() throws DataSpacesException {
            if (!isStarted())
                throw new DataSpacesException("Server " + name + " is not running");

            try {
                this.deployer.terminate();
            } catch (ProActiveException e) {
                throw new DataSpacesException("Failed to terminate DataServer " + name + " at " + url, e);
            }
            this.started = false;
            this.url = null;
            this.deployer = null;
        }

        public Server(String rootDir, String name) {
            this(rootDir, name, null);
        }

        public Server(String rootDir, String name, String protocol) {
            this.rootDir = rootDir;
            this.name = name;
            this.started = false;
            this.proto = protocol;
        }
    }

    private DataServers() {
        servers = new HashMap<String, Server>();
    }

    /**
     * Adds a new Server to the list
     * 
     * @param rootDir the rootDir used to create the Deployer
     * @param name the name of the server represented by this deployer
     * @param rebind try to rebind an existing server
     * @param start if true, start the server, else add it in a stopped state
     * @param protocol communication protocol
     * @throws DataSpacesException DS was added but could not be started
     */
    public void addServer(String rootDir, String name, boolean rebind, boolean start, String protocol)
            throws DataSpacesException {
        if (this.servers.containsKey(name))
            throw new DataSpacesException("Name " + name + " is already used");

        Server s = new Server(rootDir, name, protocol);

        if (start) {
            s.start(rebind);
        }

        this.servers.put(name, s);
    }

    /**
     * Remove the specified server, stop it if it was running
     * 
     * @param name the name of the Server to remove
     * @throws DataSpacesException DS was removed but could not be stopped
     */
    public void removeServer(String name) throws DataSpacesException {
        Server s = this.servers.remove(name);

        if (s.isStarted()) {
            s.stop();
        }
    }

    /**
     * @return all the currently deployed servers
     */
    public Map<String, Server> getServers() {
        return this.servers;
    }

    /**
     * @param name the name of a deployed server
     * @return the corresponding server
     */
    public Server getServer(String name) {
        return this.servers.get(name);
    }

    /**
     * @return the singleton DataServers instance, cannot be null
     */
    public static DataServers getInstance() {
        if (instance == null) {
            instance = new DataServers();

            // recover history
            String[] lines = ServersView.getHistory(serversHistoryFile);
            for (int i = 0; i < lines.length; i += 3) {
                if (lines.length == i + 1)
                    break;

                String proto = lines[i];
                String root = lines[i + 1];
                String name = lines[i + 2];

                if (proto.trim().equals(""))
                    proto = null;

                try {
                    instance.addServer(root, name, false, false, proto);
                } catch (DataSpacesException e) {
                    Activator.log(IStatus.ERROR, "Failed to restore server from history: root=" + root +
                        " name" + name, e);
                }
            }
        }
        return instance;
    }

    /**
     * Terminate all running DataServers, destroy the singleton instance
     */
    public static void cleanup() {
        if (instance != null) {
            instance._cleanup();
            instance = null;
        }
    }

    private void _cleanup() {
        serversHistoryFile.delete();

        for (Server srv : this.servers.values()) {
            ServersView.addHistory(serversHistoryFile, srv.getName(), true);
            ServersView.addHistory(serversHistoryFile, srv.getRootDir(), true);
            String proto = srv.getProtocol();
            if (proto == null || proto.trim().equals(""))
                proto = "";
            ServersView.addHistory(serversHistoryFile, proto, true);

            if (srv.isStarted()) {
                try {
                    srv.deployer.terminate();
                } catch (ProActiveException e) {
                    Activator.log(IStatus.ERROR, "Failed to terminate DataServer " + srv.getName() + " at " +
                        srv.getUrl(), e);
                }
            }
        }
    }

}
