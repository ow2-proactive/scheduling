package org.objectweb.proactive.core.component.adl.component;

import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Implementtion of an interface of a component
 * @author Nicolas Dalmasso
 *
 */
public class ADL2NInterfaceImpl implements ADL2NInterface{
	/**Name and signature of the interface*/
	private String name,signature;
	/**List of methods on the interface*/
	private Vector methods;
	/**Array of methods on the interface*/
	Method[] methodsArray;
	/**True if the interface is a client one*/
	boolean isClient;
	/**Component on wich the interface belongs*/
	ADL2NComponent component;
	/**List of interfaces binded with this one*/
	private Vector bindings;
	
	/**
	 * Constructs the interface
	 * @param name Name of the interface
	 * @param signature Signature of the interface
	 */
	public ADL2NInterfaceImpl(String name,String signature){
		this.name = name;
		this.signature = signature;
		methods = new Vector();
		bindings = new Vector();
		try {
			Class c = Class.forName(signature);
			Method[] methodsArray = c.getMethods();
			this.methodsArray = methodsArray;
			for(int i=0;i<methodsArray.length;i++)
				addMethod(methodsArray[i]);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the name of the interface
	 * @return Name of this interface
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the interface
	 * @param name Name of this interface
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get all methods on this interface
	 * @return Array of java.lang.reflect.Method on this interface
	 */
	public Method[] getMethods() {
		return methodsArray;
	}

	/**
	 * Add a method in the vectors of methods on this interface
	 * @param method Method added in the vector of methods on this interface
	 */ 
	public void addMethod(Method method) {
		methods.add(method);
	}

	/**
	 * Removes a method in the vectors of methods on this interface
	 * @param method Method removed in the vector of methods on this interface
	 */
	public void removeMethod(Method method) {
		methods.remove(method);
	}

	/**
	 * Is this a client or a server interface
	 * @return True if this interface is a client interface
	 */
	public boolean isClientInterface() {
		return isClient;
	}

	/**
	 * Defines this interface as a client interface
	 *
	 */
	public void setInterfaceAsClient() {
		isClient = true;
	}

	/**
	 * Defines this interface as a server interface
	 *
	 */
	public void setInterfaceAsServer() {
		isClient = false;
	}
	
	/**
	 * Brief representation of the interface
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("<interface>\n");
		sb.append("\tName: "+name+"\n");
		sb.append("\tSignature: "+signature+"\n");
		sb.append("\tRole: "+(isClient ? "Client" : "Server")+"\n");
		sb.append("</interface>\n");
		return sb.toString();
	}

	/**
	 * Gets the components wich this interface belong to
	 * @return Component associated with this interface
	 */
	public ADL2NComponent getComponent() {
		return component;
	}
	
	/**
	 * Sets the components wich this interface belong to
	 * @param component Component wich this interface belong to
	 */
	public void setComponent(ADL2NComponent component){
		this.component = component;
	}
	
	/**
	 * Add a binding beetween this interface and the one passed by parameter
	 * @param itf Interface to bind with this one
	 */
	public void addBinding(ADL2NInterface itf,boolean isClient){
		this.isClient = isClient;
		itf.setRole(!isClient);
		bindings.add(itf);
	}

	/**
	 * Sets an interface as client or server one
	 * @param b Role of this interface
	 */
	public void setRole(boolean b) {
		isClient = b;
	}

	/**
	 * Gets all interfaces binded with this component
	 * @return Array of interfaces
	 */
	public Vector getBindings() {
		return bindings;
	}
	
	/**
	 * Redefinition of the method equals on an interface
	 */
	public boolean equals(Object itf){
		if(! (itf instanceof ADL2NInterface))
			return false;
		ADL2NInterface litf = (ADL2NInterface) itf;
		if(!litf.getName().equals(this.getName()))
			return false;
		return true;
	}
}
