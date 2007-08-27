package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.proactive.extra.gcmdeployment.process.ListGenerator;


public class GroupSSH extends AbstractGroup {
    public final static String DEFAULT_SSHPATH = "ssh";
    private String hostList;
    private String username;

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
        }

        res.append(" ").append(hostname);

        return res.toString();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
