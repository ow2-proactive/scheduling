package modelisation.forwarder;

import modelisation.TimedMigrationManagerWithoutForwarder;
import modelisation.timedrequest.TimedFactory;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.rmi.ProActiveRmiMetaObjectFactory;
import org.objectweb.proactive.core.util.ThreadStore;
import org.objectweb.proactive.core.util.ThreadStoreFactory;
import org.objectweb.proactive.core.util.ThreadStoreImpl;

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
public class NoForwarderMetaObjectFactory extends ProActiveRmiMetaObjectFactory {

  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //

  private static final MetaObjectFactory instance = new NoForwarderMetaObjectFactory();

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Constructor for TimedLocationServerMetaObjectFactory.
   */
  public NoForwarderMetaObjectFactory() {
    super();
  }


  //
  // -- PUBLICS METHODS -----------------------------------------------
  //

  public static MetaObjectFactory newInstance() {
    return instance;
  }

  //
  // -- PROTECTED METHODS -----------------------------------------------
  //

  protected RequestFactory newRequestFactorySingleton() {
    return new TimedFactory();
  }

  protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
    return new MigrationManagerFactoryImpl();
  }

  protected ThreadStoreFactory newThreadStoreFactorySingleton() {
    return new ThreadStoreFactoryImpl();
  }

  //
  // -- INNER CLASSES -----------------------------------------------
  //

  protected static class MigrationManagerFactoryImpl implements MigrationManagerFactory, java.io.Serializable {
    public MigrationManager newMigrationManager() {
        return new TimedMigrationManagerWithoutForwarder();
    }
  } // end inner class MigrationManagerFactoryImpl


  protected static class ThreadStoreFactoryImpl implements ThreadStoreFactory, java.io.Serializable {
    public ThreadStore newThreadStore() {
        return new TimedThreadStoreImpl();
    }
  } // end inner class ThreadStoreFactoryImpl


  private static class TimedThreadStoreImpl extends ThreadStoreImpl implements java.io.Serializable {
      public synchronized void enter() {
          long startTime = System.currentTimeMillis();
          super.enter();
          System.out.println("TimedBody: waitTillAccept() waited " + (System.currentTimeMillis() - startTime));
      }

      public synchronized void close() {
          long startTime = System.currentTimeMillis();
          super.close();
          System.out.println("Barrier: close() lasted " + (System.currentTimeMillis() - startTime));
      }
  } // end inner class TimedThreadStoreImpl
  
}