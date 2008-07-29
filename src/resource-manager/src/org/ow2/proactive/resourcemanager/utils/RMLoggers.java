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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.utils;

/**
 * RMLoggers
 *
 * @author The ProActive Team
 */
public interface RMLoggers {

    static final public String RESOURCEMANAGER = "resourceManager";

    static final public String CORE = RESOURCEMANAGER + ".core";

    static final public String ADMIN = RESOURCEMANAGER + ".admin";

    static final public String USER = RESOURCEMANAGER + ".user";

    static final public String MONITORING = RESOURCEMANAGER + ".monitoring";

    static final public String PROXY = RESOURCEMANAGER + ".proxy";

    static final public String NODESOURCE = RESOURCEMANAGER + ".nodeSource";

    static final public String RMNODE = RESOURCEMANAGER + ".rmnode";

    static final public String RMFACTORY = RESOURCEMANAGER + ".rmfactory";

}
