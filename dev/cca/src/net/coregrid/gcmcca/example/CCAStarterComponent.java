package net.coregrid.gcmcca.example;

import mocca.cca.CCAException;
import mocca.cca.Services;
import mocca.cca.Port;
import mocca.cca.ports.GoPort;

public class CCAStarterComponent implements GoPort, mocca.cca.Component {

	
	private HelloPort helloPort;
	private Services services;
	
	public int go() {
		try {
			helloPort = (HelloPort) services.getPort("h");
			System.err.println("CCA Starter Component received: " + helloPort.hello("Hello!!!"));
			services.releasePort("h");
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public void setServices(Services services) {
		this.services = services;
		try {
			services.addProvidesPort((Port) this, "MyGoPort", GoPort.class.getName(), null);
			services.registerUsesPort("h", HelloPort.class.getName(), null);
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
