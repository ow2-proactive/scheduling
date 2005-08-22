/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the LifeCycleController ({@link org.objectweb.fractal.api.control.LifeCycleController}).<br>
 * It uses the request queue of the active objects.
 *
 * @author Matthieu Morel
 *
 */
public class ProActiveLifeCycleControllerImpl
    extends AbstractProActiveController implements ProActiveLifeCycleController,
        Serializable {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    private String fcState = LifeCycleController.STOPPED;

    public ProActiveLifeCycleControllerImpl(Component owner) {
        super(owner);
        try {
            setItfType(ProActiveTypeFactory.instance().createFcItfType(Constants.LIFECYCLE_CONTROLLER,
                    ProActiveLifeCycleController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " +
                this.getClass().getName());
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return fcState;
        //        return getRequestQueue().isStarted() ? LifeCycleController.STARTED
        //                                             : LifeCycleController.STOPPED;
    }

    /*
     *@see org.objectweb.fractal.api.control.LifeCycleController#startFc()
     * recursive if composite
     * ( recursivity is allowed here as we do not implement sharing )
     */
    public void startFc() {
        try {
            //check that all mandatory client interfaces are bound
            Object[] itfs = getFcItfOwner().getFcInterfaces();
            for (int i = 0; i < itfs.length; i++) {
                InterfaceType itf_type = (InterfaceType) (((Interface) itfs[i]).getFcItfType());
                if (itf_type.isFcClientItf() && !itf_type.isFcOptionalItf()) {
                    if (Fractal.getBindingController(getFcItfOwner()).lookupFc(itf_type.getFcItfName()) == null) {
                        throw new IllegalLifeCycleException(
                            "compulsory client interface " +
                            itf_type.getFcItfName() + " in component " +
                            Fractal.getNameController(getFcItfOwner())
                                   .getFcName() + " is not bound. ");
                    }
                }
            }
            String hierarchical_type = Fractive.getComponentParametersController(getFcItfOwner())
                                               .getComponentParameters()
                                               .getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE) ||
                    hierarchical_type.equals(Constants.PARALLEL)) {
                // start all inner components
                Component[] inner_components = Fractal.getContentController(getFcItfOwner())
                                                      .getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        ((LifeCycleController) inner_components[i].getFcInterface(Constants.LIFECYCLE_CONTROLLER)).startFc();
                    }
                }
            }

            //getRequestQueue().start();
            fcState = LifeCycleController.STARTED;
            if (logger.isDebugEnabled()) {
                logger.debug("started " +
                    ((ComponentParametersController) getFcItfOwner()
                     .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
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

    /*
     *@see org.objectweb.fractal.api.control.LifeCycleController#stopFc()
     * recursive if composite
     */
    public void stopFc() {
        try {
            String hierarchical_type = ((ComponentParametersController) getFcItfOwner()
                                        .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
                                        .getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE) ||
                    hierarchical_type.equals(Constants.PARALLEL)) {
                // stop all inner components
                Component[] inner_components = ((ContentController) getFcItfOwner()
                                                                        .getFcInterface(Constants.CONTENT_CONTROLLER)).getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        ((LifeCycleController) inner_components[i].getFcInterface(Constants.LIFECYCLE_CONTROLLER)).stopFc();
                    }
                }
            }

            //getRequestQueue().stop();
            fcState = LifeCycleController.STOPPED;
            if (logger.isDebugEnabled()) {
                logger.debug("stopped" +
                    ((ComponentParametersController) getFcItfOwner()
                     .getFcInterface(Constants.COMPONENT_PARAMETERS_CONTROLLER)).getComponentParameters()
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

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#getFcState(short)
     */
    public String getFcState(short priority) {
        return getFcState();
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#startFc(short)
     */
    public void startFc(short priority) {
        startFc();
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#stopFc(short)
     */
    public void stopFc(short priority) {
        stopFc();
    }
}
