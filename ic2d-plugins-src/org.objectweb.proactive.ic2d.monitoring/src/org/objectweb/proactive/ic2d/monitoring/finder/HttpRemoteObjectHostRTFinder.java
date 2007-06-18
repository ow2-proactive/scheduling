package org.objectweb.proactive.ic2d.monitoring.finder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;

public class HttpRemoteObjectHostRTFinder implements HostRTFinder {

	public List<ProActiveRuntime> findPARuntime(HostObject host) {
		
		Console console = Console.getInstance(Activator.CONSOLE_NAME);
		
		console.log("Exploring "+host+" with Http on port "+host.getPort());
		
		List<ProActiveRuntime> runtimes = new ArrayList<ProActiveRuntime>();
		
		//ProActiveRuntimeAdapterImpl adapter = null;
		try {
//			adapter = new ProActiveRuntimeAdapterImpl(new HttpProActiveRuntime(
//			        UrlBuilder.buildUrl(host.getHostName(), "", Constants.XMLHTTP_PROTOCOL_IDENTIFIER, host.getPort())));
//			

			
			
			URI url = new URI(host.getProtocol(),null,host.getHostName(),host.getPort(),"/",null,null);
			
			URI[] remoteObjectUris = RemoteObjectFactory.getRemoteObjectFactory(Constants.XMLHTTP_PROTOCOL_IDENTIFIER).list(url);
			
			for (int i = 0 ; i < remoteObjectUris.length; i++) {
				RemoteObject rro =  RemoteObjectFactory.getRemoteObjectFactory(Constants.XMLHTTP_PROTOCOL_IDENTIFIER).lookup(remoteObjectUris[i]);
				Object stub = rro.getObjectProxy();
				if (stub instanceof ProActiveRuntime) {
				runtimes.add((ProActiveRuntime ) stub);
			}

			}
			
//			System.out.println("RMIHostRTFinder.findPARuntime()  "  + stub);
		} catch (ProActiveException e) {
			// TODO Auto-generated catch block
			console.logException(e);
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		runtimes.add(adapter);
		return runtimes;
	}

}
