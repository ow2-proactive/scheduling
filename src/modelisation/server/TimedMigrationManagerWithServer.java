package modelisation.server;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.MigrationManagerWithLocationServer;


public class TimedMigrationManagerWithServer extends MigrationManagerWithLocationServer {

    public TimedMigrationManagerWithServer() {
    }

    public TimedMigrationManagerWithServer(LocationServer l) {
        super(l);
    }


    public UniversalBody migrateTo(Node node, Body body) throws MigrationException {
        System.out.println("TimedMigrationManagerWithServer: starting migration to " + node.getNodeInformation().getURL());
        long startTime = System.currentTimeMillis();
        UniversalBody remote = super.migrateTo(node, body);
        long endTime = System.currentTimeMillis();
        System.out.println("TimedMigrationManagerWithServer: Migration Time " + (endTime - startTime));
        return remote;
    }
}
