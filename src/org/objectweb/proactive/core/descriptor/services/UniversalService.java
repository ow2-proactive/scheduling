/*
 * Created on 23 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;

import java.io.Serializable;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface UniversalService extends Serializable {

    /**
     * Starts this Service
     * @return an array of ProActiveRuntime
     */
    public ProActiveRuntime[] startService() throws ProActiveException;

    /**
     * Returns the name of the service.
     * The name is static, it means that it is the same name for all instances of a
     * sefvice's class 
     * @return the static name of the service
     */
    public String getServiceName();
}
