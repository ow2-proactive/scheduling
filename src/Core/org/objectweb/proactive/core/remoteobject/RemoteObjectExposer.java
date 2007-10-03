package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author acontes
 * The RemoteObjectExposer is in charge of exposing an object as a remote object.
 * It allows the exposition of the object it represents on one or multiple protocols, keeps
 * references on already activated protocols, allows to unexpose one or more protocols.
 */
public class RemoteObjectExposer implements Serializable {
    private static final long serialVersionUID = -4162510983888994949L;
    protected Hashtable<URI, RemoteRemoteObject> activatedProtocols;
    private String className;
    private RemoteObjectImpl remoteObject;

    public RemoteObjectExposer() {
    }

    public RemoteObjectExposer(String className, Object target) {
        this(className, target, null);
    }

    /**
     *
     * @param className the classname of the stub for the remote object
     * @param target the object to turn into a remote object
     * @param targetRemoteObjectAdapter the adapter object that allows to implement specific behaviour like cache mechanism
     */
    public RemoteObjectExposer(String className, Object target,
        Adapter<?> targetRemoteObjectAdapter) {
        this.className = className;
        this.remoteObject = new RemoteObjectImpl(className, target,
                targetRemoteObjectAdapter);
        this.activatedProtocols = new Hashtable<URI, RemoteRemoteObject>();
    }

    /**
     * activate and register the remote object on the given url
     * @param url The URI where to register the remote object
     * @return a remote reference to the remote object ie a RemoteRemoteObject
     * @throws UnknownProtocolException thrown if the protocol specified within the url is unknow
     */
    public synchronized RemoteRemoteObject activateProtocol(URI url)
        throws UnknownProtocolException {
        String protocol = null;

        // check if the url contains a scheme (protocol)
        if (!url.isAbsolute()) {
            // if not expand it
            url = RemoteObjectHelper.expandURI(url);
        }

        protocol = url.getScheme();

        // select the factory matching the required protocol
        RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(protocol);

        try {
            int port = url.getPort();
            if (port == -1) {
                try {
                    url = new URI(url.getScheme(), url.getUserInfo(),
                            url.getHost(),
                            RemoteObjectHelper.getDefaultPortForProtocol(
                                protocol), url.getPath(), url.getQuery(),
                            url.getFragment());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            // register the object on the register
            RemoteRemoteObject rmo = rof.register(this.remoteObject, url, true);

            // put the url within the list of the activated protocols
            this.activatedProtocols.put(url, rmo);

            return rmo;
        } catch (ProActiveException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                           .warn("unable to activate a remote object at endpoint " +
                url.toString());

            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param protocol
     * @return return the remote reference on the remote object targetted by the protocol
     */
    public RemoteRemoteObject getRemoteObject(String protocol) {
        Enumeration<URI> e = this.activatedProtocols.keys();

        while (e.hasMoreElements()) {
            URI url = e.nextElement();
            if (protocol.equals(url.getScheme())) {
                return this.activatedProtocols.get(url);
            }
        }

        return null;
    }

    /**
     * @return return the activated urls
     */
    public String[] getURLs() {
        String[] urls = new String[this.activatedProtocols.size()];

        Enumeration<URI> e = this.activatedProtocols.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            urls[i] = e.nextElement().toString();
            i++;
        }

        return urls;
    }

    public String getURL(String protocol) {
        Enumeration<URI> e = this.activatedProtocols.keys();

        while (e.hasMoreElements()) {
            URI url = e.nextElement();
            if (protocol.equals(url.getScheme())) {
                return url.toString();
            }
        }

        return null;
    }

    public String getURL() {
        return getURL(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue());
    }

    /**
     * unregister all the remote references on the remote object.
     */
    public void unregisterAll() {
        Enumeration<URI> uris = this.activatedProtocols.keys();
        URI uri = null;
        int i = 0;
        while (uris.hasMoreElements()) {
            uri = uris.nextElement();
            RemoteRemoteObject rro = this.activatedProtocols.get(uri);
            try {
                RemoteObjectHelper.getRemoteObjectFactory(uri.getScheme())
                                  .unregister(uri);
            } catch (ProActiveException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return return the remote object
     */
    public RemoteObjectImpl getRemoteObject() {
        return this.remoteObject;
    }
}
