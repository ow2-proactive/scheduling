package modelisation.server;

import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

/**
 * <p>
 * This class overrides the default Factory to provide Request and MigrationManager
 * with location server and timing.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public class TimedLocationServerMetaObjectFactory extends ProActiveMetaObjectFactory {

  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //

  private static MetaObjectFactory instance = null; 

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Constructor for TimedLocationServerMetaObjectFactory.
   */
  public TimedLocationServerMetaObjectFactory() {
    super();
  }


  //
  // -- PUBLICS METHODS -----------------------------------------------
  //

  public synchronized static MetaObjectFactory newInstance() {
  	if (instance == null) {
  		instance = new TimedLocationServerMetaObjectFactory();
  	}
    return instance;
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected RequestFactory newRequestFactorySingleton() {
    return new TimedRequestWithLocationServerFactory();
  }

  protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
    return new MigrationManagerFactoryImpl();
  }

  //
  // -- INNER CLASSES -----------------------------------------------
  //

  protected class TimedRequestWithLocationServerFactory implements RequestFactory, java.io.Serializable {
    private LocationServer server = LocationServerFactory.getLocationServer();
    public Request newRequest(MethodCall methodCall, UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
      return new TimedRequestWithLocationServer(methodCall, sourceBody, isOneWay, sequenceID, server);
    }
  } // end inner class TimedRequestWithLocationServerFactory



  protected static class MigrationManagerFactoryImpl implements MigrationManagerFactory, java.io.Serializable {
    public MigrationManager newMigrationManager() {
    	System.out.println("MigrationManagerFactoryImpl: Creating TimedMigrationManagerWithServer");
        return new TimedMigrationManagerWithServer(LocationServerFactory.getLocationServer());
    }
  } // end inner class MigrationManagerFactoryImpl

}