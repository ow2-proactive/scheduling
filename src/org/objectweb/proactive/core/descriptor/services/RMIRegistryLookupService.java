/*
 * Created on 23 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.UrlBuilder;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RMIRegistryLookupService implements UniversalService {

    /** lookup url */
    protected String url;
    protected static String serviceName = "RMIRegistryLookup";
    protected ProActiveRuntime[] runtimeArray;

    public RMIRegistryLookupService(String url) {
        this.url = url;
        this.runtimeArray = new ProActiveRuntime[1];
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException{
        ProActiveRuntime part = RuntimeFactory.getRuntime(UrlBuilder.removeProtocol(url,"rmi:"),"rmi:");
        runtimeArray[0] = part;
        return runtimeArray;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }
}
