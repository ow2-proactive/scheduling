package org.objectweb.proactive.ic2d.jmxmonitoring;

public class MVCNotifications {
    public static enum MVC_Notifications {
        //General messages
        ADD_CHILD,REMOVE_CHILD,
        REMOVE_CHILD_FROM_MONITORED_CHILDREN,
        STATE_CHANGED,
        WORLD_OBJECT_ADD_HOST,

        /*
         * Notification sent When the first host is added. It is used to start a refreshing thread
         */
        WORLD_OBJECT_FIRST_CHILD_ADDED,WORLD_OBJECT_LAST_CHILD_REMOVED,
        WORLD_OBJECT_ADD_VIRTUAL_NODE,
        WORLD_OBJECT_REMOVE_VIRTUAL_NODE,
        HOST_OBJECT_UPDATED_OSNAME_AND_VERSON,
        RUNTIME_OBJECT_RUNTIME_KILLED,
        RUNTIME_OBJECT_RUNTIME_NOT_RESPONDING,
        RUNTIME_OBJECT_RUNTIME_NOT_MONITORED,
        ACTIVE_OBJECT_ADD_COMMUNICATION,
        ACTIVE_OBJECT_RESET_COMMUNICATIONS,
        ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED;
    }
}
