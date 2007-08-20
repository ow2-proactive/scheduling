package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment;

import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;


public interface GCMDeploymentDescriptor {

    /** A magic number to indicate that the maximum capacity of
     * this GCM Deployment Descriptor is not known.
     */
    public static final long UNKNOWN_CAPACITY = -1;

    /**
     * Start the deployment
     *
     * The first step is to perform all required file transfers. Then
     * Use the CommandBuilder to build the command to be launched.
     */
    public void start(CommandBuilder commandBuilder);

    /**
     * Returns the maximum capacity this GCM Deployment Descriptor can provide
     *
     * @return the maximum capacity as declared in the descriptor file. If the
     * max capacity cannot be computed UNKNOWN_CAPACITY is returned.
     */
    public long getMaxCapacity();
}
