package org.objectweb.proactive.extensions.resourcemanager.gui.tree;

import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;


/**
 * @author FRADJ Johann
 */
public class Source extends TreeParentElement {

    private String type = null;

    public Source(String name, String type) {
        super(name, TreeElementType.SOURCE);
        this.type = type;
    }

    public boolean isDynamic() {
        return type.equals(RMConstants.P2P_NODE_SOURCE_TYPE);
    }

    public boolean isStatic() {
        return type.equals(RMConstants.PAD_NODE_SOURCE_TYPE);
    }

    public boolean isTheDefault() {
        return getName().equals(RMConstants.DEFAULT_STATIC_SOURCE_NAME);
    }
}
