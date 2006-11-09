package glue.net.coregrid.gcmcca.example;

import java.io.IOException;
import java.util.Collection;

import javax.naming.NamingException;

import mocca.cca.CCAException;
import mocca.cca.Component;
import mocca.cca.Services;
import mocca.cca.TypeMap;
import mocca.cca.ports.BasicParameterPort;
import net.coregrid.gcmcca.example.HelloPort;
import net.coregrid.gcmcca.wrappers.composite.GlueControlPort;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Fractive;

public class HelloPortCCAGlueComponent implements Component, HelloPort, BasicParameterPort, GlueControlPort {

	private TypeMap properties;
	private org.objectweb.fractal.api.Component fractalComponent;
	private HelloPort helloPort;
	
	public void setServices(Services services) throws CCAException {
		// TODO Auto-generated method stub
		services.addProvidesPort(this, "h", HelloPort.class.getName(), null);
		services.addProvidesPort(this, "config", BasicParameterPort.class.getName(), null);
		services.addProvidesPort(this, "glue-control", GlueControlPort.class.getName(), null);
	}

	public String hello(String s) {
		// TODO Auto-generated method stub
		try {			
			System.out.println("CCAGlue: hello invoked!");

			return helloPort.hello("Glue received: " + s);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	public Collection readConfigurationKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeMap readConfigurationMap() throws CCAException {
		// TODO Auto-generated method stub
		return properties;
	}

	public void writeConfigurationMap(TypeMap map) throws CCAException {
		this.properties = map.cloneTypeMap();
		String fractalComponentURL = properties.getString("URL", null);
		try {
			System.out.println("CCAGlue: looking up component: " + fractalComponentURL);
			fractalComponent = Fractive.lookup(fractalComponentURL);
			System.out.println("CCAGlue: getting content controller.");
//			ContentController contentController = Fractal.getContentController(fractalComponent);
			System.out.println("CCAGlue: getting internal interface");
			helloPort = (HelloPort) fractalComponent.getFcInterface("h");
//			helloPort = (HelloPort) contentController.getFcInternalInterface("h");
			System.out.println("CCAGlue: got internal interface to HeloPort!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getComponentURL() {
		return null;
	}

	public void setComponent(org.objectweb.fractal.api.Component fractalComponent) {
		this.fractalComponent = fractalComponent;
		System.out.println("Received reference to component: " + fractalComponent.toString());
		try {
			helloPort = (HelloPort) fractalComponent.getFcInterface("h");
		} catch (NoSuchInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
