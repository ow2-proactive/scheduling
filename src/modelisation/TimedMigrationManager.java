package modelisation;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.migration.MigrationManagerImpl;
import org.objectweb.proactive.core.node.Node;


public class TimedMigrationManager extends MigrationManagerImpl {
    public TimedMigrationManager() {
    }

    public UniversalBody migrateTo(Node node, Body body)
        throws MigrationException {
        System.out.println(System.currentTimeMillis() +
            " TimedMigrationManager: Starting migration ");
        System.out.println(" Total Memory " +
            Runtime.getRuntime().totalMemory() + " Free Memory " +
            Runtime.getRuntime().freeMemory());

        long startTime = System.currentTimeMillis();
        UniversalBody remote = super.migrateTo(node, body);
        long endTime = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis() +
            " TimedMigrationManager: length of the migration " +
            (endTime - startTime));
        return remote;
    }
}
