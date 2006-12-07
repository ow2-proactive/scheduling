package org.objectweb.proactive.ic2d.monitoring.finder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceTemplate;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.RemoteProActiveRuntime;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;
import org.objectweb.proactive.ic2d.monitoring.data.Protocol;

public class JiniHostRTFinder implements HostRTFinder {

	public List<ProActiveRuntime> findPARuntime(HostObject host) {
		// TODO if host.getName() == null (recherche multicast) ???
		
		Console console = Console.getInstance(Activator.CONSOLE_NAME);
		
		console.log("Exploring " + host + " with JINI ");

		List<ProActiveRuntime> runtimes = new ArrayList<ProActiveRuntime>();
		
		LookupLocator lookup = null;
		try {
			lookup = new LookupLocator(Protocol.JINI.toString()+"://" + host.getHostName());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			console.err("Lookup failed:");
			console.logException(e);
			e.printStackTrace();
		}
		ServiceRegistrar registrar = null;
		try {
			registrar = lookup.getRegistrar();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			console.err("Registrar search failed:");
			console.logException(e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			console.err("Class Not Found:");
			console.logException(e);
			e.printStackTrace();
		}
		Class[] classes = new Class[] { RemoteProActiveRuntime.class };
		ServiceTemplate template = new ServiceTemplate(null, classes, null);
		ServiceMatches matches = null;
		try {
			matches = registrar.lookup(template, Integer.MAX_VALUE);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			console.logException(e);
			e.printStackTrace();
		}

		if (matches.totalMatches > 0) {
			for (int i = 0; i < matches.items.length; i++) {
				//check if it is really a node and not a ProactiveRuntime
				String jiniName = matches.items[i].attributeSets[0].toString();
				if ((jiniName.indexOf("PA_JVM") != -1)) {
					// it is a runtime
					/*int k = jiniName.indexOf("=");*/
					/*String name = jiniName.substring(k + 1, jiniName.length() - 1);*/
					
					if (matches.items[i].service == null) {
						console.warn("Service : NULL !!!");
					}
					else {
						RemoteProActiveRuntime runtime = (RemoteProActiveRuntime) matches.items[i].service;
                        ProActiveRuntime part = null;
						try {
							part = new ProActiveRuntimeAdapterImpl(runtime);
						} catch (ProActiveException e) {
							// TODO Auto-generated catch block
							console.logException(e);
							e.printStackTrace();
						}
                        runtimes.add(part);
                    }
				}
			}
		}
		else {
			console.err("JiniRTListener: No Service");
		}

		return null;
	}

}
