package org.objectweb.proactive.p2p.registry;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.DiscoveryEvent;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;

/**
* This class represent the Server that manage the platform
*/

public class ServerListener implements DiscoveryListener, Runnable {

	// Hashtable of registration leases keyed by the lookup service
	private Hashtable leases = new Hashtable();
	private ServiceItem item;          // Item to be registered with lookup
	private static final long ltime = Lease.FOREVER;
	private static final int mtime = 30*1000;       // 30 seconds (Minimum lease)

	private LookupDiscovery ld;        // The discovery object we're listening to

	public ServerListener(LookupDiscovery ld, Object object) {
		item = new ServiceItem(null, object, null);
		this.ld = ld;
		// Start the new thread to renew the leases
		new Thread(this).start();
	}

	// Automatically called when new lookup service(s) are discovered
	public synchronized void discovered(DiscoveryEvent dev) {
		ServiceRegistrar[] lookup = dev.getRegistrars();
		// For each discovered service, see if we're already registered.
		// If not, register
		for (int i = 0; i < lookup.length; i++) {
			if (leases.containsKey(lookup[i]) == false) {
				// Not already registered
				try {
					//Register
					ServiceRegistration ret = lookup[i].register(item, ltime);
					// You must assign the serviceID based on what the
					// lookup service returns
					if (item.serviceID == null) {
						item.serviceID = ret.getServiceID();
					}
					// Save this registration
					leases.put(lookup[i], ret);
					// There's a new lease, notify the renewal thread
					notify();
				} catch (RemoteException ex) {
					System.out.println("ServerListener error: " + ex);
				}
			}
			// else we were already registered in this service
		}
	}

	// Automatically called when lookup service(s) are no longer available
	public synchronized void discarded(DiscoveryEvent dev) {
		ServiceRegistrar[] lookup = dev.getRegistrars();
		for (int i = 0; i < lookup.length; i++) {
			if (leases.containsKey(lookup[i]) == true) {
				// Remove the registration. If the lookup service comes
				// back later, we'll re-register at that time.
				leases.remove(lookup[i]);
			}
		}
	}

	public synchronized void run() {
		while (true) {
			long nextRenewal = Long.MAX_VALUE;
			long now = System.currentTimeMillis();

			Enumeration e = leases.keys();
			// Loop to renew all leases that are about to expire
			// and also to find the time when the next lease will
			// expire so we know when to run the loop again.
			while (e.hasMoreElements()) {
				ServiceRegistrar lookup = (ServiceRegistrar) e.nextElement();
				ServiceRegistration sr = (ServiceRegistration) leases.get(lookup);
				Lease l = sr.getLease();
				long expire = l.getExpiration();

				// See if the current lease has the minimum time.
				// If we can't renew it, discard that lookup service.
				// That will generate an event to the discarded() method
				// which will actually remove the lease from our list.
				try {
					if (expire <= now + mtime) {
						l.renew(ltime);
						expire = l.getExpiration();
					} 
					if (nextRenewal > expire - mtime) {
						nextRenewal = expire - mtime;
					}
				} catch (LeaseDeniedException lex) {
				} catch (UnknownLeaseException lex) {
					ld.discard(lookup);
				} catch (RemoteException ex) {
					ld.discard(lookup);
				}
			}
			try {
				// Wait until the next renewal time. A new lease
				// will notify us prematurely in case the new
				// lease has a smaller time until it must be renewed
				wait(nextRenewal - now);
			} catch (InterruptedException ex) {};
		}
	}
}
