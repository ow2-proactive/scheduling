package net.coregrid.gcmcca.wrappers.primitive;

import java.util.*;
import mocca.cca.CCAException;
import mocca.cca.ComponentID;
import mocca.cca.ComponentRelease;
import mocca.cca.Port;
import mocca.cca.Services;
import mocca.cca.TypeMap;

/**
 * This class serves as a dummy implementation of cca.Services interface and 
 * stores types and names of ports a component exposes. 
 * The types may be then used for introspection, creation of ADL etc.
 * @author malawski
 * 
 */
public class IntrospectionServices implements Services {
	
	private Map usesPortTypes = new HashMap();
	private Map providesPortTypes = new HashMap();

	public Map getProvidesPortTypes() {
		return providesPortTypes;
	}

	public Map getUsesPortTypes() {
		return usesPortTypes;
	}

	public String getProvidesPortType(String providesPortName) {
		return (String) providesPortTypes.get(providesPortName);
	}
	
	public String getUsesPortType(String providesPortName) {
		return (String) usesPortTypes.get(providesPortName);
	}
	
	
	public Port getPort(String portName) throws CCAException {
		// TODO Auto-generated method stub
		return null;
	}

	public Port getPortNonblocking(String portName) throws CCAException {
		// TODO Auto-generated method stub
		return null;
	}

	public void releasePort(String portName) throws CCAException {
		// TODO Auto-generated method stub

	}

	public TypeMap createTypeMap() throws CCAException {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerUsesPort(String portName, String type,
			TypeMap properties) throws CCAException {
		usesPortTypes.put(portName, type);

	}

	public void unregisterUsesPort(String portName) throws CCAException {
		// TODO Auto-generated method stub

	}

	public void addProvidesPort(Port inPort, String portName, String type,
			TypeMap properties) throws CCAException {
		providesPortTypes.put(portName, type);

	}

	public TypeMap getPortProperties(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeProvidesPort(String portName) throws CCAException {
		// TODO Auto-generated method stub

	}

	public ComponentID getComponentID() {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerForRelease(ComponentRelease callBack)
			throws CCAException {
		// TODO Auto-generated method stub

	}

}
