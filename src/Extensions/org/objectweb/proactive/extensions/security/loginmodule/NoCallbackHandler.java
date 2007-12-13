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
package org.objectweb.proactive.extensions.security.loginmodule;

import java.io.Serializable;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * CallbackHandler that actually does no callback, but takes its informations as
 * a map in the constructor and then gives them to all the callbacks given in
 * the handle method.
 *
 * @author nhouillo
 *
 */
public class NoCallbackHandler implements CallbackHandler, Serializable {
    private Map<String, Object> items;

    public NoCallbackHandler(Map<String, Object> items) {
        super();

        this.items = items;
    }

    /**
     * Invoke an array of Callbacks.
     *
     * <p>
     *
     * @param callbacks
     *            an array of <code>Callback</code> objects that will be given
     *            the informations given to the handler at its creation, they
     *            must be <code>NoCallback</code>s.
     *
     * @exception UnsupportedCallbackException
     *                if the implementation of this method does not support one
     *                or more of the Callbacks specified in the
     *                <code>callbacks</code> parameter.
     */
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        if (callbacks[0] instanceof NoCallback) {
            NoCallback nc = (NoCallback) callbacks[0];
            nc.set(items);
        } else {
            throw new UnsupportedCallbackException(callbacks[0], "Unrecognized Callback");
        }
    }
}
