package org.objectweb.proactive.extra.gcmdeployment.process;

import java.io.Serializable;
import java.util.List;


public interface Group extends Serializable {
    public String getId();

    /**
     * Set environment variables for this cluster
     * @param env environment variables
     */
    public void setEnvironment(String env);

    /**
     * Set the command path to override the default one
     * @param commandPath path to the command
     */
    public void setCommandPath(String commandPath);

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

    /**
     * Build the command to start the group
     *
     * @param commandBuilder The final command builder
     * @return The command to be used to start this group
     */
    public List<String> buildCommands(CommandBuilder commandBuilder);
}
