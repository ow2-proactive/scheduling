package org.ow2.proactive.scheduler.gui.data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.Activator;


/**
 * Statically stores the information about currently available DataServers
 * <p>
 * 
 *
 */
public class DataServers {

    private static DataServers instance = null;

    private Map<String, Server> servers = null;

    /**
     * Holds DataServer data
     */
    public static class Server {
        private FileSystemServerDeployer deployer;
        private String url;
        private String rootDir;
        private String name;
        private boolean forked;
        private int pid;

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

        public boolean isForked() {
            return forked;
        }

        public int getPid() {
            return pid;
        }

        public Server(FileSystemServerDeployer deployer, String url, String rootDir, String name,
                boolean forked, int pid) {
            this.deployer = deployer;
            this.url = url;
            this.rootDir = rootDir;
            this.name = name;
            this.forked = forked;
            this.pid = pid;
        }
    }

    private DataServers() {
        servers = new HashMap<String, Server>();
    }

    /**
     * @param deployer the DataServer
     * @param url VFS url of the provider
     * @param rootDir the rootDir used to create the Deployer
     * @param name the name of the server represented by this deployer
     */
    public void addServer(FileSystemServerDeployer deployer, String url, String rootDir, String name) {
        this.addServer(deployer, url, rootDir, name, false, 0);
    }

    /**
     * @param url VFS url of the provider
     * @param rootDir the rootDir used to create the Deployer
     * @param name the name of the server represented by this deployer
     * @param forked true when the Server was launched in a forked process
     * @param pid the PID of the forked process if appliable
     */
    public void addServer(String url, String rootDir, String name, boolean forked, int pid) {
        this.addServer(null, url, rootDir, name, true, pid);
    }

    /**
     * @param deployer the DataServer
     * @param url VFS url of the provider
     * @param rootDir the rootDir used to create the Deployer
     * @param name the name of the server represented by this deployer
     * @param forked true when the Server was launched in a forked process
     * @param pid the PID of the forked process if appliable
     */
    public void addServer(FileSystemServerDeployer deployer, String url, String rootDir, String name,
            boolean forked, int pid) {
        Server s = new Server(deployer, url, rootDir, name, forked, pid);
        this.servers.put(url, s);
    }

    /**
     * @param url the URL of the Server to remove
     */
    public void removeServer(String url) {
        Server s = this.servers.remove(url);

        if (s.deployer != null) {
            try {
                s.deployer.terminate();
            } catch (ProActiveException e) {
                Activator.log(IStatus.ERROR, "Could terminate DataServer at '" + url + "'", e);
            }
        } else {
            Activator.log(IStatus.INFO, "DataServer at '" + url + "' was not terminated", null);
        }
    }

    /**
     * @param url see {@link FileSystemServerDeployer#getVFSRootURL()}
     * @return the server corresponding the URL, or null
     */
    public Server getServer(String url) {
        return this.servers.get(url);
    }

    /**
     * @return all the currently deployed servers
     */
    public Map<String, Server> getServers() {
        return this.servers;
    }

    /**
     * @return the singleton DataServers instance, cannot be null
     */
    public static DataServers getInstance() {
        if (instance == null) {
            instance = new DataServers();
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
        for (Server srv : this.servers.values()) {
            try {
                srv.deployer.terminate();
            } catch (ProActiveException e) {
                Activator.log(IStatus.ERROR, "Failed to terminate DataServer " + srv.getUrl(), e);
            }
        }
    }

}
