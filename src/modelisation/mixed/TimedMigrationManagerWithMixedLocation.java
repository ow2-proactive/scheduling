package modelisation.mixed;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;
import org.objectweb.proactive.ext.mixedlocation.MigrationManagerWithMixedLocation;
import org.objectweb.proactive.ext.mixedlocation.UniversalBodyWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;


public class TimedMigrationManagerWithMixedLocation
    extends MigrationManagerWithMixedLocation implements Serializable {
    protected double ttl;
    protected int maxMigrations;
protected  Body myBody;
    //   public TimedMigrationManagerWithMixedLocation() {
    //      super();
    //   }
    public TimedMigrationManagerWithMixedLocation() {
    }

    public TimedMigrationManagerWithMixedLocation(
        LocationServer locationServer, double ttl, int maxMigrations) {
        this.migrationCounter = 0;
        this.locationServer = locationServer;
        this.ttl = ttl;
        this.maxMigrations = maxMigrations;
        System.out.println("LocationServer is " + locationServer);
        System.out.println("ttl  is " + ttl);
        System.out.println("maxMigration is " + maxMigrations);
    }

    protected synchronized void createWrapper(UniversalBody remoteBody) {
        if (this.wrapper == null) {
            System.out.println(
                "TimedMigrationManagerWithMixedLocation: wrapper created with ttl " +
                ttl);
            this.wrapper = new UniversalBodyWrapper(remoteBody, (long) ttl);
        }
    }

    public UniversalBody migrateTo(Node node, Body body)
        throws MigrationException {
        System.out.println(
            "TimedMigrationManagerWithServer: starting migration to " +
            node.getNodeInformation().getURL());
if (myBody == null) {
	this.myBody = body;
}
        long startTime = System.currentTimeMillis();
        UniversalBody remote = super.migrateTo(node, body);
        long endTime = System.currentTimeMillis();
        System.out.println("TimedMigrationManagerWithServer: Migration Time " +
            (endTime - startTime));
        return remote;
    }

//    public void updateLocation(Body body) {
//        //        System.out.println("MigrationManagerWithMixedLocation.updateLocation " +
//        //        locationServer);
//        System.out.println(" Migration Counter is " + (this.migrationCounter) +
//            " maxMigrations is " + this.maxMigrations + " should update " +
//            (this.migrationCounter % maxMigrations));
//        if ((this.migrationCounter % maxMigrations) == 0) {
//            if (locationServer == null) {
//                this.locationServer = LocationServerFactory.getLocationServer();
//            }
//            System.out.println(
//                "MigrationManagerWithMixedLocation.updateLocation");
//            locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
//        }
//    }
    
    public void updateLocation2(Body body) {
    	//        System.out.println("MigrationManagerWithMixedLocation.updateLocation " +
    	//        locationServer);
    	System.out.println(" Migration Counter is " + (this.migrationCounter) +
    			" maxMigrations is " + this.maxMigrations + " should update " +
    			(this.migrationCounter % maxMigrations));
    	if ((this.migrationCounter % maxMigrations) == 0) {
    		if (locationServer == null) {
    			this.locationServer = LocationServerFactory.getLocationServer();
    		}
    		System.out.println(
    		"MigrationManagerWithMixedLocation.updateLocation");
    		locationServer.updateLocation(body.getID(), body.getRemoteAdapter());
    	}
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.updateLocation2(myBody);
        //	this.updateLocation();
    }
}
