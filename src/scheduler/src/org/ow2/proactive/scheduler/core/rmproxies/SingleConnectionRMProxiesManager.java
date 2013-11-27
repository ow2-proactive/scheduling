package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;


public final class SingleConnectionRMProxiesManager extends RMProxiesManager {

    private final SchedulerRMProxy schedulerRMProxy;

    private final UserRMProxy userRMProxy;

    private Connection currentRMConnection;

    private RMProxyActiveObject currentRMProxy;

    private URI rmURI;

    public SingleConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException {
        super(schedulerProxyCredentials);
        rebindRMProxiesManager(rmURI);

        schedulerRMProxy = new SchedulerRMProxy(this);
        userRMProxy = new UserRMProxy(this, schedulerProxyCredentials);
    }

    @Override
    synchronized public URI getRmUrl() {
        return rmURI;
    }

    @Override
    synchronized public void rebindRMProxiesManager(URI rmURI) throws RMException, RMProxyCreationException {
        terminateAllProxies();

        String rmUrl = rmURI.toString();
        RMAuthentication auth = RMConnection.join(rmUrl);
        currentRMConnection = new Connection(rmURI, auth);
        currentRMProxy = RMProxyActiveObject.createAOProxy(auth, schedulerProxyCredentials);

        this.rmURI = rmURI;
    }

    @Override
    public void terminateUserRMProxy(String user) {
        // ignore
    }

    @Override
    public synchronized void terminateAllProxies() {
        if (currentRMConnection != null) {
            userRMProxy.terminate();
            currentRMProxy.terminateProxy();
            currentRMConnection = null;
            currentRMProxy = null;
        }
    }

    @Override
    public UserRMProxy getUserRMProxy(String user, Credentials credentials) {
        return userRMProxy;
    }

    @Override
    public SchedulerRMProxy getSchedulerRMProxy() {
        return schedulerRMProxy;
    }

    @Override
    synchronized Connection getCurrentRMConnection() {
        return currentRMConnection;
    }

    @Override
    synchronized RMProxyActiveObject getSchedulerProxyActiveObjectForCurrentRM() {
        if (currentRMProxy == null) {
            throw new IllegalStateException("Not connected to the RM");
        }
        return currentRMProxy;
    }

}
