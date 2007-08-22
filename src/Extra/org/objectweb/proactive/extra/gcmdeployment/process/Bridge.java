package org.objectweb.proactive.extra.gcmdeployment.process;

import java.io.Serializable;
import java.util.List;


/**
 *
 * A Bridge can have Bridges, Groups and HostInfo as children
 *
 */
public interface Bridge extends Serializable {

    /**
     * Set environment variables for this cluster
     * @param env environment variables
     */
    public void setEnvironment(String env);

    /**
     * Set the destination host
     * @param hostname destination host as FQDN or IP address
     */
    public void setHostname(String hostname);

    /**
     * Username to be used on the destination host
     * @param username an username
     */
    public void setUsername(String username);

    /**
     * Set the command path to override the default one
     * @param commandPath path to the command
     */
    public void setCommandPath(String commandPath);

    public String getId();

    /**
     * Add a bridge to children elements
     *
     * @param bridge The child bridge
     */
    public void addBridge(Bridge bridge);

    /**
     * Returns all children of type Bridge
     *
     * @return
     */
    public List<Bridge> getBridges();

    /**
     * Add a group to children elements
     *
     * @param group The child group
     */
    public void addGroup(Group group);

    /**
     * Returns all children of type Group
     *
     * @return
     */
    public List<Group> getGroups();

    /**
     * Set the HostInfo
     *
     * @param hostInfo
     */
    public void setHostInfo(HostInfo hostInfo);

    /**
     * Get the HostInfo
     *
     * @return if set the HostInfo is returned. null is returned otherwise
     */
    public HostInfo getHostInfo();

    /**
     * Check that this bridge is in a consistent state and is ready to be
     * used.
     *
     * @throws IllegalStateException thrown if anything is wrong
     */
    public void check() throws IllegalStateException;

    public List<String> buildCommands(CommandBuilder commandBuilder);
}
