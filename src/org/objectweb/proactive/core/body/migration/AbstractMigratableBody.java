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

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.event.MigrationEventListener;
import org.objectweb.proactive.core.node.Node;

public abstract class AbstractMigratableBody extends AbstractBody implements Migratable, java.io.Serializable {

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** The object responsible for the migration */
    protected MigrationManager migrationManager;

    /** signal that the body has just migrated */
    private transient boolean hasJustMigrated;


    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public AbstractMigratableBody() {
    }

    public AbstractMigratableBody(Object reifiedObject, String nodeURL) {
        super(reifiedObject, nodeURL);
        this.migrationManager = createMigrationManager();
    }



    //
    // -- PUBLIC METHODS -----------------------------------------------
    //


    //
    // -- implements Migratable -----------------------------------------------
    //

    public void migrateTo(Node node) throws MigrationException {
        UniversalBody remoteBody = internalMigrateTo(node, false);
    }


    public UniversalBody cloneTo(Node node) throws MigrationException {
        UniversalBody remoteBody = internalMigrateTo(node, true);
        return remoteBody;
    }


    public void addMigrationEventListener(MigrationEventListener listener) {
        migrationManager.addMigrationEventListener(listener);
    }


    public void removeMigrationEventListener(MigrationEventListener listener) {
        migrationManager.removeMigrationEventListener(listener);
    }



    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Whenever an exception occurs during the migration
     * this method will leave the Body in a safe state
     * ready to continue its normal service
     */
    protected void cancelMigration() {
        futures.unsetMigrationTag();
    }


    /**
     * Creates the component in charge of migration.
     * @return the component in charge of migration.
     */
    protected abstract MigrationManager createMigrationManager();


    /**
     * Signals that the activity of this body, managed by the active thread has just started.
     */
    protected void activityStarted() {
        super.activityStarted();
        //System.out.println("Body run on node "+nodeURL+" migration="+hasJustMigrated);
        if (hasJustMigrated) {
            if (migrationManager != null) {
                migrationManager.startingAfterMigration(this);
            }
            hasJustMigrated = false;
        }
    }



    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    protected UniversalBody internalMigrateTo(Node node, boolean byCopy) throws MigrationException {
        if (!isAlive()) throw new MigrationException("The body has been terminated");
        // check node with Manager
        node = migrationManager.checkNode(node);

        // get the name of the node
        String saveNodeURL = nodeURL;
        nodeURL = node.getNodeInformation().getURL();
        // save the id
        UniqueID savedID = bodyID;
        if (byCopy) {
            // if moving by copy we have to create a new unique ID
            // the bodyID will be automatically recreate when deserialized
            bodyID = null;
        }

        // stop accepting communication
        blockCommunication();

        // try to migrate
        UniversalBody migratedBody = null;
        try {
            migratedBody = migrationManager.migrateTo(node, this);
        } catch (MigrationException e) {
            nodeURL = saveNodeURL;
            bodyID = savedID;
            cancelMigration();
            acceptCommunication();
            throw e;
        }
        if (!byCopy) {
            // cleanup after migration
            activityStopped();
            reifiedObject = new Object();
            requestReceiver = migrationManager.createRequestReceiver(migratedBody, requestReceiver);
            replyReceiver = migrationManager.createReplyReceiver(migratedBody, replyReceiver);

            //replySender = null;
            //requestSender = null;
            //migrationManager = null;

        }
        acceptCommunication();
        return migratedBody;
    }


    //
    // -- SERIALIZATION METHODS -----------------------------------------------
    //

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }


    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        hasJustMigrated = true;
    }
}
