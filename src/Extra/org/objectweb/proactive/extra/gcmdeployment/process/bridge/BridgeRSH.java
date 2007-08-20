package org.objectweb.proactive.extra.gcmdeployment.process.bridge;

public class BridgeRSH extends AbstractBridge {
    public final static String DEFAULT_RSHPATH = "rsh";

    public BridgeRSH() {
        setCommandPath(DEFAULT_RSHPATH);
    }

    @Override
    public String internalBuildCommand() {
        StringBuilder command = new StringBuilder();
        command.append(getCommandPath());
        // append username
        if (getUsername() != null) {
            command.append(" -l ");
            command.append(getUsername());
        }

        // append host
        command.append(" ");
        command.append(getHostname());
        command.append(" ");

        return command.toString();
    }
}
