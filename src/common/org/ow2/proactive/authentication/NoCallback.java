/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.authentication;

import java.io.Serializable;
import java.util.Map;

import javax.security.auth.callback.Callback;


/**
 * A callback stub to <code>NoCallbackHandler</code>. Holds some properties related to authentication method.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 *
 */
public class NoCallback implements Callback, Serializable {
    /**
     * Properties map
     */
    private Map<String, Object> values;

    /**
     * Sets properties of the callback
     *
     * @param values properties provided by <code>NoCallbackHandler</code>
     */
    protected void set(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Gets properties map
     *
     * @return stored properties map
     */
    protected Map<String, Object> get() {
        return values;
    }

    /**
     * Resets properties.
     */
    protected void clear() {
        values = null;
    }
}
