package org.objectweb.proactive.ic2d.monitoring.finder;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeAdapterImpl;
import org.objectweb.proactive.core.runtime.http.HttpProActiveRuntime;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.monitoring.Activator;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;

public class HttpHostRTFinder implements HostRTFinder {

	public List<ProActiveRuntime> findPARuntime(HostObject host) {
		
		Console console = Console.getInstance(Activator.CONSOLE_NAME);
		
		console.log("Exploring "+host+" with Http on port "+host.getPort());
		
		List<ProActiveRuntime> runtimes = new ArrayList<ProActiveRuntime>();
		
		ProActiveRuntimeAdapterImpl adapter = null;
		try {
			adapter = new ProActiveRuntimeAdapterImpl(new HttpProActiveRuntime(
			        UrlBuilder.buildUrl(host.getHostName(), "", Constants.XMLHTTP_PROTOCOL_IDENTIFIER, host.getPort())));
		} catch (ProActiveException e) {
			// TODO Auto-generated catch block
			console.logException(e);
			e.printStackTrace();
		}
		runtimes.add(adapter);
		return runtimes;
	}

}
