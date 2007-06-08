package org.objectweb.proactive.core.remoteobject;

import java.net.URI;
import java.util.Hashtable;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.ibis.IbisRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.rmissh.RmiSshRemoteObjectFactory;
import org.objectweb.proactive.core.rmi.ClassServer;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * This class provides helper methods for manipulation remote objects.
 *
 */
public abstract class RemoteObjectFactory {
    protected static ClassServerHelper classServerHelper;

    //	  protected static String codebase;
    protected static Hashtable<String, Class<?extends RemoteObjectFactory>> remoteObjectFactories;

    static {
        remoteObjectFactories = new Hashtable<String, Class<?extends RemoteObjectFactory>>();
        remoteObjectFactories.put(Constants.RMI_PROTOCOL_IDENTIFIER,
            RmiRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
            HTTPRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.RMISSH_PROTOCOL_IDENTIFIER,
            RmiSshRemoteObjectFactory.class);
        remoteObjectFactories.put(Constants.IBIS_PROTOCOL_IDENTIFIER,
            IbisRemoteObjectFactory.class);

        createClassServer();
    }

    protected static synchronized String addCodebase(String newLocationURL) {
        String oldCodebase = System.getProperty("java.rmi.server.codebase");
        String newCodebase = null;
        if (oldCodebase != null) {
            // RMI support multiple class server locations
            newCodebase = oldCodebase + " " + newLocationURL;
        } else {
            newCodebase = newLocationURL;
        }

        System.setProperty("java.rmi.server.codebase", newCodebase);

        return newCodebase;
    }

    protected static synchronized void createClassServer() {
        if (classServerHelper == null) {
            try {
                classServerHelper = new ClassServerHelper();
                String codebase = classServerHelper.initializeClassServer();

                addCodebase(codebase);
            } catch (Exception e) {
                ProActiveLogger.getLogger(Loggers.CLASS_SERVER)
                               .warn("Error with the ClassServer : " +
                    e.getMessage());
            }
        }
    }

    /**
       * returns the default port set for the given protocol
       * @param protocol the protocol
       * @return the default port number associated to the protocol
       */
    public static int getDefaultPortForProtocol(String protocol) {
        if (Constants.XMLHTTP_PROTOCOL_IDENTIFIER.equals(protocol)) {
            if (ProActiveConfiguration.getInstance()
                                          .getProperty(Constants.PROPERTY_PA_XMLHTTP_PORT) != null) {
                return Integer.parseInt(ProActiveConfiguration.getInstance()
                                                              .getProperty(Constants.PROPERTY_PA_XMLHTTP_PORT));
            }
        } else if ((Constants.RMI_PROTOCOL_IDENTIFIER.equals(protocol)) ||
                Constants.IBIS_PROTOCOL_IDENTIFIER.equals(protocol) ||
                Constants.RMISSH_PROTOCOL_IDENTIFIER.equals(protocol)) {
            return Integer.parseInt(ProActiveConfiguration.getInstance()
                                                          .getProperty(Constants.PROPERTY_PA_RMI_PORT));
        }

        // default would be to return the RMI default port
        return -1;
    }

    /**
     * returns an url for a object to be exposed on the current host for a given protocol and name
     * @param protocol the protocol
     * @return the default port number associated to the protocol
     */
    public static URI generateUrl(String protocol, String name) {
        return URI.create(UrlBuilder.buildUrl(null, name, protocol,
                getDefaultPortForProtocol(protocol), true));
    }

    public static RemoteObjectFactory getRemoteObjectFactory(String protocol) {
        try {
            return remoteObjectFactories.get(protocol).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return a remote part of a remote object according to the factory i.e the protocol
     * @param target the RemoteObject to expose
     * @return        the remote part of the remote object
     * @throws ProActiveException
     */
    public abstract RemoteRemoteObject newRemoteObject(RemoteObject target)
        throws ProActiveException;

    /**
     * Bind a remote object to the registry used by the factory and return the remote remote object
     * corresponding to this bind
     * @param target the remote object to register
     * @param url        the url associated to the remote object
     * @param replacePreviousBinding if true replace an existing remote object by the new one
     * @return a reference to the remote remote object
     * @throws ProActiveException throws a ProActiveException if something went wrong during the registration
     */
    public abstract RemoteRemoteObject register(RemoteObject target, URI url,
        boolean replacePreviousBinding) throws ProActiveException;

    /**
     * unregister the remote remote object located at a given
     * @param url the url
     * @throws ProActiveException throws a ProActiveException if something went wrong during the unregistration
     */
    public abstract void unregister(URI url) throws ProActiveException;

    /**
     * list all the remote objects register into a registry located at the url
     * @param url the location of the registry
     * @throws ProActiveException
     */
    public abstract URI[] list(URI url) throws ProActiveException;

    /**
     * Returns a reference, a stub, for the remote object associated with the specified url.
     * @param url
     * @return
     * @throws ProActiveException
     */
    public abstract RemoteObject lookup(URI url) throws ProActiveException;
}
