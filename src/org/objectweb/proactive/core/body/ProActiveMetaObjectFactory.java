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
package org.objectweb.proactive.core.body;


/**
 * THIS JAVADOC SHOULD BE REWRITTEN
 * <p>
 * This class provides singleton instances of all default factories
 * creating MetaObjects used in the Body.
 * </p>
 *
 * <b>Since version 1.8, it is also possible to parameterized the factories on a per-object basis. </b>
 * In that case,  public ProActiveMetaObjectFactory(Hashtable parameters) is the constructor to use.
 * <p>
 * One can inherit from this class in order to provide custom implementation
 * of one or several factories. This class provide a default implementation that
 * makes the factories a singleton. One instance of each mata object factory is
 * created when this object is built and the same instance is returned each time
 * somebody ask for an instance.
 * </p>
 * <p>
 * In order to change one meta object factory following that singleton pattern,
 * only the protected method <code>newXXXSingleton</code> has to be overwritten.
 * The method <code>newXXXSingleton</code> is guarantee to be called only once at
 * construction time of this object.
 * </p>
 * <p>
 * In order to change one meta object factory that does not follow the singleton
 * pattern, the public method <code>newXXX</code> has to be overwritten in order
 * to return a new instance of the factory each time. The default implementation
 * of each <code>newXXX</code> method if to return the singleton instance of the
 * factory created from <code>newXXXSingleton</code> method call.
 * </p>
 * <p>
 * Each sub class of this class should be implemented as a singleton and provide
 * a static method <code>newInstance</code> for this purpose.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationManager;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.reply.ReplyReceiverFactory;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestQueueFactory;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.body.request.RequestReceiverFactory;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.identity.ProActiveComponentFactory;
import org.objectweb.proactive.core.component.identity.ProActiveComponentImpl;
import org.objectweb.proactive.core.component.request.ComponentRequestQueueImpl;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.util.ThreadStore;
import org.objectweb.proactive.core.util.ThreadStoreFactory;
import org.objectweb.proactive.ext.security.ProActiveSecurityManager;


public class ProActiveMetaObjectFactory implements MetaObjectFactory,
    java.io.Serializable {
    public static final String COMPONENT_PARAMETERS_KEY = "component-parameters";
    protected static Logger logger = Logger.getLogger(ProActiveMetaObjectFactory.class.getName());

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    // private static final MetaObjectFactory instance = new ProActiveMetaObjectFactory();
    private static MetaObjectFactory instance = new ProActiveMetaObjectFactory();
    public Hashtable parameters;

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //
    protected RequestFactory requestFactoryInstance;
    protected ReplyReceiverFactory replyReceiverFactoryInstance;
    protected RequestReceiverFactory requestReceiverFactoryInstance;
    protected RequestQueueFactory requestQueueFactoryInstance;
    protected MigrationManagerFactory migrationManagerFactoryInstance;
    protected RemoteBodyFactory remoteBodyFactoryInstance;
    protected ThreadStoreFactory threadStoreFactoryInstance;
    protected ProActiveComponentFactory componentFactoryInstance;
	protected ProActiveSecurityManager proActiveSecurityManager;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    protected ProActiveMetaObjectFactory() {
        requestFactoryInstance = newRequestFactorySingleton();
        replyReceiverFactoryInstance = newReplyReceiverFactorySingleton();
        requestReceiverFactoryInstance = newRequestReceiverFactorySingleton();
        requestQueueFactoryInstance = newRequestQueueFactorySingleton();
        migrationManagerFactoryInstance = newMigrationManagerFactorySingleton();
        remoteBodyFactoryInstance = newRemoteBodyFactorySingleton();
        threadStoreFactoryInstance = newThreadStoreFactorySingleton();
    }

    /**
     * Constructor with parameters
     * It is used for per-active-object configurations of ProActive factories
     * @param parameters the parameters of the factories; these parameters can be of any type
     */
    public ProActiveMetaObjectFactory(Hashtable parameters) {
        this.parameters = parameters;
        if (parameters.containsKey(COMPONENT_PARAMETERS_KEY)) {
            ComponentParameters initialComponentParameters = (ComponentParameters) parameters.get(COMPONENT_PARAMETERS_KEY);
            componentFactoryInstance = newComponentFactorySingleton(initialComponentParameters);
            requestFactoryInstance = newRequestFactorySingleton();
            replyReceiverFactoryInstance = newReplyReceiverFactorySingleton();
            requestReceiverFactoryInstance = newRequestReceiverFactorySingleton();
            requestQueueFactoryInstance = newRequestQueueFactorySingleton();
            migrationManagerFactoryInstance = newMigrationManagerFactorySingleton();
            remoteBodyFactoryInstance = newRemoteBodyFactorySingleton();
            threadStoreFactoryInstance = newThreadStoreFactorySingleton();
        }
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public static MetaObjectFactory newInstance() {
        return instance;
    }

    public static void setNewInstance(MetaObjectFactory mo) {
        instance = mo;
    }

    /**
     * getter for the parameters of the factory (per-active-object config)
     * @return the parameters of the factory
     */
    public Hashtable getParameters() {
        return parameters;
    }

    //
    // -- implements MetaObjectFactory -----------------------------------------------
    //
    public RequestFactory newRequestFactory() {
        return requestFactoryInstance;
    }

    public ReplyReceiverFactory newReplyReceiverFactory() {
        return replyReceiverFactoryInstance;
    }

    public RequestReceiverFactory newRequestReceiverFactory() {
        return requestReceiverFactoryInstance;
    }

    public RequestQueueFactory newRequestQueueFactory() {
        return requestQueueFactoryInstance;
    }

    public MigrationManagerFactory newMigrationManagerFactory() {
        return migrationManagerFactoryInstance;
    }

    public RemoteBodyFactory newRemoteBodyFactory() {
        return remoteBodyFactoryInstance;
    }

    public ThreadStoreFactory newThreadStoreFactory() {
        return threadStoreFactoryInstance;
    }

    public ProActiveComponentFactory newComponentFactory() {
        return componentFactoryInstance;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected RequestFactory newRequestFactorySingleton() {
        return new RequestFactoryImpl();
    }

    protected ReplyReceiverFactory newReplyReceiverFactorySingleton() {
        return new ReplyReceiverFactoryImpl();
    }

    protected RequestReceiverFactory newRequestReceiverFactorySingleton() {
        return new RequestReceiverFactoryImpl();
    }

    protected RequestQueueFactory newRequestQueueFactorySingleton() {
        return new RequestQueueFactoryImpl();
    }

    protected MigrationManagerFactory newMigrationManagerFactorySingleton() {
        return new MigrationManagerFactoryImpl();
    }

    protected RemoteBodyFactory newRemoteBodyFactorySingleton() {
        return new RemoteBodyFactoryImpl();
    }

    protected ThreadStoreFactory newThreadStoreFactorySingleton() {
        return new ThreadStoreFactoryImpl();
    }

    protected ProActiveComponentFactory newComponentFactorySingleton(
        ComponentParameters initialComponentParameters) {
        return new ProActiveComponentFactoryImpl(initialComponentParameters);
    }

    //  //
    //  // -- INNER CLASSES -----------------------------------------------
    //  //
    protected static class RequestFactoryImpl implements RequestFactory,
        java.io.Serializable {
        public Request newRequest(MethodCall methodCall,
            UniversalBody sourceBody, boolean isOneWay, long sequenceID) {
            //########### exemple de code pour les nouvelles factories
            //			if(System.getProperty("migration.stategy").equals("locationserver")){
            //				  return new RequestWithLocationServer(methodCall, sourceBody,
            //                isOneWay, sequenceID, LocationServerFactory.getLocationServer());
            //			}else{
            return new org.objectweb.proactive.core.body.request.RequestImpl(methodCall,
                sourceBody, isOneWay, sequenceID);
            //}
        }
    }

    // end inner class RequestFactoryImpl
    protected static class ReplyReceiverFactoryImpl
        implements ReplyReceiverFactory, java.io.Serializable {
        public ReplyReceiver newReplyReceiver() {
            return new org.objectweb.proactive.core.body.reply.ReplyReceiverImpl();
        }
    }

    // end inner class ReplyReceiverFactoryImpl
    protected static class RequestReceiverFactoryImpl
        implements RequestReceiverFactory, java.io.Serializable {
        public RequestReceiver newRequestReceiver() {
            return new org.objectweb.proactive.core.body.request.RequestReceiverImpl();
        }
    }

    // end inner class RequestReceiverFactoryImpl
    protected class RequestQueueFactoryImpl implements RequestQueueFactory,
        java.io.Serializable {
        public BlockingRequestQueue newRequestQueue(UniqueID ownerID) {
            if (componentFactoryInstance != null) {
                // COMPONENTS
                // we need a request queue for components
                return new ComponentRequestQueueImpl(ownerID);
            } else {
                return new org.objectweb.proactive.core.body.request.BlockingRequestQueueImpl(ownerID);
            }
        }
    }

    // end inner class RequestQueueFactoryImpl
    protected static class MigrationManagerFactoryImpl
        implements MigrationManagerFactory, java.io.Serializable {
        public MigrationManager newMigrationManager() {
            //########### example de code pour les nouvelles factories
            //			if(System.getProperty("migration.stategy").equals("locationserver")){
            //				return new MigrationManagerWithLocationServer(LocationServerFactory.getLocationServer());
            //			}else{
            return new org.objectweb.proactive.core.body.migration.MigrationManagerImpl();
            //}
        }
    }

    // end inner class MigrationManagerFactoryImpl
    protected static class RemoteBodyFactoryImpl implements RemoteBodyFactory,
        java.io.Serializable {
        public UniversalBody newRemoteBody(UniversalBody body) {
            try {
                if ("ibis".equals(System.getProperty("proactive.rmi"))) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Factory is ibis");
                    }
                    return new org.objectweb.proactive.core.body.ibis.IbisRemoteBodyAdapter(body);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Factory is rmi");
                    }
                    return new org.objectweb.proactive.core.body.rmi.RemoteBodyAdapter(body);
                }
            } catch (ProActiveException e) {
                throw new ProActiveRuntimeException("Cannot create Remote body adapter ",
                    e);
            }
        }
    }

    // end inner class RemoteBodyFactoryImpl
    protected static class ThreadStoreFactoryImpl implements ThreadStoreFactory,
        java.io.Serializable {
        public ThreadStore newThreadStore() {
            return new org.objectweb.proactive.core.util.ThreadStoreImpl();
        }
    }

    // end inner class ThreadStoreFactoryImpl
    protected class ProActiveComponentFactoryImpl
        implements ProActiveComponentFactory, java.io.Serializable {
        // COMPONENTS
        private ComponentParameters componentParameters;

        public ProActiveComponentFactoryImpl(
            ComponentParameters initialComponentParameters) {
            this.componentParameters = initialComponentParameters;
        }

        public ProActiveComponent newProActiveComponent(Body myBody) {
            return new ProActiveComponentImpl(componentParameters, myBody);
        }
    }
    
    // SECURITY
	public void setProActiveSecurityManager(ProActiveSecurityManager psm) {
		proActiveSecurityManager = psm;
	}

	public ProActiveSecurityManager getProActiveSecurityManager() {
		return proActiveSecurityManager;
	}    
}
