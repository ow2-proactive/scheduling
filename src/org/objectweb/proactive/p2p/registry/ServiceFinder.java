/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.objectweb.proactive.p2p.registry;

import java.io.*;
import java.rmi.*;
import java.util.*;
import net.jini.discovery.*;
import net.jini.core.lookup.*;
import net.jini.core.entry.*;

public class ServiceFinder implements DiscoveryListener {
		private static String[] publicGroup = new String[] { "" };
		private Vector returnObject = new Vector();

		private LookupDiscovery reg;
		private ServiceTemplate template;

		public ServiceFinder(Class serviceInterface) throws IOException {
			this(publicGroup, serviceInterface, (Entry[])null);
		}

		public ServiceFinder(Class serviceInterface, Entry attribute)
							 throws IOException {
			this(publicGroup, serviceInterface, new Entry[] { attribute });
		}

		public ServiceFinder(Class serviceInterface, Entry[] attributes)
							 throws IOException {
			this(publicGroup, serviceInterface, attributes);
		}

		public ServiceFinder(String[] groups, Class serviceInterface,
							 Entry[] attributes) throws IOException {
			// Construct the template here for matching in the lookup service
			// We don't use the template until we actually discover a service
			Class[] name = new Class[] { serviceInterface };
			template = new ServiceTemplate(null, name, attributes);

			// Create the facility to perform multicast discovery for all
			// lookup services
			reg = new LookupDiscovery(groups);
			reg.addDiscoveryListener(this);
		}

		// Automatically called when a lookup service is discovered
		// (the listener callback of the addDiscoveryListener method)
		public synchronized void discovered(DiscoveryEvent dev) {
			ServiceRegistrar[] lookup = dev.getRegistrars();
			// We may have discovered one or more lookup services
			for (int i = 0; i < lookup.length; i++) {
				try {
					ServiceMatches items =
						lookup[i].lookup(template, Integer.MAX_VALUE);
					// Each lookup service may have zero or more registered
					// servers that implement our desired template
					for (int j = 0; j < items.items.length; j++) {
						if (items.items[j].service != null)
							// Put each matching service into our vector
							returnObject.addElement(items.items[j]);
				// else the service item couldn't be deserialized
				// so the lookup() method skipped it
					}
						notifyAll();
				} catch (RemoteException ex) {
					System.err.println("[Service Finder] ServiceFinder Error: " + ex);
				}
			}
		}

/************* DISCARDED ***************/
		public synchronized void discarded(DiscoveryEvent dev) {
		}


/************** GET OBJECT ****************/

		// This class is to be used by the client. It will return only
		// the first service object that satisfies the template request.
		public synchronized Object getObject() {
			while (returnObject.size() == 0) {
				try {
					wait();
				} catch (InterruptedException ex) {
				};
			}
//			if (returnObject.size() == 0) return null;
			return ((ServiceItem)returnObject.elementAt(0)).service;
		}

/*************** ERROR HANDLE ********************/

		// If an error is encountered when using a service object, the client
		// shoud call this method.
		// A new object can then be gotten from the getObject() method.
		public synchronized void errored(Object obj) {
			if ((obj != null) && (returnObject.size() != 0)) {
				if (obj.equals(((ServiceItem)returnObject.elementAt(0)).service)) {
					returnObject.removeElementAt(0);
				}
			}
		}
	}

