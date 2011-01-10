/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend.topology;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.frontend.ResourceManager;


/**
 * An exception thrown by {@link ResourceManager.getAtMostNodes} request when
 * the topology information is missing.
 *
 */
@PublicAPI
public class TopologyException extends IllegalArgumentException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;

	/**
     * Create a new instance of TopologyException
     *
     */
    public TopologyException() {
        super();
    }

    /**
     * Create a new instance of TopologyException
     *
     * @param message
     * @param cause
     */
    public TopologyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new instance of TopologyException
     *
     * @param s
     */
    public TopologyException(String s) {
        super(s);
    }

    /**
     * Create a new instance of TopologyException
     *
     * @param cause
     */
    public TopologyException(Throwable cause) {
        super(cause);
    }

}
