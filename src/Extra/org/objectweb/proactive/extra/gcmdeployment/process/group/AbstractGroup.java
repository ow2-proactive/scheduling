package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public abstract class AbstractGroup implements Group {
    private HostInfo hostInfo;
    private String commandPath;
    private String env;
    private String id;

    public AbstractGroup() {
    }

    public AbstractGroup(AbstractGroup group) {
        try {
            this.hostInfo = (HostInfo) ((group.hostInfo != null)
                ? group.hostInfo.clone() : null);
            this.commandPath = (commandPath != null) ? new String(commandPath)
                                                     : null;
            this.env = (group.env != null) ? new String(group.env) : null;
            this.id = (group.id != null) ? new String(group.id) : null;
        } catch (CloneNotSupportedException e) {
            // can't happen
        }
    }

    public void setCommandPath(String commandPath) {
        this.commandPath = commandPath;
    }

    public void setEnvironment(String env) {
        this.env = env;
    }

    protected String getCommandPath() {
        return commandPath;
    }

    protected String getEnv() {
        return env;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void check() throws IllegalStateException {
        // 1- hostInfo must be set
        synchronized (hostInfo) {
            if (hostInfo == null) {
                throw new IllegalStateException("hostInfo is not set in " +
                    this);
            }
            hostInfo.check();
        }

        if (id == null) {
            throw new IllegalStateException("id is not set in " + this);
        }
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HostInfo hostInfo) {
        synchronized (hostInfo) {
            assert (hostInfo == null);
            this.hostInfo = hostInfo;
        }
    }

    public List<String> buildCommands(CommandBuilder commandBuilder) {
        List<String> commands = internalBuildCommands();
        List<String> ret = new ArrayList<String>();
        for (String comnand : commands) {
            ret.add(comnand + " " +
                Helpers.escapeCommand(commandBuilder.buildCommand(hostInfo)));
        }

        return ret;
    }

    abstract public List<String> internalBuildCommands();
}
