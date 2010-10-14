package org.ow2.proactive.scheduler.ext.filessplitmerge.util;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.util.SchedulerProxyUserInterface;

/**
 * A class that maintains a reference to the scheduler
 *
 */
public class MySchedulerProxy {

    protected static SchedulerProxyUserInterface activeInstance;

    public synchronized static SchedulerProxyUserInterface getActiveInstance()
            throws ActiveObjectCreationException, NodeException {
        if (activeInstance == null) {
            activeInstance = (SchedulerProxyUserInterface) PAActiveObject.newActive(
                    SchedulerProxyUserInterface.class.getName(), new Object[] {});
        }

        return activeInstance;
    }
}
