package org.objectweb.proactive.extra.infrastructuremanager.core;

public interface IMConstants {
    // Name of the property file 
    public static final String NAME_PROPERTY_FILE = "im.properties";

    // Constants for getting properties to the file im.properties
    public static final String NAME_NODE_IM = "IMNODE";
    public static final String NAME_ACTIVE_OBJECT_IMCORE = "IMCORE";

    //public static final String LOCATION_TO_SAVE_PAD 	 = "infrastructuremanager/test/pull";

    /*
    public static final String NAME_NODE_IM                      = "NAME_NODE_IM";
    public static final String NAME_ACTIVE_OBJECT_IMCORE = "NAME_ACTIVE_OBJECT_IMCORE";
    */
    public static final String PULL_PAD_LOCATION = "LOCATION_TO_SAVE_PAD";

    // The states IMNode :
    public static final String IMNODE_STATE_AVAILABLE = "Available";
    public static final String IMNODE_STATE_BUSY = "Busy";
    public static final String IMNODE_STATE_DOWN = "Down";
}
