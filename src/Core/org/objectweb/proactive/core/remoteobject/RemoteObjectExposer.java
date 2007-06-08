package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectExposer implements Serializable {
    protected Hashtable<String, RemoteObjectFactory> activatedRemoteObjectFactories;
    protected Hashtable<URI, RemoteRemoteObject> activatedProtocols;
    private String className;
    private RemoteObjectImpl remoteObject;

    public RemoteObjectExposer() {
    }

    public RemoteObjectExposer(String className, Object target) {
        this.className = className;
        this.remoteObject = new RemoteObjectImpl(className, target);
        this.activatedRemoteObjectFactories = new Hashtable<String, RemoteObjectFactory>();
        this.activatedProtocols = new Hashtable<URI, RemoteRemoteObject>();
    }

    public synchronized void activateProtocol(URI url) {
        String protocol = url.getScheme();
        RemoteObjectFactory rof = RemoteObjectFactory.getRemoteObjectFactory(protocol);

        if ((protocol == null) || (rof == null)) {
            throw new RuntimeException("unknown protocol : " + protocol);
        } else {
            if (this.activatedRemoteObjectFactories.get(protocol) == null) {
                try {
                    int port = url.getPort();
                    if (port == -1) {
                        try {
                            url = new URI(url.getScheme(), url.getUserInfo(),
                                    url.getHost(),
                                    RemoteObjectFactory.getDefaultPortForProtocol(
                                        protocol), url.getPath(),
                                    url.getQuery(), url.getFragment());
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }

                    RemoteRemoteObject rmo = rof.register(this.remoteObject,
                            url, true);

                    this.activatedProtocols.put(url, rmo);
                } catch (ProActiveException e) {
                    ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                                   .warn("unable to activate a remote object at endpoint " +
                        url.toString());

                    e.printStackTrace();
                }
            } else {
                ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                               .info("protocol " + protocol +
                    "was already activated for this remote object");
            }
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
                RemoteObjectFactory.getRemoteObjectFactory(uri.getScheme())
                                   .unregister(uri);
            } catch (ProActiveException e) {
                e.printStackTrace();
            }
        }
    }
}
