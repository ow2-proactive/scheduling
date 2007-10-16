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

import java.util.Timer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverForwarder;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverForwarder;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.event.AbstractEventProducer;
import org.objectweb.proactive.core.event.MigrationEvent;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.event.ProActiveEvent;
import org.objectweb.proactive.core.event.ProActiveListener;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;


public class MigrationManagerImpl extends AbstractEventProducer
    implements MigrationManager, java.io.Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    transient protected LocationServer locationServer;
    protected int nbOfMigrationWithoutUpdate; // used to compare with maxMigrationNb
    protected int migrationCounter; // used to set the version for the update
    public static final int INFINITE_TTL = -1;
    public static final int INFINITE_MAX_MIGRATION_NB = -1;
    public static final int INFINITE_MAX_TIME_ON_SITE = -1;

    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    private int ttl;
    private boolean updatingForwarder;
    private int maxMigrationNb;
    private int maxTimeOnSite;
    private transient Timer maxTimeOnSiteTimer;
    private transient Timer ttlTimer;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MigrationManagerImpl() {
        super(true);

        if (PAProperties.PA_MIXEDLOCATION_TTL.isSet()) {
            this.ttl = Integer.valueOf(PAProperties.PA_MIXEDLOCATION_TTL.getValue())
                              .intValue();
        } else {
            this.ttl = INFINITE_TTL;
        }

        this.updatingForwarder = PAProperties.PA_MIXEDLOCATION_UPDATINGFORWARDER.isTrue();
        if (PAProperties.PA_MIXEDLOCATION_MAXMIGRATIONNB.isSet()) {
            this.maxMigrationNb = Integer.valueOf(PAProperties.PA_MIXEDLOCATION_MAXMIGRATIONNB.getValue())
                                         .intValue();
        } else {
            this.maxMigrationNb = INFINITE_MAX_MIGRATION_NB;
        }

        if (PAProperties.PA_MIXEDLOCATION_MAXTIMEONSITE.isSet()) {
            this.maxTimeOnSite = Integer.valueOf(PAProperties.PA_MIXEDLOCATION_MAXTIMEONSITE.getValue())
                                        .intValue();
        } else {
            this.maxTimeOnSite = INFINITE_MAX_TIME_ON_SITE;
        }

        this.nbOfMigrationWithoutUpdate = 0;
        this.migrationCounter = 0;
    }

    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void updateLocation(UniversalBody body) {
        if (this.locationServer == null) {
            this.locationServer = LocationServerFactory.getLocationServer();
        }
        if (this.locationServer != null) {
            this.locationServer.updateLocation(body.getID(),
                body.getRemoteAdapter(), this.migrationCounter);
        }
        resetNbOfMigrationWithoutUpdate();
    }

    public void resetNbOfMigrationWithoutUpdate() {
        this.nbOfMigrationWithoutUpdate = 0;
    }

    public void launchTimeToLive(MigratableBody body, UniversalBody migratedBody) {
        if (this.ttl != INFINITE_TTL) {
            this.ttlTimer = new Timer();
            this.ttlTimer.schedule(new TimeToLiveTimerTask(this, body,
                    migratedBody), this.ttl);
        }
    }

    //
    // -- Implements MigrationManager
    // -----------------------------------------------
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
        // ProActiveEvent
        if (hasListeners()) {
            notifyAllListeners(new MigrationEvent(body,
                    MigrationEvent.BEFORE_MIGRATION));
        }

        // END ProActiveEvent

        // JMX Notification
        BodyWrapperMBean mbean = body.getMBean();
        if (mbean != null) {
            mbean.sendNotification(NotificationType.migrationAboutToStart,
                node.getProActiveRuntime().getURL());
        }

        // End JMX Notification
        try {
            long l1 = 0;
            if (logger.isDebugEnabled()) {
                l1 = System.currentTimeMillis();
            }

            //
            // UniversalBody remoteBody = node.receiveBody(body);
            // --------------------added lines---------------------------
            ProActiveRuntime part = node.getProActiveRuntime();
            UniversalBody remoteBody = part.receiveBody(node.getNodeInformation()
                                                            .getName(), body);

            if (logger.isDebugEnabled()) {
                logger.debug("runtime = " + part);
                logger.debug("remoteBody = " + remoteBody);
            }

            // --------------------added lines--------------------------
            // activityStopped();
            //
            long l2 = 0;
            if (logger.isDebugEnabled()) {
                l2 = System.currentTimeMillis();
                logger.debug("Migration took " + (l2 - l1));
            }

            // ProActiveEvent
            if (hasListeners()) {
                notifyAllListeners(new MigrationEvent(body,
                        MigrationEvent.AFTER_MIGRATION));
            }

            // END ProActiveEvent

            // JMX Notification
            if (mbean != null) {
                mbean.sendNotification(NotificationType.migrationFinished,
                    node.getProActiveRuntime().getURL());
            }

            // End JMX Notification

            // we are not on this site anymore,
            // so there is no need to send this
            // position to the server
            if (this.maxTimeOnSiteTimer != null) {
                this.maxTimeOnSiteTimer.cancel();
            }

            return remoteBody;
            //} catch (ProActiveException e) {
        } catch (Exception e) {
            e.printStackTrace();
            MigrationException me = new MigrationException("Exception while sending the Object",
                    e.getCause());

            // ProActiveEvent
            if (hasListeners()) {
                notifyAllListeners(new MigrationEvent(me));
            }

            // END ProActiveEvent

            // JMX Notification
            if (mbean != null) {
                mbean.sendNotification(NotificationType.migrationExceptionThrown,
                    me);
            }

            // END JMX Notification
            throw me;
        }
    }

    /**
     * Called by the MigratableBody after a succeded migration
     * it changes the body into a forwarder or terminate it
     * if we don't forwarders
     */
    public void changeBodyAfterMigration(MigratableBody body,
        UniversalBody migratedBody) {
        if (this.ttl == 0) {
            // we don't need forwarders so we don't create them
            // the body is dead now
            body.terminate();
        } else {
            body.setRequestReceiver(createRequestReceiver(migratedBody,
                    body.getRequestReceiver()));
            body.setReplyReceiver(createReplyReceiver(migratedBody,
                    body.getReplyReceiver()));

            body.setHasMigrated();

            LocalBodyStore.getInstance().registerForwarder(body);

            // the migration has succeeded so
            // we have to launch the TTL
            // of the forwarder
            launchTimeToLive(body, migratedBody);
        }
    }

    public void startingAfterMigration(Body body) {
        // ProActiveEvent
        if (hasListeners()) {
            notifyAllListeners(new MigrationEvent(body,
                    MigrationEvent.RESTARTING_AFTER_MIGRATING));
        }

        // END ProActiveEvent

        // JMX Notification
        BodyWrapperMBean mbean = body.getMBean();
        if (mbean != null) {
            mbean.sendNotification(NotificationType.migratedBodyRestarted);
        }
        // END JMX Notification
        this.nbOfMigrationWithoutUpdate++;
        this.migrationCounter++;
        if (logger.isDebugEnabled()) {
            logger.debug("XXX counter == " + this.nbOfMigrationWithoutUpdate);
        }

        // TTU : maxMigrationNb
        if ((this.maxMigrationNb != INFINITE_MAX_MIGRATION_NB) &&
                (this.nbOfMigrationWithoutUpdate >= this.maxMigrationNb)) {
            updateLocation(body);
        }
        // TTU : maxTimeOnSite
        else if (this.maxTimeOnSite != INFINITE_MAX_TIME_ON_SITE) {
            this.maxTimeOnSiteTimer = new Timer();
            this.maxTimeOnSiteTimer.schedule(new MaxTimeOnSiteTimerTask(this,
                    body), this.maxTimeOnSite);
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

    public void setMigrationStrategy(int ttl, boolean updatingForwarder,
        int maxMigrationNb, int maxTimeOnSite) {
        this.ttl = ttl;
        this.updatingForwarder = updatingForwarder;
        this.maxMigrationNb = maxMigrationNb;
        this.maxTimeOnSite = maxTimeOnSite;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    @Override
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

    // -- INNER CLASSES -----------------------------------------------
    //
    protected class MaxTimeOnSiteTimerTask extends java.util.TimerTask {
        protected MigrationManagerImpl migrationManager;
        protected Body body;

        public MaxTimeOnSiteTimerTask(MigrationManagerImpl migrationManager,
            Body body) {
            this.migrationManager = migrationManager;
            this.body = body;
        }

        @Override
        public void run() {
            this.body.enterInThreadStore(); // make sure that the body isn't
                                            // trying to migrate

            if ((this.body instanceof Migratable) &&
                    !((Migratable) this.body).hasJustMigrated()) {
                this.migrationManager.updateLocation(this.body);
            }
            this.body.exitFromThreadStore();
        }
    }

    protected class TimeToLiveTimerTask extends java.util.TimerTask {
        protected MigrationManagerImpl migrationManager;
        protected MigratableBody body;
        protected UniversalBody migratedBody;
        private long creationTime;

        public TimeToLiveTimerTask(MigrationManagerImpl migrationManager,
            MigratableBody body, UniversalBody migratedBody) {
            this.migrationManager = migrationManager;
            this.body = body;
            this.migratedBody = migratedBody;
            this.creationTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if (this.migrationManager.updatingForwarder) {
                this.migrationManager.updateLocation(this.migratedBody.getRemoteAdapter());
            }
            //this.migrationManager.updateRemoteLocation(this.migratedBody.getRemoteAdapter());
            LocalBodyStore.getInstance().unregisterForwarder(this.body);

            this.body.terminate();
            this.body.setRequestReceiver(null);
            this.body.setReplyReceiver(null);
        }
    }
}
