package modelisation.multiqueueserver;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.RequestQueueFactory;

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
public class CompositeQueueMetaObjectFactory extends ProActiveMetaObjectFactory {

  //
  // -- PRIVATE MEMBERS -----------------------------------------------
  //

  private static final MetaObjectFactory instance = new CompositeQueueMetaObjectFactory();

  //
  // -- CONSTRUCTORS -----------------------------------------------
  //

  /**
   * Constructor for TimedLocationServerMetaObjectFactory.
   */
  public CompositeQueueMetaObjectFactory() {
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

  protected RequestQueueFactory newRequestQueueFactorySingleton() {
    return new RequestQueueFactoryImpl();
  }


  //
  // -- INNER CLASSES -----------------------------------------------
  //

  protected static class RequestQueueFactoryImpl implements RequestQueueFactory, java.io.Serializable {
    public BlockingRequestQueue newRequestQueue(UniqueID ownerID) {
        return new CompositeRequestQueue(ownerID);
    }
  } // end inner class RequestQueueFactoryImpl


}