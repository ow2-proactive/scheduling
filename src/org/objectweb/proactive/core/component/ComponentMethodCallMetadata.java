package org.objectweb.proactive.core.component;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ComponentMethodCallMetadata implements Serializable {
    static final transient Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);
    private String componentInterfaceName = null;
    private boolean isComponentMethodCall;
    protected Shortcut shortcut = null;
    protected short priority;
    private ItfID senderItfID = null;

    public void shortcutNotification(UniversalBody sender,
        UniversalBody intermediate) {
        if (shortcut == null) {
            // store only first sender?
            shortcut = new Shortcut(getComponentInterfaceName(), sender,
                    intermediate);
        } else {
            shortcut.updateDestination(intermediate);
            if (logger.isDebugEnabled()) {
                logger.debug("added shortcut : shortcutCounter is now " +
                    shortcut.length());
            }
        }
    }

    /**
     * @return Returns the componentInterfaceName.
     */
    public String getComponentInterfaceName() {
        return componentInterfaceName;
    }

    /**
     * @param componentInterfaceName The componentInterfaceName to set.
     */
    public void setComponentInterfaceName(String componentInterfaceName) {
        this.componentInterfaceName = componentInterfaceName;
    }

    /**
     * @return Returns the isComponentMethodCall.
     */
    public boolean isComponentMethodCall() {
        return isComponentMethodCall;
    }

    /**
     * @param isComponentMethodCall The isComponentMethodCall to set.
     */
    public void setComponentMethodCall(boolean isComponentMethodCall) {
        this.isComponentMethodCall = isComponentMethodCall;
    }

    /**
     * @return Returns the priority.
     */
    public short getPriority() {
        return priority;
    }

    /**
     * @param priority The priority to set.
     */
    public void setPriority(short priority) {
        this.priority = priority;
    }

    /**
     * @return Returns the shortcut.
     */
    public Shortcut getShortcut() {
        return shortcut;
    }

    /**
     * @param shortcut The shortcut to set.
     */
    public void setShortcut(Shortcut shortcut) {
        this.shortcut = shortcut;
    }

    /**
     * @return Returns the sourceItfID.
     */
    public ItfID getSenderItfID() {
        return senderItfID;
    }

    /**
     * @param senderItfID The sourceItfID to set.
     */
    public void setSenderItfID(ItfID senderItfID) {
        this.senderItfID = senderItfID;
    }
}
