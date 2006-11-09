package net.coregrid.gcmcca.example;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import mocca.cca.ports.GoPort;


public class StarterComponent implements GoPort, BindingController {

	
	private HelloPort helloPort;
	
	public StarterComponent() {
		//ProActive does not work without this empty constructor
	}
	
	public StarterComponent(String a) {
		//this is called by passing parameters to ContentDescription
		System.err.println("Constructor received parameters: " + a);
	}
	public int go() {
		System.err.println(helloPort.hello("Hello!!!"));
		return 0;
	}

	public void bindFc(String name, Object sItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (name.equals("h"))
			helloPort = (HelloPort) sItf;
	}

	public String[] listFc() {
	       return new String[] { "h" };	}

	public Object lookupFc(String cItf) throws NoSuchInterfaceException {
		if (cItf.equals("h")) 
			return helloPort;
		return null;
	}

	public void unbindFc(String cItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (cItf.equals("h"))
			helloPort = null;
	}

}
