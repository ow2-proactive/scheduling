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
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;

public class RemoteObjectHostRTFinder implements HostRTFinder{

	//
	// -- PUBLIC METHODS -----------------------------------------------
	//

	public List<ProActiveRuntime> findPARuntime(HostObject host) {

		Console console = Console.getInstance(Activator.CONSOLE_NAME);

		console.log("Exploring "+host+"  on port "+host.getPort());
		/* List of ProActive runtime */
		List<ProActiveRuntime> runtimes = new ArrayList<ProActiveRuntime>();


		URI [] uris = null;
		try {

			 URI target = URIBuilder.buildURI(host.getHostName(), null, host.getProtocol(), host.getPort());
			 uris = RemoteObjectHelper.getRemoteObjectFactory(host.getProtocol()).list(target);

			if (uris != null ) {
			 /* Searchs all ProActive Runtimes */
				for (int i = 0; i < uris.length; ++i) {

					URI url = uris[i];

					try {
					    RemoteObject ro = RemoteObjectHelper.lookup(url);
					    Object stub = ro.getObjectProxy();

	                    if (stub instanceof ProActiveRuntime) {
	                        runtimes.add((ProActiveRuntime ) stub);
	                    }
					} catch (ProActiveException pae) {
					    // the lookup returned an active object, and an active object is
					    // not a remote object (for now...)
					    // TODO : Arnaud, Active objects should become Remote Objects...
					    console.log("Found active object in registry at " + url);
					}

				}
			}
		} catch (Exception e) {
			if(e instanceof ConnectException || e instanceof ConnectIOException) {
				console.debug(e);
			}
			else
				console.logException(e);
			return runtimes;
		}


		return runtimes;
	}
}
