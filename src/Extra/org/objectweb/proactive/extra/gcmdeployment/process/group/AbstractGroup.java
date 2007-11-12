/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.extra.gcmdeployment.Helpers;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.Group;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;
import org.w3c.dom.Node;


public abstract class AbstractGroup implements Group {
    private HostInfo hostInfo;
    private String commandPath;
    private String env;
    private String id;
    private String username;
    private String bookedNodesAccess;
    private PathElement scriptPath;

    public AbstractGroup() {
    }

    public AbstractGroup(AbstractGroup group) {
        try {
            this.hostInfo = (HostInfo) ((group.hostInfo != null)
                ? Utils.makeDeepCopy(group.hostInfo) : null);
            this.commandPath = (group.commandPath != null)
                ? new String(group.commandPath) : null;
            this.env = (group.env != null) ? new String(group.env) : null;
            this.id = (group.id != null) ? new String(group.id) : null;
            this.username = (group.username != null)
                ? new String(group.username) : null;
            this.bookedNodesAccess = (group.bookedNodesAccess != null)
                ? new String(group.bookedNodesAccess) : null;
            this.scriptPath = (PathElement) ((group.scriptPath != null)
                ? Utils.makeDeepCopy(group.scriptPath) : null);
        } catch (IOException e) {
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBookedNodesAccess(String bookedNodesAccess) {
        this.bookedNodesAccess = bookedNodesAccess;
    }

    public void setScriptPath(PathElement scriptPath) {
        this.scriptPath = scriptPath;
    }
}
