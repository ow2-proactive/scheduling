package net.coregrid.gcmcca.wrappers.composite;

import java.util.Map;
import java.util.HashMap;

import mocca.cca.ComponentID;


/**
 * @author malawski
 *
 * The class used to keep runtime parameters of a wrapped CCA system:
 * a list of port names and their component IDs 
 * Uses ports of client CCA components
 * Provides ports of server CCA components
 */
public class CCACompositeDescriptor implements java.io.Serializable {

	private Map clientComponents = new HashMap();
	private Map serverComponents = new HashMap();
	private Map glueClassPaths = new HashMap();
	private Map portTypes = new HashMap();
	
	public void addUsesPort(String portName, String componentID) {
		clientComponents.put(portName, componentID);
	}

	public void addProvidesPort(String portName, String componentID) {
		serverComponents.put(portName, componentID);
	}

	public String getClientComponentID (String portName) {
		return (String) clientComponents.get(portName);
	}

	public String getServerComponentID (String portName) {
		return (String) serverComponents.get(portName);
	}
	
	public String[] listUsesPortNames() {
		// here we list our registered uses ports
		return (String[]) clientComponents.keySet().toArray(
				new String[clientComponents.size()]);
	}

	public String[] listProvidesPortNames() {
		//here we list our registered provides ports
		return (String[]) serverComponents.keySet().toArray(
				new String[serverComponents.size()]);
	}
	
	public void setClassPath (String portName, String classPath) {
		glueClassPaths.put(portName, classPath);
	}
	
	public String getClassPath (String portName) {
		return (String) glueClassPaths.get(portName);
	}
	
	public void setType (String portName, String className) {
		portTypes.put(portName, className);
	}
	
	public String getType(String portName) {
		return (String) portTypes.get(portName);
	}
	
	public String getClientGlueClassName (String portName) {
		return "glue." + getType(portName) + "CCAFractalGlueComponent";
	}
	
	public String getServerGlueClassName (String signature) {
		return "glue." + signature + "FractalGlueComponent";
	}
}
