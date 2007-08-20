package org.objectweb.proactive.extra.gcmdeployment.process.commandbuilder;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.PathElement;
import org.objectweb.proactive.extra.gcmdeployment.process.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.process.HostInfo;


public class CommandBuilderScript implements CommandBuilder {

    /** List of providers to be used */
    private List<GCMDeploymentDescriptor> providers;
    private String command;

    /** The path to the command */
    private PathElement path;

    /** The arguments*/
    private List<String> args;

    /** File transfers to perform before starting the command */
    private List<FileTransferBlock> fts;
    public enum Instances {onePerHost,
        onePerVM,
        onePerCapacity;
    }
    private Instances instances;

    public CommandBuilderScript() {
        providers = new ArrayList<GCMDeploymentDescriptor>();
        args = new ArrayList<String>();
        instances = Instances.onePerHost;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setPath(PathElement pe) {
        path = pe;
    }

    public void addArg(String arg) {
        args.add(arg);
    }

    public void addFileTransferBlock(FileTransferBlock ftb) {
        fts.add(ftb);
    }

    public void addDescriptor(GCMDeploymentDescriptor desc) {
        providers.add(desc);
    }

    public String buildCommand(HostInfo hostInfo) {
        StringBuilder sb = new StringBuilder();
        if (path != null) {
            sb.append(PathElement.appendPath(path.getFullPath(hostInfo, this),
                    command, hostInfo));
        } else {
            sb.append(command);
        }

        for (String arg : args) {
            sb.append(" " + arg);
        }

        return sb.toString();
    }

    public String getPath(HostInfo hostInfo) {
        return path.getFullPath(hostInfo, this);
    }

    public void setInstances(String instancesValue) {
        if (instancesValue.equals("onePerHost")) {
            instances = Instances.onePerHost;
        } else if (instancesValue.equals("onePerVM")) {
            instances = Instances.onePerVM;
        } else if (instancesValue.equals("onePerCapacity")) {
            instances = Instances.onePerCapacity;
        }
    }
}
