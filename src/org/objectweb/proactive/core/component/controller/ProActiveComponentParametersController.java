package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;

import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;


/**
 * a controller for accessing configuration parameters of the component.
 * 
 * 
 * @author Matthieu Morel
 *
 */
public class ProActiveComponentParametersController extends ProActiveController
    implements Serializable, ComponentParametersController {
    protected static Logger logger = Logger.getLogger(ProActiveComponentParametersController.class.getName());
    private ComponentParameters componentParameters;

	/**
	 * Constructor
	 * @param owner
	 */
    public  ProActiveComponentParametersController(Component owner) {
        super(owner, Constants.COMPONENT_PARAMETERS_CONTROLLER);
    }

    /**
     * see {@link org.objectweb.fractal.proactive.control.ComponentParametersController#getFcComponentParameters()}
     */
    public ComponentParameters getComponentParameters() {
        return componentParameters;
    }

    /**
     * see {@link org.objectweb.fractal.proactive.control.ComponentParametersController#setFcComponentParameters(ComponentParameters)}
     */
    public void setComponentParameters(ComponentParameters componentParameters) {
        this.componentParameters = componentParameters;
    }
    
}