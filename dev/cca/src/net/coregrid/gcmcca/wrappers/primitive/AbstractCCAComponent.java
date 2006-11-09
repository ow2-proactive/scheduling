package net.coregrid.gcmcca.wrappers.primitive;

import java.util.*;

import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.ComponentRelease;
import mocca.cca.Port;
import mocca.cca.TypeMap;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

public abstract class AbstractCCAComponent implements BindingController, mocca.cca.Services {

	//wrapped CCA component
	mocca.cca.Component ccaComponent;
	//map for storing references to clientInterfaces (= uses ports)
	private Map clientInterfaces = new HashMap();
	//map for storing references to serverInterfaces (= provides prots) 
	private Map providesPorts = new HashMap();
	
	
	public AbstractCCAComponent() {
		//ProActive does not work without this empty constructor
	}
	
	public AbstractCCAComponent(String componentClass) throws Exception {
		//this is called by passing parameters to ContentDescription
		//here we instantiate the CCA component
		ccaComponent = (mocca.cca.Component) Class.forName(componentClass).newInstance();
		//here our CCA component registers its ports and we heep them in a hashmap
		ccaComponent.setServices(this);
		
	}

	public Port getProvidesPort(String name) {
		return (Port) providesPorts.get(name);
	}

	
	public void bindFc(String name, Object sItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		//here we add the port to the hashmap and make it available for CCA Component via getPort()
		clientInterfaces.put(name, sItf);
	}

	public String[] listFc() {
	//here we list our registered ports
		return (String[]) clientInterfaces.keySet().toArray(new String[clientInterfaces.size()]);	
	}

	public Object lookupFc(String cItf) throws NoSuchInterfaceException {
		//here we return our registered ports		
		try {
			return getPort(cItf);
		} catch (CCAException e) {
			throw new NoSuchInterfaceException(cItf);
		}
	}

	public void unbindFc(String cItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		//here we remove hashmap entry
		clientInterfaces.remove(cItf);
	}

	public void addProvidesPort(Port port, String name, String type, TypeMap arg3) throws CCAException {
		// here we add a Port to the hashmap
		providesPorts.put(name, port);
		
	}


	public void registerUsesPort(String arg0, String arg1, TypeMap arg2) throws CCAException {
		//here we do nothing for the moment, since uses ports (client interfaces)
		//are known from the component type and handled by BindFc
	}

	public Port getPort(String name) throws CCAException {
		return (Port) clientInterfaces.get(name); //here we return the interface from the hashmap which was bound using bindFc 
	}

	
	public TypeMap createTypeMap() throws CCAException {
		return new mocca.srv.impl.MoccaTypeMap();
	}

	public ComponentID getComponentID() {
		// TODO Auto-generated method stub
		return null;
	}


	public Port getPortNonblocking(String arg0) throws CCAException {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeMap getPortProperties(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerForRelease(ComponentRelease arg0) throws CCAException {
		// TODO Auto-generated method stub
		
	}

	public void releasePort(String arg0) throws CCAException {
		// TODO Auto-generated method stub
		
	}

	public void removeProvidesPort(String arg0) throws CCAException {
		// TODO Auto-generated method stub
		
	}

	public void unregisterUsesPort(String arg0) throws CCAException {
		// TODO Auto-generated method stub
		
	}


	
}
