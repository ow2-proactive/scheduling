package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import javax.naming.NamingException;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.omg.CosNaming.NamingContext;

/**
 * Base class for all component controllers.
 * 
 * @author Matthieu Morel
 *
 */
public abstract class ProActiveController implements Interface, Serializable {
	private Component owner;
	private boolean isInternal = true;
	private InterfaceType interfaceType;
	private String controllerName;

	/**
	 * Constructor for ProActiveController.
	 */
	public ProActiveController(Component owner, String controllerName) {
		this.owner = owner;
		//this.interfaceType = interfaceType;
		this.controllerName = controllerName;
		if (controllerName.equals(Constants.CONTENT_CONTROLLER)) {
			interfaceType =
				ProActiveTypeFactory.instance().createFcItfType(
					Constants.CONTENT_CONTROLLER,
					ProActiveContentController.class.getName(),
					TypeFactory.SERVER,
					TypeFactory.MANDATORY,
					TypeFactory.SINGLE);
		} else if (controllerName.equals(Constants.BINDING_CONTROLLER)) {
			interfaceType =
				ProActiveTypeFactory.instance().createFcItfType(
					Constants.BINDING_CONTROLLER,
					ProActiveBindingController.class.getName(),
					TypeFactory.SERVER,
					TypeFactory.MANDATORY,
					TypeFactory.SINGLE);
		} else if (controllerName.equals(Constants.LIFECYCLE_CONTROLLER)) {
			interfaceType =
				ProActiveTypeFactory.instance().createFcItfType(
					Constants.LIFECYCLE_CONTROLLER,
					ProActiveLifeCycleController.class.getName(),
					TypeFactory.SERVER,
					TypeFactory.MANDATORY,
					TypeFactory.SINGLE);
			//        } else if (controllerName.equals(UserBindingController.USER_BINDING_CONTROLLER)) {
			//            interfaceType = ProActiveTypeFactory.instance()
			//                                                .createFcItfType(UserBindingController.USER_BINDING_CONTROLLER, 
			//                                                                 ProActivePrimitiveBindingController.class.getName(), 
			//                                                                 TypeFactory.SERVER, TypeFactory.MANDATORY, 
			//                                                                 TypeFactory.SINGLE);
		} else if (controllerName.equals(Constants.COMPONENT_PARAMETERS_CONTROLLER)) {
			interfaceType =
				ProActiveTypeFactory.instance().createFcItfType(
					Constants.COMPONENT_PARAMETERS_CONTROLLER,
					ProActiveComponentParametersController.class.getName(),
					TypeFactory.SERVER,
					TypeFactory.MANDATORY,
					TypeFactory.SINGLE);
		} else {
			throw new RuntimeException("unknown controller");
		}
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
	 */
	public Component getFcItfOwner() {
		return owner;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
	 */
	public boolean isFcInternalItf() {
		return isInternal;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfName()
	 */
	public String getFcItfName() {
		return controllerName;
	}

	/**
	 * @see org.objectweb.fractal.api.Interface#getFcItfType()
	 */
	public Type getFcItfType() {
		return interfaceType;
	}

	/**
	 * most control operations are to be performed while the component is stopped
	 */
	protected void checkLifeCycleIsStopped() throws IllegalLifeCycleException {
		try {
			if (!((LifeCycleController) getFcItfOwner().getFcInterface(Constants.LIFECYCLE_CONTROLLER))
				.getFcState()
				.equals(LifeCycleController.STOPPED)) {
				throw new IllegalLifeCycleException("this control operation should be performed while the component is stopped");
			}
		} catch (NoSuchInterfaceException nsie) {
			throw new ProActiveRuntimeException("life cycle controller interface not found");

		}
	}

	// -------------------------------------------------------------------------
	// Implementation of the Name interface
	// -------------------------------------------------------------------------

	public NamingContext getNamingContext() {
		return null;
	}

	public byte[] encode() throws NamingException {
		throw new NamingException("Unsupported operation");
	}

}