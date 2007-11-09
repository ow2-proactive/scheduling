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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.process.ListGenerator;


public class GroupSSH extends AbstractGroup {
    public final static String DEFAULT_SSHPATH = "ssh";
    private String hostList;
    private String username;
    private String commandOptions;

    public String getCommandOption() {
        return commandOptions;
    }

    public void setCommandOption(String commandOption) {
        this.commandOptions = commandOption;
    }

    public GroupSSH() {
        setCommandPath(DEFAULT_SSHPATH);
        hostList = "";
    }

    public GroupSSH(GroupSSH groupSSH) {
        super(groupSSH);
        this.hostList = groupSSH.hostList;
        this.username = groupSSH.username;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    @Override
    public List<String> internalBuildCommands() {
        List<String> commands = new ArrayList<String>();

        for (String hostname : ListGenerator.generateNames(hostList)) {
            String command = makeSingleCommand(hostname);
            commands.add(command);
        }

        return commands;
    }

    /**
     * return ssh command given the hostname, e.g. :
     *
     * ssh -l username hostname.domain
     *
     * @param hostname
     * @return
     */
    private String makeSingleCommand(String hostname) {
        StringBuilder res = new StringBuilder(getCommandPath());
        res.append(" ");

        if (username != null) {
            res.append("-l ").append(username);
            res.append(" ");
        }

        if (commandOptions != null) {
            res.append(" ");
            res.append(commandOptions);
            res.append(" ");
        }

        res.append(hostname);

        return res.toString();
    }

    @Override
	public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
