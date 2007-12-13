/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
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

    public void shortcutNotification(UniversalBody sender, UniversalBody intermediate) {
        if (shortcut == null) {
            // store only first sender?
            shortcut = new Shortcut(getComponentInterfaceName(), sender, intermediate);
        } else {
            shortcut.updateDestination(intermediate);
            if (logger.isDebugEnabled()) {
                logger.debug("added shortcut : shortcutCounter is now " + shortcut.length());
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
