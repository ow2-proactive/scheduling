package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupOAR extends AbstractGroup {
    protected String resources;
    protected static final String DEFAULT_HOSTS_NUMBER = "1";
    protected String hostNumber = DEFAULT_HOSTS_NUMBER;
    protected String weight = "2";
    protected String interactive = "false";
    protected String queueName;
    protected String accessProtocol;
    private PathElement scriptLocation;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
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
        if (res != null) {
            this.resources = res;
            parseRes(res);
        }
    }

    /**
     * @param res
     */
    private void parseRes(String res) {
        String[] resTab = res.split(",");
        for (int i = 0; i < resTab.length; i++) {
            if (!(resTab[i].indexOf("nodes") < 0)) {
                hostNumber = resTab[i].substring(resTab[i].indexOf("=") + 1,
                        resTab[i].length());
            }
            if (!(resTab[i].indexOf("weight") < 0)) {
                weight = resTab[i].substring(resTab[i].indexOf("=") + 1,
                        resTab[i].length());
            }
        }
    }

    public void setScriptLocation(PathElement location) {
        this.scriptLocation = location;
    }
}
