package modelisation.mixed;

import modelisation.server.TimedRequestWithLocationServer;

import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;
import org.objectweb.proactive.ext.mixedlocation.MixedLocationMetaObjectFactory;


public class TimedMixedMetaObjectFactory extends MixedLocationMetaObjectFactory {
    private static MetaObjectFactory instance;

    public TimedMixedMetaObjectFactory() {
        super();
        System.out.println("TimedMetaObjectFactory <init>");
    }

    protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
        System.out.println("XXX");
        return new TimedMigrationManagerFactoryImpl();
        //return super.newMigrationManagerFactorySingleton();
    }

    protected RequestFactory newRequestFactorySingleton() {
        return new TimedRequestWithLocationServerFactory();
    }

    public static synchronized MetaObjectFactory newInstance() {
        if (instance == null) {
            instance = new TimedMixedMetaObjectFactory();
        }
        return instance;
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    protected class TimedRequestWithLocationServerFactory
        implements RequestFactory, java.io.Serializable {
        transient private LocationServer server = LocationServerFactory.getLocationServer();

        public Request newRequest(MethodCall methodCall,
            UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            return new TimedRequestWithLocationServer(methodCall, sourceBody,
                isOneWay, sequenceID, server);
        }
    }
     // end inner class TimedRequestWithLocationServerFactory

    protected static class TimedMigrationManagerFactoryImpl
        implements MigrationManagerFactory, java.io.Serializable {
        public TimedMigrationManagerFactoryImpl() {
            System.out.println("TimedMigrationManagerFactoryImpl");
        }

        public MigrationManager newMigrationManager() {
            System.out.println(
                "TimedMixedMetaObjectFactory creating migrationManager");
            System.out.println("TimedMixedMetaObjectFactory ttl = " + System.getProperty("modelisation.ttl"));
            System.out.println("TimedMixedMetaObjectFactory ttu = " + System.getProperty("modelisation.maxMigrations"));
            return new TimedMigrationManagerWithMixedLocation(LocationServerFactory.getLocationServer(),
                Double.parseDouble(System.getProperty("modelisation.ttl")),
                Integer.parseInt(System.getProperty(
                        "modelisation.maxMigrations")));
        }
    }
     // end inner class MigrationManagerFactoryImpl
}
