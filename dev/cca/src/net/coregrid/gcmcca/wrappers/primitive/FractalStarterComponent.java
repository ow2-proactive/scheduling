package net.coregrid.gcmcca.wrappers.primitive;


import net.coregrid.gcmcca.example.CCAStarterComponent;
import mocca.cca.ports.GoPort;


public class FractalStarterComponent extends AbstractCCAComponent implements GoPort {

		
	public FractalStarterComponent() {
		//ProActive does not work without this empty constructor
	}
	
	public FractalStarterComponent(String a) throws Exception{
		//this code should be generated
		super(CCAStarterComponent.class.getName());
		System.err.println("Constructor received parameters: " + a);
	}
	
	public int go() {
		//this delegation code should be generated
		return ((GoPort)getProvidesPort("MyGoPort")).go();
	}

}
