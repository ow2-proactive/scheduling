package org.ow2.proactive.resourcemanager.gui.data.model;

import org.ow2.proactive.resourcemanager.nodesource.NodeSource;


/**
 * @author The ProActive Team
 */
public class Source extends TreeParentElement {

    private String description = null;

    public Source(String name, String description) {
        super(name, TreeElementType.SOURCE);
        this.description = description;
    }

    public boolean isTheDefault() {
        return getName().equals(NodeSource.DEFAULT_NAME);
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return getName() + " [" + description + "]";
    }
}
