package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Binding;
import org.objectweb.proactive.core.component.Bindings;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;

/**
 * Abstract implementation of BindingController.
 * 
 * It defines common operations of both primitive and composite binding controllers.
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveBindingController
	extends ProActiveController
	implements BindingController, Serializable {
	//private Vector bindings; // contains Binding objects
	//private Hashtable bindings; // key = clientInterfaceName ; value = Binding
	protected static Logger logger = Logger.getLogger(ProActiveBindingController.class.getName());
	private Bindings bindings; // key = clientInterfaceName ; value = Binding
	protected Hashtable groupBindings;

	public ProActiveBindingController(Component owner) {
		super(owner, Constants.BINDING_CONTROLLER);
		bindings = new Bindings();
	}

	public void addBinding(Binding binding) {
		//bindings.put(binding.getClientInterface().getFcItfName(), binding);
		bindings.add(binding);
	}

	protected boolean existsBinding(String clientItfName) {
		return bindings.containsBindingOn(clientItfName);
	}

	protected boolean existsClientInterface(String clientItfName) {
		try {
			return (getFcItfOwner().getFcInterface(clientItfName) != null);
		} catch(NoSuchInterfaceException nsie) {
			logger.error("interface not found : " + nsie.getMessage());
			throw new ProActiveRuntimeException(nsie);
		}
	}

	protected void checkBindability(String clientItfName, Interface serverItf)
		throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		if (!existsClientInterface(clientItfName)) {
			throw new NoSuchInterfaceException(clientItfName + " is not a client interface");
		}
		if (existsBinding(clientItfName)) {
			// cases where multiple bindings are allowed : 
			// - external server interface of a parallel component
			// - interface defined as 'collective'
			if (((ComponentParametersController) getFcItfOwner().getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
				.getComponentParameters()
				.getHierarchicalType()
				.equals(ComponentParameters.PARALLEL)) {
				if (((ProActiveInterfaceType) ((Interface)getFcItfOwner().getFcInterface(clientItfName)).getFcItfType())
					.isFcClientItf()) {
					throw new IllegalBindingException("component is parallel, but " + clientItfName + " is not a server port");
				}
			} else if (
				!((ProActiveInterfaceType)((Interface) getFcItfOwner().getFcInterface(clientItfName)).getFcItfType())
					.isFcCollectionItf()) {
				throw new IllegalBindingException(clientItfName + " is already bound");
			}

		}
		// TODO : check if binding is between a client and a server interface
		// see next, but need to consider internal interfaces (i.e. valid if server AND internal)
		
//		if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf()) {
//			throw new IllegalBindingException(serverItf.getFcItfName() + " is not a server interface");
//		}
		// TODO : other checks are to be performed (viability of the bindings)

		if (((LifeCycleController) getFcItfOwner().getFcInterface(Constants.LIFECYCLE_CONTROLLER))
			.getFcState()
			!= LifeCycleController.STOPPED) {
			throw new IllegalLifeCycleException("component has to be stopped to perform binding operations");
		}
	}

	protected void checkUnbindability(String clientItfName)
		throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
		checkLifeCycleIsStopped();
		if (!existsClientInterface(clientItfName)) {
			throw new NoSuchInterfaceException(clientItfName + " is not a client interface");
		}
		if (!existsBinding(clientItfName)) {
			throw new IllegalBindingException(clientItfName + " is not yet bound");
		}

	}
	/**
	 * 
	 * @param the name of the client interface
	 * @return a Binding object if single binding, Vector of Binding objects otherwise
	 */
	public Object removeBinding(String clientItfName) {
		return bindings.remove(clientItfName);
	}

	/**
	 * 
	 * @param the name of the client interface
	 * @return a Binding object if single binding, Vector of Binding objects otherwise
	 */
	public Object getBinding(String clientItfName) {
		return bindings.get(clientItfName);
	}

	/**
		 * @see org.objectweb.fractal.api.control.BindingController#lookupFc(String)
		 */
	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
		if (!existsBinding(clientItfName)) {
			throw new NoSuchInterfaceException(clientItfName);
		} else {
			// FIXME : cannot return 1 interface reference if we have a group? 
			//return ((Binding) getBinding((clientItfName)).getServerInterface();
			if (getBinding(clientItfName) instanceof Collection) {
				logger.error(
					"you are looking up a collection of bindings. This method cannot return one single Interface object");
				return null;
			} else {
				return ((Binding) getBinding(clientItfName)).getServerInterface();
			}
		}
	}
	

	/**
	 * implementation of the interface BindingController
	 */
	public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException{
		checkBindability(clientItfName, (Interface)serverItf);
		if ((Fractal.getComponentParametersController(getFcItfOwner())).getComponentParameters().getHierarchicalType().equals(ComponentParameters.PRIMITIVE)) {
			primitiveBindFc(clientItfName, (Interface) serverItf);
		} else {
			compositeBindFc(clientItfName, (Interface)serverItf);
		}
	}
	
	
	
	private void primitiveBindFc(String clientItfName, Interface serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException{
		// delegate binding operation to the reified object
		BindingController user_binding_controller = (BindingController)( (ProActiveComponent)getFcItfOwner()).getReifiedObject();

		// serverItf cannot be a Future (because it has to be casted) => make sure if binding to a composite's internal interface
		serverItf = (Interface)ProActive.getFutureValue(serverItf);
		user_binding_controller.bindFc(clientItfName, serverItf);
		addBinding(new Binding((Interface)getFcItfOwner().getFcInterface(clientItfName), serverItf));
	}

	
	/**
	 * binding method enforcing Interface type for the server interface, for composite components
	 */
	private void compositeBindFc(String clientItfName, Interface serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException{

		// link client interface impl object to server interface impl object
		Object impl = serverItf;
		ProActiveInterface clientItf =
			(ProActiveInterface) getFcItfOwner().getFcInterface(clientItfName);
	
		InterfaceType client_itf_type =
			((ComponentParametersController) getFcItfOwner()
				.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
				.getComponentParameters()
				.getComponentType()
				.getFcInterfaceType(clientItfName);
		// if we have a collection interface, the impl object is actually a group of references to components (type = Component)
		// Thus we have to add the link to the new component in this group
		if (client_itf_type.isFcCollectionItf()
			|| ((ComponentParametersController) getFcItfOwner()
				.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
				.getComponentParameters()
				.getHierarchicalType()
				.equals(ComponentParameters.PARALLEL)) {
			if (ProActiveGroup.isGroup(clientItf.getFcItfImpl())) {
				Group itf_ref_group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
				itf_ref_group.add(((Interface)serverItf).getFcItfOwner());
				if (logger.isDebugEnabled()) {
					logger.debug(
						"performed collective binding : "
							+ ((ComponentParametersController) getFcItfOwner()
								.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
								.getComponentParameters()
								.getName()
							+ '.'
							+ clientItfName
							+ " --> "
							+ ((ComponentParametersController) ((Interface)serverItf).getFcItfOwner()
								.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
								.getComponentParameters()
								.getName()
							+ '.'
							+ ((Interface)serverItf).getFcItfName());
				}
			}
		} else {
			clientItf.setFcItfImpl(serverItf);
		}
		addBinding(new Binding(clientItf, (Interface)serverItf));
	
	}

	/**
		 * @see org.objectweb.fractal.api.control.BindingController#unbindFc(String)
		 * 
		 * CAREFUL : unbinding action on collective interfaces will remove all the bindings to this interface.
		 * This is also the case when removing bindings from the server interface of a parallel component 
		 *  (yes you can do unbindFc(parallelServerItfName) !)
		*/
	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException{ // remove from bindings and set impl object to null
		checkUnbindability(clientItfName);
		ProActiveInterface clientItf =
			(ProActiveInterface) getFcItfOwner().getFcInterface(clientItfName);
		if (clientItf == null) {
			throw new NoSuchInterfaceException(clientItfName);
		}
		// FIXME : pb? -- this is actually corresponding to the Fractal specification 1.0.6
		// in the case of a collection interface, we actually remove ALL THE BINDINGS to this interface
		if (ProActiveGroup.isGroup(clientItf.getFcItfImpl())) {
			// we do not set the delegatee to null, the delegatee being a group, 
			// but we remove all the elements of this group
			Group group = ProActiveGroup.getGroup(clientItf.getFcItfImpl());
			group.clear();
			removeBinding(clientItfName);
	
		} else {
			Binding binding = (Binding) (removeBinding(clientItfName));
			((ProActiveInterface) (binding.getClientInterface())).setFcItfImpl(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.fractal.api.control.BindingController#listFc()
	 */
	public String[] listFc() {
		return null;
	}

}