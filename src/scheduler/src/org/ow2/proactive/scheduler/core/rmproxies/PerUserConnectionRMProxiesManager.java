package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


public final class PerUserConnectionRMProxiesManager extends RMProxiesManager {

    private final Map<URI, RMAuthentication> rmAuthentifications = new HashMap<URI, RMAuthentication>(2);

    private final Map<String, UserRMProxy> userProxiesMap = new HashMap<String, UserRMProxy>();

    private final SchedulerRMProxy schedulerRMProxy;

    private Connection currentRMConnection;

    private RMProxyActiveObject rmProxyActiveObject;

    private final Object connectionStateLock = new Object();

    private URI rmURI;

    public PerUserConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException {
        super(schedulerProxyCredentials);
        rebindRMProxiesManager(rmURI);

        schedulerRMProxy = new SchedulerRMProxy(this);
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
            String rmUrl = rmURI.toString();
            RMAuthentication auth = RMConnection.join(rmUrl);
            rmAuthentifications.put(rmURI, auth);
            currentRMConnection = new Connection(rmURI, auth);

            if (rmProxyActiveObject != null) {
                rmProxyActiveObject.terminateProxy();
            }
            rmProxyActiveObject = RMProxyActiveObject.createAOProxy(auth, schedulerProxyCredentials);
            this.rmURI = rmURI;
        }
    }

    @Override
    public UserRMProxy getUserRMProxy(String user, Credentials credentials) {
        synchronized (userProxiesMap) {
            UserRMProxy proxy = userProxiesMap.get(user);
            if (proxy == null) {
                proxy = new UserRMProxy(this, credentials);
                userProxiesMap.put(user, proxy);
            }
            return proxy;
        }
    }

    @Override
    public SchedulerRMProxy getSchedulerRMProxy() {
        return schedulerRMProxy;
    }

    @Override
    public void terminateUserRMProxy(String user) {
        synchronized (userProxiesMap) {
            UserRMProxy proxy = userProxiesMap.remove(user);
            if (proxy != null) {
                proxy.terminate();
            }
        }
    }

    @Override
    public void terminateAllProxies() {
        synchronized (connectionStateLock) {
            synchronized (userProxiesMap) {
                for (UserRMProxy userProxy : userProxiesMap.values()) {
                    userProxy.terminate();
                }
                userProxiesMap.clear();
            }
            if (rmProxyActiveObject != null) {
                rmProxyActiveObject.terminateProxy();
                rmProxyActiveObject = null;
                currentRMConnection = null;
            }
        }
    }

    @Override
    synchronized Connection getCurrentRMConnection() {
        return currentRMConnection;
    }

    @Override
    RMProxyActiveObject getSchedulerProxyActiveObjectForCurrentRM() {
        synchronized (connectionStateLock) {
            if (rmProxyActiveObject == null) {
                throw new IllegalStateException("Not connected to the RM");
            }
            return rmProxyActiveObject;
        }
    }

}
