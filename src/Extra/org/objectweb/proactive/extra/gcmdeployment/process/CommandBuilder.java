package org.objectweb.proactive.extra.gcmdeployment.process;

public interface CommandBuilder {

    /**
     * Build the command to start the application
     * @param hostInfo Host information to customize the command according to this host type
     * @return The command to be used to start the application
     */
    public String buildCommand(HostInfo hostInfo);

    /**
     * Returns the base path associated to this command builder
     *
     * Since the base path is always relative to the home directory a HostInfo
     * must be passed as parameter.
     *
     * @return the base path if the base path is specified otherwise null is returned
     */
    public String getPath(HostInfo hostInfo);
}
