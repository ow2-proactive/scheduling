package net.coregrid.gcmcca.example;

import mocca.cca.CCAException;
import mocca.cca.Component;
import mocca.cca.Services;

public class CCAHelloComponent implements HelloPort, Component {

	public void setServices(Services services) {
		try {
			services.addProvidesPort(this, "h", HelloPort.class.getName(), null);
		} catch (CCAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String hello(String s) {
		System.err.println("Server received: " + s);
		return s;
	}

}
