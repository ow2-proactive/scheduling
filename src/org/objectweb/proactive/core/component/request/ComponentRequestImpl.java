package org.objectweb.proactive.core.component.request;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.LifeCycleController;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractal;
import org.objectweb.proactive.core.component.body.ComponentBodyImpl;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;

import java.io.Serializable;

/**
 * Method calls to components are actually reified calls, and ComponentRequest contains
 * a reification of the call.
 *
 * This class allows for the tagging of the call (a component call), and the redispatching
 * to the targeted component metaobject.
 *
 * @author Matthieu Morel
 *
 */
public class ComponentRequestImpl extends RequestImpl implements ComponentRequest, Serializable {
	protected static Logger logger = Logger.getLogger(ComponentRequestImpl.class.getName());

	public ComponentRequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID) {
		super(methodCall, sender, isOneWay, nextSequenceID);
	}

	public ComponentRequestImpl(Request request) {
		super(request.getMethodCall(), request.getSender(), request.isOneWay(), request.getSequenceNumber());
	}

	/**
	 * redirects the call to the adequate component metaobject
	 */
	protected Object serveInternal(Body targetBody) throws ServeException {
		if (logger.isDebugEnabled()) {
			//logger.debug("ComponentRequestImpl.serveInternal : redirecting the call to the component metaobject");
		}
		try {
			Object result = null;
			Class target_class = methodCall.getReifiedMethod().getDeclaringClass();
			if (target_class.equals(BindingController.class)) {
				result =
					methodCall.execute(
						((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(
							Constants.BINDING_CONTROLLER));
			} else if (target_class.equals(ContentController.class)) {
				result =
					methodCall.execute(
						((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(
							Constants.CONTENT_CONTROLLER));
			} else if (target_class.equals(LifeCycleController.class)) {
				result =
					methodCall.execute(
						((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(
							Constants.LIFECYCLE_CONTROLLER));
			} else if (target_class.equals(ComponentParametersController.class)) {
				result =
					methodCall.execute(
						((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(
							Constants.COMPONENT_PARAMETERS_CONTROLLER));
			} else if (target_class.equals(AttributeController.class)) {
				// directly invoke on reified object, as AttributeController is only defined on primitive components
				result = methodCall.execute(targetBody.getReifiedObject());
			} else if (target_class.equals(Interface.class)) {
				result = methodCall.execute((Interface) ((ComponentBodyImpl) targetBody).getProActiveComponent());
			} else if (target_class.equals(Component.class)) {
				result =
					methodCall.execute(
						((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(Constants.COMPONENT));
			} else {
				if (((ComponentBodyImpl) targetBody).getProActiveComponent() != null) {
					String hierarchical_type =
						Fractal
							.getComponentParametersController(((ComponentBodyImpl) targetBody).getProActiveComponent())
							.getComponentParameters()
							.getHierarchicalType();

					// if the component is a composite OR A PARALLEL , forward to functional interface 
					if (hierarchical_type.equals(ComponentParameters.COMPOSITE)
						|| hierarchical_type.equals(ComponentParameters.PARALLEL)) {
						//						//						result =
						//						//							methodCall.execute(
						//						//								((ComponentBodyImpl) targetBody).getProActiveComponentIdentity().getFcInterface(
						//						//									methodCall.getReifiedMethod().getName()));
						//						// forward to functional interface whose name is given as a parameter in the method call
						if (logger.isDebugEnabled()) {
							logger.debug(
								" forwarding the call : "
									+ methodCall.getFcFunctionalInterfaceName()
									+ " to : "
									+ ((ComponentParametersController) ((ComponentBodyImpl) targetBody)
										.getProActiveComponent()
										.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
										.getComponentParameters()
										.getName());
						}
						try {
							result =
								methodCall.execute(
									((ComponentBodyImpl) targetBody).getProActiveComponent().getFcInterface(
										methodCall.getFcFunctionalInterfaceName()));
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						// the component is a primitive
						// directly execute the method on the active object
						//                    if (logger.isDebugEnabled()) {
						//                        logger.debug("primitive component forwarding the call : " + 
						//                                     methodCall.getFcFunctionalInterfaceName());
						//                    }
						if (logger.isDebugEnabled()) {
							logger.debug(" directly executing the call : " + methodCall.getFcFunctionalInterfaceName());
						}
						result = methodCall.execute(targetBody.getReifiedObject());
					}
				} else {
					throw new ServeException("trying to execute a component method on an object that is not a component");
				}
			}
			return result;
		} catch (NoSuchInterfaceException nsie) {
			nsie.printStackTrace();
			throw new ServeException("cannot serve request : problem accessing a component controller", nsie);
		} catch (MethodCallExecutionFailedException e) {
			e.printStackTrace();
			throw new ServeException("serve method " + methodCall.getReifiedMethod().toString() + " failed", e);
		} catch (java.lang.reflect.InvocationTargetException e) {
			Throwable t = e.getTargetException();

			// t.printStackTrace();
			if (isOneWay) {
				throw new ServeException("serve method " + methodCall.getReifiedMethod().toString() + " failed", t);
			} else {
				return t;
			}
		}
	}

	/**
	 * @see org.objectweb.proactive.core.component.request.ComponentRequest#isControllerRequest()
	 */
	public boolean isControllerRequest() {
		Class declaring_class = methodCall.getReifiedMethod().getDeclaringClass();
		if (declaring_class.equals(ComponentParametersController.class)
			|| declaring_class.equals(ContentController.class)
			|| declaring_class.equals(BindingController.class)
			|| declaring_class.equals(LifeCycleController.class)
			|| declaring_class.equals(Interface.class)) {
			return true;
		} else {
			return false;
		}
	}
}
