package org.objectweb.proactive.core.component;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.type.InterfaceType;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;

import java.io.Serializable;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


/**
 * A bindings container.
 * This class stores the following bindings for a given component :
 * - thisComponent.clientInterface --> serverComponent.serverInterface
 *         (it also takes in charge collective bindings, ie 1 client to serveral servers)
 * - thisParallelComponent.serverInterface --> serverComponents.serverInterface
 *         (in the case of a parallel component, requests on a server port are forwarded to
 *         the inner components)
 *
 *
 *
 * @author Matthieu Morel
 */
public class Bindings implements Serializable {
    private Hashtable clientInterfaceBindings;
    private Hashtable parallelInternalClientInterfaceBindings;

    // key = interfaceName ; value = binding
    // if collective binding : key = interfaceName ; value = Vector (Binding objects)
    public Bindings() {
        clientInterfaceBindings = new Hashtable();
    }

    /**
     * @param binding the binding to add
     */
    public void add(Binding binding) {
        try {
            InterfaceType client_itf_type = (InterfaceType) binding.getClientInterface()
                                                                   .getFcItfType();
            if (client_itf_type.isFcCollectionItf()) {
                addCollectiveBindingOnExternalClientItf(binding);
            } else if (((ComponentParametersController) binding.getClientInterface()
                            .getFcItfOwner().getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                            .getHierarchicalType().equals(Constants.PARALLEL)) {
                addCollectiveBindingOnInternalClientItf(binding);
            } else {
                clientInterfaceBindings.put(client_itf_type.getFcItfName(),
                    binding);
            }
        } catch (NoSuchInterfaceException nsie) {
            throw new ProActiveRuntimeException("interface not found : " +
                nsie.getMessage());
        }
    }

    // returns either a Binding or a Vector of Binding objects (collection interface case)
    
    /**
     * removes the binding on the given client interface
     */
    public Object remove(String clientItfName) {
        return clientInterfaceBindings.remove(clientItfName);
    }

    public Object get(String clientItfName) {
        return clientInterfaceBindings.get(clientItfName);
    }

	/**
	 * tests if binding exists on the given interface
	 * @param clientItfName the client inteface to check
	 * @return true if binding exists
	 */
    public boolean containsBindingOn(String clientItfName) {
        return clientInterfaceBindings.containsKey(clientItfName);
    }

    //	/**
    //	* returns all the bindings, including the bindings for the
    //	* collective interfaces (meaning there can be several Binding objects
    //	* with the same client interface)
    //	* 
    //	* @return all the bindings 
    //	*/
    //	public Binding[] getBindings() {
    //		Vector list_of_bindings = new Vector();
    //		Enumeration enum = clientInterfaceBindings.elements();
    //		while (enum.hasMoreElements()) {
    //			Object elem = enum.nextElement();
    //			if (elem instanceof Collection) {
    //				// a collective binding : add all the elements of the corresponding collection
    //				list_of_bindings.addAll((Collection) elem);
    //			} else {
    //				list_of_bindings.addElement(elem);
    //			}
    //		}
    //		
    //		list_of_bindings.trimToSize();
    //		return (Binding[]) (list_of_bindings.toArray(new Binding[list_of_bindings.size()]));
    //	}

    /**
     * Returns the names of the external client bindings for this component.
     * In case of a collective interface, the names of each of its constituing interfaces are not returned ;
     * only the name of the collective interface is returned.
     */
    public String[] getExternalClientBindings() {
        return (String[]) clientInterfaceBindings.keySet().toArray(new String[clientInterfaceBindings.keySet()
                                                                                                     .size()]);
    }

    private void addCollectiveBinding(Map bindingsTable, Binding binding) {
        String clientItfName = binding.getClientInterface().getFcItfName();
        if (bindingsTable.containsKey(clientItfName)) {
            // there should be a Vector for containing the bindings associated
            ((Vector) bindingsTable.get(clientItfName)).add(binding);
        } else { // we create a Vector for keeping the bindings
            Vector bindings_collection = new Vector();
            bindings_collection.add(binding);
            bindingsTable.put(clientItfName, bindings_collection);
        }
    }

    private void addCollectiveBindingOnExternalClientItf(Binding binding) {
        addCollectiveBinding(clientInterfaceBindings, binding);
    }

    private void addCollectiveBindingOnInternalClientItf(Binding binding) {
        if (parallelInternalClientInterfaceBindings == null) {
            parallelInternalClientInterfaceBindings = new Hashtable();
        }
        addCollectiveBinding(parallelInternalClientInterfaceBindings, binding);
    }
}
