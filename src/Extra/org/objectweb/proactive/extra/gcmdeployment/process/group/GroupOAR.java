package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupOAR extends AbstractGroup {
    protected String resources;
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String OARSUB = "oarsub";
    protected String interactive = "false";
    protected String queueName;
    protected String accessProtocol;
    private PathElement scriptLocation;
    private PathElement directory;
    private PathElement stdOutFile;
    private PathElement stdErrFile;
    private String type = "deploy";
    private String nodes = null;
    private String cpu = null;
    private String core = null;

    @Override
    public List<String> internalBuildCommands() {
        StringBuffer commandBuf = new StringBuffer(OARSUB);

        commandBuf.append(" --type=");
        commandBuf.append(type);

        if (interactive.equalsIgnoreCase("true")) {
            commandBuf.append(" --interactive");
        }

        if (queueName != null) {
            commandBuf.append(" --queue=");
            commandBuf.append(queueName);
        }

        String resources = computeResourcesString();

        if (resources != null) {
            commandBuf.append(" --resource=");
            commandBuf.append(resources);
        }

        if (directory != null) {
            commandBuf.append(" --directory=");
            commandBuf.append(directory);
        }

        if (stdOutFile != null) {
            commandBuf.append(" --stdout=");
            commandBuf.append(stdOutFile.toString());
        }

        if (stdErrFile != null) {
            commandBuf.append(" --stderr=");
            commandBuf.append(stdErrFile.toString());
        }

        // argument - must be last append
        commandBuf.append(" ");
        commandBuf.append(scriptLocation.toString());

        List<String> res = new ArrayList<String>();
        res.add(commandBuf.toString());

        return res;
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public void setResources(String res) {
        this.resources = res;
    }

    public void setScriptLocation(PathElement location) {
        this.scriptLocation = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public void setDirectory(PathElement directory) {
        this.directory = directory;
    }

    public void setStdOutFile(PathElement stdOutFile) {
        this.stdOutFile = stdOutFile;
    }

    public void setStdErrFile(PathElement stdErrFile) {
        this.stdErrFile = stdErrFile;
    }

    protected String computeResourcesString() {
        if (resources != null) {
            return resources;
        }

        StringBuffer resourcesBuf = new StringBuffer();

        if (nodes != null) {
            resourcesBuf.append("/nodes=" + nodes);
        }
        if (cpu != null) {
            resourcesBuf.append("/cpu=" + cpu);
        }
        if (core != null) {
            resourcesBuf.append("/core=" + core);
        }

        return resourcesBuf.toString();
    }
}
