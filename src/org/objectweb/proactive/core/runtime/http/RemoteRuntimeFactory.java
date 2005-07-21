/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 **/
package org.objectweb.proactive.core.runtime.http;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.rmi.ClassServerHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


public class RemoteRuntimeFactory extends RuntimeFactory {
    protected static ClassServerHelper classServerHelper = new ClassServerHelper();
    private static ProActiveRuntime defaultRuntime = null;

    public RemoteRuntimeFactory() {
    }  

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.RuntimeFactory#getProtocolSpecificRuntimeImpl()
     */
    protected ProActiveRuntime getProtocolSpecificRuntimeImpl()
        throws ProActiveException {
        defaultRuntime = createRuntimeAdapter();

        return defaultRuntime;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.runtime.RuntimeFactory#getRemoteRuntimeImpl(java.lang.String)
     */
    protected ProActiveRuntime getRemoteRuntimeImpl(String s)
        throws ProActiveException {
        // Here we do a lookup of the RemoteRuntime 
        //registered at the url given in parameter.
        //In fact it oly consists with the creation of an adapter 
        //with the specified url;
        System.out.println(s);
        ProActiveRuntime remoteProActiveRuntime = new HttpRuntimeAdapter(s);
//      remote
        return remoteProActiveRuntime;
    }

    private HttpRuntimeAdapter createRuntimeAdapter() {
//    	 local
    	return new HttpRuntimeAdapter();
    }
}
