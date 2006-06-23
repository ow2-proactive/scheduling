/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */ 
package org.objectweb.proactive.core.runtime;

import java.io.Serializable;


/**
 * An adapter for a RemoteProActiveRuntime. The Adpater is the generic entry point for remote calls
 * to a RemoteProActiveRuntime using different protocols such as RMI, RMISSH, IBIS, HTTP, JINI.
 * This also allows to cache informations, and so to avoid crossing the network when calling some methods.
 * @author ProActiveTeam
 * @version 1.0
 * @since ProActive 2.2
 * @see <a href="http://www.javaworld.com/javaworld/jw-11-2000/jw-1110-smartproxy.html">smartProxy Pattern.</a>
 *
 */
public abstract class ProActiveRuntimeAdapter implements ProActiveRuntime,
    Serializable {
    protected VMInformation vmInformation;
}
