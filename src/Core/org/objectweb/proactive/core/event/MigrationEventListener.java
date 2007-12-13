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

/**
 * <p>
 * A class implementating this interface is listener of <code>MigrationEvent</code>
 * that occurs in the process of the migration of a body associated to an active object.
 * </p>
 *
 * @see MigrationEvent
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface MigrationEventListener extends ProActiveListener {

    /**
     * Signals that a migration is about to start
     * @param event the event that details the migration
     */
    public void migrationAboutToStart(MigrationEvent event);

    /**
     * Signals that the migration is finished on the originating host side
     * @param event the event that details the migration
     */
    public void migrationFinished(MigrationEvent event);

    /**
     * Signals that the migration failed with a exception detailed in the event.
     * @param event the event that details the exception occured in the migration
     */
    public void migrationExceptionThrown(MigrationEvent event);

    /**
     * Signals that the migrated body has restarted of the destination host side
     * @param event the event that details the migration
     */
    public void migratedBodyRestarted(MigrationEvent event);
}
