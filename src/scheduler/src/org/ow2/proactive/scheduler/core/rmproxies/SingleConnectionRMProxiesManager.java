package org.ow2.proactive.scheduler.core.rmproxies;

import java.net.URI;
import java.net.URISyntaxException;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.exception.RMException;


public final class SingleConnectionRMProxiesManager extends RMProxiesManager {

    private final RMProxy rmProxy;

    private URI rmURI;

    public SingleConnectionRMProxiesManager(URI rmURI, Credentials schedulerProxyCredentials)
            throws RMException, RMProxyCreationException, URISyntaxException {
        super(schedulerProxyCredentials);
        this.rmURI = rmURI;

        rmProxy = new RMProxy(rmURI, schedulerProxyCredentials);
    }

    @Override
    synchronized public void rebindRMProxiesManager(URI rmURI) throws RMException, RMProxyCreationException {
        this.rmURI = rmURI;
        rmProxy.rebind(rmURI);
    }

    @Override
    public void terminateRMProxy(String user) {
        // nothing to do
    }

    @Override
    public void terminateAllProxies() {
        rmProxy.terminate();
    }

    @Override
    public RMProxy getUserRMProxy(String user, Credentials credentials) {
        return rmProxy;
    }

    @Override
    public RMProxy getRmProxy() {
        return rmProxy;
    }

}
