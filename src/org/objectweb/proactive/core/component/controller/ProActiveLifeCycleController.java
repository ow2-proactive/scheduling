package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.request.ComponentRequestQueue;

/**
 * Implementation of the LifeCycleController (@see org.objectweb.fractal.api.control.LifeCycleController).
 * It uses the request queue of the active objects.
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveLifeCycleController extends ProActiveController implements LifeCycleController, Serializable {
	protected static Logger logger = Logger.getLogger(ProActiveLifeCycleController.class.getName());

	public ProActiveLifeCycleController(Component owner) {
		super(owner, Constants.LIFECYCLE_CONTROLLER);
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
	 */
	public String getFcState() {
		return getRequestQueue().isStarted() ? LifeCycleController.STARTED : LifeCycleController.STOPPED;
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#startFc()
	 * recursive if composite
	 * ( recursivity is allowed here as we don't implement sharing )
	 */
	public void startFc() {
		try {
			String hierarchical_type =
				((ComponentParametersController) getFcItfOwner()
					.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
					.getComponentParameters()
					.getHierarchicalType();
			if (hierarchical_type.equals(ComponentParameters.COMPOSITE)
				|| hierarchical_type.equals(ComponentParameters.PARALLEL)) {
				// start all inner components
				Component[] inner_components =
					((ContentController) getFcItfOwner().getFcInterface(Constants.CONTENT_CONTROLLER))
						.getFcSubComponents();
				if (inner_components != null) {
					for (int i = 0; i < inner_components.length; i++) {
						((LifeCycleController) inner_components[i]
							.getFcInterface(Constants.LIFECYCLE_CONTROLLER))
							.startFc();
					}
				}
			}
			getRequestQueue().start();
			if (logger.isDebugEnabled()) {
				logger.debug("started "
				//+ ((NameController) getFcItfOwner().getFcInterface(NameController.NAME_CONTROLLER)).getFcName());
				+ (
					(ComponentParametersController) getFcItfOwner().getFcInterface(
						Constants.COMPONENT_PARAMETERS_CONTROLLER))
					.getComponentParameters()
					.getName());
			}
		} catch (NoSuchInterfaceException nsie) {
			logger.error("interface not found : " + nsie.getMessage());
			nsie.printStackTrace();
		} catch (IllegalLifeCycleException ilce) {
			logger.error("illegal life cycle operation : " + ilce.getMessage());
			ilce.printStackTrace();
		}
	}

	/**
	 * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
	 * recursive if composite
	 */
	public void stopFc() {
		try {
			String hierarchical_type =
				((ComponentParametersController) getFcItfOwner()
					.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
					.getComponentParameters()
					.getHierarchicalType();
			if (hierarchical_type.equals(ComponentParameters.COMPOSITE)
				|| hierarchical_type.equals(ComponentParameters.PARALLEL)) {
				// stop all inner components
				Component[] inner_components =
					((ContentController) getFcItfOwner().getFcInterface(Constants.CONTENT_CONTROLLER))
						.getFcSubComponents();
				if (inner_components != null) {
					for (int i = 0; i < inner_components.length; i++) {
						((LifeCycleController) inner_components[i]
							.getFcInterface(Constants.LIFECYCLE_CONTROLLER))
							.stopFc();
					}
				}
			}
			getRequestQueue().stop();
			if (logger.isDebugEnabled()) {
				logger.debug(
					"stopped"
						+ ((ComponentParametersController) getFcItfOwner()
							.getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER))
							.getComponentParameters()
							.getName());
			}
		} catch (NoSuchInterfaceException nsie) {
			logger.error("interface not found : " + nsie.getMessage());
			nsie.printStackTrace();
		} catch (IllegalLifeCycleException ilce) {
			logger.error("illegal life cycle operation : " + ilce.getMessage());
			ilce.printStackTrace();
		}
	}

	private ComponentRequestQueue getRequestQueue() {
		return ((ProActiveComponent) getFcItfOwner()).getRequestQueue();
	}
}