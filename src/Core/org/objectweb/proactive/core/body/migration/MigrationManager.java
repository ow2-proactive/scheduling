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
package org.objectweb.proactive.core.body.migration;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.node.Node;


public interface MigrationManager {
    public Node checkNode(Node node) throws MigrationException;

    public UniversalBody migrateTo(Node node, Body body)
        throws MigrationException;

    public void changeBodyAfterMigration(MigratableBody body,
        UniversalBody migratedBody);

    public void startingAfterMigration(Body body);

    public RequestReceiver createRequestReceiver(UniversalBody remoteBody,
        RequestReceiver currentRequestReceiver);

    public ReplyReceiver createReplyReceiver(UniversalBody remoteBody,
        ReplyReceiver currentReplyReceiver);

    public void addMigrationEventListener(MigrationEventListener listener);

    public void removeMigrationEventListener(MigrationEventListener listener);

    public void setMigrationStrategy(int ttl, boolean updatingForwarder,
        int maxMigrationNb, int maxTimeOnSite);
}
