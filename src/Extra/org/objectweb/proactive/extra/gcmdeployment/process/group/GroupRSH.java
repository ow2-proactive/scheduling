package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.process.ListGenerator;


public class GroupRSH extends AbstractGroup {
    public final static String DEFAULT_RSHPATH = "rsh";
    private String hostList;
    private String domain;
    private String username;

    public GroupRSH() {
        setCommandPath(DEFAULT_RSHPATH);
        hostList = "";
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
     * return rsh command given the hostname, e.g. :
     *
     * rsh -l username hostname.domain
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

        if (domain != null) {
            res.append(".").append(domain);
        }

        return res.toString();
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDomain() {
        return domain;
    }

    public String getUsername() {
        return username;
    }
}
