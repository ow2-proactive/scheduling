/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
