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
package org.objectweb.proactive.core.component.request;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents a shortcut. A shortcut is a link between a
 * client interface of a primitive component and the server interface of another
 * primitive component. The shortcuting mechanism is based on tensioning, which
 * is also used for migration of active objects with forwarders. When performing
 * the first invocation on a functional interface, a shortcut object is created
 * and transferred along with the component request. While it crosses membranes
 * of possible intermediate composite components, it updates the path it has
 * taken to reach its final destination. This final destination is an interface
 * of a primitive component that contains the functional code.
 * <p>
 * When the final destination is reached, the shortcut object is sent back to
 * the original sender that can decide to send further requests directly to the
 * final destination.
 * <p>
 * Currently, the shortcut object keeps references on all crossed components,
 * because this may be useful for managing dynamic reconfiguration in the
 * future.
 * <p>
 *
 * @author The ProActive Team
 */
public class Shortcut implements Serializable {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);
    private transient UniversalBody sender;
    private List<UniversalBody> steps; // the list of crossed membranes; TODO_M transient 

    // FIXME replace with a custom list with custom serialization of contained bodies 
    private String fcFunctionalInterfaceName;

    public Shortcut() {
    }

    public Shortcut(String functionalInterfaceName, UniversalBody sender, UniversalBody intermediate) {
        fcFunctionalInterfaceName = functionalInterfaceName;
        steps = new Vector<UniversalBody>();
        this.sender = sender;
        steps.add(intermediate);
    }

    public UniversalBody getSender() {
        return sender;
    }

    public void setSender(UniversalBody sender) {
        this.sender = sender;
    }

    /**
     * This method returns length of the shortcut, in other words the number
     * of bindings it represents.
     * A shortcut of length 1 indicates a normal binding.
     * @return the length of the shortcut
     */
    public int length() {
        return steps.size();
    }

    public void updateDestination(UniversalBody destination) {
        if (logger.isDebugEnabled()) {
            logger.debug("adding a new step to the shortcut chain");
        }
        steps.add(destination);
    }

    public String getFcFunctionalInterfaceName() {
        return fcFunctionalInterfaceName;
    }

    /**
     *
     * @return the id of the interface which is bound through the BindingController ; it is different from the one
     * that is the target of the communication through the shortcut.
     */
    public ItfID getLinkedInterfaceID() {
        // it is the first encountered interface while creating the shortcut
        return new ItfID(fcFunctionalInterfaceName, steps.get(0).getID());
    }

    /**
     *
     * @return the ID of the last encountered interface when creating the shortcut
     */
    public ItfID getShortcutInterfaceID() {
        // it is the last encountered interface while creating the shortcut
        return new ItfID(fcFunctionalInterfaceName, steps.get(steps.size() - 1).getID());
    }

    /**
     *
     * @return a reference on the body which is targetted by this shortcut
     */
    public UniversalBody getShortcutTargetBody() {
        return steps.get(steps.size() - 1);
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        out.writeObject(sender.getRemoteAdapter());
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        sender = (UniversalBody) in.readObject(); // it is actually a UniversalBody
    }
}
