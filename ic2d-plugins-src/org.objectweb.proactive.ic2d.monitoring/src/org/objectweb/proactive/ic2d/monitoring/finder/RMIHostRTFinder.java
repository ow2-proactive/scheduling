/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.monitoring.finder;

import java.net.URI;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;

public class RMIHostRTFinder implements HostRTFinder{

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//

	public List<ProActiveRuntime> findPARuntime(HostObject host) {

		Console console = Console.getInstance(Activator.CONSOLE_NAME);

		console.log("Exploring "+host+" with RMI on port "+host.getPort());
		/* List of ProActive runtime */
		List<ProActiveRuntime> runtimes = new ArrayList<ProActiveRuntime>();

		Registry registry = null;
		String[] names = null;
		try {
			/* Hook the registry */
			registry = LocateRegistry.getRegistry(host.getHostName(),host.getPort());

			console.debug("Listing bindings for " + registry);
			/* Gets a snapshot of the names bounds in the 'registry' */
			names = registry.list();

		} catch (Exception e) {
			if(e instanceof ConnectException || e instanceof ConnectIOException) {
				console.debug(e);
			}
			else
				console.logException(e);
			return runtimes;
		}

		/* Searchs all ProActive Runtimes */
		for (int i = 0; i < names.length; ++i) {
			String name = names[i];
			if (name.indexOf("PA_JVM") != -1) {
				try {
//					RemoteProActiveRuntime remote = (RemoteProActiveRuntime) registry.lookup(name);
//					ProActiveRuntime proActiveRuntime = new ProActiveRuntimeAdapterImpl(remote);
					
					URI url = new URI(Constants.RMI_PROTOCOL_IDENTIFIER,null,host.getHostName(),host.getPort(),"/"+name,null,null);
					System.out.println("RMIHostRTFinder.findPARuntime()  "  + url);
					RemoteObject ro = RemoteObjectFactory.getRemoteObjectFactory(Constants.RMI_PROTOCOL_IDENTIFIER).lookup(url);  
					
					System.out.println("RMIHostRTFinder.findPARuntime()  "  + ro);
					
					Object stub = ro.getObjectProxy();
					
					System.out.println("RMIHostRTFinder.findPARuntime()  "  + stub);
					if (stub instanceof ProActiveRuntime) {
						runtimes.add((ProActiveRuntime ) stub);
					}
					
					
				} catch(Exception e) {
					//System.out.println("RMIHostRTFinder.findPARuntime() ***"+host.getFullName());
					console.debug(e);
					/*if(e instanceof ProActiveException){
						console.debug("Serial Version UID is incompatible");
					}
					else
						console.logException(e);
					 */
				}
			}
		}
		return runtimes;
	}
}
