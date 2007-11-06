package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


public class GroupGridBus extends AbstractGroup {
    private List<String> argumentsList;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setArgumentsList(List<String> argumentsList) {
        this.argumentsList = argumentsList;
    }
}
