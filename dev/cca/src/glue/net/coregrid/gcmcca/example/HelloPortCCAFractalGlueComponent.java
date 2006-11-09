package glue.net.coregrid.gcmcca.example;

import java.io.IOException;
import java.util.Collection;

import javax.naming.NamingException;

import mocca.cca.CCAException;
import mocca.cca.Component;
import mocca.cca.Port;
import mocca.cca.Services;
import mocca.cca.TypeMap;
import mocca.cca.ports.BasicParameterPort;
import mocca.cca.ports.GoPort;
import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.wrappers.composite.GlueControlPort;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.util.UrlBuilder;

public class HelloPortCCAFractalGlueComponent implements Component, HelloPort, BindingController, GlueControlPort {

	private TypeMap properties;
	private org.objectweb.fractal.api.Component fractalComponent;
	private HelloPort helloPort;
	
	public void setServices(Services services) throws CCAException {
		// TODO Auto-generated method stub
		services.addProvidesPort(this, "glue-control", GlueControlPort.class
				.getName(), null);

		try {
			System.setProperty("fractal.provider","org.objectweb.proactive.core.component.Fractive");
			org.objectweb.fractal.api.Component boot = org.objectweb.fractal.api.Fractal
					.getBootstrapComponent();
			TypeFactory tf = Fractal.getTypeFactory(boot);

			// type of client component
			ComponentType cType = tf.createFcType(new InterfaceType[] {
					//server interface of the glue
					tf.createFcItfType("hs", HelloPort.class.getName(),
	                        false, false, false),
	                //client interface of the glue
					tf.createFcItfType("h", HelloPort.class.getName(), true,
							false, false) });
	        GenericFactory cf = Fractal.getGenericFactory(boot);
	        // create client component
	        fractalComponent = cf.newFcInstance(cType,
	                new ControllerDescription("StarterComponent", Constants.PRIMITIVE),
	                new ContentDescription(HelloPortCCAFractalGlueComponent.class.getName())); // other properties could be added (activity for example)

	        //here we pass the fractal interface to the CCA framework
	        services.addProvidesPort((Port) fractalComponent.getFcInterface("hs"), "h", HelloPort.class.getName(), null);
	        

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public String hello(String s) {
		// TODO Auto-generated method stub
		try {			
			System.out.println("CCAFractalGlue: hello invoked!");

			return helloPort.hello("Glue received: " + s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public String getComponentURL() {
		String URL = UrlBuilder.buildUrlFromProperties("localhost", "hello");
		// get localhost address
			try {
				Fractive.register(fractalComponent, URL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return URL;

	}

	public void setComponent(org.objectweb.fractal.api.Component fractalComponent) {
//		this.fractalComponent = fractalComponent;
//		System.out.println("Received reference to component: " + fractalComponent.toString());
//		try {
//			helloPort = (HelloPort) fractalComponent.getFcInterface("h");
//		} catch (NoSuchInterfaceException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
