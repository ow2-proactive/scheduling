package org.objectweb.proactive.ext.mixedlocation;

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManagerImpl;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverForwarder;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverForwarder;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class MigrationManagerWithMixedLocation extends MigrationManagerImpl
    implements java.io.Serializable {
    static Logger logger = Logger.getLogger(MigrationManagerWithMixedLocation.class.getName());
    protected UniversalBodyWrapper wrapper;
    transient protected LocationServer locationServer;
    protected int migrationCounter;

    public MigrationManagerWithMixedLocation() {
        logger.info("<init> LocationServer is " + locationServer);
    }

    public MigrationManagerWithMixedLocation(LocationServer locationServer) {
        this.migrationCounter = 0;
        if (logger.isDebugEnabled()) {
            logger.debug("LocationServer is " + locationServer);
        }
        this.locationServer = locationServer;
    }

    protected synchronized void createWrapper(UniversalBody remoteBody) {
        if (this.wrapper == null) {
            this.wrapper = new UniversalBodyWrapper(remoteBody, 6000);
        }
    }

    public RequestReceiver createRequestReceiver(UniversalBody remoteBody,
        RequestReceiver currentRequestReceiver) {
        this.createWrapper(remoteBody);
        return new RequestReceiverForwarder(wrapper);
    }

    public ReplyReceiver createReplyReceiver(UniversalBody remoteBody,
        ReplyReceiver currentReplyReceiver) {
        this.createWrapper(wrapper);
        return new ReplyReceiverForwarder(wrapper);
    }

    public void updateLocation(Body body) {
        if (locationServer == null) {
            this.locationServer = LocationServerFactory.getLocationServer();
        }
        if (locationServer != null) {
            locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
        }
    }

    public void startingAfterMigration(Body body) {
        super.startingAfterMigration(body);
        //we update our location
        this.migrationCounter++;
        if (logger.isDebugEnabled()) {
            logger.debug("XXX counter == " + this.migrationCounter);
        }

        //          if (this.migrationCounter > 3) {
        updateLocation(body);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("MigrationManagerWithMixedLocation readObject XXXXXXX");
        }
        in.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("MigrationManagerWithMixedLocation writeObject YYYYYY");
        }
        this.locationServer = null;
        out.defaultWriteObject();
    }
}
