/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.components.c3d;

import org.objectweb.fractal.api.control.BindingController;

import org.objectweb.proactive.examples.c3d.C3DUser;
import org.objectweb.proactive.examples.c3d.User;
import org.objectweb.proactive.examples.c3d.gui.NameAndHostDialog;


/** The component container for a User. */
public class UserImpl extends C3DUser implements Runnable, BindingController,
    User {
    public UserImpl() {
    }

    /** returns all the possible bindings, here just user--dispatcher . */
    public String[] listFc() {
        return new String[] { "user2dispatcher" };
    }

    /** Returns the current dispatcher being used */
    public Object lookupFc(final String cItf) {
        if (cItf.equals("user2dispatcher")) {
            return c3ddispatcher;
        }

        return null;
    }

    /** Gives the dispatcher that the user should use. */
    public void bindFc(final String cItf, final Object sItf) {
        if (cItf.equals("user2dispatcher")) {
            c3ddispatcher = (org.objectweb.proactive.examples.c3d.Dispatcher) sItf;

            // Registering back to the dispatcher is done in the go() method 
        }
    }

    /**
     * Detaches the user from its dispatcher.
     * Notice how it has not been called in terminate() ?
     * This is due to the fact that unbinding only sets a reference to null,
     * and does no cleaning up.
     */
    public void unbindFc(final String cItf) {
        if (cItf.equals("user2dispatcher")) {
            c3ddispatcher = null;
        }
    }

    /** The initialization and linkage is made in this method, instead of using the constructor */
    public void findDispatcher() {
        // active Object related fields
        this.me = (User) org.objectweb.proactive.ProActive.getStubOnThis();

        if (getUserName() == null) { // just in case it was not yet set.
            setUserName("Bob");
        }

        // Maybe 'binding to dispatcher' has been done before
        if (this.c3ddispatcher == null) {
            logger.error(
                "User component could not find a dispatcher. Performing lookup");

            String localHost = getLocalHostString();

            // ask user through Dialog for userName & host
            NameAndHostDialog userAndHostNameDialog = new NameAndHostDialogForComponent(localHost);
            this.c3ddispatcher = userAndHostNameDialog.getValidatedDispatcher();
            setUserName(userAndHostNameDialog.getValidatedUserName());

            if (this.c3ddispatcher == null) {
                logger.error("Could not find a dispatcher. Closing.");
                System.exit(-1);
            }
        }
    }
}
