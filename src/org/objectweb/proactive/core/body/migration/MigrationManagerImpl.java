/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.body.migration;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverForwarder;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverForwarder;
import org.objectweb.proactive.core.event.AbstractEventProducer;
import org.objectweb.proactive.core.event.MigrationEvent;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.event.ProActiveEvent;
import org.objectweb.proactive.core.event.ProActiveListener;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MigrationManagerImpl extends AbstractEventProducer
    implements MigrationManager, java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MigrationManagerImpl() {
        super(true);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements MigrationManager -----------------------------------------------
    //
    public Node checkNode(Node node) throws MigrationException {
        if (node == null) {
            throw new MigrationException(
                "The RemoteNodeImpl could not be found");
        }

        // check if the node is remote
        if (NodeFactory.isNodeLocal(node)) {
            MigrationException me = new MigrationException("The given node " +
                    node.getNodeInformation().getURL() +
                    " is in the same virtual machine");
            if (hasListeners()) {
                notifyAllListeners(new MigrationEvent(me));
            }
            throw me;
        }
        return node;
    }

    public UniversalBody migrateTo(Node node, Body body)
        throws MigrationException {
        if (hasListeners()) {
            notifyAllListeners(new MigrationEvent(body,
                    MigrationEvent.BEFORE_MIGRATION));
        }
        try {
            long l1 = 0;
            if (logger.isDebugEnabled()) {
                l1 = System.currentTimeMillis();
            }

            //      
            //UniversalBody remoteBody = node.receiveBody(body);
            //--------------------added lines---------------------------
            ProActiveRuntime part = node.getProActiveRuntime();
            UniversalBody remoteBody = part.receiveBody(node.getNodeInformation()
                                                            .getName(), body);

            if (logger.isDebugEnabled()) {
                logger.debug("runtime = " + part);
                logger.debug("remoteBody = " + remoteBody);
            }

            //--------------------added lines--------------------------
            //activityStopped();
            //    
            long l2 = 0;
            if (logger.isDebugEnabled()) {
                l2 = System.currentTimeMillis();
                logger.debug("Migration took " + (l2 - l1));
            }
            if (hasListeners()) {
                notifyAllListeners(new MigrationEvent(body,
                        MigrationEvent.AFTER_MIGRATION));
            }
            return remoteBody;
        } catch (ProActiveException e) {
            MigrationException me = new MigrationException("Exception while sending the Object",
                    e.getCause());
            if (hasListeners()) {
                notifyAllListeners(new MigrationEvent(me));
            }
            throw me;
        }
    }

    public void startingAfterMigration(Body body) {
        if (hasListeners()) {
            notifyAllListeners(new MigrationEvent(body,
                    MigrationEvent.RESTARTING_AFTER_MIGRATING));
        }
    }

    public RequestReceiver createRequestReceiver(UniversalBody remoteBody,
        RequestReceiver currentRequestReceiver) {
        return new RequestReceiverForwarder(remoteBody);
    }

    public ReplyReceiver createReplyReceiver(UniversalBody remoteBody,
        ReplyReceiver currentReplyReceiver) {
        return new ReplyReceiverForwarder(remoteBody);
    }

    public void addMigrationEventListener(MigrationEventListener listener) {
        addListener(listener);
    }

    public void removeMigrationEventListener(MigrationEventListener listener) {
        removeListener(listener);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void notifyOneListener(ProActiveListener listener,
        ProActiveEvent event) {
        MigrationEvent migrationEvent = (MigrationEvent) event;
        MigrationEventListener migrationEventListener = (MigrationEventListener) listener;
        switch (event.getType()) {
        case MigrationEvent.BEFORE_MIGRATION:
            migrationEventListener.migrationAboutToStart(migrationEvent);
            break;
        case MigrationEvent.AFTER_MIGRATION:
            migrationEventListener.migrationFinished(migrationEvent);
            break;
        case MigrationEvent.MIGRATION_EXCEPTION:
            migrationEventListener.migrationExceptionThrown(migrationEvent);
            break;
        case MigrationEvent.RESTARTING_AFTER_MIGRATING:
            migrationEventListener.migratedBodyRestarted(migrationEvent);
            break;
        }
    }
}
