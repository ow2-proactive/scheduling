package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;

/**
 * A bindings container.
 * This class stores the following bindings for a given component :
 * - thisComponent.clientInterface --> serverComponent.serverInterface
 * 	(it also takes in charge collective bindings, ie 1 client to serveral servers)
 * - thisParallelComponent.serverInterface --> serverComponents.serverInterface
 * 	(in the case of a parallel component, requests on a server port are forwarded to
 * 	the inner components)
 * 
 *   
 * 
 * @author Matthieu Morel
 */
public class Bindings implements Serializable {

	private Hashtable clientInterfaceBindings;
	// key = interfaceName ; value = binding
	// if collective binding : key = interfaceName ; value = Vector (Binding objects)

	public Bindings() {
		clientInterfaceBindings = new Hashtable();
	}

	/**
	 * @param binding
	 */
	public void add(Binding binding) {
		try {
		InterfaceType client_itf_type = (InterfaceType) binding.getClientInterface().getFcItfType();
		if (client_itf_type.isFcCollectionItf()
			|| ((ComponentParametersController) binding
				.getClientInterface()
				.getFcItfOwner()
				.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
				.getComponentParameters()
				.getHierarchicalType()
				.equals(ComponentParameters.PARALLEL)) {
			addCollectiveBinding(binding); } else {
			clientInterfaceBindings.put(client_itf_type.getFcItfName(), binding); }
		} catch (NoSuchInterfaceException nsie) {
			throw new ProActiveRuntimeException("interface not found : " + nsie.getMessage());
		}
	} 
	
	// returns either a Binding or a Vector of Binding objects (collection interface case)
	public Object remove(String clientItfName) {
		return clientInterfaceBindings.remove(clientItfName); }

	public Object get(String clientItfName) {
		return clientInterfaceBindings.get(clientItfName); }

	public boolean containsBindingOn(String clientItfName) {
		return clientInterfaceBindings.containsKey(clientItfName);
			} /**
		 * returns all the bindings, including the bindings for the
		 * collective interfaces (meaning there can be several Binding objects
		 * with the same client interface)
		 * 
		 * @return all the bindings 
		 */
	public Binding[] getBindings() {
		Vector list_of_bindings = new Vector();
			Enumeration enum = clientInterfaceBindings.elements();
			while (enum.hasMoreElements()) {
			Object elem = enum.nextElement();
				if (elem
					instanceof Collection) {
				// a collective binding : add all the elements of the corresponding collection
			list_of_bindings
						.addAll((Collection) elem);
					} else {
				list_of_bindings.addElement(elem); }
		}
		list_of_bindings.trimToSize();
			return (Binding[]) (list_of_bindings.toArray(new Binding[list_of_bindings.size()]));
			}

	private void addCollectiveBinding(Binding binding) {
		String clientItfName = binding.getClientInterface().getFcItfName();
			if (clientInterfaceBindings
				.containsKey(
					clientItfName)) { // there should be a Vector for containing the bindings associated
		 (
						(Vector) clientInterfaceBindings
						.get(clientItfName))
				.add(binding);
				} else { // we create a Vector for keeping the bindings
		Vector bindings_collection = new Vector();
				bindings_collection.add(binding);
				clientInterfaceBindings.put(clientItfName, bindings_collection);
				}
	}

}
