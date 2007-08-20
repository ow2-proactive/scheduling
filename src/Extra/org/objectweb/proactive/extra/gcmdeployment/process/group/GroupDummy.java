package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;


/**
 * A dummy Group implementation for unit testing
 *
 */
public class GroupDummy extends AbstractGroup {
    List<String> commands;

    public GroupDummy(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public List<String> internalBuildCommands() {
        return commands;
    }
}
