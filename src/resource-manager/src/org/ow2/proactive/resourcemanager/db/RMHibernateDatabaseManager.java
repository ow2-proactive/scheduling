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
package org.ow2.proactive.resourcemanager.db;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.db.HibernateDatabaseManager;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * DatabaseManager is responsible of every database transaction.<br />
 * Hibernate entities can be managed by this manager. It provides method to register, delete, synchronize
 * objects in database.
 * Each method will work on the object and its inheritance.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class RMHibernateDatabaseManager extends HibernateDatabaseManager {

    public static final Logger logger = ProActiveLogger.getLogger(RMLoggers.DATABASE);

    @Override
    public String getConfigFile() {
        return PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_DB_HIBERNATE_CONFIG
                .getValueAsString());
    }

    @Override
    public Logger getDevLogger() {
        return logger;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
