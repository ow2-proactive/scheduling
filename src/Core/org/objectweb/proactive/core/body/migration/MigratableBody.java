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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.security.InternalBodySecurity;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.crypto.AuthenticationException;
import org.objectweb.proactive.core.security.crypto.Session;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MigratableBody extends BodyImpl implements Migratable,
    java.io.Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -4176648945308840505L;
    protected static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);
    protected static Logger migrationLogger = ProActiveLogger.getLogger(Loggers.MIGRATION);

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** The object responsible for the migration */
    protected MigrationManager migrationManager;

    /** signal that the body has just migrated */
    protected transient boolean hasJustMigrated;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public MigratableBody() {
    }

    public MigratableBody(Object reifiedObject, String nodeURL,
        MetaObjectFactory factory, String jobID)
        throws ActiveObjectCreationException {
        super(reifiedObject, nodeURL, factory, jobID);
        this.migrationManager = factory.newMigrationManagerFactory()
                                       .newMigrationManager();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    //
    // -- implements Migratable -----------------------------------------------
    //
    public boolean hasJustMigrated() {
        return this.hasJustMigrated;
    }

    public UniversalBody migrateTo(Node node) throws MigrationException {
        return internalMigrateTo(node, false);
    }

    public UniversalBody cloneTo(Node node) throws MigrationException {
        return internalMigrateTo(node, true);
    }

    public void addMigrationEventListener(MigrationEventListener listener) {
        if (migrationManager != null) {
            migrationManager.addMigrationEventListener(listener);
        }
    }

    public void removeMigrationEventListener(MigrationEventListener listener) {
        if (migrationManager != null) {
            migrationManager.removeMigrationEventListener(listener);
        }
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Signals that the activity of this body, managed by the active thread has just started.
     */
    @Override
    protected void activityStarted() {
        super.activityStarted();

        if (migrationLogger.isDebugEnabled()) {
            migrationLogger.debug("Body run on node " + nodeURL +
                " migration=" + hasJustMigrated);
        }
        if (bodyLogger.isDebugEnabled()) {
            bodyLogger.debug("Body run on node " + nodeURL + " migration=" +
                hasJustMigrated);
        }
        if (hasJustMigrated) {
            if (migrationManager != null) {
                migrationManager.startingAfterMigration(this);
            }
            hasJustMigrated = false;
        }
    }

    protected void setRequestReceiver(RequestReceiver requestReceiver) {
        this.requestReceiver = requestReceiver;
    }

    protected void setReplyReceiver(ReplyReceiver replyReceiver) {
        this.replyReceiver = replyReceiver;
    }

    protected void setHasMigrated() {
        this.hasJustMigrated = true;
    }

    protected RequestReceiver getRequestReceiver() {
        return this.requestReceiver;
    }

    protected ReplyReceiver getReplyReceiver() {
        return this.replyReceiver;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private UniversalBody internalMigrateTo(Node node, boolean byCopy)
        throws MigrationException {
        UniqueID savedID = null;
        UniversalBody migratedBody = null;

        if (!isAlive()) {
            throw new MigrationException(
                "Attempt to migrate a dead body that has been terminated");
        }

        if (!isActive()) {
            throw new MigrationException("Attempt to migrate a non active body");
        }

        try {
            // check node with Manager
            node = migrationManager.checkNode(node);
        } catch (MigrationException me) {
            // JMX Notification
            if (mbean != null) {
                mbean.sendNotification(NotificationType.migrationExceptionThrown,
                    me);
            }

            // End JMX Notification
            throw me;
        }

        // get the name of the node
        String saveNodeURL = nodeURL;
        nodeURL = node.getNodeInformation().getURL();

        //      security checks
        try {
            ProActiveRuntime runtimeDestination = node.getProActiveRuntime();

            if (this.securityManager != null) {
                Session session = this.securityManager.initiateSession(runtimeDestination);

                if (!session.getSecurityContext().isMigration()) {
                    ProActiveLogger.getLogger(Loggers.SECURITY)
                                   .info("NOTE : Security manager forbids the migration");
                    return this;
                }
            } else {
                //  			no local security but need to check if distant runtime accepts migration
                SecurityContext scDistant = runtimeDestination.getPolicy(this.getEntities(),
                        runtimeDestination.getEntities());
                if (!scDistant.isMigration()) {
                    ProActiveLogger.getLogger(Loggers.SECURITY)
                                   .info("NOTE : Security manager forbids the migration");
                    return this;
                }
            }
        } catch (SecurityNotAvailableException e1) {
            bodyLogger.debug("Security not available");
        } catch (CommunicationForbiddenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            nodeURL = node.getNodeInformation().getURL();

            // stop accepting communication
            blockCommunication();

            // save the id
            savedID = bodyID;
            if (byCopy) {
                // if moving by copy we have to create a new unique ID
                // the bodyID will be automatically recreate when deserialized
                bodyID = null;
            }

            // security
            // save opened sessions
            if (this.isSecurityOn) {
                openedSessions = securityManager.getOpenedConnexion();
            }

            // Set copyMode tag in all futures
            // those futures are going to be serialized for migration (i.e. no AC registration)
            this.getFuturePool().setCopyMode(true);

            // try to migrate
            migratedBody = migrationManager.migrateTo(node, this);

            if (isSecurityOn) {
                this.internalBodySecurity.setDistantBody(migratedBody);
            }

            // ****************************************************************
            // Javier dixit: This is the moment to update the location on the FT Manager
            // ****************************************************************
            if (this.ftmanager != null) {
                this.ftmanager.updateLocationAtServer(savedID, migratedBody);
            }
        } catch (MigrationException e) {
            openedSessions = null;
            nodeURL = saveNodeURL;
            bodyID = savedID;
            localBodyStrategy.getFuturePool().setCopyMode(false);
            if (this.isSecurityOn) {
                this.internalBodySecurity.setDistantBody(null);
            }
            acceptCommunication();
            throw e;
        }

        if (!byCopy) {
            this.migrationManager.changeBodyAfterMigration(this, migratedBody);
            activityStopped(false);
        } else {
            bodyID = savedID;
            nodeURL = saveNodeURL;
        }
        acceptCommunication();

        return migratedBody;
    }

    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        if (migrationLogger.isDebugEnabled()) {
            migrationLogger.debug("stream =  " + out);
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
        if (migrationLogger.isDebugEnabled()) {
            migrationLogger.debug("stream =  " + in);
        }
        in.defaultReadObject();
        hasJustMigrated = true;
        if (this.isSecurityOn) {
            internalBodySecurity = new InternalBodySecurity(null);
            //            securityManager.setBody(this);
        }
    }

    /*
     * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getNextSequenceID()
     */
    public long getNextSequenceID() {
        return localBodyStrategy.getNextSequenceID();
    }
}
