package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;


public final class PerUserConnectionRMProxiesManager extends RMProxiesManager {

    private final Map<URI, RMAuthentication> rmAuthentifications = new HashMap<URI, RMAuthentication>(2);

    private final Map<String, RMProxy> userProxiesMap = new HashMap<String, RMProxy>();

    private final RMProxy schedulerRMProxy;

    private Connection currentRMConnection;

    private RMProxyActiveObject rmProxyActiveObject;

    private final Object connectionStateLock = new Object();

    private URI rmURI;

    public PerUserConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException, URISyntaxException {
        super(schedulerProxyCredentials);
        this.rmURI = rmURI;
        schedulerRMProxy = new RMProxy(rmURI, schedulerProxyCredentials);
    }

    @Override
    public URI getRmUrl() {
        synchronized (connectionStateLock) {
            return rmURI;
        }
    }

    @Override
    public void rebindRMProxiesManager(URI rmURI) throws RMException, RMProxyCreationException {
        synchronized (connectionStateLock) {
            synchronized (userProxiesMap) {
                for (RMProxy userProxy : userProxiesMap.values()) {
                    userProxy.rebind(rmURI);
                }
            }
            schedulerRMProxy.rebind(rmURI);
        }
    }

    @Override
    public RMProxy getUserRMProxy(String user, Credentials credentials) throws RMProxyCreationException {
        synchronized (userProxiesMap) {
            RMProxy proxy = userProxiesMap.get(user);
            if (proxy == null) {
                try {
                    proxy = new RMProxy(rmURI, credentials);
                    userProxiesMap.put(user, proxy);
                } catch (RMException e) {
                    throw new RMProxyCreationException(e);
                }
            }
            return proxy;
        }
    }

    @Override
    public RMProxy getRmProxy() {
        return schedulerRMProxy;
    }

    @Override
    public void terminateRMProxy(String user) {
        synchronized (userProxiesMap) {
            RMProxy proxy = userProxiesMap.remove(user);
            if (proxy != null) {
                proxy.terminate();
            }
        }
    }

    @Override
    public void terminateAllProxies() {
        schedulerRMProxy.terminate();
        synchronized (connectionStateLock) {
            synchronized (userProxiesMap) {
                for (RMProxy userProxy : userProxiesMap.values()) {
                    userProxy.terminate();
                }
                userProxiesMap.clear();
            }
        }

    }
}
