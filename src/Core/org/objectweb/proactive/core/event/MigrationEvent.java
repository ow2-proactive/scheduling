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
package org.objectweb.proactive.core.event;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.migration.MigrationException;


/**
 * <p>
 * <code>MigrationEvent</code>s occur during the migration of an active object. Several type
 * allow to determine when the event occured in the process of migration.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class MigrationEvent extends ProActiveEvent implements java.io.Serializable {
    public static final int BEFORE_MIGRATION = 10;
    public static final int AFTER_MIGRATION = 20;
    public static final int RESTARTING_AFTER_MIGRATING = 30;
    public static final int MIGRATION_EXCEPTION = 40;

    /**
     * Creates a new <code>MigrationEvent</code> occuring during the migration of the
     * active object linked to the given body.
     * @param body the body associated to the migrating active object
     * @param type a number specifying when in the process of migration the event occured.
     */
    public MigrationEvent(Body body, int type) {
        super(body, type);
    }

    /**
     * Creates a new <code>MigrationEvent</code> based on an exception occuring during the process of migration.
     * @param exception the exception that occured
     */
    public MigrationEvent(MigrationException exception) {
        super(exception, MIGRATION_EXCEPTION);
    }
}
