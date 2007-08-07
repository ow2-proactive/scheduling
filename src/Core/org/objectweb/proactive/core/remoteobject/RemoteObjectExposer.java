package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectExposer implements Serializable {

    /**
         *
         */
    private static final long serialVersionUID = -4162510983888994949L;
    protected Hashtable<URI, RemoteRemoteObject> activatedProtocols;
    private String className;
    private RemoteObjectImpl remoteObject;

    public RemoteObjectExposer() {
    }

    public RemoteObjectExposer(String className, Object target) {
        this(className, target, null);
    }

    public RemoteObjectExposer(String className, Object target,
        Adapter targetRemoteObjectAdapter) {
        this.className = className;
        this.remoteObject = new RemoteObjectImpl(className, target,
                targetRemoteObjectAdapter);
        this.activatedProtocols = new Hashtable<URI, RemoteRemoteObject>();
    }

    public synchronized RemoteRemoteObject activateProtocol(URI url)
        throws UnknownProtocolException {
        String protocol = null;

        if (!url.isAbsolute()) {
            url = RemoteObjectHelper.expandURI(url);
        }

        protocol = url.getScheme();

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

            RemoteRemoteObject rmo = rof.register(this.remoteObject, url, true);

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
        return getURL(ProActiveConfiguration.getInstance()
                                            .getProperty(Constants.PROPERTY_PA_COMMUNICATION_PROTOCOL));
    }

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

    public RemoteObjectImpl getRemoteObject() {
        return this.remoteObject;
    }
}
