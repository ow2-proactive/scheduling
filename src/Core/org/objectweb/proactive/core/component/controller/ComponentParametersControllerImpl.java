/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * This class defines a controller for accessing configuration parameters of the component.
 *
 * @author Matthieu Morel
 *
 */
public class ComponentParametersControllerImpl
    extends AbstractProActiveController implements Serializable,
        ComponentParametersController {
    private ComponentParameters componentParameters;

    /**
     * Constructor
     * @param owner the super controller
     */
    public ComponentParametersControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance()
                                               .createFcItfType(Constants.COMPONENT_PARAMETERS_CONTROLLER,
                    ComponentParametersController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException(
                "cannot create controller type : " + this.getClass().getName());
        }
    }

    /*
    * see {@link ComponentParametersController#getComponentParameters()}
    */
    public ComponentParameters getComponentParameters() {
        return componentParameters;
    }

    /*
     * see {@link ComponentParametersController#setComponentParameters(ComponentParameters)}
     */
    public void setComponentParameters(ComponentParameters componentParameters) {
        this.componentParameters = componentParameters;
    }

    /*
     * see @link ComponentParametersController#setComponentName(String)
     */
    public void setFcName(String componentName) {
        componentParameters.setName(componentName);
    }

    /*
     * see {@link org.objectweb.fractal.api.control.NameController#getFcName()}
     */
    public String getFcName() {
        return componentParameters.getName();
    }

    /*
     * used in the ProActiveComponentRepresentativeFactory.create[NF]ComponentRepresentative
     * to set the getComponentParameters method as an immediate service
     *
     * return int for synchronous call
     */
    public int setImmediateServices() {
        ProActiveObject.setImmediateService("getComponentParameters");
        return 0; // Synchronous call
    }

    /*
     * used in the ProActiveComponentRepresentativeFactory.create[NF]ComponentRepresentative
     * to remove the getComponentParameters method as an immediate service
     * return int for synchronous call
     */
    public int removeImmediateServices() {
        ProActiveObject.removeImmediateService("getComponentParameters");
        return 0; // Synchronous call
    }
}
