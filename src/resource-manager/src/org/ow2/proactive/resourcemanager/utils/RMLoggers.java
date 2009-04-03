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
 * Log4j loggers names of different Resource manager components. 
 *
 * @author The ProActive Team
 */
public interface RMLoggers {

    /**
     * prefix name of RM's loggers  
     */
    static final public String RESOURCEMANAGER = "proactive.resourceManager";

    /**
     * RMCore logger's name.
     */
    static final public String CORE = RESOURCEMANAGER + ".core";

    /**
     * RMAdmin logger's name.  
     */
    static final public String ADMIN = RESOURCEMANAGER + ".admin";

    /**
     * RMUser logger's name.
     */
    static final public String USER = RESOURCEMANAGER + ".user";

    /**
     * RMMonitoring logger's name. 
     */
    static final public String MONITORING = RESOURCEMANAGER + ".monitoring";

    /**
     * Node sources logger's name. 
     */
    static final public String NODESOURCE = RESOURCEMANAGER + ".nodeSource";

    /**
     * RMNode logger's name. 
     */
    static final public String RMNODE = RESOURCEMANAGER + ".rmnode";

    /**
     * RMFactory logger's name. 
     */
    static final public String RMFACTORY = RESOURCEMANAGER + ".rmfactory";

    /**
     * RMLauncher logger's name. 
     */
    static final public String RMLAUNCHER = RESOURCEMANAGER + ".rmlauncher";

    static final public String CONNECTION = RESOURCEMANAGER + ".connection";
    /**
     * Selection manager logger's name. 
     */
    static final public String RMSELECTION = RESOURCEMANAGER + ".rmselection";

    static final public String CONSOLE = RESOURCEMANAGER + ".consol";

    static final public String POLICY = RESOURCEMANAGER + ".policy";
}
