package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

/**
 * @author Matthieu Morel
 *
 */
public class ProActiveNameController extends AbstractProActiveController implements NameController {
    
    // FIXME coherency between this value and the one in component parameters controller
    String name;
    
    /**
     * @param owner
     */
    public ProActiveNameController(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(Constants.NAME_CONTROLLER,
                    NameController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName(), e);
        }
    }
    /*
     * @see org.objectweb.fractal.api.control.NameController#getFcName()
     */
    public String getFcName() {
        return name;
    }

    /*
     * @see org.objectweb.fractal.api.control.NameController#setFcName(java.lang.String)
     */
    public void setFcName(String name) {
        this.name=name;
    }
    
    

}
