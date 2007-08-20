package org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder;

import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class CommandBuilderDummy implements CommandBuilder {
    String command;

    public CommandBuilderDummy(String command) {
        this.command = command;
    }

    public String buildCommand(HostInfo hostInfo) {
        return command;
    }

    public String getPath(HostInfo hostInfo) {
        return "";
    }
}
