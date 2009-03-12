package org.ow2.proactive.resourcemanager.gui.data.model;

import org.ow2.proactive.resourcemanager.common.RMConstants;


/**
 * @author The ProActive Team
 */
public class Source extends TreeParentElement {

    private String type = null;

    public Source(String name, String type) {
        super(name, TreeElementType.SOURCE);
        this.type = type;
    }

    public boolean isDynamic() {
        return false;
    }

    public boolean isStatic() {
        return type.equals(RMConstants.GCM_NODE_SOURCE_TYPE);
    }

    public boolean isTheDefault() {
        return getName().equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME);
    }
}
