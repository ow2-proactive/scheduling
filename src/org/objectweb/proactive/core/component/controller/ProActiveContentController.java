package org.objectweb.proactive.core.component.controller;

import org.apache.log4j.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.controller.ComponentParametersController;

import java.io.Serializable;


/**
 * Implementation of ContentController (@see org.objectweb.fractal.api.control.ContentController).
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveContentController extends ProActiveController
    implements ContentController, Serializable {
    protected static Logger logger = Logger.getLogger(ProActiveContentController.class.getName());
    Component[] fcSubComponents;

    /**
     * Constructor for ProActiveContentController.
     */
    public ProActiveContentController(Component owner) {
        super(owner, Constants.CONTENT_CONTROLLER);
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterfaces()
     *
     * in this implementation, the external interfaces are also internal interfaces
     */
    public Object[] getFcInternalInterfaces() {
        return getFcItfOwner().getFcInterfaces();
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcInternalInterface(String)
    *
    *  in this implementation, the external interfaces are also internal interfaces
     *         */
    public Object getFcInternalInterface(String interfaceName)
        throws NoSuchInterfaceException {
        return getFcItfOwner().getFcInterface(interfaceName);
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#getFcSubComponents()
     */
    public Component[] getFcSubComponents() {
        return fcSubComponents;
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#addFcSubComponent(Component)
     */
    public void addFcSubComponent(Component subComponent)
        throws IllegalLifeCycleException {
        checkLifeCycleIsStopped();

        // check whether the subComponent is the component itself
        // TODO check this!
        //				if (((ProActiveComponent)subComponent).equals(getFcItfOwner())) {
        //					try {
        //					throw new IllegalArgumentException(
        //						"cannot add "
        //							+ Fractal.getComponentParametersController(getFcItfOwner()).getComponentParameters().getName()
        //							+ " component into itself ");
        //					} catch (NoSuchInterfaceException e) {
        //						logger.error(e.getMessage());
        //					}
        //				}
        // check whether already a sub component
        if (fcSubComponents != null) {
            for (int i = 0; i < fcSubComponents.length; i++) {
                if (fcSubComponents[i].equals(subComponent)) {
                    String name = null;
                    try {
                        name = ((ComponentParametersController) subComponent
                                .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                                .getName();
                    } catch (NoSuchInterfaceException nsie) {
                        throw new ProActiveRuntimeException(
                            "cannot access the component parameters controller");
                    }
                    throw new IllegalArgumentException(
                        "already a sub component : " + name);
                }
            }
        }

        // FIXME : pb with component cycle checking
        // we have a pb here : this should raise an exception if the subcomponent is primitive,
        // but proactive remote calls don't relay exception ; they stay in the body and lead to its termination
        //		// check whether current component is contained in subComponent
        //		Component[] subComponent_sub_components =
        //			((ContentController) subComponent.getFcInterface(ContentController.CONTENT_CONTROLLER))
        //				.getFcSubComponents();
        //		for (int i = 0; i < subComponent_sub_components.length; i++) {
        //			if (getFcItfOwner().equals(subComponent_sub_components[i])) {
        //				throw new IllegalArgumentException(
        //					"cannot add "
        //						+ ((ProActiveComponent) subComponent).getComponentParameters().getName()
        //						+ " ; \nthis operation would create a cycle in the component hierarchy");
        //			}
        //		}
        int length = (fcSubComponents == null) ? 0 : fcSubComponents.length;
        Component[] oldSubComponents = fcSubComponents;
        Component[] subComponents = new Component[length + 1];
        if (fcSubComponents != null) {
            System.arraycopy(fcSubComponents, 0, subComponents, 0, length);
        }
        subComponents[length] = subComponent;
        fcSubComponents = subComponents;
    }

    /**
     * @see org.objectweb.fractal.api.control.ContentController#removeFcSubComponent(Component)
     */
    public void removeFcSubComponent(Component subComponent)
        throws IllegalLifeCycleException {
        checkLifeCycleIsStopped();

        boolean ok = false;
        if (fcSubComponents != null) {
            for (int i = 0; i < fcSubComponents.length; i++) {
                if (fcSubComponents[i].equals(subComponent)) {
                    ok = true;
                    break;
                }
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("not a sub component : " +
                subComponent);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("TODO : check the bindings");
        }
    }

    /**
     * NOT IMPLEMENTED YET !
     * @see org.objectweb.fractal.api.control.ContentController#checkFc()
     */

    // TODO : To implement!
    public void checkFc() {
        throw new ProActiveRuntimeException("not yet implemented");
    }
}
